package com.junkfood.seal.ui.page.settings.about

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalGradientDarkMode
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SponsorItem
import com.junkfood.seal.ui.component.gitHubAvatar
import com.junkfood.seal.ui.component.gitHubProfile
import com.junkfood.seal.ui.theme.GradientBrushes
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.makeToast
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_SPONSOR_MSG
import com.junkfood.seal.util.Sponsor
import com.junkfood.seal.util.SocialAccount
import com.junkfood.seal.util.SocialAccounts
import com.junkfood.seal.util.SponsorEntity
import com.junkfood.seal.util.SponsorShip
import com.junkfood.seal.util.SponsorUtil
import com.junkfood.seal.util.Tier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SponsorPage"

private const val SPONSORS = "Patrocinadores ☕️"
private const val BACKERS = "Apoiadores ❤️"
private const val SUPPORTERS = "Colaboradores 💖"

// Links do desenvolvedor (Destiny)
private const val DEVELOPER_GITHUB_USER = "CamposEB"
private const val DEVELOPER_SPONSOR_URL = "https://github.com/sponsors/CamposEB"
private const val PROJECT_REPOSITORY_URL = "https://github.com/CamposEB/app-downloader"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubSponsorsPage(onNavigateBack: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val uriHandler = LocalUriHandler.current
    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
    val isGradientDark = LocalGradientDarkMode.current
    val sponsorList = remember { mutableStateListOf<SponsorShip>() }
    val backerList = remember { mutableStateListOf<SponsorShip>() }
    val supporterList = remember { mutableStateListOf<SponsorShip>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var viewingSponsorShip by remember {
        mutableStateOf(SponsorShip(sponsorEntity = SponsorEntity("login")))
    }
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onSponsorClick: (SponsorShip) -> Unit = {
        viewingSponsorShip = it
        showSheet = true
        scope.launch {
            delay(80)
            sheetState.show()
        }
    }

    // A busca de patrocinadores do projeto original foi removida:
    // as listas ficam vazias e as seções de apoiadores não aparecem.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            SHOW_SPONSOR_MSG.updateInt(0)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(modifier = Modifier, text = stringResource(id = R.string.sponsors))
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { values ->
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 12.dp),
                columns = GridCells.Fixed(12),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = values,
            ) {
                // Cabeçalho
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HeroHeader(isDarkTheme = isDarkTheme, isGradientDark = isGradientDark)
                }

                // Sobre o Destiny
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AboutSealPlusSection()
                }

                // Recursos
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturesSection()
                }

                // Por que apoiar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    WhySponsorSection(isDarkTheme = isDarkTheme, isGradientDark = isGradientDark)
                }
                if (supporterList.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = SUPPORTERS) {
                        PreferenceSubtitle(
                            text = SUPPORTERS,
                            contentPadding =
                                PaddingValues(start = 12.dp, top = 24.dp, bottom = 12.dp),
                        )
                    }

                    items(
                        span = { GridItemSpan(maxLineSpan / 3) },
                        items = supporterList,
                        key = { it.sponsorEntity.login },
                    ) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) { onSponsorClick(sponsorShip) }
                    }
                }

                if (backerList.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = BACKERS) {
                        PreferenceSubtitle(
                            text = BACKERS,
                            contentPadding =
                                PaddingValues(start = 12.dp, top = 12.dp, bottom = 12.dp),
                        )
                    }

                    items(
                        items = backerList,
                        span = { GridItemSpan(maxLineSpan / 3) },
                        key = { it.sponsorEntity.login },
                    ) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) { onSponsorClick(sponsorShip) }
                    }
                }

                if (sponsorList.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = SPONSORS) {
                        PreferenceSubtitle(
                            text = SPONSORS,
                            contentPadding =
                                PaddingValues(start = 12.dp, top = 12.dp, bottom = 12.dp),
                        )
                    }

                    items(
                        items = sponsorList,
                        span = { GridItemSpan(maxLineSpan / 4) },
                        key = { it.sponsorEntity.login },
                    ) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) { onSponsorClick(sponsorShip) }
                    }
                }

                // Mensagem do desenvolvedor e botões
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DeveloperMessageSection(
                        uriHandler = uriHandler,
                        isDarkTheme = isDarkTheme,
                        isGradientDark = isGradientDark
                    )
                }
            }
            if (showSheet) {
                SponsorDialog(sponsorShip = viewingSponsorShip, sheetState = sheetState) {
                    scope.launch { sheetState.hide(); showSheet = false }
                }
            }
        },
    )
}

