package com.junkfood.seal.ui.page.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.theme.GradientBrushes
import com.junkfood.seal.ui.theme.GradientDarkColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dados de cada página da introdução
 */
data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: Brush
)

/**
 * Tela principal da introdução, com navegação por páginas deslizantes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.VideoLibrary,
            title = "Bem-vindo ao Destiny",
            description = "Seu downloader de vídeos e áudios com tecnologia yt-dlp. Baixe vídeos, músicas e playlists das suas plataformas favoritas com facilidade.",
            gradient = GradientBrushes.Primary
        ),
        OnboardingPage(
            icon = Icons.Filled.Download,
            title = "Downloads poderosos",
            description = "Baixe em vários formatos e qualidades. Escolha entre vídeo, somente áudio ou formatos personalizados, com configurações avançadas.",
            gradient = GradientBrushes.Secondary
        ),
        OnboardingPage(
            icon = Icons.Filled.Settings,
            title = "Totalmente personalizável",
            description = "Muitas configurações para deixar o app do seu jeito. Controle a pasta de downloads, as preferências de rede, os temas e muito mais.",
            gradient = GradientBrushes.Accent
        ),
        OnboardingPage(
            icon = Icons.Filled.Security,
            title = "Privacidade e segurança",
            description = "Seus downloads são privados e seguros. O bloqueio opcional do app com PIN ou biometria mantém o seu conteúdo protegido.",
            gradient = GradientBrushes.Vibrant
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) GradientDarkColors.Background
                else MaterialTheme.colorScheme.background
            )
    ) {
        // Fundo com manchas de gradiente
        AnimatedBackgroundGradient(
            currentPage = pagerState.currentPage,
            isDarkTheme = isDarkTheme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão Pular no topo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pages.size - 1)
                            }
                        }
                    ) {
                        Text(
                            text = "Pular",
                            color = if (isDarkTheme)
                                GradientDarkColors.OnSurface.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Páginas deslizantes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Indicadores de página
            PageIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botões de navegação
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = if (isDarkTheme)
                                    GradientDarkColors.GradientPrimaryEnd
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Voltar", fontSize = 16.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Botão Próximo / Começar
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (pagerState.currentPage > 0) 16.dp else 0.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme)
                                GradientDarkColors.GradientPrimaryEnd
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (pagerState.currentPage < pages.size - 1) "Próximo" else "Começar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Sem tint fixo: usa a cor do texto do botão, para a seta aparecer
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Conteúdo de cada página da introdução, com animações
 */
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isDarkTheme: Boolean
) {
    // Estado da animação
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(page) {
        visible = false
        delay(100)
        visible = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(contentAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícone com fundo em gradiente e brilho
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(iconScale),
            contentAlignment = Alignment.Center
        ) {
            // Brilho
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .alpha(0.4f)
                    .blur(20.dp)
                    .background(
                        brush = page.gradient,
                        shape = CircleShape
                    )
            )

            // Container do ícone
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkTheme)
                            GradientDarkColors.SurfaceContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(brush = page.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Título
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (isDarkTheme)
                GradientDarkColors.OnBackground
            else
                MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descrição
        Text(
            text = page.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            color = if (isDarkTheme)
                GradientDarkColors.OnSurface.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Indicadores de página (bolinhas)
 */
@Composable
fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    isDarkTheme: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val isSelected = page == currentPage

            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicatorWidth"
            )

            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(durationMillis = 300),
                label = "indicatorAlpha"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .alpha(alpha)
                    .background(
                        if (isDarkTheme)
                            GradientDarkColors.GradientPrimaryEnd
                        else
                            MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}

/**
 * Fundo com manchas de gradiente que acompanham a página atual
 * (apenas no tema escuro). Removi a animação infinita de rotação,
 * que não era usada e gastava processamento à toa.
 */
@Composable
fun AnimatedBackgroundGradient(
    currentPage: Int,
    isDarkTheme: Boolean
) {
    if (!isDarkTheme) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.15f)
    ) {
        // Mancha no canto superior direito
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = (-100).dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientDarkColors.GradientPrimaryEnd.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Mancha no canto inferior esquerdo
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 100.dp)
                .blur(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientDarkColors.GradientAccentEnd.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Mancha central (se move conforme a página)
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .offset(
                    x = (currentPage * 50 - 100).dp,
                    y = 0.dp
                )
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientDarkColors.GradientSecondaryEnd.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}