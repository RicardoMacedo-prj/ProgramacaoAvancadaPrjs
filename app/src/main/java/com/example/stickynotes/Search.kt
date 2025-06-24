package com.example.stickynotes

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//--------------------------------------------------------
// ECRÃ PRINCIPAL DE PESQUISA DAS NOTAS
//--------------------------------------------------------

/**
 * Ecrã de pesquisa.
 * Permite procurar notas existentes pelo título ou conteúdo.
 * Aplica as preferências de tema, fonte e ordenação.
 */
@Composable
fun SearchScreen() {
    val context = LocalContext.current

    // Carrega todas as notas gravadas localmente (não refresca dinâmico).
    val notes = remember { NotesStorage.loadNotes(context) }

    // Preferências de tema para aplicação das cores.
    val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeName = prefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    // Preferências de visualização e fonte.
    val viewPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    val selectedFontSize = viewPrefs.getString("font_size", "Medium") ?: "Medium"
    val selectedFontStyle = viewPrefs.getString("font_style", "Sans-serif") ?: "Sans-serif"
    val fontSizeSp = fontSizeToSp(selectedFontSize)
    val fontFamily = fontStyleToFamily(selectedFontStyle)

    // Ordenação das notas conforme o critério do utilizador.
    val sortBy = viewPrefs.getString("sort_by", "Title (A-Z)") ?: "Title (A-Z)"
    val sortedNotes = sortNotes(notes, sortBy)

    // Passa todos os parâmetros para o conteúdo do ecrã de pesquisa.
    SearchScreenContent(
        allNotes = sortedNotes,
        themeColors = themeColors,
        fontSizeSp = fontSizeSp,
        fontFamily = fontFamily
    )
}

/**
 * Composable responsável pelo layout e lógica do ecrã de pesquisa.
 * Permite filtrar as notas à medida que o utilizador escreve.
 */
@Composable
fun SearchScreenContent(
    allNotes: List<Notes>,
    themeColors: AppThemeColors,
    fontSizeSp: Float,
    fontFamily: FontFamily
) {
    var search by remember { mutableStateOf("") }

    // Filtra as notas de acordo com o texto de pesquisa.
    val filteredNotes = if (search.isNotBlank()) {
        allNotes.filter {
            it.title.contains(search, ignoreCase = true) ||
                    it.subtitle.contains(search, ignoreCase = true)
        }
    } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(24.dp)
    ) {
        // Título do ecrã.
        Text(
            text = "Search your notes",
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = themeColors.text,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // Campo de pesquisa.
        SearchBar(
            value = search,
            onValueChange = { search = it },
            themeColors = themeColors,
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Mensagens auxiliares e apresentação dos resultados da pesquisa.
        when {
            search.isEmpty() -> {
                // Mensagem de instrução inicial.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type to search your notes…",
                        color = themeColors.text.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
            }
            filteredNotes.isEmpty() -> {
                // Nenhum resultado encontrado.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes found.",
                        color = themeColors.text.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
            }
            else -> {
                // Lista das notas que correspondem à pesquisa.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredNotes.size) { index ->
                        NoteCard(
                            note = filteredNotes[index],
                            themeColors = themeColors,
                            fontSizeSp = fontSizeSp,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }
}

/**
 * Campo de pesquisa (barra superior) com ícone e placeholder.
 * Não usa preferências de tamanho nem estilo (para garantir legibilidade).
 */
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    themeColors: AppThemeColors,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = themeColors.action)
        },
        placeholder = {
            Text(
                "Search by title or text...",
                color = themeColors.text.copy(alpha = 0.7f)
            )
        },
        singleLine = true,
        textStyle = TextStyle(
            color = themeColors.text,
            fontSize = 16.sp,
            fontFamily = FontFamily.Default
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

/**
 * Cartão visual para cada nota nos resultados de pesquisa.
 * Respeita as preferências de fonte e tamanho do utilizador.
 */
@Composable
fun NoteCard(
    note: Notes,
    themeColors: AppThemeColors,
    fontSizeSp: Float,
    fontFamily: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.noteBackground, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            note.title,
            fontWeight = FontWeight.Bold,
            fontSize = fontSizeSp.sp,
            color = themeColors.text,
            fontFamily = fontFamily
        )
        Spacer(Modifier.height(8.dp))
        Text(
            note.subtitle,
            fontSize = fontSizeSp.sp,
            color = themeColors.text.copy(alpha = 0.8f),
            fontFamily = fontFamily
        )
        if (note.reminderAt != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Reminder: ${formatDate(note.reminderAt)}",
                fontSize = (fontSizeSp * 0.85f).sp,
                color = themeColors.action,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
        }
        Text(
            "Created: ${formatDate(note.createdAt)}",
            fontSize = (fontSizeSp * 0.70f).sp,
            color = themeColors.text.copy(alpha = 0.7f),
            fontFamily = fontFamily
        )
    }
}

//-----------------------------------------
// FUNÇÕES AUXILIARES DE ESTILO E ORDENAÇÃO
//-----------------------------------------

/**
 * Converte a string do tamanho de letra em valor em sp (escala visual do Android).
 */
fun fontSizeToSp(size: String): Float = when (size) {
    "Extra Small" -> 12f
    "Small" -> 14f
    "Medium" -> 16f
    "Large" -> 18f
    "Extra Large" -> 22f
    else -> 16f
}

/**
 * Converte string do estilo de letra em FontFamily do Compose.
 */
fun fontStyleToFamily(style: String): FontFamily = when (style) {
    "Sans-serif" -> FontFamily.SansSerif
    "Serif" -> FontFamily.Serif
    "Monospace" -> FontFamily.Monospace
    "Cursive" -> FontFamily.Cursive
    else -> FontFamily.Default
}

/**
 * Ordena a lista de notas conforme critério escolhido pelo utilizador.
 * Suporta ordem por título, reminder ou data de criação.
 */
fun sortNotes(notes: List<Notes>, sortBy: String): List<Notes> {
    return when (sortBy) {
        "Title (A-Z)" -> notes.sortedBy { it.title.lowercase() }
        "Title (Z-A)" -> notes.sortedByDescending { it.title.lowercase() }
        "Reminder Date" -> notes.sortedWith(
            compareBy<Notes> { it.reminderAt == null }.thenBy { it.reminderAt }
        )
        "Creation Date (Newest)" -> notes.sortedByDescending { it.createdAt }
        "Creation Date (Oldest)" -> notes.sortedBy { it.createdAt }
        else -> notes
    }
}

//------------------------------------------------
// PREVIEWS PARA REVISÃO VISUAL NO ANDROID STUDIO
//------------------------------------------------

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        value = "",
        onValueChange = {},
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first()
    )
}
