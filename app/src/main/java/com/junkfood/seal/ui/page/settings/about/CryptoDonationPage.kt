package com.junkfood.seal.ui.page.settings.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalGradientDarkMode
import com.junkfood.seal.ui.theme.GradientBrushes
import com.junkfood.seal.ui.theme.GradientDarkColors
import com.junkfood.seal.util.makeToast

// Alterado de BEP20 para a sua chave PIX
private const val PIX_KEY = "b6b3d5bd-0c6c-4b08-b928-845ab7e90252"
// QR Code gerado a partir da chave PIX
private const val QR_CODE_API_URL =
    "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=b6b3d5bd-0c6c-4b08-b928-845ab7e90252"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDonationPage(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
    val isGradientDark = LocalGradientDarkMode.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Doação via PIX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header com gradiente verde
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isDarkTheme && isGradientDark) {
                                GradientBrushes.Accent
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00B060), // Verde PIX
                                        Color(0xFF32D583)  // Verde claro
                                    )
                                )
                            }
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = if (isDarkTheme && isGradientDark)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Doação Rápida e Segura",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isDarkTheme && isGradientDark)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Ajude a manter o Destiny gratuito e em constante desenvolvimento via PIX.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = if (isDarkTheme && isGradientDark)
                                Color.White.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Aviso
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme && isGradientDark) {
                            GradientDarkColors.SurfaceContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = if (isDarkTheme && isGradientDark) {
                                Color(0xFF00B060)
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "O PIX é instantâneo e não pode ser estornado. Verifique os dados do destinatário antes de confirmar a transferência.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.OnSurface
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Informações da Chave
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme && isGradientDark) {
                            GradientDarkColors.SurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Método
                        Text(
                            text = "Método de Doação",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.OnSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalance,
                                contentDescription = null,
                                tint = if (isDarkTheme && isGradientDark) {
                                    GradientDarkColors.GradientCyan
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "PIX",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkTheme && isGradientDark) {
                                        GradientDarkColors.OnSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = "Chave Aleatória",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDarkTheme && isGradientDark) {
                                        GradientDarkColors.OnSurface.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    }
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        // Chave PIX
                        Text(
                            text = "Chave PIX (Copiar e Colar)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.OnSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.SurfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ) {
                            Text(
                                text = PIX_KEY,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme && isGradientDark) {
                                    GradientDarkColors.GradientCyan
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(16.dp),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                        }

                        // Botão de Copiar
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(PIX_KEY))
                                context.makeToast("Chave PIX copiada com sucesso!")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = if (isDarkTheme && isGradientDark) {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = GradientDarkColors.OnPrimary
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isDarkTheme && isGradientDark) {
                                            GradientBrushes.Primary
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Copiar Chave PIX",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // QR Code Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme && isGradientDark) {
                            GradientDarkColors.SurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Escanear QR Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.OnSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Surface(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImageImpl(
                                    model = QR_CODE_API_URL,
                                    contentDescription = "QR Code PIX",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Text(
                            text = "Abra o aplicativo do seu banco e escaneie o QR Code para preencher os dados da doação automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.OnSurface.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }

            // Notas Importantes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme && isGradientDark) {
                            GradientDarkColors.SurfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = if (isDarkTheme && isGradientDark) {
                                GradientDarkColors.GradientPurpleBright
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Notas Importantes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme && isGradientDark) {
                                    GradientDarkColors.OnSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = "• Envie qualquer valor via PIX\n• Sua contribuição é voluntária\n• Todas as doações são usadas para manter o projeto ativo\n• Não é possível solicitar reembolso",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkTheme && isGradientDark) {
                                    GradientDarkColors.OnSurface.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }
                }
            }

            // Espaçamento final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}