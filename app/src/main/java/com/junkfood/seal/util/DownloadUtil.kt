package com.junkfood.seal.util

import android.media.MediaCodecList
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import androidx.annotation.CheckResult
import com.junkfood.seal.App
import java.io.File
import com.junkfood.seal.BuildConfig
import com.junkfood.seal.App.Companion.audioDownloadDir
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.videoDownloadDir
import com.junkfood.seal.Downloader
import com.junkfood.seal.Downloader.onProcessEnded
import com.junkfood.seal.Downloader.onProcessStarted
import com.junkfood.seal.Downloader.onTaskEnded
import com.junkfood.seal.Downloader.onTaskError
import com.junkfood.seal.Downloader.onTaskStarted
import com.junkfood.seal.Downloader.toNotificationId
import com.junkfood.seal.R
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.ui.page.settings.network.Cookie
import com.junkfood.seal.util.FileUtil.getArchiveFile
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getExternalTempDir
import com.junkfood.seal.util.FileUtil.getFileName
import com.junkfood.seal.util.FileUtil.getSdcardTempDir
import com.junkfood.seal.util.makeToast
import com.junkfood.seal.util.FileUtil.moveFilesToSdcard
import com.junkfood.seal.util.PreferenceUtil.COOKIE_HEADER
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

object DownloadUtil {

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class CookieJson(
        val name: String = "",
        val value: String = "",
        val domain: String = "",
        val path: String = "/",
        val secure: Boolean = false,
        @SerialName("httpOnly") val httpOnly: Boolean = false,
        val expirationDate: Double = 0.0,
        val expires: Double = 0.0,
        val session: Boolean = true,
    )

