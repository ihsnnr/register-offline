package com.registeroffline.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Colors matching Figma ──
val Navy = Color(0xFF2C3A6E)
val NavyDark = Color(0xFF1E2A5E)
val NavyLight = Color(0xFF3F51B5)
val White = Color(0xFFFFFFFF)
val Background = Color(0xFFF7F7FB)
val Surface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val TextHint = Color(0xFF9CA3AF)
val ErrorRed = Color(0xFFDC2626)
val SuccessGreen = Color(0xFF16A34A)
val WarningAmber = Color(0xFFF59E0B)
val DraftOrange = Color(0xFFEF4444)
val SyncedGreen = Color(0xFF22C55E)
val BorderGray = Color(0xFFE5E7EB)
val DividerGray = Color(0xFFF3F4F6)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = White,
    primaryContainer = NavyLight,
    secondary = NavyLight,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = White,
    outline = BorderGray,
)

@Composable
fun RegisterOfflineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
            headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
            titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
            titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
            bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = TextPrimary),
            bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, color = TextSecondary),
            bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = TextHint),
            labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White),
        ),
        content = content,
    )
}
