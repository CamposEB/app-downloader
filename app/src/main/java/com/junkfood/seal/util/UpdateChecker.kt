package com.junkfood.seal.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
data class GithubAsset(
    val name: String,
    val browser_download_url: String
)

@Serializable
data class GithubRelease(
    val tag_name: String,
    val assets: List<GithubAsset>
)

@Composable
fun CheckForUpdatesDialog() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }
    var isDownloading by remember { mutableStateOf(false) }

    // Memória para não avisar novamente a mesma versão que o usuário ignorou
    val prefs = remember { context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE) }
    var skippedVersion by remember { mutableStateOf(prefs.getString("skipped_version", "") ?: "") }

    // Monitora quando o DownloadManager termina o download
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri = downloadManager.getUriForDownloadedFile(id)

                    if (uri != null) {
                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/vnd.android.package-archive")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(installIntent)
                        } catch (e: Exception) {
                            context.makeToast("Erro ao abrir instalador.")
                        }
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(Unit) {
        val updateInfo = withContext(Dispatchers.IO) { fetchLatestRelease() }
        if (updateInfo != null) {
            val currentVersion = com.junkfood.seal.BuildConfig.VERSION_NAME // Ex: "1.0 BETA" ou "1.0.3"
            val newVersion = updateInfo.tag_name.replace("v", "").trim() // Ex: "1.0.4"

            // Se a versão for mais nova E não for a versão que o usuário já ignorou
            if (isNewerVersion(currentVersion, newVersion) && newVersion != skippedVersion && !currentVersion.contains("debug")) {
                val apkAsset = updateInfo.assets.find { it.name.endsWith(".apk") }
                if (apkAsset != null) {
                    latestVersion = updateInfo.tag_name
                    downloadUrl = apkAsset.browser_download_url
                    showDialog = true
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showDialog = false },
            icon = { Icon(Icons.Outlined.SystemUpdate, contentDescription = null) },
            title = { Text("Atualização Disponível!") },
            text = {
                Text("A versão $latestVersion do Destiny está disponível. Deseja baixar e instalar agora?")
            },
            confirmButton = {
                TextButton(
                    enabled = !isDownloading,
                    onClick = {
                        isDownloading = true

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                            context.makeToast("Permita instalar apps e toque em atualizar novamente.")
                            isDownloading = false
                        } else {
                            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                                .setTitle("Atualizando Destiny")
                                .setDescription("Baixando nova versão...")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DestinyUpdate.apk")
                                .setMimeType("application/vnd.android.package-archive")

                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)

                            showDialog = false
                            context.makeToast("Download iniciado. A instalação abrirá automaticamente.")
                        }
                    }
                ) {
                    Text(if (isDownloading) "Baixando..." else "Atualizar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Salva no celular que o usuário ignorou ESSA versão específica
                        skippedVersion = latestVersion.replace("v", "").trim()
                        prefs.edit().putString("skipped_version", skippedVersion).apply()
                        showDialog = false
                    },
                    enabled = !isDownloading
                ) {
                    Text("Depois")
                }
            }
        )
    }
}

// Função para comparar versões corretamente (ex: 1.0.10 é maior que 1.0.2)
private fun isNewerVersion(current: String, latest: String): Boolean {
    // Remove qualquer letra da string e deixa só números e pontos
    val currentClean = current.replace(Regex("[^0-9.]"), "").trim('.')
    val latestClean = latest.replace(Regex("[^0-9.]"), "").trim('.')

    val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
    val latestParts = latestClean.split(".").mapNotNull { it.toIntOrNull() }

    for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
        val c = currentParts.getOrElse(i) { 0 }
        val l = latestParts.getOrElse(i) { 0 }
        if (l > c) return true
        if (l < c) return false
    }
    return false
}

private fun fetchLatestRelease(): GithubRelease? {
    return try {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://api.github.com/repos/CamposEB/app-downloader/releases/latest")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val json = response.body?.string() ?: return null
            Json { ignoreUnknownKeys = true }.decodeFromString<GithubRelease>(json)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}