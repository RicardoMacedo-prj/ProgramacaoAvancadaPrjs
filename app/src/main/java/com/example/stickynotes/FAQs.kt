package com.example.stickynotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//----------------------------------------------
// ECRÃ DE FAQ (Perguntas Frequentes) da aplicação
//----------------------------------------------

/**
 * Ecrã principal das FAQs.
 * Lê as preferências de tema e apresenta a lista de perguntas frequentes.
 */
@Composable
fun FAQsScreen() {
    val context = LocalContext.current

    // Lê as preferências de tema da app (para garantir coerência visual)
    val prefs = context.getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE)
    val themeName = prefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    // Renderiza o conteúdo da página de FAQs
    FAQsScreenContent(themeColors = themeColors)
}

/**
 * Mostra a lista de perguntas frequentes.
 * Permite fácil expansão/edição das FAQs (só precisa editar a lista 'faqList').
 */
@Composable
fun FAQsScreenContent(themeColors: AppThemeColors) {
    // Lista fixa com todas as FAQs do app (pode ser expandida facilmente)
    val faqList = remember {
        listOf(
            FAQ(
                question = "How do I create a new note?",
                answer = "Tap the '+' button in the bottom right corner of the Home screen to add a new note. Fill in the title, your text, and optionally set a reminder date."
            ),
            FAQ(
                question = "Can I change the app theme?",
                answer = "Yes! Go to Settings and select your preferred visual theme. All UI elements will adapt instantly."
            ),
            FAQ(
                question = "How do I change the note font size or style?",
                answer = "In Settings, you can adjust the font size and style used to display your notes. The changes are applied in real-time."
            ),
            FAQ(
                question = "How do I sort my notes?",
                answer = "Go to Settings > Order Notes By, and choose your preferred sorting option: Title, Reminder Date, or Creation Date."
            ),
            FAQ(
                question = "How does note deletion work?",
                answer = "You can delete a note by long-pressing it and selecting 'Delete'. If the confirmation option is enabled in Settings, you'll be asked before the note is deleted."
            ),
            FAQ(
                question = "Can I search my notes?",
                answer = "Yes! Use the Search tab to find notes by their title or content."
            ),
            FAQ(
                question = "Who developed StickyNotes?",
                answer = "This app was developed as part of the Advanced Programming course in Computer Engineering (ESTG-IPG, 2024/2025), by Ricardo Macedo."
            )
        )
    }

    // Layout principal: coluna com scroll e espaçamento entre os cartões FAQ
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        faqList.forEach { faq ->
            FAQCard(faq = faq, themeColors = themeColors)
        }
    }
}

/**
 * Cartão expand/collapse para cada pergunta + resposta.
 * Clica para expandir/colapsar a resposta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQCard(faq: FAQ, themeColors: AppThemeColors) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.noteBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = themeColors.action,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    faq.question,
                    color = themeColors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    faq.answer,
                    color = themeColors.text.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * Estrutura de dados simples para representar uma FAQ (pergunta e resposta).
 */
data class FAQ(val question: String, val answer: String)


//-------------------------------------------
// PREVIEWS PARA REVISÃO VISUAL NO ANDROID STUDIO
//-------------------------------------------

@Preview(showBackground = true)
@Composable
fun FAQCardPreview() {
    // Preview de um cartão FAQ expandido
    FAQCard(
        faq = FAQ(
            question = "What is StickyNotes?",
            answer = "StickyNotes is a simple app to organize your notes and reminders."
        ),
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first()
    )
}

@Preview(showBackground = true)
@Composable
fun FAQsScreenContentPreview() {
    FAQsScreenContent(themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first())
}
