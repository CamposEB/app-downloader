package com.junkfood.seal.ui.page.downloadv2.configure

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.TaskFactory
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.FormatSubtitle
import com.junkfood.seal.ui.component.FormatVideoPreview
import com.junkfood.seal.ui.component.GradientCircularProgressIndicator
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SealSearchBar
import com.junkfood.seal.ui.component.SuggestedFormatItem
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.download.VideoClipDialog
import com.junkfood.seal.ui.page.download.VideoSelectionSlider
import com.junkfood.seal.ui.page.settings.general.DialogCheckBoxItem
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.ui.theme.generateLabelColor
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_LIST_VIEW
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FORMAT_MP4_ONLY
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.FormatValidator
import com.junkfood.seal.util.MERGE_MULTI_AUDIO_STREAM
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.SUBTITLE_LANGUAGE
import com.junkfood.seal.util.SubtitleFormat
import com.junkfood.seal.util.VIDEO_CLIP
import com.junkfood.seal.util.VideoClip
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toFileSizeText
import com.junkfood.seal.util.toHttpsUrl
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

private const val TAG = "FormatPage"

private fun extractResolution(format: Format): Pair<Int, Int>? {
    if (format.width != null && format.height != null && format.width > 0 && format.height > 0) {
        return Pair(format.width.toInt(), format.height.toInt())
    }
    val formatString = format.format ?: ""
    val resolutionRegex = """(\d{3,4})x(\d{3,4})""".toRegex()
    val resolutionMatch = resolutionRegex.find(formatString)
    if (resolutionMatch != null) {
        val width = resolutionMatch.groupValues[1].toIntOrNull()
        val height = resolutionMatch.groupValues[2].toIntOrNull()
        if (width != null && height != null && width > 0 && height > 0) {
            return Pair(width, height)
        }
    }
    val heightPattern = """(\d{3,4})p""".toRegex()
    val heightMatch = heightPattern.find(formatString)
    if (heightMatch != null) {
        val height = heightMatch.groupValues[1].toIntOrNull()
        if (height != null && height > 0) {
            val width = (height * 16.0 / 9.0).toInt()
            return Pair(width, height)
        }
    }
    return null
}

private fun getQualityScore(format: Format): Int {
    val resolution = extractResolution(format)
    if (resolution != null) {
        return resolution.first * resolution.second
    }
    val fileSize = format.fileSizeApprox ?: format.fileSize ?: 0.0
    return fileSize.toInt()
}

private fun getAudioQualityScore(format: Format): Double {
    return format.abr
        ?: format.tbr
        ?: format.fileSizeApprox
        ?: format.fileSize
        ?: 0.0
}

private fun requiresStreamMerging(videoInfo: VideoInfo): Boolean {
    val extractor = videoInfo.extractor?.lowercase() ?: ""
    val extractorKey = videoInfo.extractorKey.lowercase()
    val url = (videoInfo.webpageUrl ?: videoInfo.originalUrl ?: "").lowercase()
    return extractor.contains("youtube") ||
            extractorKey.contains("youtube") ||
            extractor.contains("reddit") ||
            extractorKey.contains("reddit") ||
            url.contains("youtube.com") ||
            url.contains("youtu.be") ||
            url.contains("reddit.com") ||
            url.contains("redd.it")
}

private data class FormatConfig(
    val formatList: List<Format>,
    val videoClips: List<VideoClip>,
    val splitByChapter: Boolean,
    val newTitle: String,
    val selectedSubtitles: List<String>,
    val selectedAutoCaptions: List<String>,
    val isAudioOnlyDownload: Boolean,
    val downloadDocs: Boolean = false,
)

