package com.example.stickynotes

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//-----------------------------------------------------
// ECRÃ DE ESCOLHA DE TEMA
//-----------------------------------------------------

/**
 * Ecrã que permite ao utilizador escolher o tema visual da app.
 * Mostra todos os temas disponíveis como cartões.
 * Ao selecionar um tema, guarda a preferência e reinicia a activity para aplicar globalmente.
 */
@Composable
fun ThemeScreen() {
    val context = LocalContext.current

    // Lê os temas disponíveis e o tema atualmente selecionado
    val themes = ThemesMap.keys.toList()
    val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val currentTheme = prefs.getString("app_theme", "Serenity") ?: "Serenity"

    // Mostra o grid dos temas para escolha
    ThemePickerScreen(
        themes = themes,
        currentTheme = currentTheme,
        onThemeSelected = { themeName ->
            // Guarda a nova escolha
            prefs.edit().putString("app_theme", themeName).apply()
            // Força refresh global para aplicar o tema (recria a activity)
            (context as? androidx.activity.ComponentActivity)?.recreate()
        }
    )
}

/**
 * Mostra todos os temas disponíveis como uma lista (grid vertical).
 * Permite selecionar um deles.
 */
@Composable
fun ThemePickerScreen(
    themes: List<String>,
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    // O ecrã inteiro tem o background do tema Serenity para dar unidade visual
    val serenityColors = ThemesMap["Serenity"] ?: ThemesMap.values.first()

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(serenityColors.background)
    ) {
        items(themes) { themeName ->
            val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()
            ThemeCard(
                themeName = themeName,
                themeColors = themeColors,
                selected = (themeName == currentTheme),
                onThemeSelected = { onThemeSelected(themeName) }
            )
        }
    }
}

/**
 * Cartão visual que mostra nome, descrição e palete de cada tema.
 * Indica o tema selecionado com uma borda mais grossa e cor destacada.
 */
@Composable
fun ThemeCard(
    themeName: String,
    themeColors: AppThemeColors,
    selected: Boolean = false,
    onThemeSelected: () -> Unit = {}
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else themeColors.highlight

    val borderWidth = if (selected) 4.dp else 2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(themeColors.background, shape = RoundedCornerShape(16.dp))
            .clickable { onThemeSelected() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Nome do tema
            Text(
                text = themeName,
                color = themeColors.action,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            // Descrição resumida do tema
            Text(
                text = themeColors.description,
                color = themeColors.text.copy(alpha = 0.7f),
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            // Palete visual do tema (para melhor comparação de cor)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                themeColors.palette.forEach { color ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

//----------------------------------------------
// PREVIEWS PARA REVISÃO VISUAL NO ANDROID STUDIO
//----------------------------------------------

@Preview(showBackground = true)
@Composable
fun ThemeCardPreview() {
    ThemeCard(
        themeName = "Serenity",
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first(),
        selected = true
    )
}

@Preview(showBackground = true)
@Composable
fun ThemePickerScreenPreview() {
    ThemePickerScreen(
        themes = ThemesMap.keys.toList(),
        currentTheme = "Serenity",
        onThemeSelected = {}
    )
}
