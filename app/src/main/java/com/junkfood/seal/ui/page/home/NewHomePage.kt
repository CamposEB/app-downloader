package com.junkfood.seal.ui.page.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.junkfood.seal.R
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalGradientDarkMode
import com.junkfood.seal.ui.common.ThemedIconColors
import com.junkfood.seal.ui.page.downloadv2.UiAction
import com.junkfood.seal.ui.page.downloadv2.configure.Config
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialog
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.configure.FormatPage
import com.junkfood.seal.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.theme.GradientDarkColors
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import java.io.File
import com.junkfood.seal.util.toFileSizeText
import com.junkfood.seal.util.getErrorReport
import com.junkfood.seal.util.makeToast
import com.junkfood.seal.util.matchUrlFromClipboard
import com.junkfood.seal.util.SPONSOR_DIALOG_FREQUENCY
import com.junkfood.seal.util.SPONSOR_DIALOG_LAST_SHOWN
import com.junkfood.seal.util.SPONSOR_FREQ_OFF
import com.junkfood.seal.util.SPONSOR_FREQ_WEEKLY
import com.junkfood.seal.util.BATTERY_POPUP_HIDDEN
import com.junkfood.seal.util.BatteryUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getLong
import com.junkfood.seal.util.PreferenceUtil.updateLong
import com.junkfood.seal.util.CheckForUpdatesDialog
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/** Paleta preto/azul da tela inicial do Destiny. */
private object DestinyHome {
    val Black = Color(0xFF05070D)
    val CardBg = Color(0xFF0E1630)
    val Blue = Color(0xFF1E6BFF)
    val BlueLight = Color(0xFF4DA3FF)
    val BlueDeep = Color(0xFF1448C8)