@Composable
fun FormatPage(
    modifier: Modifier = Modifier,
    videoInfo: VideoInfo,
    downloader: DownloaderV2 = koinInject(),
    onNavigateBack: () -> Unit = {},
) {
    if (videoInfo.formats.isNullOrEmpty()) return
    val audioOnly = EXTRACT_AUDIO.getBoolean()
    val mergeAudioStream = MERGE_MULTI_AUDIO_STREAM.getBoolean()
    val subtitleLanguageRegex = SUBTITLE_LANGUAGE.getString()
    val downloadSubtitle = SUBTITLE.getBoolean()
    val initialSelectedSubtitles =
        if (downloadSubtitle) {
            videoInfo
                .run { subtitles.keys + automaticCaptions.keys }
                .filterWithRegex(subtitleLanguageRegex)
        } else {
            emptySet()
        }

    var showUpdateSubtitleDialog by remember { mutableStateOf(false) }
    var diffSubtitleLanguages by remember { mutableStateOf(emptySet<String>()) }
    val siteSplitsStreams = requiresStreamMerging(videoInfo)

    FormatPageImpl(
        modifier = modifier,
        videoInfo = videoInfo,
        onNavigateBack = onNavigateBack,
        audioOnly = audioOnly,
        mergeAudioStream = !audioOnly && mergeAudioStream,
        siteSplitsStreams = siteSplitsStreams,
        selectedSubtitleCodes = initialSelectedSubtitles,
        isClippingAvailable = VIDEO_CLIP.getBoolean() && (videoInfo.duration ?: .0) >= 0,
    ) { config ->
        with(config) {
            diffSubtitleLanguages =
                (selectedSubtitles + selectedAutoCaptions)
                    .run { this - this.filterWithRegex(subtitleLanguageRegex) }
                    .toSet()

            run {
                val taskWithState = TaskFactory.createWithConfigurations(
                    videoInfo = videoInfo,
                    formatList = formatList,
                    videoClips = videoClips,
                    splitByChapter = splitByChapter,
                    newTitle = newTitle,
                    selectedSubtitles = selectedSubtitles,
                    selectedAutoCaptions = selectedAutoCaptions,
                    overridePreferences = null,
                    isAudioOnlyDownload = config.isAudioOnlyDownload,
                )
                downloader.enqueue(taskWithState)

                if (diffSubtitleLanguages.isNotEmpty()) {
                    showUpdateSubtitleDialog = true
                } else {
                    onNavigateBack()
                }
            }
        }
    }
    if (showUpdateSubtitleDialog) {
        UpdateSubtitleLanguageDialog(
            modifier = Modifier,
            languages = diffSubtitleLanguages,
            onDismissRequest = {
                showUpdateSubtitleDialog = false
                onNavigateBack()
            },
            onConfirm = {
                SUBTITLE_LANGUAGE.updateString(
                    (diffSubtitleLanguages + subtitleLanguageRegex).joinToString(separator = ",") {
                        it
                    }
                )
                showUpdateSubtitleDialog = false
                onNavigateBack()
            },
        )
    }
}

private const val NOT_SELECTED = -1

