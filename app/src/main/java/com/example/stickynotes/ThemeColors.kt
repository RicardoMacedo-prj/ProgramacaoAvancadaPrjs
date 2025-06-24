package com.example.stickynotes

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//---------------------------------------------------
// ESTRUTURAS E MAPA DE TEMAS DA APP STICKYNOTES
//---------------------------------------------------

/**
 * Estrutura principal para definir todas as cores de um tema.
 * Cada tema tem cor de fundo, cartão de nota, ação, destaque, texto, descrição e palete.
 */
data class AppThemeColors(
    val background: Color,         // Fundo geral da app
    val noteBackground: Color,     // Fundo dos cartões de nota
    val action: Color,             // Cor para ações (ex: botões principais)
    val highlight: Color,          // Destaque secundário (ex: barras, switch, seleção)
    val text: Color,               // Cor base do texto
    val description: String,       // Descrição do tema (mostrado nas definições)
    val palette: List<Color>       // Palete completa para previews
)

/**
 * Mapa de todos os temas disponíveis.
 * Chave = nome visível do tema. Valor = objeto AppThemeColors com todas as cores.
 * Fácil de adicionar novos temas: basta criar novo AppThemeColors e acrescentar ao map.
 */
val ThemesMap = mapOf(
    "Serenity" to AppThemeColors(
        background = Color(0xFFF5F6FA),
        noteBackground = Color(0xFFFFFFFF),
        action = Color(0xFF6C63FF),
        highlight = Color(0xFFFAE39A),
        text = Color(0xFF232323),
        description = "A calming, modern theme for daily focus and clarity.",
        palette = listOf(
            Color(0xFFF5F6FA),
            Color(0xFFFFFFFF),
            Color(0xFF6C63FF),
            Color(0xFFFAE39A),
            Color(0xFF232323)
        )
    ),
    "Midnight Focus" to AppThemeColors(
        background = Color(0xFF23272F),
        noteBackground = Color(0xFF323642),
        action = Color(0xFFFFA500),
        highlight = Color(0xFF91C6E7),
        text = Color(0xFFF8F8FF),
        description = "For low-light environments, reducing eye strain at night.",
        palette = listOf(
            Color(0xFF23272F),
            Color(0xFF323642),
            Color(0xFFFFA500),
            Color(0xFF91C6E7),
            Color(0xFFF8F8FF)
        )
    ),
    "Sandstone" to AppThemeColors(
        background = Color(0xFFFFFCF7),
        noteBackground = Color(0xFFFFF7E6),
        action = Color(0xFFDBA15B),      // Dourado ameno
        highlight = Color(0xFFD3E1DF),   // Azul-cinza suave
        text = Color(0xFF463F3A),
        description = "A warm, natural palette for a classic paper-like experience.",
        palette = listOf(
            Color(0xFFFFFCF7),
            Color(0xFFFFF7E6),
            Color(0xFFDBA15B),
            Color(0xFFD3E1DF),
            Color(0xFF463F3A)
        )
    ),
    "Minimal Black & White" to AppThemeColors(
        background = Color(0xFFFFFFFF),
        noteBackground = Color(0xFFF5F5F5),
        action = Color(0xFF222222),
        highlight = Color(0xFF00B2FF),
        text = Color(0xFF232323),
        description = "Minimalist black and white for maximum readability.",
        palette = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF5F5F5),
            Color(0xFF222222),
            Color(0xFF00B2FF),
            Color(0xFF232323)
        )
    ),
    "Cotton" to AppThemeColors(
        background = Color(0xFFFFFAFB),
        noteBackground = Color(0xFFF3F6FF),
        action = Color(0xFF69B1FF),
        highlight = Color(0xFFE6B0FF),
        text = Color(0xFF262A32),
        description = "A soft pastel look for a gentle, creative workspace.",
        palette = listOf(
            Color(0xFFFFFAFB),
            Color(0xFFF3F6FF),
            Color(0xFF69B1FF),
            Color(0xFFE6B0FF),
            Color(0xFF262A32)
        )
    )
)

/**
 * Wrapper para aplicar o tema personalizado à tua UI usando o sistema Material 3 do Compose.
 * Garante que todas as cores são coerentes e compatíveis com o MaterialTheme.
 * Deve envolver toda a UI no setContent().
 */
@Composable
fun StickyNotesTheme(
    themeColors: AppThemeColors,
    content: @Composable () -> Unit
) {
    // Preenche todos os parâmetros do ColorScheme (muitos redundantes para garantir compatibilidade futura)
    val colorScheme = ColorScheme(
        primary = themeColors.action,
        onPrimary = themeColors.text,
        primaryContainer = themeColors.noteBackground,
        onPrimaryContainer = themeColors.text,
        inversePrimary = themeColors.highlight,

        secondary = themeColors.highlight,
        onSecondary = themeColors.text,
        secondaryContainer = themeColors.noteBackground,
        onSecondaryContainer = themeColors.text,

        tertiary = themeColors.action,
        onTertiary = themeColors.text,
        tertiaryContainer = themeColors.noteBackground,
        onTertiaryContainer = themeColors.text,

        background = themeColors.background,
        onBackground = themeColors.text,

        surface = themeColors.noteBackground,
        onSurface = themeColors.text,

        surfaceVariant = themeColors.noteBackground,
        onSurfaceVariant = themeColors.text,
        surfaceTint = themeColors.action,

        inverseSurface = themeColors.noteBackground,
        inverseOnSurface = themeColors.text,

        error = Color.Red,
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),

        outline = themeColors.action,
        outlineVariant = themeColors.highlight,
        scrim = Color.Black.copy(alpha = 0.32f),

        surfaceBright = themeColors.noteBackground,
        surfaceDim = themeColors.background,
        surfaceContainer = themeColors.noteBackground,
        surfaceContainerHigh = themeColors.noteBackground,
        surfaceContainerHighest = themeColors.noteBackground,
        surfaceContainerLow = themeColors.noteBackground,
        surfaceContainerLowest = themeColors.background
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