    val backgroundBrush =
        Brush.verticalGradient(listOf(Color(0xFF0B2250), Color(0xFF060A16), Black))
    val buttonBrush = Brush.horizontalGradient(listOf(Blue, BlueLight))
    val titleBrush = Brush.horizontalGradient(listOf(Blue, BlueLight))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomePage(
    modifier: Modifier = Modifier,
    onMenuOpen: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToBatchUrlImport: () -> Unit = {},
    onNavigateToVideoInfoDownload: () -> Unit = {},
    onNavigateToThumbnailDownload: () -> Unit = {},
    onNavigateToCommentDownload: () -> Unit = {},
    dialogViewModel: DownloadDialogViewModel,
    downloader: DownloaderV2 = koinInject(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var isFetchingInfo by remember { mutableStateOf(false) }
    var clipboardUrl by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Pre-fill URL from share intent and auto-open the download dialog.
    val sharedUrl by dialogViewModel.sharedUrlFlow.collectAsState()
    LaunchedEffect(sharedUrl) {
        if (sharedUrl.isNotBlank()) {
            urlText = sharedUrl
            dialogViewModel.consumeSharedUrl()
            dialogViewModel.postAction(Action.ShowSheet(listOf(sharedUrl)))
        }
    }

    // Get lifecycle owner
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // State to track lifecycle and force refresh
    var lifecycleRefreshTrigger by remember { mutableStateOf(0) }

    // Monitor lifecycle events to trigger refresh when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                lifecycleRefreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Detecção Automática de Link na área de transferência
    LaunchedEffect(lifecycleRefreshTrigger) {
        if (urlText.isEmpty() && !isFetchingInfo) {
            val clipText = clipboardManager.getText()?.text
            if (clipText != null) {
                val matchedUrl = context.matchUrlFromClipboard(clipText)
                if (matchedUrl != null && matchedUrl != clipboardUrl) {
                    clipboardUrl = matchedUrl
                    urlText = matchedUrl

                    delay(300)

                    // Correção: startDownload busca as informações E abre o painel
                    dialogViewModel.startDownload(listOf(matchedUrl))
                }
            }
        }
    }

    // Permission states
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    var permissionsChecked by remember { mutableStateOf(false) }

    // Check notification permission
    val hasNotificationPermission = remember(lifecycleRefreshTrigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed below Android 13
        }
    }

    var isBatteryOptimizationDisabled by remember {
        mutableStateOf(BatteryUtil.isIgnoringBatteryOptimizations(context))
    }
    val shouldPromptBatteryDialog = { !isBatteryOptimizationDisabled }

    LaunchedEffect(lifecycleRefreshTrigger) {
        isBatteryOptimizationDisabled = BatteryUtil.isIgnoringBatteryOptimizations(context)
    }

    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Permission state will be checked on resume */ }

    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isBatteryOptimizationDisabled = BatteryUtil.isIgnoringBatteryOptimizations(context)
        scope.launch {
            delay(500L)
            isBatteryOptimizationDisabled = BatteryUtil.isIgnoringBatteryOptimizations(context)
        }
    }

    // Check permissions on first load
    LaunchedEffect(Unit) {
        if (!permissionsChecked) {
            permissionsChecked = true
            if (!hasNotificationPermission) {
                showNotificationPermissionDialog = true
            } else if (shouldPromptBatteryDialog()) {
                showBatteryOptimizationDialog = true
            }
        }
    }

    // Monitor permission state changes to show next dialog when user returns from settings
    LaunchedEffect(hasNotificationPermission, isBatteryOptimizationDisabled) {
        if (permissionsChecked) {
            if (!showNotificationPermissionDialog && hasNotificationPermission && shouldPromptBatteryDialog()) {
                showBatteryOptimizationDialog = true
            }
        }
    }

    LaunchedEffect(lifecycleRefreshTrigger) {
        if (permissionsChecked &&
            !showNotificationPermissionDialog &&
            hasNotificationPermission &&
            shouldPromptBatteryDialog()
        ) {
            showBatteryOptimizationDialog = true
        }
    }

    var recentDownloads by remember { mutableStateOf(emptyList<DownloadedVideoInfo>()) }
    LaunchedEffect(Unit) {
        DatabaseUtil.getVisibleDownloadHistoryFlow().collect { list ->
            recentDownloads = list
        }
    }

    var localHiddenIds by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(recentDownloads) {
        val currentIds = recentDownloads.map { it.id }.toSet()
        localHiddenIds = localHiddenIds.intersect(currentIds)
    }

    val recentFiveDownloads = remember(recentDownloads) {
        recentDownloads
            .distinctBy { it.videoUrl + it.videoPath }
            .takeLast(5)
            .reversed()
    }

    val taskStateMap = downloader.getTaskStateMap()

    val activeTaskUrls by remember {
        derivedStateOf {
            taskStateMap
                .filter { (_, state) -> state.downloadState !is Task.DownloadState.Completed }
                .keys
                .map { it.url }
                .toSet()
        }
    }

    val recentDownloadIdentifiers = remember(recentFiveDownloads) {
        recentFiveDownloads.flatMap { download ->
            listOf(
                download.videoUrl,
                download.videoPath,
                "${download.videoUrl}|${download.videoPath}"
            )
        }.toSet()
    }

    val activeDownloads by remember(recentDownloadIdentifiers) {
        derivedStateOf {
            taskStateMap
                .filter { (task, state) ->
                    val ds = state.downloadState
                    when {
                        ds is Task.DownloadState.Completed -> {
                            val filePath = ds.filePath
                            val taskUrl  = task.url
                            val isInRecent =
                                recentDownloadIdentifiers.contains(taskUrl) ||
                                        recentDownloadIdentifiers.contains(filePath) ||
                                        recentDownloadIdentifiers.contains("$taskUrl|$filePath")
                            !isInRecent
                        }
                        else -> true
                    }
                }
                .toList()
                .sortedWith(
                    compareBy<Pair<Task, Task.State>> { (_, state) ->
                        downloadStateSortPriority(state.downloadState)
                    }.thenByDescending { (task, _) -> task.timeCreated }
                )
        }
    }

    val recentFiveDownloadsFiltered = remember(recentFiveDownloads, activeTaskUrls, localHiddenIds) {
        recentFiveDownloads.filter { it.videoUrl !in activeTaskUrls && it.id !in localHiddenIds }
    }

    val allVisibleIdentifiers = remember(recentDownloads) {
        recentDownloads.flatMap { download ->
            listOf(
                download.videoUrl,
                download.videoPath,
                "${download.videoUrl}|${download.videoPath}"
            )
        }.toSet()
    }
    LaunchedEffect(allVisibleIdentifiers) {
        taskStateMap.entries
            .filter { (task, state) ->
                val downloadState = state.downloadState
                downloadState is Task.DownloadState.Completed &&
                        (allVisibleIdentifiers.contains(task.url) ||
                                allVisibleIdentifiers.contains(downloadState.filePath) ||
                                allVisibleIdentifiers.contains("${task.url}|${downloadState.filePath}"))
            }
            .map { it.key }
            .forEach { task -> downloader.remove(task) }
    }

    var detailsDialogIndex by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        showExitDialog = true
    }

    // Notification Permission Dialog
    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationPermissionDialog = false
                if (shouldPromptBatteryDialog()) {
                    showBatteryOptimizationDialog = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.notification_permission_required),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.notification_permission_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            notificationSettingsLauncher.launch(intent)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.grant_permission),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionDialog = false
                        if (shouldPromptBatteryDialog()) {
                            showBatteryOptimizationDialog = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    // Battery Optimization Dialog
    if (showBatteryOptimizationDialog && !BATTERY_POPUP_HIDDEN.getBoolean()) {
        AlertDialog(
            onDismissRequest = {
                showBatteryOptimizationDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.battery_configuration),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.battery_configuration_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val batteryDescResId = remember {
                        BatteryUtil.getBatterySettingsDescResId(BatteryUtil.getManufacturer())
                    }
                    Text(
                        text = stringResource(batteryDescResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBatteryOptimizationDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            runCatching {
                                val intent = BatteryUtil.buildBatterySettingsIntent(context)
                                batteryOptimizationLauncher.launch(intent)
                            }.onFailure { oemErr ->
                                Log.w("NewHomePage", "OEM battery intent failed, using fallback: ${oemErr.message}")
                                runCatching {
                                    batteryOptimizationLauncher.launch(
                                        BatteryUtil.buildStandardBatteryIntent(context)
                                    )
                                }.onFailure { fallbackErr ->
                                    Log.e("NewHomePage", "Fallback battery intent also failed: ${fallbackErr.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.open_settings),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBatteryOptimizationDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.exit_app_title)) },
            text = { Text(stringResource(R.string.exit_app_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text(stringResource(R.string.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }

    Scaffold(
        modifier =
            if (LocalDarkTheme.current.isDarkTheme()) Modifier.background(DestinyHome.backgroundBrush)
            else Modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuOpen) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Abrir menu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSupport) {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = "Apoiar o desenvolvedor",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = onNavigateToDownloads) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.downloads_history),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho da marca
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Destiny",
                        style = MaterialTheme.typography.displayMedium.merge(
                            TextStyle(brush = DestinyHome.titleBrush)
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Baixe vídeos e áudios com rapidez",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Atalhos Rápidos (Quick Tools)
            item {
                QuickToolsRow(
                    onThumbnailDownload = onNavigateToThumbnailDownload,
                    onVideoInfoDownload = onNavigateToVideoInfoDownload,
                    onCommentDownload = onNavigateToCommentDownload,
                    onBatchUrlImport = onNavigateToBatchUrlImport,
                )
            }

            // Banner de Detecção Automática de Link
            if (clipboardUrl != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DestinyHome.CardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.Link, contentDescription = null, tint = DestinyHome.BlueLight)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Link copiado detectado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    clipboardUrl!!,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = {
                                    urlText = clipboardUrl!!
                                    clipboardUrl = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DestinyHome.Blue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Colar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // URL Input Field with Download Button
            item {
                URLInputField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    isFetchingInfo = isFetchingInfo,
                    onDownloadClick = {
                        if (urlText.isNotBlank()) {
                            view.slightHapticFeedback()
                            scope.launch {
                                isFetchingInfo = true
                                try {
                                    dialogViewModel.startDownload(listOf(urlText))
                                    delay(800) // Dá tempo para o usuário ver o loading antes de abrir o modal
                                } finally {
                                    isFetchingInfo = false
                                    urlText = ""
                                    clipboardUrl = null
                                    keyboardController?.hide()
                                }
                            }
                        } else {
                            context.makeToast(R.string.url_empty)
                        }
                    },
                    onPasteClick = {
                        val clipText = clipboardManager.getText()?.text
                        if (clipText != null) {
                            context.matchUrlFromClipboard(clipText)?.let { url ->
                                urlText = url
                                clipboardUrl = null
                                context.makeToast(R.string.paste_msg)
                            } ?: context.makeToast(R.string.paste_fail_msg)
                        }
                    }
                )
            }

            // Recent Downloads Section
            if (activeDownloads.isNotEmpty() || recentFiveDownloadsFiltered.isNotEmpty()) {
                item {
                    val hasRetryableTasks by remember {
                        derivedStateOf {
                            taskStateMap.values.any {
                                it.downloadState is Task.DownloadState.Canceled ||
                                        it.downloadState is Task.DownloadState.Error
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.recent_downloads),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (hasRetryableTasks) {
                            IconButton(
                                onClick = {
                                    view.slightHapticFeedback()
                                    taskStateMap.entries
                                        .filter { (_, state) ->
                                            state.downloadState is Task.DownloadState.Canceled ||
                                                    state.downloadState is Task.DownloadState.Error
                                        }
                                        .map { it.key }
                                        .forEach { downloader.restart(it) }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Replay,
                                    contentDescription = stringResource(R.string.retry_all),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            // Show active downloads first
            if (activeDownloads.isNotEmpty()) {
                items(
                    items = activeDownloads,
                    key = { (task, _) -> task.id }
                ) { (task, state) ->
                    var showDetailsDialog by remember { mutableStateOf(false) }
                    var detailsInfo by remember { mutableStateOf<Pair<Task, Task.State>?>(null) }
                    var showActiveDeleteDialog by remember { mutableStateOf(false) }

                    ActiveDownloadCard(
                        task = task,
                        state = state,
                        onAction = { action ->
                            view.slightHapticFeedback()
                            when (action) {
                                UiAction.Pause -> downloader.pause(task)
                                UiAction.Cancel -> downloader.cancel(task)
                                UiAction.Delete -> showActiveDeleteDialog = true
                                UiAction.Resume -> downloader.resume(task)
                                UiAction.Retry -> downloader.restart(task)
                                is UiAction.CopyErrorReport -> {
                                    clipboardManager.setText(
                                        AnnotatedString(getErrorReport(action.throwable, task.url))
                                    )
                                    context.makeToast(R.string.error_copied)
                                }
                                is UiAction.CopyVideoURL -> {
                                    clipboardManager.setText(AnnotatedString(task.url))
                                    context.makeToast(R.string.link_copied)
                                }
                                UiAction.ShowDetails -> {
                                    detailsInfo = task to state
                                    showDetailsDialog = true
                                }
                                is UiAction.OpenFile -> {
                                    action.filePath?.let {
                                        FileUtil.openFile(path = it) {
                                            context.makeToast(R.string.file_unavailable)
                                        }
                                    }
                                }
                                is UiAction.OpenThumbnailURL -> {
                                    uriHandler.openUri(action.url)
                                }
                                is UiAction.OpenVideoURL -> {
                                    uriHandler.openUri(action.url)
                                }
                                is UiAction.ShareFile -> {
                                    val shareTitle = context.getString(R.string.share)
                                    FileUtil.createIntentForSharingFile(action.filePath)?.let {
                                        context.startActivity(Intent.createChooser(it, shareTitle))
                                    }
                                }
                            }
                        }
                    )

                    val detailsSnapshot = detailsInfo
                    if (showDetailsDialog && detailsSnapshot != null) {
                        DownloadDetailsDialog(
                            task = detailsSnapshot.first,
                            state = detailsSnapshot.second,
                            onDismiss = { showDetailsDialog = false }
                        )
                    }

                    if (showActiveDeleteDialog) {
                        val deleteTitle = state.viewState.title.ifBlank { task.url }
                        SealDialog(
                            onDismissRequest = { showActiveDeleteDialog = false },
                            title = { Text(text = stringResource(R.string.delete_info)) },
                            icon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    text = stringResource(R.string.delete_info_msg).format(deleteTitle),
                                )
                            },
                            confirmButton = {
                                ConfirmButton {
                                    showActiveDeleteDialog = false
                                    scope.launch(Dispatchers.IO) {
                                        val videoId = state.videoInfo?.id
                                        val baseName =
                                            state.videoInfo?.title?.ifBlank { deleteTitle }
                                                ?: deleteTitle
                                        FileUtil.deleteTempFilesForTask(baseName, videoId)
                                        downloader.remove(task)
                                    }
                                }
                            },
                            dismissButton = { DismissButton { showActiveDeleteDialog = false } },
                        )
                    }
                }
            }

            // Show recent completed downloads
            if (recentFiveDownloadsFiltered.isNotEmpty()) {
                items(
                    items = recentFiveDownloadsFiltered,
                    key = { it.id }
                ) { downloadInfo ->
                    var showRecentDeleteDialog by remember { mutableStateOf(false) }

                    val currentDownloadInfo by rememberUpdatedState(downloadInfo)
                    val currentList by rememberUpdatedState(recentFiveDownloadsFiltered)
                    val currentLocalHiddenIds by rememberUpdatedState(localHiddenIds)

                    val stableOnClick: () -> Unit = remember {
                        {
                            FileUtil.openFile(currentDownloadInfo.videoPath) {
                                context.makeToast(R.string.file_unavailable)
                            }
                            Unit
                        }
                    }
                    val stableOnShare: () -> Unit = remember {
                        {
                            view.slightHapticFeedback()
                            val shareTitle = context.getString(R.string.share)
                            FileUtil.createIntentForSharingFile(currentDownloadInfo.videoPath)?.let {
                                context.startActivity(Intent.createChooser(it, shareTitle))
                            }
                            Unit
                        }
                    }
                    val stableOnCopyLink: () -> Unit = remember {
                        {
                            view.slightHapticFeedback()
                            clipboardManager.setText(AnnotatedString(currentDownloadInfo.videoUrl))
                            context.makeToast(R.string.link_copied)
                        }
                    }
                    val stableOnShowDetails: () -> Unit = remember {
                        {
                            view.slightHapticFeedback()
                            val idx = currentList.indexOfFirst { it.id == currentDownloadInfo.id }
                            detailsDialogIndex = if (idx >= 0) idx else 0
                        }
                    }
                    val stableOnDelete: () -> Unit = remember {
                        {
                            view.slightHapticFeedback()
                            showRecentDeleteDialog = true
                        }
                    }
                    val stableOnHide: () -> Unit = remember {
                        {
                            view.slightHapticFeedback()
                            localHiddenIds = currentLocalHiddenIds + currentDownloadInfo.id
                            scope.launch(Dispatchers.IO) {
                                DatabaseUtil.hideItem(currentDownloadInfo)
                            }
                            Unit
                        }
                    }

                    RecentDownloadCard(
                        downloadInfo = downloadInfo,
                        refreshKey = lifecycleRefreshTrigger,
                        onClick = stableOnClick,
                        onShare = stableOnShare,
                        onCopyLink = stableOnCopyLink,
                        onShowDetails = stableOnShowDetails,
                        onDelete = stableOnDelete,
                        onHide = stableOnHide,
                    )

                    if (showRecentDeleteDialog) {
                        SealDialog(
                            onDismissRequest = { showRecentDeleteDialog = false },
                            title = { Text(text = stringResource(R.string.delete_info)) },
                            icon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            },
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    text = stringResource(R.string.delete_info_msg).format(downloadInfo.videoTitle),
                                )
                            },
                            confirmButton = {
                                ConfirmButton {
                                    showRecentDeleteDialog = false
                                    val info = currentDownloadInfo
                                    localHiddenIds = currentLocalHiddenIds + info.id
                                    scope.launch(Dispatchers.IO) {
                                        val baseName =
                                            File(info.videoPath)
                                                .nameWithoutExtension
                                                .ifEmpty { info.videoTitle }
                                        FileUtil.deleteTempFilesForTask(baseName, info.videoId)
                                        DatabaseUtil.deleteInfoList(
                                            infoList = listOf(info),
                                            deleteFile = false
                                        )
                                    }
                                }
                            },
                            dismissButton = { DismissButton { showRecentDeleteDialog = false } },
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    detailsDialogIndex?.let { index ->
        if (recentFiveDownloadsFiltered.isEmpty()) {
            detailsDialogIndex = null
        } else {
            val safeIndex = index.coerceIn(0, recentFiveDownloadsFiltered.lastIndex)
            RecentDownloadDetailsDialog(
                downloadInfoList = recentFiveDownloadsFiltered,
                initialIndex = safeIndex,
                onDismiss = { detailsDialogIndex = null }
            )
        }
    }

    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val dialogState by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide(); showDialog = false }
        }
    }

    // FIX: Fecha automaticamente a tela de seleção de formato se um download já estiver ativo.
    // Isso evita que o app fique "travado" com uma camada invisível por cima.
    LaunchedEffect(activeDownloads, selectionState) {
        if (activeDownloads.isNotEmpty() && selectionState !is DownloadDialogViewModel.SelectionState.Idle) {
            dialogViewModel.postAction(Action.Reset)
        }
    }

    val dialogConfig = remember { Config() }
    if (showDialog) {
        DownloadDialog(
            state = dialogState,
            sheetState = sheetState,
            config = dialogConfig,
            preferences = preferences,
            onPreferencesUpdate = { preferences = it },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }

    when (selectionState) {
        is DownloadDialogViewModel.SelectionState.FormatSelection ->
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )

        is DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        }

        DownloadDialogViewModel.SelectionState.Idle -> {}
    }

    // Chama a verificação de atualização do GitHub
    CheckForUpdatesDialog()
}

@Composable
fun URLInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isFetchingInfo: Boolean,
    onDownloadClick: () -> Unit,
    onPasteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(64.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.enter_url_to_download),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(32.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (!isFetchingInfo) onDownloadClick() }),
        trailingIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value.isEmpty() && !isFetchingInfo) {
                    IconButton(onClick = onPasteClick) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = stringResource(R.string.paste),
                            tint = DestinyHome.BlueLight
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DestinyHome.buttonBrush)
                        .clickable(enabled = !isFetchingInfo, onClick = onDownloadClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFetchingInfo) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = stringResource(R.string.download),
                            tint = Color.White
                        )
                    }
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DestinyHome.Blue,
            unfocusedBorderColor = DestinyHome.Blue.copy(alpha = 0.35f),
            focusedContainerColor = DestinyHome.CardBg,
            unfocusedContainerColor = DestinyHome.CardBg,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = DestinyHome.BlueLight,
        )
    )
}

@Composable
fun RecentDownloadCard(
    downloadInfo: DownloadedVideoInfo,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit = {},
    refreshKey: Int = 0,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
    val isGradientDark = true
    var showMenu by remember { mutableStateOf(false) }
    val fileExists by produceState(initialValue = java.io.File(downloadInfo.videoPath).exists(), key1 = downloadInfo.videoPath, key2 = refreshKey) {
        value = java.io.File(downloadInfo.videoPath).exists()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (fileExists) 1f else 0.55f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGradientDark && isDarkTheme) {
                DestinyHome.CardBg
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = downloadInfo.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = downloadInfo.videoTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isGradientDark && isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fileExists) {
                        Text(
                            text = stringResource(R.string.completed),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isGradientDark && isDarkTheme) {
                                Color(0xFF4ADE80)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.BrokenImage,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.file_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Mais opções",
                        tint = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.details)) },
                        onClick = {
                            onShowDetails()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.share)) },
                        onClick = {
                            onShare()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copy_link)) },
                        onClick = {
                            onCopyLink()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.hide)) },
                        onClick = {
                            onHide()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    task: Task,
    state: Task.State,
    onAction: (UiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
    val isGradientDark = true
    var showMenu by remember { mutableStateOf(false) }

    val downloadState = state.downloadState
    val progress = when (downloadState) {
        is Task.DownloadState.Running -> downloadState.progress
        is Task.DownloadState.Paused -> downloadState.progress ?: -1f
        is Task.DownloadState.Canceled -> downloadState.progress ?: -1f
        else -> 0f
    }

    val progressText = if (downloadState is Task.DownloadState.Running) downloadState.progressText else ""
    val context = androidx.compose.ui.platform.LocalContext.current

    var hasSeenFormatInfo by remember { mutableStateOf(false) }
    var hasSeenVideoComplete by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("downloading") }

    val downloadPhase = when {
        progressText.contains("[Merger]", ignoreCase = true) ||
                progressText.contains("Merging formats", ignoreCase = true) -> {
            currentPhase = "merging"
            hasSeenVideoComplete = false
            hasSeenFormatInfo = false
            "merging"
        }
        progressText.contains("[info]", ignoreCase = true) && progressText.contains("format", ignoreCase = true) -> {
            hasSeenFormatInfo = true
            hasSeenVideoComplete = false
            currentPhase = "downloading"
            "downloading"
        }
        progressText.matches(Regex("""^\d+(\.\d+)?%.*""")) || progressText.contains("% of ") -> {
            when {
                (progressText.startsWith("100%") || progressText.contains("100% of ")) && !hasSeenVideoComplete -> {
                    hasSeenVideoComplete = true
                    currentPhase = "video"
                    "video"
                }
                hasSeenVideoComplete -> {
                    currentPhase = "audio"
                    "audio"
                }
                hasSeenFormatInfo -> {
                    currentPhase = "video"
                    "video"
                }
                else -> {
                    currentPhase = "downloading"
                    "downloading"
                }
            }
        }
        progressText.contains("Deleting original file", ignoreCase = true) ||
                progressText.contains("[Metadata]", ignoreCase = true) ||
                progressText.contains("[MoveFiles]", ignoreCase = true) -> {
            currentPhase
        }
        progressText.contains("[youtube]", ignoreCase = true) ||
                progressText.contains("Downloading webpage", ignoreCase = true) ||
                progressText.contains("Downloading player", ignoreCase = true) -> {
            if (hasSeenVideoComplete) {
                currentPhase = "audio"
                "audio"
            } else {
                currentPhase
            }
        }
        else -> currentPhase
    }

    val statusText = when (downloadState) {
        is Task.DownloadState.Running -> {
            val pct = if (progress >= 0) " ${(progress * 100).toInt()}%" else ""
            when (downloadPhase) {
                "merging" -> stringResource(R.string.status_merging)
                "video"   -> "Baixando vídeo...$pct"
                "audio"   -> "Baixando áudio...$pct"
                "fetching" -> stringResource(R.string.fetching_info)
                else -> if (progress >= 0) "Baixando... ${(progress * 100).toInt()}%"
                else stringResource(R.string.status_downloading)
            }
        }
        is Task.DownloadState.Paused -> if (progress >= 0) stringResource(R.string.status_paused) + " ${(progress * 100).toInt()}%" else stringResource(R.string.status_paused)
        is Task.DownloadState.Canceled -> stringResource(R.string.status_canceled)
        is Task.DownloadState.Error -> stringResource(R.string.download_error)
        is Task.DownloadState.Completed -> stringResource(R.string.completed) + " 100%"
        is Task.DownloadState.FetchingInfo -> stringResource(R.string.fetching_info)
        Task.DownloadState.Idle,
        Task.DownloadState.ReadyWithInfo -> stringResource(R.string.queue_status)
        else -> ""
    }

    val statusColor = when (downloadState) {
        is Task.DownloadState.Running -> if (isGradientDark && isDarkTheme) {
            DestinyHome.Blue
        } else {
            MaterialTheme.colorScheme.primary
        }
        is Task.DownloadState.Paused -> Color(0xFFFBBF24)
        is Task.DownloadState.Canceled -> Color(0xFFEF4444)
        is Task.DownloadState.Error -> Color(0xFFEF4444)
        is Task.DownloadState.Completed -> Color(0xFF4ADE80)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val speedEtaText = if (downloadState is Task.DownloadState.Running && progressText.isNotEmpty()) {
        val speed = Regex("""at\s+([\d.]+\s*\S+/s)""").find(progressText)?.groupValues?.get(1)
        val eta = Regex("""ETA\s+(\d+:\d+)""").find(progressText)?.groupValues?.get(1)
        when {
            speed != null && eta != null -> "$speed  •  ETA $eta"
            speed != null -> speed
            eta != null -> "ETA $eta"
            else -> null
        }
    } else null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGradientDark && isDarkTheme) {
                DestinyHome.CardBg
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.videoInfo?.thumbnail?.let { thumbnailUrl ->
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VideoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = state.viewState.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        // FIX: Cor do texto garantida para ficar visível no fundo escuro
                        color = if (isGradientDark && isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )

                        if (downloadState is Task.DownloadState.Idle || downloadState is Task.DownloadState.ReadyWithInfo) {
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isGradientDark && isDarkTheme) {
                                    DestinyHome.BlueDeep.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.queue_status),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isGradientDark && isDarkTheme) {
                                        DestinyHome.BlueLight
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (speedEtaText != null) {
                        Text(
                            text = speedEtaText,
                            style = MaterialTheme.typography.labelSmall,
                            // FIX: Cor do texto garantida para ficar visível no fundo escuro
                            color = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (downloadState is Task.DownloadState.Running) {
                    IconButton(
                        onClick = { onAction(UiAction.Pause) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pause,
                            contentDescription = stringResource(R.string.pause),
                            tint = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (downloadState is Task.DownloadState.Paused) {
                    IconButton(
                        onClick = { onAction(UiAction.Resume) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(R.string.resume),
                            tint = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Mais opções",
                            tint = if (isGradientDark && isDarkTheme) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        val downloadState = state.downloadState

                        if (downloadState is Task.DownloadState.Running) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.pause)) },
                                onClick = {
                                    onAction(UiAction.Pause)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Pause,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        if (downloadState is Task.DownloadState.Paused) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.resume)) },
                                onClick = {
                                    onAction(UiAction.Resume)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        if (downloadState is Task.DownloadState.Canceled || downloadState is Task.DownloadState.Error) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.retry)) },
                                onClick = {
                                    onAction(UiAction.Retry)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        if (downloadState is Task.DownloadState.Running ||
                            downloadState is Task.DownloadState.FetchingInfo ||
                            downloadState is Task.DownloadState.Paused ||
                            downloadState == Task.DownloadState.Idle ||
                            downloadState == Task.DownloadState.ReadyWithInfo) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.cancel)) },
                                onClick = {
                                    onAction(UiAction.Cancel)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.copy_link)) },
                            onClick = {
                                onAction(UiAction.CopyVideoURL)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        )

                        if (downloadState is Task.DownloadState.Completed) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.details)) },
                                onClick = {
                                    onAction(UiAction.ShowDetails)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        }

                        if (downloadState is Task.DownloadState.Completed || downloadState is Task.DownloadState.Error || downloadState is Task.DownloadState.Canceled) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    onAction(UiAction.Delete)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Barra de Progresso Customizada
            if (downloadState is Task.DownloadState.Running || downloadState is Task.DownloadState.Paused) {
                val barColor = when (downloadState) {
                    is Task.DownloadState.Paused -> Color(0xFFFBBF24)
                    else -> if (isGradientDark && isDarkTheme) DestinyHome.Blue
                    else MaterialTheme.colorScheme.primary
                }
                DestinyProgressBar(
                    progress = if (progress >= 0) progress else null,
                    color = barColor
                )
            }
        }
    }
}

