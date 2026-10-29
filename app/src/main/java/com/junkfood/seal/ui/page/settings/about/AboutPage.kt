package com.junkfood.seal.ui.page.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.UpdateDisabled
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.App.Companion.packageInfo
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton

const val weblate = "https://hosted.weblate.org/engage/seal/"
const val YtdlpRepository = "https://github.com/yt-dlp/yt-dlp"
private const val whatsappChannelUrl =
    "https://whatsapp.com/channel/0029Vb9Q40EKmCPPNj4peT2w"
private const val portfolioUrl = "https://portfolio.rafaelcamposcontato7.workers.dev/"
private const val TAG = "AboutPage"

// Edite esta lista com as novidades de cada versão do seu app.
private val whatsNewItems =
    listOf(
        "Interface totalmente em português",
        "App mais leve e mais rápido",
        "Novo nome e novo ícone: Destiny",
        "Nova tela de apoio ao desenvolvedor",
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    onNavigateToCreditsPage: () -> Unit,
    onNavigateToUpdatePage: () -> Unit,
    onNavigateToDonatePage: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    var showWhatsNew by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }

    if (showWhatsNew) {
        WhatsNewDialog(
            versionName = packageInfo.versionName ?: "",
            onDismissRequest = { showWhatsNew = false },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.about)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(it),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(
                        onClick = onNavigateToOnboarding,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ver introdução",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Veja novamente as telas de apresentação do app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.8f
                                        ),
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CommunityCard(
                                title = stringResource(R.string.release),
                                description = "Veja as novidades desta versão",
                                icon = Icons.Outlined.NewReleases,
                                onClick = { showWhatsNew = true },
                                modifier = Modifier.weight(1f),
                            )
                            CommunityCard(
                                title = stringResource(R.string.sponsor),
                                description = stringResource(R.string.sponsor_desc),
                                icon = Icons.Outlined.VolunteerActivism,
                                onClick = onNavigateToDonatePage,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CommunityCard(
                                title = "Canal no WhatsApp",
                                description = "Novidades e avisos do app",
                                icon = Icons.AutoMirrored.Outlined.Chat,
                                onClick = { openUrl(whatsappChannelUrl) },
                                modifier = Modifier.weight(1f),
                            )
                            CommunityCard(
                                title = stringResource(R.string.website),
                                description = "Conheça meu portfólio",
                                icon = Icons.Outlined.Public,
                                onClick = { openUrl(portfolioUrl) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CommunityCard(
                                title = stringResource(R.string.credits),
                                description = stringResource(R.string.credits_desc),
                                icon = Icons.Outlined.AutoAwesome,
                                onClick = onNavigateToCreditsPage,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        text =
                            "Versão ${packageInfo.versionName ?: ""}  \u2022  ${context.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}

@Composable
private fun WhatsNewDialog(versionName: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton(stringResource(id = R.string.got_it)) { onDismissRequest() }
        },
        icon = {
            Icon(
                Icons.Outlined.NewReleases,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(text = "Novidades da versão $versionName", textAlign = TextAlign.Center)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                whatsNewItems.forEach { item ->
                    Text(
                        text = "\u2022  $item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@Composable
private fun CommunityCard(
    title: String,
    description: String,
    icon: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
@Preview
fun AutoUpdateUnavailableDialog(onDismissRequest: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current
    val hyperLinkText = stringResource(id = R.string.switch_to_github_builds)
    val text = stringResource(id = R.string.auto_update_disabled_msg, "F-Droid", hyperLinkText)

    val annotatedString = buildAnnotatedString {
        append(text)
        val startIndex = text.indexOf(hyperLinkText)
        val endIndex = startIndex + hyperLinkText.length
        addUrlAnnotation(
            UrlAnnotation("https://github.com/CamposEB/app-downloader/releases/latest"),
            start = startIndex,
            end = endIndex,
        )
        addStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                textDecoration = TextDecoration.Underline,
            ),
            start = startIndex,
            end = endIndex,
        )
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton(stringResource(id = R.string.got_it)) { onDismissRequest() }
        },
        icon = {
            Icon(Icons.Outlined.UpdateDisabled, null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text(
                text = stringResource(id = R.string.feature_unavailable),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            ClickableText(
                text = annotatedString,
                onClick = { index ->
                    annotatedString.getUrlAnnotations(index, index).firstOrNull()?.let {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        uriHandler.openUri(it.item.url)
                    }
                },
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            )
        },
    )
}