@Preview
@Composable
fun FormatPagePreview() {
    val captionsMap =
        mapOf(
            "en-en" to listOf(SubtitleFormat(ext = "", url = "", name = "English from English")),
            "ja-en" to listOf(SubtitleFormat(ext = "", url = "", name = "Japanese from English")),
        )
    val subMap = buildMap {
        put("en", listOf(SubtitleFormat(ext = "ass", url = "", name = "English")))
        put("ja", listOf(SubtitleFormat(ext = "ass", url = "", name = "Japanese")))
    }
    val videoInfo =
        VideoInfo(
            formats =
                buildList {
                    repeat(7) { add(Format(formatId = "$it")) }
                    repeat(7) { add(Format(formatId = "$it", vcodec = "avc1", acodec = "none")) }
                    repeat(7) {
                        add(
                            Format(
                                formatId = "$it",
                                acodec = "aac",
                                vcodec = "none",
                                format = "251 - audio only (medium)",
                                fileSizeApprox = 2000000.0,
                                tbr = 128.0,
                            )
                        )
                    }
                },
            subtitles = subMap,
            automaticCaptions = captionsMap,
            requestedFormats =
                buildList {
                    add(
                        Format(
                            formatId = "616",
                            format = "616 - 1920x1080 (Premium)",
                            acodec = "none",
                            vcodec = "vp09.00.40.08",
                            ext = "webm",
                        )
                    )
                    add(
                        Format(
                            formatId = "251",
                            format = "251 - audio only (medium)",
                            acodec = "opus",
                            vcodec = "none",
                            ext = "webm",
                        )
                    )
                },
            duration = 180.0,
        )
    SealTheme {
        FormatPageImpl(
            videoInfo = videoInfo,
            isClippingAvailable = true,
            mergeAudioStream = true,
            selectedSubtitleCodes = setOf("en", "ja-en"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatPageImpl(
    modifier: Modifier = Modifier,
    videoInfo: VideoInfo = VideoInfo(),
    audioOnly: Boolean = false,
    mergeAudioStream: Boolean = false,
    siteSplitsStreams: Boolean = true,
    isClippingAvailable: Boolean = false,
    selectedSubtitleCodes: Set<String>,
    onNavigateBack: () -> Unit = {},
    onDownloadPressed: (FormatConfig) -> Unit = { _ -> },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isValidatingFormats by remember { mutableStateOf(true) }
    var validatedVideoOnlyFormats by remember { mutableStateOf<List<Format>>(emptyList()) }
    var validatedAudioOnlyFormats by remember { mutableStateOf<List<Format>>(emptyList()) }
    var validatedVideoAudioFormats by remember { mutableStateOf<List<Format>>(emptyList()) }

    if (videoInfo.formats.isNullOrEmpty()) return

    var mp4Only by remember { mutableStateOf(FORMAT_MP4_ONLY.getBoolean()) }
    fun List<Format>.keepMp4(wantedExt: String): List<Format> {
        if (!mp4Only) return this
        val filtered = filter { it.ext.equals(wantedExt, ignoreCase = true) }
        return if (filtered.isNotEmpty()) filtered else this
    }

    val rawVideoOnlyFormats =
        videoInfo.formats.filter { it.vcodec != "none" && it.acodec == "none" }
            .reversed()
            .keepMp4("mp4")
    val rawAudioOnlyFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec == "none" }
            .reversed()
            .keepMp4("m4a")
    val rawVideoAudioFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec != "none" }
            .reversed()
            .keepMp4("mp4")

    LaunchedEffect(videoInfo, mp4Only) {
        isValidatingFormats = true
        try {
            validatedVideoOnlyFormats = FormatValidator.filterValidFormats(rawVideoOnlyFormats, checkUrlAccessibility = false)
            validatedAudioOnlyFormats =
                FormatValidator.filterValidFormats(rawAudioOnlyFormats, checkUrlAccessibility = false)
                    .sortedWith(
                        compareByDescending<Format> { getAudioQualityScore(it) }
                            .thenByDescending { it.fileSizeApprox ?: it.fileSize ?: 0.0 }
                    )
            validatedVideoAudioFormats = FormatValidator.filterValidFormats(rawVideoAudioFormats, checkUrlAccessibility = false)

            val durationSec = videoInfo.duration ?: 0.0
            validatedVideoOnlyFormats =
                FormatValidator.filterImplausibleVideoSizes(validatedVideoOnlyFormats, durationSec)
            validatedVideoAudioFormats =
                FormatValidator.filterImplausibleVideoSizes(validatedVideoAudioFormats, durationSec)

            validatedVideoOnlyFormats = FormatValidator.deduplicateByResolution(validatedVideoOnlyFormats)
            validatedVideoAudioFormats = FormatValidator.deduplicateByResolution(validatedVideoAudioFormats)
        } catch (e: Exception) {
            validatedVideoOnlyFormats = rawVideoOnlyFormats
            validatedAudioOnlyFormats = rawAudioOnlyFormats
            validatedVideoAudioFormats = rawVideoAudioFormats
        } finally {
            isValidatingFormats = false
        }
    }

    val videoOnlyFormats = validatedVideoOnlyFormats
    val audioOnlyFormats = validatedAudioOnlyFormats
    val videoAudioFormats = validatedVideoAudioFormats
    val bestAudioFormat = remember(audioOnlyFormats) { audioOnlyFormats.firstOrNull() }

    val mergedVideoFormats = remember(audioOnly, bestAudioFormat, videoOnlyFormats) {
        if (!audioOnly && bestAudioFormat != null) {
            videoOnlyFormats.map { videoFormat ->
                videoFormat.copy(format = "${videoFormat.format} + audio")
            }
        } else emptyList()
    }

    val allVideoFormats = remember(audioOnly, mergedVideoFormats, videoAudioFormats) {
        if (audioOnly) {
            emptyList()
        } else {
            val combined = (mergedVideoFormats + videoAudioFormats).distinctBy { it.formatId }
            combined.sortedByDescending { getQualityScore(it) }
        }
    }

    val highestVideoFormat = remember(audioOnly, allVideoFormats) {
        if (!audioOnly && allVideoFormats.isNotEmpty()) allVideoFormats.firstOrNull() else null
    }

    val duration = videoInfo.duration ?: 0.0
    var isListView by remember { mutableStateOf(FORMAT_LIST_VIEW.getBoolean()) }
    var showAdvancedMenu by remember { mutableStateOf(false) }

    var videoOnlyItemLimit by remember { mutableIntStateOf(6) }
    var audioOnlyItemLimit by remember { mutableIntStateOf(6) }
    var videoAudioItemLimit by remember { mutableIntStateOf(Int.MAX_VALUE) }

    val isSuggestedFormatAvailable =
        !audioOnly &&
                (!videoInfo.requestedFormats.isNullOrEmpty() ||
                        !videoInfo.requestedDownloads.isNullOrEmpty())

    var isSuggestedFormatSelected by remember { mutableStateOf(isSuggestedFormatAvailable) }
    var selectedVideoAudioFormat by remember { mutableIntStateOf(NOT_SELECTED) }
    var selectedVideoOnlyFormat by remember { mutableIntStateOf(NOT_SELECTED) }
    val selectedAudioOnlyFormats = remember { mutableStateListOf<Int>() }

    LaunchedEffect(allVideoFormats.size, audioOnlyFormats.size, videoOnlyFormats.size) {
        if (selectedVideoAudioFormat != NOT_SELECTED && selectedVideoAudioFormat >= allVideoFormats.size) {
            selectedVideoAudioFormat = NOT_SELECTED
        }
        if (selectedVideoOnlyFormat != NOT_SELECTED && selectedVideoOnlyFormat >= videoOnlyFormats.size) {
            selectedVideoOnlyFormat = NOT_SELECTED
        }
        selectedAudioOnlyFormats.removeAll { it >= audioOnlyFormats.size }
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current

    fun String?.share() =
        this?.let {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            context.startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, it)
                    },
                    null,
                ),
                null,
            )
        }

    var isClippingVideo by remember { mutableStateOf(false) }
    var isSplittingVideo by remember { mutableStateOf(false) }
    val isSplitByChapterAvailable = !videoInfo.chapters.isNullOrEmpty()

    val videoDurationRange = 0f..(videoInfo.duration?.toFloat() ?: 0f)
    var showVideoClipDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSubtitleSelectionDialog by remember { mutableStateOf(false) }

    var videoClipDuration by remember { mutableStateOf(videoDurationRange) }
    var videoTitle by remember { mutableStateOf("") }

    val suggestedSubtitleMap: Map<String, List<SubtitleFormat>> =
        videoInfo.subtitles.takeIf { it.isNotEmpty() }
            ?: videoInfo.automaticCaptions.filterKeys { it.endsWith("-orig") }

    val otherSubtitleMap: Map<String, List<SubtitleFormat>> =
        videoInfo.subtitles + videoInfo.automaticCaptions - suggestedSubtitleMap.keys

    LaunchedEffect(isClippingVideo) {
        delay(200)
        videoClipDuration = videoDurationRange
    }

    val lazyListState = rememberLazyListState()

    val formatList: List<Format> by remember(
        allVideoFormats, mergedVideoFormats, videoOnlyFormats, audioOnlyFormats, videoAudioFormats,
        bestAudioFormat, highestVideoFormat, audioOnly, siteSplitsStreams,
    ) {
        derivedStateOf {
            mutableListOf<Format>().apply {
                if (isSuggestedFormatSelected) {
                    if (!audioOnly) {
                        if (siteSplitsStreams) {
                            if (highestVideoFormat != null) {
                                val isMergedFormat = mergedVideoFormats.any { it.formatId == highestVideoFormat.formatId }
                                if (isMergedFormat && bestAudioFormat != null) {
                                    val originalVideoFormat = videoOnlyFormats.find { it.formatId == highestVideoFormat.formatId }
                                    if (originalVideoFormat != null) {
                                        add(originalVideoFormat)
                                        add(bestAudioFormat)
                                    }
                                } else {
                                    add(highestVideoFormat)
                                }
                            } else {
                                videoInfo.requestedFormats?.let { addAll(it) }
                                    ?: videoInfo.requestedDownloads?.forEach { it.requestedFormats?.let { addAll(it) } }
                            }
                        } else {
                            val bestCombined = videoAudioFormats.maxByOrNull { getQualityScore(it) }
                            if (bestCombined != null) {
                                add(bestCombined)
                            } else if (highestVideoFormat != null) {
                                val isMergedFormat = mergedVideoFormats.any { it.formatId == highestVideoFormat.formatId }
                                if (isMergedFormat && bestAudioFormat != null) {
                                    val originalVideoFormat = videoOnlyFormats.find { it.formatId == highestVideoFormat.formatId }
                                    if (originalVideoFormat != null) {
                                        add(originalVideoFormat)
                                        add(bestAudioFormat)
                                    }
                                } else {
                                    add(highestVideoFormat)
                                }
                            } else {
                                videoInfo.requestedFormats?.let { addAll(it) }
                                    ?: videoInfo.requestedDownloads?.forEach { it.requestedFormats?.let { addAll(it) } }
                            }
                        }
                    } else {
                        videoInfo.requestedFormats?.let { addAll(it) }
                            ?: videoInfo.requestedDownloads?.forEach { it.requestedFormats?.let { addAll(it) } }
                    }
                } else {
                    selectedAudioOnlyFormats.forEach { index ->
                        audioOnlyFormats.getOrNull(index)?.let { add(it) }
                    }
                    allVideoFormats.getOrNull(selectedVideoAudioFormat)?.let { selectedFormat ->
                        val isMergedFormat = mergedVideoFormats.any { it.formatId == selectedFormat.formatId }
                        if (isMergedFormat && bestAudioFormat != null) {
                            val originalVideoFormat = videoOnlyFormats.find { it.formatId == selectedFormat.formatId }
                            if (originalVideoFormat != null) {
                                add(originalVideoFormat)
                                add(bestAudioFormat)
                            }
                        } else {
                            add(selectedFormat)
                        }
                    }
                    videoOnlyFormats.getOrNull(selectedVideoOnlyFormat)?.let { add(it) }
                }
            }
        }
    }

    val isFabExpanded by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
    val selectedSubtitles = remember { mutableStateListOf<String>().apply { addAll(selectedSubtitleCodes) } }
    val selectedAutoCaptions = remember { mutableStateListOf<String>() }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Opções de Download", // SnapTube style
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Outlined.Close, stringResource(R.string.close))
                    }
                },
                actions = {
                    // Menu de 3 pontinhos para opções avançadas
                    Box {
                        IconButton(onClick = { showAdvancedMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showAdvancedMenu,
                            onDismissRequest = { showAdvancedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.mp4_only)) },
                                onClick = {
                                    mp4Only = !mp4Only
                                    FORMAT_MP4_ONLY.updateBoolean(mp4Only)
                                    showAdvancedMenu = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, tint = if(mp4Only) MaterialTheme.colorScheme.primary else Color.Transparent) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.format_list_view_title)) },
                                onClick = {
                                    isListView = !isListView
                                    FORMAT_LIST_VIEW.updateBoolean(isListView)
                                    showAdvancedMenu = false
                                },
                                leadingIcon = { Icon(if (isListView) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView, null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        floatingActionButton = {
            val isFormatSelected = isSuggestedFormatSelected ||
                    selectedVideoAudioFormat != NOT_SELECTED ||
                    selectedVideoOnlyFormat != NOT_SELECTED ||
                    selectedAudioOnlyFormats.isNotEmpty()
            val isNetworkAvailable = FormatValidator.isNetworkAvailable()
            val canDownload = isFormatSelected && isNetworkAvailable && !isValidatingFormats

            AnimatedVisibility(
                visible = isFormatSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                GradientExtendedFab(
                    modifier = Modifier.padding(12.dp),
                    expanded = true, // Always expanded for SnapTube style
                    enabled = canDownload,
                    onClick = {
                        if (canDownload) {
                            onDownloadPressed(
                                FormatConfig(
                                    formatList = formatList,
                                    videoClips = if (isClippingVideo) listOf(VideoClip(videoClipDuration)) else emptyList(),
                                    splitByChapter = isSplittingVideo,
                                    newTitle = videoTitle,
                                    selectedSubtitles = selectedSubtitles,
                                    selectedAutoCaptions = selectedAutoCaptions,
                                    isAudioOnlyDownload = audioOnly || (
                                            !isSuggestedFormatSelected &&
                                                    selectedVideoAudioFormat == NOT_SELECTED &&
                                                    selectedVideoOnlyFormat == NOT_SELECTED &&
                                                    selectedAudioOnlyFormats.isNotEmpty()
                                            ),
                                )
                            )
                        }
                    },
                    icon = {
                        if (isValidatingFormats) {
                            GradientCircularProgressIndicator(size = 22.dp, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(22.dp))
                        }
                    },
                    text = {
                        Text(
                            if (!isNetworkAvailable) "Sem Rede"
                            else if (isValidatingFormats) "Validando..."
                            else if (selectedVideoOnlyFormat != NOT_SELECTED && selectedAudioOnlyFormats.isNotEmpty())
                                stringResource(R.string.download_and_merge)
                            else "Baixar Agora"
                        )
                    },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center, // Center FAB for SnapTube style
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp), // CORRIGIDO AQUI
        ) {
            videoInfo.run {
                item {
                    FormatVideoPreview(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        title = videoTitle.ifEmpty { title },
                        author = uploader ?: channel ?: uploaderId.toString(),
                        thumbnailUrl = thumbnail.toHttpsUrl(),
                        duration = duration.roundToInt(),
                        isClippingVideo = isClippingVideo,
                        isSplittingVideo = isSplittingVideo,
                        isClippingAvailable = isClippingAvailable,
                        isSplitByChapterAvailable = isSplitByChapterAvailable,
                        onClippingToggled = { isClippingVideo = !isClippingVideo },
                        onSplittingToggled = { isSplittingVideo = !isSplittingVideo },
                        onRename = { showRenameDialog = true },
                        onOpenThumbnail = { uriHandler.openUri(thumbnail.toHttpsUrl()) },
                    )
                }
            }

            // Live summary
            val hasAnySelection =
                isSuggestedFormatSelected || selectedVideoAudioFormat != NOT_SELECTED || selectedVideoOnlyFormat != NOT_SELECTED || selectedAudioOnlyFormats.isNotEmpty()
            val isMerging = selectedVideoOnlyFormat != NOT_SELECTED && selectedAudioOnlyFormats.isNotEmpty()

            if (hasAnySelection) {
                item {
                    val resolutionText = remember(formatList) {
                        formatList.firstNotNullOfOrNull { extractResolution(it) }?.let { (w, h) -> "${w}\u00d7${h}" }
                    }
                    val totalSize = remember(formatList, duration) {
                        formatList.sumOf { f -> f.fileSize ?: f.fileSizeApprox ?: (f.tbr?.times(duration * 125) ?: 0.0) }
                    }
                    val totalSizeText = totalSize.toFileSizeText()
                    val extText = remember(formatList) { formatList.firstOrNull()?.ext?.uppercase() }

                    SelectionSummaryCard(
                        modifier = Modifier.fillMaxWidth(),
                        resolutionOrLabel = resolutionText ?: if (audioOnly) stringResource(R.string.audio) else stringResource(R.string.video),
                        formatExt = extText,
                        sizeText = totalSizeText,
                        isMerging = isMerging,
                        audioTrackCount = selectedAudioOnlyFormats.size,
                    )
                }
            }

            if (isValidatingFormats) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            GradientCircularProgressIndicator(size = 48.dp, strokeWidth = 4.dp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando formatos...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Suggested Section (Main Big Button)
            if (isSuggestedFormatAvailable && !isValidatingFormats) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        FormatSubtitle(text = "Sugerido (Melhor Qualidade)", modifier = Modifier.padding(bottom = 8.dp), showDivider = false)
                        val onClick = {
                            isSuggestedFormatSelected = true
                            selectedAudioOnlyFormats.clear()
                            selectedVideoAudioFormat = NOT_SELECTED
                            selectedVideoOnlyFormat = NOT_SELECTED
                        }
                        val displayFormat = if (highestVideoFormat != null && !audioOnly) {
                            val isMergedFormat = mergedVideoFormats.any { it.formatId == highestVideoFormat.formatId }
                            if (isMergedFormat && bestAudioFormat != null) {
                                val originalVideoFormat = videoOnlyFormats.find { it.formatId == highestVideoFormat.formatId }
                                listOfNotNull(originalVideoFormat, bestAudioFormat)
                            } else listOf(highestVideoFormat)
                        } else null

                        SuggestedFormatItem(
                            modifier = Modifier.fillMaxWidth(),
                            videoInfo = videoInfo,
                            overrideFormats = displayFormat,
                            selected = isSuggestedFormatSelected,
                            onClick = onClick,
                        )
                    }
                }
            }

            // Video Section
            if (allVideoFormats.isNotEmpty() && !isValidatingFormats) {
                item {
                    FormatSubtitle(
                        text = "Vídeo (com áudio)",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        showDivider = false,
                        count = allVideoFormats.size,
                    )
                }
                itemsIndexed(allVideoFormats) { index, formatInfo ->
                    SnapTubeFormatCard(
                        formatInfo = formatInfo,
                        duration = duration,
                        selected = selectedVideoAudioFormat == index,
                        isAudio = false,
                        onLongClick = { formatInfo.url.share() },
                        onClick = {
                            selectedVideoAudioFormat = if (selectedVideoAudioFormat == index) NOT_SELECTED else {
                                selectedAudioOnlyFormats.clear()
                                selectedVideoOnlyFormat = NOT_SELECTED
                                isSuggestedFormatSelected = false
                                index
                            }
                        }
                    )
                }
            }

            // Audio Section
            if (audioOnlyFormats.isNotEmpty() && !isValidatingFormats) {
                item {
                    FormatSubtitle(
                        text = "Somente Áudio",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        showDivider = false,
                        count = audioOnlyFormats.size,
                    )
                }
                itemsIndexed(audioOnlyFormats) { index, formatInfo ->
                    SnapTubeFormatCard(
                        formatInfo = formatInfo,
                        duration = duration,
                        selected = selectedAudioOnlyFormats.contains(index),
                        isAudio = true,
                        onLongClick = { formatInfo.url.share() },
                        onClick = {
                            if (selectedAudioOnlyFormats.contains(index)) {
                                selectedAudioOnlyFormats.remove(index)
                            } else {
                                if (!mergeAudioStream || selectedVideoOnlyFormat != NOT_SELECTED) {
                                    selectedAudioOnlyFormats.clear()
                                }
                                selectedVideoAudioFormat = NOT_SELECTED
                                isSuggestedFormatSelected = false
                                selectedAudioOnlyFormats.add(index)
                            }
                        }
                    )
                }
            }

            // Advanced: Video (no audio) tucked away
            if (!audioOnly && videoOnlyFormats.isNotEmpty() && !isValidatingFormats) {
                item {
                    var showAdvanced by remember { mutableStateOf(false) }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable { showAdvanced = !showAdvanced },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Opções Avançadas (Vídeo sem áudio)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(if (showAdvanced) Icons.Outlined.Delete else Icons.Outlined.MoreVert, null) // Placeholder icon
                        }
                    }
                    if (showAdvanced) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            videoOnlyFormats.forEachIndexed { index, formatInfo ->
                                SnapTubeFormatCard(
                                    formatInfo = formatInfo,
                                    duration = duration,
                                    selected = selectedVideoOnlyFormat == index,
                                    isAudio = false,
                                    onLongClick = { formatInfo.url.share() },
                                    onClick = {
                                        selectedVideoOnlyFormat = if (selectedVideoOnlyFormat == index) NOT_SELECTED else {
                                            selectedVideoAudioFormat = NOT_SELECTED
                                            isSuggestedFormatSelected = false
                                            if (selectedAudioOnlyFormats.size > 1) {
                                                val first = selectedAudioOnlyFormats.first()
                                                selectedAudioOnlyFormats.clear()
                                                selectedAudioOnlyFormats.add(first)
                                            }
                                            index
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVideoClipDialog)
        VideoClipDialog(
            onDismissRequest = { showVideoClipDialog = false },
            initialValue = videoClipDuration,
            valueRange = videoDurationRange,
            onConfirm = { videoClipDuration = it },
        )

    if (showRenameDialog)
        RenameDialog(
            initialValue = videoTitle.ifEmpty { videoInfo.title },
            onDismissRequest = { showRenameDialog = false },
        ) { videoTitle = it }

    if (showSubtitleSelectionDialog)
        SubtitleSelectionDialog(
            suggestedSubtitles = suggestedSubtitleMap,
            autoCaptions = otherSubtitleMap,
            selectedSubtitles = selectedSubtitles,
            onDismissRequest = { showSubtitleSelectionDialog = false },
            onConfirm = { subs, autoSubs ->
                selectedSubtitles.run { clear(); addAll(subs) }
                showSubtitleSelectionDialog = false
            },
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SnapTubeFormatCard(
    formatInfo: Format,
    duration: Double,
    selected: Boolean,
    isAudio: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val resolution = extractResolution(formatInfo)
    val title = if (isAudio) {
        val formatName = formatInfo.format?.substringAfter("-")?.trim()?.capitalize() ?: "Áudio"
        "Áudio ${formatInfo.ext?.uppercase() ?: ""}" // CORRIGIDO AQUI
    } else {
        if (resolution != null) "${resolution.second}p" else formatInfo.format?.substringBefore(" -") ?: "Vídeo"
    }

    val size = formatInfo.fileSize ?: formatInfo.fileSizeApprox ?: (formatInfo.tbr?.times(duration * 125) ?: 0.0)
    val sizeText = if (size > 0) size.toFileSizeText() else "Tamanho Desconhecido"
    val extText = formatInfo.ext?.uppercase() ?: ""

    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Box
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Outlined.MusicNote else Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$sizeText ${if (extText.isNotEmpty()) "· $extText" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            // Check Icon
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// --- Dialogs e funções auxiliares mantidos do original ---

@Composable
private fun RenameDialog(
    initialValue: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var filename by remember { mutableStateOf(initialValue) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { ConfirmButton { onConfirm(filename); onDismissRequest() } },
        dismissButton = { DismissButton { onDismissRequest() } },
        title = { Text(text = stringResource(id = R.string.rename)) },
        icon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text(text = stringResource(id = R.string.title)) },
                    trailingIcon = { if (filename == initialValue) ClearButton { filename = "" } },
                )
            }
        },
    )
}

private fun (Map<String, List<SubtitleFormat>>).filterWithSearchText(searchText: String): Map<String, List<SubtitleFormat>> {
    return this.filter {
        it.run {
            searchText.isBlank() || key.contains(searchText, ignoreCase = true) || value.any { format -> format.name?.contains(searchText, ignoreCase = true) ?: false }
        }
    }
}

private fun Map<String, List<SubtitleFormat>>.sortedWithSelection(selectedKeys: List<String>): Map<String, List<SubtitleFormat>> {
    return this.toList().sortedWith { entry1, entry2 ->
        when {
            entry1.first in selectedKeys && entry2.first in selectedKeys -> entry1.compareTo(entry2)
            entry1.first in selectedKeys -> -1
            entry2.first in selectedKeys -> 1
            else -> entry1.compareTo(entry2)
        }
    }.toMap()
}

private fun (Pair<String, List<SubtitleFormat>>).compareTo(other: (Pair<String, List<SubtitleFormat>>)): Int {
    val (key, list) = this
    val (otherKey, otherList) = other
    val name = list.getSubtitleName()
    val otherName = otherList.getSubtitleName()
    return if (name != null && otherName != null) name.compareTo(otherName) else key.compareTo(otherKey)
}

private fun (List<SubtitleFormat>).getSubtitleName(): String? = firstOrNull()?.name

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubtitleSelectionDialog(
    suggestedSubtitles: Map<String, List<SubtitleFormat>>,
    autoCaptions: Map<String, List<SubtitleFormat>>,
    selectedSubtitles: List<String>,
    onDismissRequest: () -> Unit = {},
    onConfirm: (subs: List<String>, autoSubs: List<String>) -> Unit = { _, _ -> },
) {
    var searchText by remember { mutableStateOf("") }
    val selectedSubtitlesState = remember { mutableStateListOf<String>().apply { addAll(selectedSubtitles) } }
    val selectedAutoCaptions = remember { mutableStateListOf<String>() }

    val suggestedSubtitlesFiltered = suggestedSubtitles.filterWithSearchText(searchText).sortedWithSelection(selectedSubtitlesState)
    val autoCaptionsFiltered = autoCaptions.filterWithSearchText(searchText).sortedWithSelection(selectedSubtitlesState)

    SealDialog(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        onDismissRequest = onDismissRequest,
        confirmButton = { ConfirmButton { onConfirm(selectedSubtitlesState, selectedAutoCaptions) } },
        dismissButton = { DismissButton { onDismissRequest() } },
        title = { Text(text = stringResource(id = R.string.subtitle_language)) },
        icon = { Icon(imageVector = Icons.Outlined.Subtitles, contentDescription = null) },
        text = {
            Column {
                if (autoCaptions.size + suggestedSubtitles.size > 5) {
                    SealSearchBar(
                        text = searchText,
                        placeholderText = stringResource(R.string.search_in_subtitles),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) { searchText = it }
                }

                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    if (suggestedSubtitlesFiltered.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.suggested),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            )
                        }
                    }
                    for ((code, formats) in suggestedSubtitlesFiltered) {
                        item(key = code) {
                            DialogCheckBoxItem(
                                modifier = Modifier.animateItem(),
                                checked = selectedSubtitlesState.contains(code),
                                onValueChange = {
                                    if (selectedSubtitlesState.contains(code)) selectedSubtitlesState.remove(code)
                                    else selectedSubtitlesState.add(code)
                                },
                                text = formats.first().run { name ?: protocol ?: code },
                            )
                        }
                    }

                    if (autoCaptionsFiltered.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.auto_subtitle),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            )
                        }
                        for ((code, formats) in autoCaptionsFiltered) {
                            item(key = code) {
                                DialogCheckBoxItem(
                                    modifier = Modifier.animateItem(),
                                    checked = selectedAutoCaptions.contains(code),
                                    onValueChange = {
                                        if (selectedAutoCaptions.contains(code)) selectedAutoCaptions.remove(code)
                                        else selectedAutoCaptions.add(code)
                                    },
                                    text = formats.first().run { name ?: protocol ?: code },
                                )
                            }
                        }
                    }
                }
                androidx.compose.material3.HorizontalDivider()
            }
        },
    )
}

