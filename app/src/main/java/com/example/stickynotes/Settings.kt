package com.example.stickynotes

import android.content.Context
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
import androidx.compose.ui.zIndex
import androidx.core.content.edit

//--------------------------------------------------------
// ECRÃ DE DEFINIÇÕES/CONFIGURAÇÕES DA APP
//--------------------------------------------------------

/**
 * Ecrã principal de definições/configurações.
 * Aplica o tema ativo e carrega o conteúdo real.
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    // Aplica tema escolhido pelo utilizador
    val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeName = prefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    SettingsScreenContent(themeColors = themeColors)
}

/**
 * Conteúdo principal do ecrã de definições.
 * Mostra todas as opções editáveis de forma agrupada.
 */
@Composable
fun SettingsScreenContent(themeColors: AppThemeColors) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    // Opções disponíveis para cada definição (tamanho/letra, etc.)
    val fontSizes = listOf("Extra Small", "Small", "Medium", "Large", "Extra Large")
    val fontStyles = listOf("Sans-serif", "Serif", "Monospace", "Cursive")
    val sortingOptions = listOf(
        "Title (A-Z)",
        "Title (Z-A)",
        "Reminder Date",
        "Creation Date (Newest)",
        "Creation Date (Oldest)"
    )

    // Estados locais (reativos) ligados às preferências
    var selectedFontSize by remember { mutableStateOf(sharedPrefs.getString("font_size", "Medium") ?: "Medium") }
    var selectedFontStyle by remember { mutableStateOf(sharedPrefs.getString("font_style", "Sans-serif") ?: "Sans-serif") }
    var selectedViewMode by remember { mutableStateOf(sharedPrefs.getString("view_mode", "Grid") ?: "Grid") }
    var confirmDelete by remember { mutableStateOf(sharedPrefs.getBoolean("confirm_delete", true)) }
    var selectedSort by remember { mutableStateOf(sharedPrefs.getString("sort_by", sortingOptions[0]) ?: sortingOptions[0]) }

    // Layout scrollável, espaçamento entre secções
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Secção 1: Fonte
        SectionCard(
            title = "Note Font",
            icon = Icons.Default.Info,
            themeColors = themeColors
        ) {
            Text(
                "Font size:",
                color = themeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            FontDropdown(
                selected = selectedFontSize,
                options = fontSizes,
                onSelect = {
                    selectedFontSize = it
                    sharedPrefs.edit { putString("font_size", it) }
                },
                themeColors = themeColors
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Font style:",
                color = themeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            FontDropdown(
                selected = selectedFontStyle,
                options = fontStyles,
                onSelect = {
                    selectedFontStyle = it
                    sharedPrefs.edit { putString("font_style", it) }
                },
                themeColors = themeColors
            )
        }

        // Secção 2: Modo de visualização (Grelha ou Lista)
        SectionCard(
            title = "View Mode",
            icon = Icons.Default.Info,
            themeColors = themeColors
        ) {
            Text(
                "Choose how to display your notes:",
                color = themeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                RadioButton(
                    selected = selectedViewMode == "Grid",
                    onClick = {
                        selectedViewMode = "Grid"
                        sharedPrefs.edit { putString("view_mode", "Grid") }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = themeColors.action)
                )
                Text(
                    "Grid",
                    color = themeColors.text,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                RadioButton(
                    selected = selectedViewMode == "List",
                    onClick = {
                        selectedViewMode = "List"
                        sharedPrefs.edit { putString("view_mode", "List") }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = themeColors.action)
                )
                Text(
                    "List",
                    color = themeColors.text,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Secção 3: Ordenação das notas
        SectionCard(
            title = "Order Notes By",
            icon = Icons.Default.Info,
            themeColors = themeColors
        ) {
            Text(
                "Sort your notes by:",
                color = themeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            FontDropdown(
                selected = selectedSort,
                options = sortingOptions,
                onSelect = {
                    selectedSort = it
                    sharedPrefs.edit { putString("sort_by", it) }
                },
                themeColors = themeColors
            )
        }

        // Secção 4: Confirmação antes de apagar
        SectionCard(
            title = "Delete Notes",
            icon = Icons.Default.Info,
            themeColors = themeColors
        ) {
            Text(
                "Ask for confirmation before deleting",
                color = themeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                "Enable to show a warning before deleting a note.",
                color = themeColors.text.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            Switch(
                checked = confirmDelete,
                onCheckedChange = {
                    confirmDelete = it
                    sharedPrefs.edit { putBoolean("confirm_delete", it) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = themeColors.action,
                    checkedTrackColor = themeColors.highlight
                )
            )
        }

        // Secção 5: Sobre a app
        SectionCard(
            title = "About",
            icon = Icons.Default.Info,
            themeColors = themeColors
        ) {
            Text(
                "StickyNotes v1.0",
                color = themeColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Academic project — Advanced Programming\nComputer Engineering, 2024/2025\n\nDeveloped by: Ricardo Macedo",
                color = themeColors.text.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
        }
    }
}

//--------------------------------------------------------
// COMPONENTES REUTILIZÁVEIS (DROPDOWN, SECTIONS)
//--------------------------------------------------------

/**
 * Dropdown para seleção de fonte, estilo, ordenação, etc.
 * Reutilizável em várias secções.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    themeColors: AppThemeColors
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(2f) // Garante que o dropdown aparece acima do resto
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.action,
                    unfocusedBorderColor = themeColors.action,
                    focusedLabelColor = themeColors.text,
                    unfocusedLabelColor = themeColors.text,
                    cursorColor = themeColors.action,
                    focusedContainerColor = themeColors.noteBackground,
                    unfocusedContainerColor = themeColors.noteBackground,
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = themeColors.text,
                    fontSize = 15.sp
                ),
                enabled = true
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(themeColors.noteBackground)
                    .zIndex(2f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = themeColors.text) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Cartão visual para agrupar cada secção de definições,
 * com título, ícone e conteúdo customizável.
 */
@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColors: AppThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                color = themeColors.noteBackground,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = themeColors.action,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = themeColors.action
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

//--------------------------------------------------------
// PREVIEWS (exemplo para SectionCard e FontDropdown)
//--------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun SectionCardPreview() {
    SectionCard(
        title = "Note Font",
        icon = Icons.Default.Info,
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first()
    ) {
        Text("Exemplo de conteúdo dentro do cartão de secção.", color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true)
@Composable
fun FontDropdownPreview() {
    FontDropdown(
        selected = "Medium",
        options = listOf("Extra Small", "Small", "Medium", "Large"),
        onSelect = {},
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first()
    )
}
