package com.junkfood.seal.ui.page.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalGradientDarkMode
import com.junkfood.seal.ui.theme.GradientDarkColors
import com.junkfood.seal.ui.theme.SealTheme
import kotlinx.coroutines.delay

/**
 * Tela de abertura do Destiny
 *
 * Recursos:
 * - Animação suave de entrada do logo
 * - Textos que aparecem em sequência
 * - Brilho pulsante no modo Gradient Dark
 * - Cores e gradientes de acordo com o tema
 * - Rodapé com os créditos do desenvolvedor
 *
 * Tempos (mais rápidos que a versão original):
 * - Logo aparece aos 100 ms
 * - Nome do app aos 350 ms
 * - Slogan aos 550 ms
 * - Tempo total na tela: cerca de 1,2 segundo
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val isGradientDark = LocalGradientDarkMode.current

    // Estados das animações
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    // Escala e transparência do logo
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    // Entrada dos textos
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "textAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "taglineAlpha"
    )

    // Brilho pulsante do logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Dispara as animações em sequência
    LaunchedEffect(Unit) {
        delay(100) // Atraso inicial
        logoVisible = true
        delay(250)
        textVisible = true
        delay(200)
        taglineVisible = true
        delay(650) // Tempo parado na tela
        onSplashFinished()
    }

    // Fundo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isGradientDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientDarkColors.Background,
                            GradientDarkColors.Surface,
                            GradientDarkColors.SurfaceContainerLow
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Logo com efeito de brilho
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Brilho (somente no modo Gradient Dark)
                if (isGradientDark && logoVisible) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(logoScale * 1.1f)
                            .alpha(glowAlpha * logoAlpha * 0.4f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GradientDarkColors.GradientPrimaryEnd.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Logo principal
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo do Destiny",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nome do app (uma linha só, para não quebrar no meio da palavra)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isGradientDark) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan
            Text(
                text = "Downloader de vídeo e áudio",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = if (isGradientDark) {
                    GradientDarkColors.GradientPrimaryEnd
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Rodapé
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha)
        ) {
            Text(
                text = "Desenvolvido por Campos",
                style = MaterialTheme.typography.bodySmall,
                color = if (isGradientDark) {
                    GradientDarkColors.OnSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "© 2026 Destiny",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGradientDark) {
                    GradientDarkColors.OnSurface.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}

/**
 * Pré-visualização no tema claro
 */
@Preview(name = "Tema claro", showBackground = true)
@Composable
private fun SplashScreenLightPreview() {
    SealTheme(darkTheme = false) {
        SplashScreen(onSplashFinished = {})
    }
}

/**
 * Pré-visualização no tema escuro
 */
@Preview(name = "Tema escuro", showBackground = true)
@Composable
private fun SplashScreenDarkPreview() {
    SealTheme(darkTheme = true) {
        SplashScreen(onSplashFinished = {})
    }
}