@Preview
@Composable
private fun SubtitleSelectionDialogPreview() {
    val captionsMap = mapOf("en-en" to listOf(SubtitleFormat(ext = "", url = "", name = "English from English")))
    val subMap = buildMap { put("en", listOf(SubtitleFormat(ext = "ass", url = "", name = "English"))) }
    SealTheme {
        SubtitleSelectionDialog(suggestedSubtitles = subMap, autoCaptions = captionsMap, selectedSubtitles = listOf())
    }
}

@Composable
private fun SelectionSummaryCard(
    modifier: Modifier = Modifier,
    resolutionOrLabel: String,
    formatExt: String?,
    sizeText: String,
    isMerging: Boolean,
    audioTrackCount: Int,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resolutionOrLabel,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val details = buildList {
                    formatExt?.takeIf { it.isNotBlank() }?.let { add(it) }
                    add(sizeText)
                    if (isMerging) add("merged" + if (audioTrackCount > 1) " ×$audioTrackCount" else "")
                }.joinToString(separator = " · ")
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GradientExtendedFab(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "fabScale",
    )

    val brush = if (enabled) {
        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    } else {
        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
    }
    val contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.height(56.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        interactionSource = interactionSource,
        shadowElevation = if (enabled) 4.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.background(brush).padding(horizontal = if (expanded) 20.dp else 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon()
                    AnimatedVisibility(visible = expanded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)) {
                                text()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableTextAction(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    AnimatedVisibility(visible = visible, exit = fadeOut(animationSpec = spring())) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelLarge,
            modifier = modifier.clip(MaterialTheme.shapes.small).clickable(onClick = onClick).padding(vertical = 6.dp, horizontal = 12.dp),
        )
    }
}

fun <T : Collection<String>> T.filterWithRegex(subtitleLanguageRegex: String): Set<String> {
    val regexGroup = subtitleLanguageRegex.split(',')
    return filter { language -> regexGroup.any { Regex(it).matchEntire(language) != null } }.toSet()
}

@Composable
@Preview
fun UpdateSubtitleLanguageDialog(
    modifier: Modifier = Modifier,
    languages: Set<String> = setOf("en", "ja"),
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.update_subtitle_languages), textAlign = TextAlign.Center) },
        icon = { Icon(imageVector = Icons.Filled.Subtitles, contentDescription = null) },
        text = {
            Column {
                Text(text = stringResource(R.string.update_language_msg))
                Spacer(modifier = Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    languages.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.padding(end = 8.dp).size(16.dp).background(color = it.hashCode().generateLabelColor(), shape = CircleShape).clearAndSetSemantics {}) {}
                            Text(text = it, modifier = Modifier, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text(text = stringResource(id = R.string.okay)) } },
        dismissButton = { OutlinedButton(onClick = onDismissRequest) { Text(text = stringResource(id = R.string.no_thanks)) } },
    )
}