@Composable
fun DestinyProgressBar(
    progress: Float?,
    modifier: Modifier = Modifier,
    color: Color = DestinyHome.Blue
) {
    val animatedProgress = if (progress != null) {
        animateFloatAsState(targetValue = progress, animationSpec = tween(500), label = "progress").value
    } else null

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val width = maxWidth
        if (animatedProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(Brush.horizontalGradient(listOf(color, DestinyHome.BlueLight)))
            )
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "indeterminate")
            val offset by infiniteTransition.animateFloat(
                initialValue = -0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "offset"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .offset(x = width * offset)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent)))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDetailsDialog(
    task: Task,
    state: Task.State,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showFilePathDialog by remember { mutableStateOf(false) }

    BackHandler {
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.download_details),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.viewState.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            state.videoInfo?.thumbnail?.let { thumbnailUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = stringResource(R.string.media_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.viewState.videoFormats?.firstOrNull()?.ext?.let { ext ->
                        if (ext.isNotBlank()) {
                            DetailCard(
                                icon = Icons.Outlined.VideoFile,
                                label = stringResource(R.string.file_format),
                                value = ext.uppercase(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val fileSize = state.viewState.fileSizeApprox
                    if (fileSize > 0) {
                        DetailCard(
                            icon = Icons.Outlined.Storage,
                            label = stringResource(R.string.file_size),
                            value = fileSize.toFileSizeText(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.viewState.uploader.isNotBlank()) {
                        DetailCard(
                            icon = Icons.Outlined.Person,
                            label = stringResource(R.string.video_creator_label),
                            value = state.viewState.uploader,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.viewState.extractorKey.isNotBlank()) {
                        DetailCard(
                            icon = Icons.Outlined.Language,
                            label = stringResource(R.string.platform),
                            value = state.viewState.extractorKey,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.downloadState is Task.DownloadState.Completed) {
                        state.downloadState.filePath?.let { path ->
                            DetailCard(
                                icon = Icons.Outlined.Folder,
                                label = stringResource(R.string.file_path),
                                value = path,
                                modifier = Modifier.weight(1f),
                                onClick = { showFilePathDialog = true }
                            )
                        }
                    }

                    DetailCard(
                        icon = Icons.Outlined.CalendarToday,
                        label = stringResource(R.string.download_date),
                        value = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(task.timeCreated)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString(state.viewState.url))
                            context.makeToast(R.string.link_copied)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.source_url),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = state.viewState.url,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showFilePathDialog && state.downloadState is Task.DownloadState.Completed) {
        state.downloadState.filePath?.let { path ->
            AlertDialog(
                onDismissRequest = { showFilePathDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.file_path),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    SelectionContainer {
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilePathDialog = false }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecentDownloadDetailsDialog(
    downloadInfoList: List<DownloadedVideoInfo>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val safeInitialIndex = initialIndex.coerceIn(0, (downloadInfoList.size - 1).coerceAtLeast(0))
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = safeInitialIndex
    ) { downloadInfoList.size }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current

    val swallowNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = available

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                available
        }
    }

    BackHandler {
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(swallowNestedScroll),
        ) {
            if (downloadInfoList.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(downloadInfoList.size) { dotIndex ->
                        val isSelected = dotIndex == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            val pagerHeight = (configuration.screenHeightDp * 0.85f).dp
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight),
                userScrollEnabled = downloadInfoList.size > 1,
            ) { page ->
                RecentDownloadDetailsContent(
                    downloadInfo = downloadInfoList[page],
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun RecentDownloadDetailsContent(
    downloadInfo: DownloadedVideoInfo,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showFilePathDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.download_details),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = downloadInfo.videoTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AsyncImage(
                model = downloadInfo.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = stringResource(R.string.media_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )

        val file = java.io.File(downloadInfo.videoPath)
        val fileExtension = downloadInfo.videoPath.substringAfterLast(".", "")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (fileExtension.isNotEmpty()) {
                    DetailCard(
                        icon = Icons.Outlined.VideoFile,
                        label = stringResource(R.string.file_format),
                        value = fileExtension.uppercase(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (file.exists()) {
                    val fileSize = file.length()
                    DetailCard(
                        icon = Icons.Outlined.Storage,
                        label = stringResource(R.string.file_size),
                        value = fileSize.toFileSizeText(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val resolution by produceState(initialValue = "N/A", key1 = downloadInfo.videoPath) {
                    value = withContext(Dispatchers.IO) {
                        val retriever = android.media.MediaMetadataRetriever()
                        try {
                            if (file.exists()) {
                                retriever.setDataSource(downloadInfo.videoPath)
                                val width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                                val height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                                if (width != null && height != null) "${width}x${height}" else "N/A"
                            } else "N/A"
                        } catch (e: Exception) {
                            "N/A"
                        } finally {
                            retriever.release()
                        }
                    }
                }

                if (resolution != "N/A") {
                    DetailCard(
                        icon = Icons.Outlined.HighQuality,
                        label = stringResource(R.string.resolution),
                        value = resolution,
                        modifier = Modifier.weight(1f)
                    )
                }

                DetailCard(
                    icon = Icons.Outlined.Language,
                    label = stringResource(R.string.platform),
                    value = downloadInfo.extractor,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    icon = Icons.Outlined.Folder,
                    label = stringResource(R.string.file_path),
                    value = downloadInfo.videoPath,
                    modifier = Modifier.weight(1f),
                    onClick = { showFilePathDialog = true }
                )

                val downloadDate = if (file.exists()) {
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(file.lastModified()))
                } else {
                    "N/A"
                }
                DetailCard(
                    icon = Icons.Outlined.CalendarToday,
                    label = stringResource(R.string.download_date),
                    value = downloadDate,
                    modifier = Modifier.weight(1f)
                )
            }

            if (downloadInfo.downloadTimeMillis > 0L || downloadInfo.averageSpeedBytesPerSec > 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (downloadInfo.downloadTimeMillis > 0L) {
                        DetailCard(
                            icon = Icons.Outlined.Timer,
                            label = stringResource(R.string.download_time),
                            value = formatDownloadTime(downloadInfo.downloadTimeMillis),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (downloadInfo.averageSpeedBytesPerSec > 0L) {
                        DetailCard(
                            icon = Icons.Outlined.Speed,
                            label = stringResource(R.string.average_speed),
                            value = formatAverageSpeed(downloadInfo.averageSpeedBytesPerSec),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(downloadInfo.videoUrl))
                        context.makeToast(R.string.link_copied)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.source_url),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            text = downloadInfo.videoUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showFilePathDialog) {
        AlertDialog(
            onDismissRequest = { showFilePathDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.file_path),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                SelectionContainer {
                    Text(
                        text = downloadInfo.videoPath,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilePathDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

private fun formatDownloadTime(millis: Long): String {
    val totalSeconds = millis / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private fun formatAverageSpeed(bytesPerSec: Long): String {
    val mb = 1024L * 1024L
    val kb = 1024L
    return when {
        bytesPerSec >= mb -> "%.1f MB/s".format(bytesPerSec.toDouble() / mb)
        bytesPerSec >= kb -> "${bytesPerSec / kb} KB/s"
        else -> "$bytesPerSec B/s"
    }
}

@Composable
private fun DetailCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else Modifier
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun downloadStateSortPriority(state: Task.DownloadState): Int = when (state) {
    is Task.DownloadState.Running       -> 0
    is Task.DownloadState.FetchingInfo  -> 1
    Task.DownloadState.ReadyWithInfo    -> 2
    Task.DownloadState.Idle             -> 3
    is Task.DownloadState.Paused        -> 4
    is Task.DownloadState.Canceled      -> 5
    is Task.DownloadState.Error         -> 6
    is Task.DownloadState.Completed     -> 7
}

@Composable
fun AnimatedGlowingPlus() {
    val infiniteTransition = rememberInfiniteTransition(label = "plusGlow")

    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientShift"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
    )

    val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.6f)

    val gradientBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(gradientShift, 0f),
        end = Offset(gradientShift + 500f, 500f),
        tileMode = TileMode.Mirror
    )

    Text(
        text = "+",
        style = MaterialTheme.typography.displayMedium.merge(
            TextStyle(
                brush = gradientBrush,
                shadow = Shadow(
                    color = glowColor,
                    offset = Offset.Zero,
                    blurRadius = 16f * glowAlpha
                )
            )
        ),
        fontWeight = FontWeight.Bold
    )
}

private data class QuickTool(
    val icon: ImageVector,
    val labelRes: Int,
    val tint: @Composable () -> Color,
    val onClick: () -> Unit,
)

@Composable
private fun QuickToolsRow(
    onThumbnailDownload: () -> Unit,
    onVideoInfoDownload: () -> Unit,
    onCommentDownload: () -> Unit,
    onBatchUrlImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tools = remember(
        onBatchUrlImport,
        onThumbnailDownload,
        onVideoInfoDownload,
        onCommentDownload,
    ) {
        listOf(
            QuickTool(
                icon = Icons.Outlined.PlaylistAdd,
                labelRes = R.string.batch_url_import,
                tint = { ThemedIconColors.primary },
                onClick = onBatchUrlImport,
            ),
            QuickTool(
                icon = Icons.Outlined.Image,
                labelRes = R.string.thumbnail_download,
                tint = { ThemedIconColors.secondary },
                onClick = onThumbnailDownload,
            ),
            QuickTool(
                icon = Icons.Outlined.Description,
                labelRes = R.string.video_info_download,
                tint = { ThemedIconColors.tertiary },
                onClick = onVideoInfoDownload,
            ),
            QuickTool(
                icon = Icons.Outlined.Chat,
                labelRes = R.string.comment_download,
                tint = { ThemedIconColors.primary },
                onClick = onCommentDownload,
            ),
        )
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tools.forEachIndexed { index, tool ->
            QuickToolIcon(
                tool = tool,
                visible = visible,
                animationDelayMs = index * 60,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickToolIcon(
    tool: QuickTool,
    visible: Boolean,
    animationDelayMs: Int,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 380,
            delayMillis = animationDelayMs,
            easing = FastOutSlowInEasing,
        ),
        label = "quickTool_alpha",
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        animationSpec = tween(
            durationMillis = 380,
            delayMillis = animationDelayMs,
            easing = FastOutSlowInEasing,
        ),
        label = "quickTool_offset",
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "quickTool_scale",
    )

    val tint = tool.tint()

    val fullLabel = stringResource(tool.labelRes)
    val shortLabel = fullLabel.split(" ").take(2).joinToString(" ")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer(alpha = alpha)
            .offset(y = offsetY)
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(alpha = 0.12f))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.slightHapticFeedback()
                        tool.onClick()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = stringResource(tool.labelRes),
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 6.dp)
                .width(64.dp),
            textAlign = TextAlign.Center
        )
    }
}