    fun parseCookieContent(profileUrl: String, content: String): List<Cookie> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        return runCatching {
            when {
                trimmed.startsWith('[') || trimmed.startsWith('{') ->
                    parseJsonCookies(profileUrl, trimmed)
                trimmed.lines().any { it.isNotBlank() && !it.startsWith('#') && '\t' in it } ->
                    parseNetscapeCookies(trimmed)
                else -> parseHeaderCookies(profileUrl, trimmed)
            }
        }.getOrElse { e ->
            Log.w(TAG, "parseCookieContent failed, attempting header fallback: ${e.message}")
            runCatching { parseHeaderCookies(profileUrl, trimmed) }.getOrDefault(emptyList())
        }
    }

    private fun parseJsonCookies(profileUrl: String, json: String): List<Cookie> {
        val normalised = if (json.trimStart().startsWith('{')) "[$json]" else json
        val items = jsonFormat.decodeFromString<List<CookieJson>>(normalised)
        val now = System.currentTimeMillis() / 1000L
        val fallbackDomain = profileUrl.let {
            val url = if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it"
            val host = Uri.parse(url).host?.removePrefix("www.")
            if (host.isNullOrBlank()) "" else ".$host"
        }
        return items.mapNotNull { c ->
            if (c.name.isEmpty()) return@mapNotNull null
            val expiry = when {
                c.expirationDate > 0 -> c.expirationDate.toLong()
                c.expires > 0        -> c.expires.toLong()
                else                 -> 0L
            }
            if (expiry > 0L && expiry < now) return@mapNotNull null
            val domain = c.domain.let { d ->
                when {
                    d.isBlank()         -> fallbackDomain
                    d.startsWith('.')   -> d
                    else                -> ".$d"
                }
            }
            Cookie(
                domain            = domain,
                name              = c.name,
                value             = c.value,
                includeSubdomains = true,
                path              = c.path.ifEmpty { "/" },
                secure            = c.secure,
                expiry            = expiry,
                isHttpOnly        = c.httpOnly,
            )
        }
    }

    private fun parseNetscapeCookies(text: String): List<Cookie> {
        val now = System.currentTimeMillis() / 1000L
        val cookies = mutableListOf<Cookie>()
        text.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith('#')) return@forEach
            val parts = trimmed.split('\t')
            if (parts.size < 7) return@forEach
            val rawDomain          = parts[0]
            val includeSubdomains  = parts[1].uppercase() == "TRUE"
            val path               = parts[2]
            val secure             = parts[3].uppercase() == "TRUE"
            val expiry             = parts[4].toLongOrNull() ?: 0L
            val name               = parts[5]
            val value              = parts.drop(6).joinToString("\t")
            if (name.isEmpty()) return@forEach
            if (expiry > 0L && expiry < now) return@forEach

            val domain = if (includeSubdomains) {
                if (rawDomain.startsWith('.')) rawDomain else ".$rawDomain"
            } else {
                rawDomain.removePrefix(".")
            }

            cookies.add(
                Cookie(
                    domain            = domain,
                    name              = name,
                    value             = value,
                    includeSubdomains = includeSubdomains,
                    path              = path,
                    secure            = secure,
                    expiry            = expiry,
                    isHttpOnly        = false,
                )
            )
        }
        return cookies
    }

    private fun parseHeaderCookies(profileUrl: String, header: String): List<Cookie> {
        val rawUrl = if (profileUrl.startsWith("http")) profileUrl else "https://$profileUrl"
        val host   = Uri.parse(rawUrl).host ?: ""
        val domain = "." + if (host.startsWith("www.")) host.removePrefix("www.") else host
        val cookies = mutableListOf<Cookie>()
        val cookiePart = header.removePrefix("Cookie:").removePrefix("cookie:").trim()
        cookiePart.split(';').forEach { pair ->
            val eqIdx = pair.indexOf('=')
            if (eqIdx < 0) return@forEach
            val name  = pair.substring(0, eqIdx).trim()
            val value = pair.substring(eqIdx + 1).trim()
            if (name.isEmpty()) return@forEach
            cookies.add(
                Cookie(
                    domain            = domain,
                    name              = name,
                    value             = value,
                    includeSubdomains = true,
                    path              = "/",
                    secure            = false,
                    expiry            = 0L,
                    isHttpOnly        = false,
                )
            )
        }
        return cookies
    }

    private const val TAG = "DownloadUtil"

    const val BASENAME = "%(title).200B"
    const val EXTENSION = ".%(ext)s"
    private const val ID = "[%(id)s]"
    private const val CLIP_TIMESTAMP = "%(section_start)d-%(section_end)d"
    const val OUTPUT_TEMPLATE_DEFAULT = BASENAME + EXTENSION
    const val OUTPUT_TEMPLATE_ID = "$BASENAME $ID$EXTENSION"
    private const val OUTPUT_TEMPLATE_CLIPS = "$BASENAME [$CLIP_TIMESTAMP]$EXTENSION"
    private const val OUTPUT_TEMPLATE_CHAPTERS = "chapter:$BASENAME/%(section_number)d - %(section_title).200B$EXTENSION"
    private const val OUTPUT_TEMPLATE_SPLIT = "$BASENAME/$OUTPUT_TEMPLATE_DEFAULT"
    private const val PLAYLIST_TITLE_SUBDIRECTORY_PREFIX = "%(playlist)s/"
    private const val CROP_ARTWORK_COMMAND = """--ppa "ffmpeg: -c:v mjpeg -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\"""""

    @CheckResult
    fun getPlaylistOrVideoInfo(
        playlistURL: String,
        downloadPreferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
    ): Result<YoutubeDLInfo> =
        YoutubeDL.runCatching {
            App.applicationScope.launch(Dispatchers.Main) {
                context.makeToast(R.string.fetching_playlist_info)
            }
            val request = YoutubeDLRequest(playlistURL)
            with(request) {
                addOption("--flat-playlist")
                addOption("--dump-single-json")
                addOption("-o", BASENAME)
                addOption("-R", "3")
                addOption("--socket-timeout", "15")
                downloadPreferences.run {
                    if (extractAudio) { addOption("-x") }
                    applyFormatSorter(this, toFormatSorter())
                    if (forceIpv4) { addOption("-4") }
                    if (noCheckCertificate) { addOption("--no-check-certificate") }
                    if (cookies) { enableCookies(userAgentString) }
                    if (restrictFilenames) { addOption("--restrict-filenames") }
                }
            }
            execute(request, playlistURL).out.run {
                val playlistInfo = jsonFormat.decodeFromString<PlaylistResult>(this)
                if (playlistInfo.type != "playlist") {
                    jsonFormat.decodeFromString<VideoInfo>(this)
                } else playlistInfo
            }
        }

    @CheckResult
    private fun getVideoInfo(
        request: YoutubeDLRequest,
        taskKey: String? = null,
    ): Result<VideoInfo> =
        request.runCatching {
            val response: YoutubeDLResponse = YoutubeDL.getInstance().execute(request, taskKey, null)
            jsonFormat.decodeFromString(response.out)
        }

    @CheckResult
    fun fetchVideoInfoFromUrl(
        url: String,
        playlistIndex: Int? = null,
        taskKey: String? = null,
        preferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
    ): Result<VideoInfo> {
        with(preferences) {
            val requestResult = runCatching {
                YoutubeDLRequest(url).apply {
                    addOption("-o", BASENAME)
                    if (restrictFilenames) { addOption("--restrict-filenames") }
                    if (extractAudio) { addOption("-x") }
                    applyFormatSorter(this@with, toFormatSorter())
                    if (cookies) { enableCookies(userAgentString) }
                    if (forceIpv4) { addOption("-4") }
                    if (noCheckCertificate) { addOption("--no-check-certificate") }
                    if (autoSubtitle) { addOption("--write-auto-subs") }
                    if (autoSubtitle && !autoTranslatedSubtitles) {
                        addOption("--extractor-args", "youtube:skip=translated_subs")
                    }
                    if (playlistIndex != null) {
                        addOption("--playlist-items", playlistIndex)
                        addOption("--dump-json")
                    } else {
                        addOption("--dump-single-json")
                        addOption("--no-playlist")
                    }
                    addOption("-R", "3")
                    addOption("--socket-timeout", "15")
                }
            }
            val request = requestResult.getOrElse { return Result.failure(it) }
            return getVideoInfo(request, taskKey)
        }
    }

    @CheckResult
    fun fetchCommentsFromUrl(
        url: String,
        maxComments: Int = 500,
        sortByTop: Boolean = true,
    ): Result<VideoInfo> {
        val request = YoutubeDLRequest(url).apply {
            addOption("--skip-download")
            addOption("--write-comments")
            addOption("--dump-single-json")
            addOption("--no-playlist")
            addOption(
                "--extractor-args",
                "youtube:max_comments=$maxComments,all,all,all" + (if (sortByTop) ";comment_sort=top" else ""),
            )
            if (COOKIES.getBoolean()) {
                val userAgentString = USER_AGENT_STRING.run { if (USER_AGENT.getBoolean()) getString() else "" }
                runCatching { enableCookies(userAgentString) }.onFailure { cookieErr -> return Result.failure(cookieErr) }
            }
            addOption("-R", "3")
            addOption("--socket-timeout", "20")
        }
        return getVideoInfo(request)
    }

    @Serializable
    data class DownloadPreferences(
        val extractAudio: Boolean,
        val createThumbnail: Boolean,
        val downloadPlaylist: Boolean,
        val subdirectoryExtractor: Boolean,
        val subdirectoryPlaylistTitle: Boolean,
        val commandDirectory: String,
        val downloadSubtitle: Boolean,
        val embedSubtitle: Boolean,
        val keepSubtitle: Boolean,
        val subtitleLanguage: String,
        val autoSubtitle: Boolean,
        val autoTranslatedSubtitles: Boolean,
        val convertSubtitle: Int,
        val concurrentFragments: Int,
        val sponsorBlock: Boolean,
        val sponsorBlockCategory: String,
        val cookies: Boolean,
        val aria2c: Boolean,
        val useCustomAudioPreset: Boolean,
        val audioFormat: Int,
        val audioQuality: Int,
        val convertAudio: Boolean,
        val formatSorting: Boolean,
        val sortingFields: String,
        val audioConvertFormat: Int,
        val videoFormat: Int,
        val formatIdString: String,
        val videoResolution: Int,
        val privateMode: Boolean,
        val rateLimit: Boolean,
        val maxDownloadRate: String,
        val privateDirectory: Boolean,
        val cropArtwork: Boolean,
        val sdcard: Boolean,
        val sdcardUri: String,
        val embedThumbnail: Boolean,
        val videoClips: List<VideoClip>,
        val splitByChapter: Boolean,
        val debug: Boolean,
        val newTitle: String,
        val userAgentString: String,
        val outputTemplate: String,
        val useDownloadArchive: Boolean,
        val embedMetadata: Boolean,
        val restrictFilenames: Boolean,
        val supportAv1HardwareDecoding: Boolean,
        val forceIpv4: Boolean,
        val noCheckCertificate: Boolean,
        val mergeAudioStream: Boolean,
        val mergeToMkv: Boolean,
        val downloadDocs: Boolean = false,
    ) {
        companion object {
            val EMPTY = DownloadPreferences(
                extractAudio = false, createThumbnail = false, downloadPlaylist = false, subdirectoryExtractor = false,
                subdirectoryPlaylistTitle = false, commandDirectory = "", downloadSubtitle = false, embedSubtitle = false,
                keepSubtitle = false, subtitleLanguage = "", autoSubtitle = false, autoTranslatedSubtitles = false,
                convertSubtitle = 0, concurrentFragments = 0, sponsorBlock = false, sponsorBlockCategory = "",
                cookies = false, aria2c = false, audioFormat = 0, audioQuality = 0, convertAudio = false,
                formatSorting = false, sortingFields = "", audioConvertFormat = 0, videoFormat = 0, formatIdString = "",
                videoResolution = 0, privateMode = false, rateLimit = false, maxDownloadRate = "", privateDirectory = false,
                cropArtwork = false, sdcard = false, sdcardUri = "", embedThumbnail = false, videoClips = emptyList(),
                splitByChapter = false, debug = false, newTitle = "", userAgentString = "", outputTemplate = "",
                useDownloadArchive = false, embedMetadata = false, restrictFilenames = false, supportAv1HardwareDecoding = false,
                forceIpv4 = false, noCheckCertificate = false, mergeAudioStream = false, mergeToMkv = false,
                useCustomAudioPreset = false, downloadDocs = false,
            )

            fun createFromPreferences(): DownloadPreferences {
                val downloadSubtitle = SUBTITLE.getBoolean()
                val embedSubtitle = EMBED_SUBTITLE.getBoolean()
                return DownloadPreferences(
                    extractAudio = EXTRACT_AUDIO.getBoolean(),
                    createThumbnail = THUMBNAIL.getBoolean(),
                    downloadPlaylist = PLAYLIST.getBoolean(),
                    subdirectoryExtractor = SUBDIRECTORY_EXTRACTOR.getBoolean(),
                    subdirectoryPlaylistTitle = SUBDIRECTORY_PLAYLIST_TITLE.getBoolean(),
                    commandDirectory = COMMAND_DIRECTORY.getString(),
                    downloadSubtitle = downloadSubtitle,
                    embedSubtitle = embedSubtitle,
                    keepSubtitle = KEEP_SUBTITLE_FILES.getBoolean(),
                    subtitleLanguage = SUBTITLE_LANGUAGE.getString(),
                    autoSubtitle = AUTO_SUBTITLE.getBoolean(),
                    autoTranslatedSubtitles = AUTO_TRANSLATED_SUBTITLES.getBoolean(),
                    convertSubtitle = CONVERT_SUBTITLE.getInt(),
                    concurrentFragments = CONCURRENT.getInt(),
                    sponsorBlock = SPONSORBLOCK.getBoolean(),
                    sponsorBlockCategory = PreferenceUtil.getSponsorBlockCategories(),
                    cookies = COOKIES.getBoolean(),
                    aria2c = ARIA2C.getBoolean(),
                    useCustomAudioPreset = USE_CUSTOM_AUDIO_PRESET.getBoolean(),
                    audioFormat = AUDIO_FORMAT.getInt(),
                    audioQuality = AUDIO_QUALITY.getInt(),
                    convertAudio = AUDIO_CONVERT.getBoolean(),
                    formatSorting = FORMAT_SORTING.getBoolean(),
                    sortingFields = SORTING_FIELDS.getString(),
                    audioConvertFormat = PreferenceUtil.getAudioConvertFormat(),
                    videoFormat = PreferenceUtil.getVideoFormat(),
                    formatIdString = "",
                    videoResolution = PreferenceUtil.getVideoResolution(),
                    privateMode = PRIVATE_MODE.getBoolean(),
                    rateLimit = RATE_LIMIT.getBoolean(),
                    maxDownloadRate = PreferenceUtil.getMaxDownloadRate(),
                    privateDirectory = PRIVATE_DIRECTORY.getBoolean(),
                    cropArtwork = CROP_ARTWORK.getBoolean(),
                    sdcard = SDCARD_DOWNLOAD.getBoolean(),
                    sdcardUri = SDCARD_URI.getString(),
                    embedThumbnail = EMBED_THUMBNAIL.getBoolean(),
                    videoClips = emptyList(),
                    splitByChapter = false,
                    debug = DEBUG.getBoolean(),
                    newTitle = "",
                    userAgentString = USER_AGENT_STRING.run { if (USER_AGENT.getBoolean()) getString() else "" },
                    outputTemplate = OUTPUT_TEMPLATE.getString(),
                    useDownloadArchive = DOWNLOAD_ARCHIVE.getBoolean(),
                    embedMetadata = EMBED_METADATA.getBoolean(),
                    restrictFilenames = RESTRICT_FILENAMES.getBoolean(),
                    supportAv1HardwareDecoding = checkIfAv1HardwareAccelerated(),
                    forceIpv4 = FORCE_IPV4.getBoolean(),
                    noCheckCertificate = NO_CHECK_CERTIFICATE.getBoolean(),
                    mergeAudioStream = false,
                    mergeToMkv = (downloadSubtitle && embedSubtitle) || MERGE_OUTPUT_MKV.getBoolean(),
                )
            }
        }
    }

    private fun YoutubeDLRequest.enableCookies(userAgentString: String): YoutubeDLRequest {
        refreshCookiesFile()
        return this.addOption("--cookies", context.getCookiesFile().absolutePath).apply {
            if (userAgentString.isNotEmpty()) { addOption("--add-header", "User-Agent:$userAgentString") }
        }
    }

    fun refreshCookiesFile() {
        context.getCookiesFile().let { cookiesFile ->
            getCookieListFromDatabase()
                .mapCatching { it.toCookiesFileContent() }
                .mapCatching { content -> FileUtil.writeContentToFile(content, cookiesFile) }
                .onFailure { err ->
                    Log.w(TAG, "Failed to refresh cookies file: ${err.message}")
                    if (cookiesFile.exists()) cookiesFile.delete()
                    throw Exception("Cookies setup failed: ${err.message}\n\nGo to Settings → Network → Cookies to add a cookie profile.")
                }
        }
    }

    private fun YoutubeDLRequest.useDownloadArchive(): YoutubeDLRequest =
        this.addOption("--download-archive", context.getArchiveFile().absolutePath)

    @CheckResult
    fun getCookieListFromDatabase(): Result<List<Cookie>> = runCatching {
        val manager = CookieManager.getInstance()
        manager.flush()
        val profiles = runBlocking(Dispatchers.IO) { DatabaseUtil.getCookieProfileList() }

        if (profiles.isEmpty()) {
            throw Exception("No cookie profiles configured. Please go to Settings → Network → Cookies.")
        }
        if (profiles.none { it.content.isNotEmpty() } && !manager.hasCookies()) {
            throw Exception("No cookies found in the browser. Please log in again.")
        }

        val seen = HashSet<String>()
        val cookies = mutableListOf<Cookie>()

        for (profile in profiles) {
            if (profile.content.isNotEmpty()) {
                val manual = parseCookieContent(profile.url, profile.content)
                if (manual.isNotEmpty()) {
                    manual.forEach { c -> if (seen.add("${c.domain}|${c.name}")) cookies.add(c) }
                    continue
                }
            }
            val rawUrl = when {
                profile.url.startsWith("https://") -> profile.url
                profile.url.startsWith("http://")  -> profile.url.replaceFirst("http://", "https://")
                else                               -> "https://${profile.url}"
            }
            val host = Uri.parse(rawUrl).host ?: continue
            val urlVariants = buildList {
                add(rawUrl)
                if (host.startsWith("www.")) add(rawUrl.replaceFirst("://www.", "://"))
                else add(rawUrl.replaceFirst("://", "://www."))
            }
            val baseDomain = "." + if (host.startsWith("www.")) host.removePrefix("www.") else host

            for (url in urlVariants) {
                val raw = manager.getCookie(url) ?: continue
                raw.split(";").forEach rawCookie@{ pair ->
                    val eqIdx = pair.indexOf('=')
                    if (eqIdx < 0) return@rawCookie
                    val name  = pair.substring(0, eqIdx).trim()
                    val value = pair.substring(eqIdx + 1)
                    if (name.isEmpty()) return@rawCookie
                    if (seen.add("$baseDomain|$name")) {
                        cookies.add(Cookie(domain = baseDomain, name = name, value = value, includeSubdomains = true, path = "/", secure = false, expiry = 0L, isHttpOnly = false))
                    }
                }
            }
        }
        if (cookies.isEmpty()) throw Exception("No cookies could be retrieved.")
        cookies
    }

    fun List<Cookie>.toCookiesFileContent(): String =
        this.fold(StringBuilder(COOKIE_HEADER)) { acc, cookie -> acc.append(cookie.toNetscapeCookieString()).append("\n") }.toString()

    fun getCookiesContentFromDatabase(): Result<String> = getCookieListFromDatabase().mapCatching { it.toCookiesFileContent() }

    private fun YoutubeDLRequest.enableAria2c(): YoutubeDLRequest {
        val connections = ARIA2C_CONNECTIONS.getInt()
        return this.addOption("--downloader", "http,https,ftp,ftps:libaria2c.so")
            .addOption("--external-downloader-args", "aria2c:-x $connections -s $connections -k 1M --file-allocation=none --max-tries=5 --retry-wait=2 --console-log-level=warn")
    }

    private fun YoutubeDLRequest.enableRetryOptions(): YoutubeDLRequest =
        this.addOption("--retries", "10")
            .addOption("--fragment-retries", "10")
            .addOption("--extractor-retries", "3")
            .addOption("--file-access-retries", "3")
            .addOption("--retry-sleep", "exp=1:120")
            .addOption("--retry-sleep", "fragment:exp=1:60")

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(downloadPreferences: DownloadPreferences): YoutubeDLRequest =
        this.apply {
            downloadPreferences.run {
                addOption("--add-metadata")
                addOption("--no-embed-info-json")
                if (formatIdString.isNotEmpty()) {
                    addOption("-f", formatIdString)
                    if (mergeAudioStream) { addOption("--audio-multistreams") }
                } else { applyFormatSorter(this, toFormatSorter()) }
                if (downloadSubtitle && autoSubtitle && !autoTranslatedSubtitles) {
                    addOption("--extractor-args", "youtube:skip=translated_subs")
                }
                if (downloadSubtitle) {
                    if (autoSubtitle) { addOption("--write-auto-subs") }
                    subtitleLanguage.takeIf { it.isNotEmpty() }?.let { addOption("--sub-langs", it) }
                    if (embedSubtitle) {
                        addOption("--embed-subs")
                        if (keepSubtitle) { addOption("--write-subs") }
                    } else { addOption("--write-subs") }
                    when (convertSubtitle) {
                        CONVERT_ASS -> addOption("--convert-subs", "ass")
                        CONVERT_SRT -> addOption("--convert-subs", "srt")
                        CONVERT_VTT -> addOption("--convert-subs", "vtt")
                        CONVERT_LRC -> addOption("--convert-subs", "lrc")
                    }
                }
                if (mergeToMkv) {
                    addOption("--remux-video", "mkv")
                    addOption("--merge-output-format", "mkv")
                } else {
                    addOption("--remux-video", "mp4")
                    addOption("--merge-output-format", "mp4")
                }
                if (embedThumbnail) { addOption("--embed-thumbnail") }
                if (videoClips.isEmpty()) addOption("--embed-chapters")
            }
        }

    @CheckResult
    private fun DownloadPreferences.toAudioFormatSorter(): String = this.run {
        if (!useCustomAudioPreset) return@run ""
        val format = when (audioFormat) { M4A -> "acodec:aac"; OPUS -> "acodec:opus"; else -> "" }
        val quality = when (audioQuality) { HIGH -> "abr~192"; MEDIUM -> "abr~128"; LOW -> "abr~64"; else -> "" }
        return@run connectWithDelimiter(format, quality, delimiter = ",")
    }

    @CheckResult
    private fun DownloadPreferences.toVideoFormatSorter(): String = this.run {
        val format = when (videoFormat) {
            FORMAT_COMPATIBILITY -> "proto,vcodec:h264,ext"
            FORMAT_QUALITY -> if (supportAv1HardwareDecoding) "vcodec:av01" else "vcodec:vp9.2"
            else -> ""
        }
        val res = when (videoResolution) {
            1 -> "res:2160"; 2 -> "res:1440"; 3 -> "res:1080"; 4 -> "res:720"; 5 -> "res:480"; 6 -> "res:360"; 7 -> "+res"; else -> ""
        }
        val sorter = if (videoFormat == FORMAT_COMPATIBILITY) connectWithDelimiter(format, res, delimiter = ",") else connectWithDelimiter(res, format, delimiter = ",")
        return@run sorter
    }

    private fun YoutubeDLRequest.applyFormatSorter(preferences: DownloadPreferences, sorter: String) = preferences.run {
        if (formatSorting && sortingFields.isNotEmpty()) addOption("-S", sortingFields)
        else if (sorter.isNotEmpty()) addOption("-S", sorter) else {}
    }

    @CheckResult
    fun DownloadPreferences.toFormatSorter(): String = connectWithDelimiter(this.toVideoFormatSorter(), this.toAudioFormatSorter(), delimiter = ",")

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(id: String, preferences: DownloadPreferences, playlistUrl: String): YoutubeDLRequest =
        this.apply {
            with(preferences) {
                addOption("-x")
                if (downloadSubtitle && autoSubtitle && !autoTranslatedSubtitles) { addOption("--extractor-args", "youtube:skip=translated_subs") }
                if (downloadSubtitle) {
                    addOption("--write-subs")
                    if (autoSubtitle) { addOption("--write-auto-subs") }
                    subtitleLanguage.takeIf { it.isNotEmpty() }?.let { addOption("--sub-langs", it) }
                    when (convertSubtitle) {
                        CONVERT_ASS -> addOption("--convert-subs", "ass")
                        CONVERT_SRT -> addOption("--convert-subs", "srt")
                        CONVERT_VTT -> addOption("--convert-subs", "vtt")
                        CONVERT_LRC -> addOption("--convert-subs", "lrc")
                    }
                }
                if (formatIdString.isNotEmpty()) {
                    addOption("-f", formatIdString)
                    if (mergeAudioStream) { addOption("--audio-multistreams") }
                } else {
                    addOption("-f", "ba/b")
                    if (convertAudio) {
                        when (audioConvertFormat) {
                            CONVERT_MP3 -> addOption("--audio-format", "mp3")
                            CONVERT_M4A -> addOption("--audio-format", "m4a")
                        }
                    }
                    applyFormatSorter(preferences, toAudioFormatSorter())
                }
                if (embedMetadata) {
                    addOption("--embed-metadata")
                    addOption("--embed-thumbnail")
                    addOption("--convert-thumbnails", "jpg")
                    if (cropArtwork) {
                        val configFile = context.getConfigFile(id)
                        FileUtil.writeContentToFile(CROP_ARTWORK_COMMAND, configFile)
                        addOption("--config", configFile.absolutePath)
                    }
                }
                addOption("--parse-metadata", "%(release_year,upload_date)s:%(meta_date)s")
                if (playlistUrl.isNotEmpty()) {
                    addOption("--parse-metadata", "%(album,playlist,title)s:%(meta_album)s")
                    addOption("--parse-metadata", "%(track_number,playlist_index)d:%(meta_track)s")
                } else {
                    addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
                }
            }
        }

    @CheckResult
    fun writeDocsTextFile(videoInfo: VideoInfo): Result<String> = runCatching {
        val title = videoInfo.title.ifBlank { "video" }
        val safeTitle = title.replace(Regex("""[<>:"/\\|?*]"""), "_").take(100)
        val docsDir = FileUtil.getDocsDirectory()
        val file = File(docsDir, "$safeTitle - Info.txt")
        val content = buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("  VIDEO INFORMATION")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("Title:")
            appendLine(videoInfo.title)
            appendLine()
            videoInfo.uploader?.let { appendLine("Uploader:"); appendLine(it); appendLine() }
            videoInfo.uploadDate?.let { appendLine("Upload Date:"); appendLine(it); appendLine() }
            videoInfo.duration?.let { sec ->
                val minutes = (sec / 60).toInt()
                val seconds = (sec % 60).toInt()
                appendLine("Duration:")
                appendLine("${minutes}m ${seconds}s")
                appendLine()
            }
            videoInfo.webpageUrl?.let { appendLine("URL:"); appendLine(it); appendLine() }
            val tags = videoInfo.tags
            if (!tags.isNullOrEmpty()) { appendLine("Tags:"); tags.forEach { tag -> appendLine("  • $tag") }; appendLine() }
            val desc = videoInfo.description
            if (!desc.isNullOrBlank()) { appendLine("Description:"); appendLine(desc); appendLine() }
            appendLine("═══════════════════════════════════════")
            // ALTERADO AQUI: De "Seal Plus" para "Destiny"
            appendLine("  Generated by Destiny")
            appendLine("═══════════════════════════════════════")
        }
        FileUtil.writeContentToFile(content, file)
        context.makeToast(R.string.docs_saved)
        file.absolutePath
    }

    private fun insertInfoIntoDownloadHistory(videoInfo: VideoInfo, filePaths: List<String>, downloadTimeMillis: Long = -1L, averageSpeedBytesPerSec: Long = -1L): List<String> =
        filePaths.onEach {
            DatabaseUtil.insertInfo(videoInfo.toDownloadedVideoInfo(videoPath = it, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec))
        }

    private fun VideoInfo.toDownloadedVideoInfo(id: Int = 0, videoPath: String, downloadTimeMillis: Long = -1L, averageSpeedBytesPerSec: Long = -1L): DownloadedVideoInfo =
        this.run {
            DownloadedVideoInfo(
                id = id,
                videoTitle = title,
                videoAuthor = uploader ?: channel ?: uploaderId.toString(),
                videoUrl = webpageUrl ?: originalUrl ?: "",
                thumbnailUrl = thumbnail.toHttpsUrl(),
                videoPath = videoPath,
                videoId = this.id,
                extractor = extractorKey,
                downloadTimeMillis = downloadTimeMillis,
                averageSpeedBytesPerSec = averageSpeedBytesPerSec,
            )
        }

    private fun insertSplitChapterIntoHistory(videoInfo: VideoInfo, filePaths: List<String>, downloadTimeMillis: Long = -1L, averageSpeedBytesPerSec: Long = -1L) =
        filePaths.onEach {
            DatabaseUtil.insertInfo(videoInfo.toDownloadedVideoInfo(videoPath = it, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec).copy(videoTitle = it.getFileName()))
        }

    private fun computeAvgSpeed(videoInfo: VideoInfo, timing: LongArray): Long {
        val elapsed = if (timing[0] > 0L) timing[1] - timing[0] else -1L
        if (elapsed <= 0L) return -1L
        val fileSize = ((videoInfo.fileSize ?: videoInfo.fileSizeApprox) ?: 0.0).toLong()
        return if (fileSize > 0L) fileSize * 1000L / elapsed else -1L
    }

    @CheckResult
    fun downloadVideo(
        videoInfo: VideoInfo? = null,
        playlistUrl: String = "",
        playlistItem: Int = 0,
        taskId: String,
        downloadPreferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit)?,
    ): Result<List<String>> {
        if (videoInfo == null) return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg)))

        with(downloadPreferences) {
            val url = playlistUrl.ifEmpty { videoInfo.originalUrl ?: videoInfo.webpageUrl ?: return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg))) }
            val request = YoutubeDLRequest(url)
            val pathBuilder = StringBuilder()
            val outputBuilder = StringBuilder()
            val downloadTiming = LongArray(2)

            request.apply {
                addOption("--no-mtime")
                addOption("--continue")
                enableRetryOptions()
                if (cookies) {
                    runCatching { enableCookies(userAgentString) }.onFailure { cookieErr -> return Result.failure(cookieErr) }
                }
                if (restrictFilenames) { addOption("--restrict-filenames") }
                if (forceIpv4) { addOption("-4") }
                if (noCheckCertificate) { addOption("--no-check-certificate") }
                if (debug) { addOption("-v") }
                if (useDownloadArchive) {
                    val archiveFile = context.getArchiveFile()
                    val archiveTarget = "${videoInfo.extractor} ${videoInfo.id}"
                    val alreadyDownloaded = archiveFile.exists() && archiveFile.bufferedReader().useLines { lines -> lines.any { it.trimEnd() == archiveTarget } }
                    if (alreadyDownloaded) { return Result.failure(YoutubeDLException(context.getString(R.string.download_archive_error))) }
                    else { useDownloadArchive() }
                }
                if (rateLimit && maxDownloadRate.isNumberInRange(1, 1000000)) { addOption("-r", "${maxDownloadRate}K") }
                if (playlistItem != 0 && downloadPlaylist) {
                    addOption("--playlist-items", playlistItem)
                    if (subdirectoryPlaylistTitle && !videoInfo.playlist.isNullOrEmpty()) { outputBuilder.append(PLAYLIST_TITLE_SUBDIRECTORY_PREFIX) }
                } else { addOption("--no-playlist") }
                if (aria2c) {
                    enableAria2c()
                    if (concurrentFragments > 1) { addOption("--concurrent-fragments", concurrentFragments) }
                } else if (concurrentFragments > 1) { addOption("--concurrent-fragments", concurrentFragments) }

                val isAudioDownload = extractAudio
                if (isAudioDownload) {
                    if (privateDirectory) pathBuilder.append(App.privateDownloadDir) else pathBuilder.append(audioDownloadDir)
                    addOptionsForAudioDownloads(id = videoInfo.id, preferences = downloadPreferences, playlistUrl = playlistUrl)
                } else {
                    if (privateDirectory) pathBuilder.append(App.privateDownloadDir) else pathBuilder.append(videoDownloadDir)
                    addOptionsForVideoDownloads(downloadPreferences)
                }
                if (sponsorBlock) { addOption("--sponsorblock-remove", sponsorBlockCategory) }
                if (createThumbnail) {
                    addOption("--write-thumbnail")
                    addOption("--convert-thumbnails", "png")
                }
                if (subdirectoryExtractor) { pathBuilder.append("/${videoInfo.extractorKey}") }
                if (sdcard) { addOption("-P", context.getSdcardTempDir(videoInfo.id).absolutePath) } else { addOption("-P", pathBuilder.toString()) }
                videoClips.forEach { addOption("--download-sections", "*%d-%d".format(locale = Locale.US, it.start, it.end)) }
                if (newTitle.isNotEmpty()) { addCommands(listOf("--replace-in-metadata", "title", ".+", newTitle)) }
                if (Build.VERSION.SDK_INT > 23 && !sdcard) { addOption("-P", "temp:" + getExternalTempDir(videoInfo.id)) }
                if (splitByChapter) {
                    addOption("-o", OUTPUT_TEMPLATE_CHAPTERS)
                    addOption("--split-chapters")
                }
                val output = if (splitByChapter) OUTPUT_TEMPLATE_SPLIT else if (videoClips.isEmpty()) outputTemplate else OUTPUT_TEMPLATE_CLIPS
                addOption("-o", outputBuilder.append(output).toString())
                for (s in request.buildCommand()) Log.d(TAG, s)
            }
                .runCatching {
                    val dlStartTime = System.currentTimeMillis()
                    YoutubeDL.getInstance().execute(request = this, processId = taskId, callback = progressCallback)
                        .also { downloadTiming[0] = dlStartTime; downloadTiming[1] = System.currentTimeMillis() }
                }
                .onFailure { th ->
                    return if (sponsorBlock && th.message?.contains("Unable to communicate with SponsorBlock API") == true) {
                        th.printStackTrace()
                        onFinishDownloading(preferences = this, videoInfo = videoInfo, downloadPath = pathBuilder.toString(), sdcardUri = sdcardUri, downloadTimeMillis = if (downloadTiming[0] > 0L) downloadTiming[1] - downloadTiming[0] else -1L, averageSpeedBytesPerSec = computeAvgSpeed(videoInfo, downloadTiming))
                    } else Result.failure(th)
                }
            return onFinishDownloading(preferences = this, videoInfo = videoInfo, downloadPath = pathBuilder.toString(), sdcardUri = sdcardUri, downloadTimeMillis = if (downloadTiming[0] > 0L) downloadTiming[1] - downloadTiming[0] else -1L, averageSpeedBytesPerSec = computeAvgSpeed(videoInfo, downloadTiming))
        }
    }

    private fun onFinishDownloading(
        preferences: DownloadPreferences,
        videoInfo: VideoInfo,
        downloadPath: String,
        sdcardUri: String,
        downloadTimeMillis: Long = -1L,
        averageSpeedBytesPerSec: Long = -1L,
    ): Result<List<String>> =
        preferences.run {
            val fileName = preferences.newTitle.ifEmpty { videoInfo.filename ?: videoInfo.requestedDownloads?.firstOrNull()?.filename ?: videoInfo.title }
            Log.d(TAG, "onFinishDownloading: $fileName")
            if (sdcard) {
                moveFilesToSdcard(sdcardUri = sdcardUri, tempPath = context.getSdcardTempDir(videoInfo.id))
                    .onSuccess {
                        if (privateMode) return Result.success(emptyList())
                        else if (splitByChapter) insertSplitChapterIntoHistory(videoInfo, it, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec)
                        else insertInfoIntoDownloadHistory(videoInfo, it, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec)
                    }
            } else {
                FileUtil.scanFileToMediaLibraryPostDownload(title = fileName, downloadDir = downloadPath)
                    .run {
                        if (privateMode) Result.success(emptyList())
                        else Result.success(if (splitByChapter) insertSplitChapterIntoHistory(videoInfo, this, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec) else insertInfoIntoDownloadHistory(videoInfo, this, downloadTimeMillis = downloadTimeMillis, averageSpeedBytesPerSec = averageSpeedBytesPerSec))
                    }
            }
        }

    @CheckResult
    fun executeCustomCommandTask(
        urlString: String,
        taskId: String,
        template: CommandTemplate,
        preferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit),
    ): Result<YoutubeDLResponse> {
        val urlList = urlString.split(Regex("[\n ]")).filter { it.isNotBlank() }
        val request = with(preferences) {
            YoutubeDLRequest(urlList).apply {
                commandDirectory.takeIf { it.isNotEmpty() }?.let { addOption("-P", it) }
                addOption("--newline")
                if (aria2c) { enableAria2c() }
                if (useDownloadArchive) { useDownloadArchive() }
                if (restrictFilenames) { addOption("--restrict-filenames") }
                addOption("--config-locations", FileUtil.writeContentToFile(template.template, context.getConfigFile()).absolutePath)
                if (cookies) { runCatching { enableCookies(userAgentString) }.onFailure { cookieErr -> return Result.failure(cookieErr) } }
                if (noCheckCertificate) { addOption("--no-check-certificate") }
            }
        }
        return runCatching { YoutubeDL.getInstance().execute(request = request, processId = taskId, callback = progressCallback) }
    }

    suspend fun executeCommandInBackground(
        url: String,
        template: CommandTemplate = PreferenceUtil.getTemplate(),
        downloadPreferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
    ) {
        downloadPreferences.run {
            val taskId = Downloader.makeKey(url = url, templateName = template.name)
            val notificationId = taskId.toNotificationId()
            val urlList = url.split(Regex("[\n ]")).filter { it.isNotBlank() }

            App.applicationScope.launch(Dispatchers.Main) { context.makeToast(R.string.start_execute) }
            val request = try {
                YoutubeDLRequest(urlList).apply {
                    commandDirectory.takeIf { it.isNotEmpty() }?.let { addOption("-P", it) }
                    addOption("--newline")
                    if (aria2c) { enableAria2c() }
                    if (useDownloadArchive) { useDownloadArchive() }
                    if (restrictFilenames) { addOption("--restrict-filenames") }
                    addOption("--config-locations", FileUtil.writeContentToFile(template.template, context.getConfigFile()).absolutePath)
                    if (cookies) { enableCookies(userAgentString) }
                    if (noCheckCertificate) { addOption("--no-check-certificate") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Downloader.onTaskError(e.message ?: "Setup error", template, url) }
                return@run
            }

            onProcessStarted()
            withContext(Dispatchers.Main) { onTaskStarted(template, url) }
            runCatching {
                val response = YoutubeDL.getInstance().execute(request = request, processId = taskId) { progress, _, text ->
                    NotificationUtil.makeNotificationForCustomCommand(notificationId = notificationId, taskId = taskId, progress = progress.toInt(), templateName = template.name, taskUrl = url, text = text)
                    Downloader.updateTaskOutput(template = template, url = url, line = text, progress = progress)
                }
                onTaskEnded(template, url, response.out + "\n" + response.err)
            }
                .onFailure {
                    it.printStackTrace()
                    if (it is YoutubeDL.CanceledException) return@onFailure
                    it.message.run {
                        if (isNullOrEmpty()) onTaskEnded(template, url)
                        else onTaskError(this, template, url)
                    }
                }
            onProcessEnded()
        }
    }

    private fun checkIfAv1HardwareAccelerated(): Boolean {
        if (PreferenceUtil.containsKey(AV1_HARDWARE_ACCELERATED)) { return AV1_HARDWARE_ACCELERATED.getBoolean() }
        val res = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) false
        else {
            MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { info ->
                info.supportedTypes.any { it.equals("video/av01", ignoreCase = true) } && info.isHardwareAccelerated
            }
        }
        AV1_HARDWARE_ACCELERATED.updateBoolean(res)
        return res
    }
}