// Componentes de interface
@Composable
fun HeroHeader(isDarkTheme: Boolean, isGradientDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isDarkTheme && isGradientDark) {
                    GradientBrushes.Primary
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isDarkTheme && isGradientDark)
                    Color.White
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Apoie o Destiny",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isDarkTheme && isGradientDark)
                    Color.White
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Ajude a manter este projeto gratuito, de código aberto e sempre atualizado",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = if (isDarkTheme && isGradientDark)
                    Color.White.copy(alpha = 0.9f)
                else
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AboutSealPlusSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Sobre o Destiny",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "O Destiny é um app para baixar vídeos e áudios de diversas plataformas, baseado nos projetos de código aberto Seal e Seal Plus. Feito com arquitetura moderna do Android e interface totalmente em português.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "O que o seu apoio possibilita",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                icon = Icons.Outlined.Speed,
                title = "Desempenho",
                description = "Downloads otimizados com aria2c",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = Icons.Outlined.Palette,
                title = "Visual moderno",
                description = "Temas e cores dinâmicas",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                icon = Icons.Outlined.CloudDownload,
                title = "Atualizações",
                description = "Novos recursos e correções",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = Icons.Outlined.Security,
                title = "Segurança",
                description = "Bloqueio do app com biometria",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun WhySponsorSection(isDarkTheme: Boolean, isGradientDark: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Por que apoiar?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            BenefitItem(
                emoji = "🚀",
                title = "Desenvolvimento mais rápido",
                description = "Seu apoio me permite dedicar mais tempo a novos recursos e melhorias"
            )

            BenefitItem(
                emoji = "🐛",
                title = "Correções rápidas",
                description = "Mais agilidade para resolver os problemas relatados pelos usuários"
            )

            BenefitItem(
                emoji = "🎨",
                title = "Melhor experiência",
                description = "Recursos para criar interfaces mais bem acabadas e agradáveis de usar"
            )

            BenefitItem(
                emoji = "🔒",
                title = "Sempre gratuito",
                description = "Seu apoio ajuda o Destiny a continuar gratuito e de código aberto para todos"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDarkTheme && isGradientDark) {
                            GradientBrushes.Secondary
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                )
                            )
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "💙 Toda contribuição, grande ou pequena, faz uma diferença real e é muito apreciada!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme && isGradientDark)
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BenefitItem(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DeveloperMessageSection(
    uriHandler: androidx.compose.ui.platform.UriHandler,
    isDarkTheme: Boolean,
    isGradientDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Mensagem do desenvolvedor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImageImpl(
                    model = gitHubAvatar(DEVELOPER_GITHUB_USER),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = if (isDarkTheme && isGradientDark) {
                                GradientBrushes.Primary
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            },
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Conversation(
                        modifier = Modifier,
                        text = stringResource(id = R.string.sponsor_msg)
                    )
                    Conversation(
                        modifier = Modifier,
                        text = stringResource(R.string.sponsor_msg2)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        uriHandler.openUri(DEVELOPER_SPONSOR_URL)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Apoiar o projeto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FilledTonalButton(
                    onClick = {
                        uriHandler.openUri(PROJECT_REPOSITORY_URL)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "⭐ Dar uma estrela no GitHub",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    text = "Até uma estrela no repositório ajuda a divulgar o projeto! 🌟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun SponsorPagePreview() {
    GitHubSponsorsPage {}
}

@Composable
fun Conversation(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier =
            modifier
                .padding(horizontal = 12.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun LazyGridItemScope.SponsorItem(sponsorShip: SponsorShip, onClick: () -> Unit) {
    SponsorItem(
        modifier = Modifier,
        userName = sponsorShip.sponsorEntity.name,
        userLogin = sponsorShip.sponsorEntity.login,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorDialog(sponsorShip: SponsorShip, sheetState: SheetState, onDismissRequest: () -> Unit) {
    val amount = sponsorShip.tier?.monthlyPriceInDollars ?: 0
    val tierText =
        if (amount in 5 until 10) {
            SPONSORS
        } else if (amount in 10 until 25) {
            BACKERS
        } else if (amount > 25) {
            SUPPORTERS
        } else {
            null
        }

    SealModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentPadding = PaddingValues(0.dp),
    ) {
        SponsorDialogContent(
            userLogin = sponsorShip.sponsorEntity.login,
            userName = sponsorShip.sponsorEntity.name,
            avatarUrl = gitHubAvatar(sponsorShip.sponsorEntity.login),
            tierText = tierText,
            website = sponsorShip.sponsorEntity.websiteUrl,
            socialLinks = sponsorShip.sponsorEntity.socialAccounts?.nodes?.map { it.url.toString() },
        )
    }
}

@Composable
fun SponsorDialogContent(
    userLogin: String,
    userName: String?,
    avatarUrl: String,
    tierText: String? = null,
    website: String? = null,
    socialLinks: List<String>? = null,
) {
    Column {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
                    .height(IntrinsicSize.Min)
        ) {
            AsyncImageImpl(
                modifier = Modifier.heightIn(max = 72.dp).aspectRatio(1f, true).clip(CircleShape),
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.padding(vertical = 20.dp).padding(start = 12.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = userName ?: "@$userLogin",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = tierText.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.HorizontalDivider()
            LinkItem(icon = Icons.Outlined.House, link = website ?: gitHubProfile(userLogin))
            socialLinks?.forEach { LinkItem(icon = Icons.Outlined.Link, link = it) }
        }
    }
}

@Composable
private fun LinkItem(modifier: Modifier = Modifier, icon: ImageVector, link: String) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler
                        .runCatching { openUri(link) }
                        .onFailure {
                            clipboardManager.setText(AnnotatedString(link))
                            context.makeToast(R.string.link_copied)
                        }
                }
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = link, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview
@Composable
private fun SponsorDialogContentPreview() {

    val sponsorShip =
        SponsorShip(
            sponsorEntity =
                SponsorEntity(
                    "example",
                    "example",
                    "https://www.example.com",
                    socialAccounts =
                        SocialAccounts(
                            buildList {
                                repeat(4) {
                                    add(
                                        SocialAccount(
                                            displayName = "Example",
                                            url = "https://www.example.com",
                                        )
                                    )
                                }
                            }
                        ),
                ),
            tier = Tier(10),
        )

    SealTheme {
        Surface {
            SponsorDialogContent(
                userLogin = sponsorShip.sponsorEntity.login,
                userName = sponsorShip.sponsorEntity.name,
                avatarUrl = gitHubAvatar(sponsorShip.sponsorEntity.login),
                website = sponsorShip.sponsorEntity.websiteUrl,
                socialLinks =
                    sponsorShip.sponsorEntity.socialAccounts?.nodes?.map { it.url.toString() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SponsorDialogPreview() {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sponsorShip =
        SponsorShip(
            sponsorEntity =
                SponsorEntity(
                    "example",
                    "example",
                    "https://www.example.com",
                    socialAccounts =
                        SocialAccounts(
                            buildList {
                                repeat(4) {
                                    add(
                                        SocialAccount(
                                            displayName = "Example",
                                            url = "https://www.example.com",
                                        )
                                    )
                                }
                            }
                        ),
                ),
            tier = Tier(10),
        )

    SponsorDialog(sponsorShip = sponsorShip, onDismissRequest = {}, sheetState = sheetState)
}