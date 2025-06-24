package com.example.stickynotes

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

//---------------------------------------------
// ECRÃ PRINCIPAL: Listagem das notas existentes
//---------------------------------------------

/**
 * Ecrã principal das notas.
 * Apresenta as notas guardadas em grelha ou lista, conforme preferência do utilizador.
 * Suporta refresh automático, edição e eliminação de notas.
 * @param navController Para navegação entre ecrãs.
 */
@Composable
fun HomeScreen(navController: NavController? = null) {
    val context = LocalContext.current

    // Preferências de tema (cores)
    val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeName = themePrefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    // Preferências de visualização (grelha/lista, ordenação, fonte)
    val viewPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    val viewMode = viewPrefs.getString("view_mode", "Grid") ?: "Grid"
    val sortBy = viewPrefs.getString("sort_by", "Title (A-Z)") ?: "Title (A-Z)"
    val selectedFontSize = viewPrefs.getString("font_size", "Medium") ?: "Medium"
    val selectedFontStyle = viewPrefs.getString("font_style", "Sans-serif") ?: "Sans-serif"
    val fontSizeSp = fontSizeToSp(selectedFontSize)
    val fontFamily = fontStyleToFamily(selectedFontStyle)
    val confirmDelete = viewPrefs.getBoolean("confirm_delete", true)

    // Permite forçar o refresh da lista de notas
    var refreshKey by remember { mutableStateOf(0) }

    // Carrega as notas (refresha quando refreshKey muda)
    val notes = remember(refreshKey) { NotesStorage.loadNotes(context) }
    val sortedNotes = sortNotes(notes, sortBy)

    // Estado para nota a eliminar
    var noteToDelete by remember { mutableStateOf<Notes?>(null) }

    Scaffold(
        floatingActionButton = {
            // Botão para adicionar nova nota
            FloatingActionButton(
                onClick = {
                    navController?.navigate("add")
                },
                containerColor = themeColors.highlight
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note", tint = themeColors.text)
            }
        },
        containerColor = themeColors.background,
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(innerPadding)
        ) {
            // Diálogo de confirmação para eliminar nota
            if (noteToDelete != null) {
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = {
                        Text(
                            "Eliminar Nota?",
                            fontWeight = FontWeight.Bold,
                            color = themeColors.highlight
                        )
                    },
                    text = {
                        Text(
                            "Tem a certeza que deseja eliminar esta nota? Esta ação é irreversível.",
                            color = themeColors.text
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val updatedNotes = notes.toMutableList()
                            updatedNotes.remove(noteToDelete)
                            NotesStorage.saveNotes(context, updatedNotes)
                            noteToDelete = null
                            refreshKey++ // força refresh da lista
                        }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { noteToDelete = null }) { Text("Cancelar") }
                    },
                    containerColor = themeColors.noteBackground
                )
            }

            // Mostra notas em grelha ou lista conforme preferência
            if (viewMode == "Grid") {
                // Grelha vertical tipo "Masonry"
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalItemSpacing = 18.dp,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(sortedNotes.size) { index ->
                        val note = sortedNotes[index]
                        NotesContent(
                            data = note,
                            themeColors = themeColors,
                            onAction = { action ->
                                when (action) {
                                    "edit" -> navController?.navigate("edit/${note.createdAt}")
                                    "delete" -> {
                                        if (confirmDelete) noteToDelete = note
                                        else {
                                            val updatedNotes = notes.toMutableList()
                                            updatedNotes.remove(note)
                                            NotesStorage.saveNotes(context, updatedNotes)
                                            refreshKey++
                                        }
                                    }
                                }
                            },
                            fontSizeSp = fontSizeSp,
                            fontFamily = fontFamily
                        )
                    }
                }
            } else {
                // Listagem tradicional vertical
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(sortedNotes) { note ->
                        NotesContent(
                            data = note,
                            themeColors = themeColors,
                            onAction = { action ->
                                when (action) {
                                    "edit" -> navController?.navigate("edit/${note.title}/${note.subtitle}")
                                    "delete" -> {
                                        if (confirmDelete) noteToDelete = note
                                        else {
                                            val updatedNotes = notes.toMutableList()
                                            updatedNotes.remove(note)
                                            NotesStorage.saveNotes(context, updatedNotes)
                                            refreshKey++
                                        }
                                    }
                                }
                            },
                            fontSizeSp = fontSizeSp,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }
}


//-------------------------------------------------------
// COMPONENTE REUTILIZÁVEL: Cartão individual de uma nota
//-------------------------------------------------------

/**
 * Cartão visual de uma nota, mostra título, conteúdo, reminder e datas.
 * Aceita ações de editar ou eliminar ao fazer long-press.
 */
@Composable
fun NotesContent(
    data: Notes,
    themeColors: AppThemeColors,
    onAction: (String) -> Unit,
    fontSizeSp: Float,
    fontFamily: FontFamily
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFontSize = 12.sp
    val reminderFontSize = 14.sp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = themeColors.noteBackground, shape = RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { expanded = true }
                )
            }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = data.title,
                fontSize = fontSizeSp.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = themeColors.text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.subtitle,
                fontSize = fontSizeSp.sp,
                color = themeColors.text.copy(alpha = 0.8f),
                fontFamily = fontFamily
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (data.reminderAt != null) {
                Text(
                    "Reminder: ${formatDate(data.reminderAt)}",
                    fontSize = reminderFontSize,
                    color = themeColors.action,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                "Created: ${formatDate(data.createdAt)}",
                fontSize = dateFontSize,
                color = themeColors.text.copy(alpha = 0.7f),
                fontFamily = FontFamily.Default
            )
        }

        // Menu de contexto ao manter pressionado (Editar/Eliminar)
        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(themeColors.highlight)
            ) {
                DropdownMenuItem(
                    onClick = {
                        onAction("edit")
                        expanded = false
                    },
                    text = { Text("Edit", color = themeColors.text.copy(alpha = 0.98f), fontWeight = FontWeight.Bold) }
                )
                DropdownMenuItem(
                    onClick = {
                        onAction("delete")
                        expanded = false
                    },
                    text = { Text("Delete", color = themeColors.text.copy(alpha = 0.98f), fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}

//----------------------------------------------------
// ECRÃ PARA ADICIONAR NOTA (formulário simples)
//----------------------------------------------------

/**
 * Formulário para criar uma nova nota.
 * Valida campos obrigatórios e permite agendar reminder.
 */
@Composable
fun AddScreen(navController: NavController) {
    val context = LocalContext.current
    val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeName = themePrefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var setReminder by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf<Long?>(null) }
    var showError by remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "New Note",
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = themeColors.text,
            fontFamily = FontFamily.Default
        )

        Spacer(Modifier.height(20.dp))

        // Campo para o título
        TextField(
            value = title,
            onValueChange = {
                title = it
                showError = false
            },
            label = { Text("Title", color = themeColors.text) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = themeColors.text,
                fontSize = 16.sp
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = themeColors.noteBackground,
                focusedContainerColor = themeColors.noteBackground,
                focusedIndicatorColor = themeColors.action,
                unfocusedIndicatorColor = themeColors.noteBackground,
                focusedLabelColor = themeColors.text,
                unfocusedLabelColor = themeColors.text,
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Campo para o conteúdo
        TextField(
            value = subtitle,
            onValueChange = {
                subtitle = it
                showError = false
            },
            label = { Text("Content", color = themeColors.text) },
            textStyle = LocalTextStyle.current.copy(
                color = themeColors.text,
                fontSize = 16.sp
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = themeColors.noteBackground,
                focusedContainerColor = themeColors.noteBackground,
                focusedIndicatorColor = themeColors.action,
                unfocusedIndicatorColor = themeColors.noteBackground,
                focusedLabelColor = themeColors.text,
                unfocusedLabelColor = themeColors.text,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Switch para ativar/desativar reminder
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = setReminder,
                onCheckedChange = {
                    setReminder = it
                    if (!it) reminderDate = null
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = themeColors.action,
                    checkedTrackColor = themeColors.highlight
                )
            )
            Spacer(Modifier.width(8.dp))
            Text("Set reminder date", color = themeColors.text)
        }

        // Botão para escolher a data do reminder
        if (setReminder) {
            Button(
                onClick = { openDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.action)
            ) {
                Text(
                    text = reminderDate?.let { formatDate(it) } ?: "Pick date",
                    color = themeColors.text
                )
            }
            if (openDialog.value) {
                val cal = java.util.Calendar.getInstance()
                androidx.compose.runtime.LaunchedEffect(reminderDate) {
                    if (reminderDate != null) cal.timeInMillis = reminderDate!!
                }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val c = java.util.Calendar.getInstance()
                        c.set(year, month, dayOfMonth, 0, 0, 0)
                        reminderDate = c.timeInMillis
                        openDialog.value = false
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                ).apply {
                    setOnCancelListener { openDialog.value = false }
                }.show()
            }
        }

        Spacer(Modifier.height(16.dp))

        if (showError) {
            Text(
                text = "Please fill in both fields!",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botão para guardar nova nota
        Button(
            onClick = {
                if (title.isNotBlank() && subtitle.isNotBlank()) {
                    val notes = NotesStorage.loadNotes(context).toMutableList()
                    notes.add(
                        Notes(
                            title = title,
                            subtitle = subtitle,
                            createdAt = System.currentTimeMillis(),
                            reminderAt = if (setReminder) reminderDate else null
                        )
                    )
                    NotesStorage.saveNotes(context, notes)
                    navController.popBackStack()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.highlight)
        ) {
            Text("Save", color = themeColors.text)
        }
    }
}

// Não tem @Preview porque depende da navegação real.

//------------------------------------------
// ECRÃ PARA EDITAR NOTA JÁ EXISTENTE
//------------------------------------------

/**
 * Formulário para editar uma nota existente.
 * Carrega a nota pelo "createdAt". Se não existir, mostra erro.
 */
@Composable
fun EditScreen(
    navController: NavController,
    createdAt: Long
) {
    val context = LocalContext.current
    val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeName = themePrefs.getString("app_theme", "Serenity") ?: "Serenity"
    val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

    // Carrega as notas e encontra a nota a editar
    val notes = NotesStorage.loadNotes(context).toMutableList()
    val note = notes.firstOrNull { it.createdAt == createdAt }

    // Estados para o formulário
    var title by remember { mutableStateOf(note?.title ?: "") }
    var subtitle by remember { mutableStateOf(note?.subtitle ?: "") }
    var setReminder by remember { mutableStateOf(note?.reminderAt != null) }
    var reminderDate by remember { mutableStateOf<Long?>(note?.reminderAt) }
    var showError by remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }

    // Se a nota não for encontrada, mostra mensagem de erro e botão de voltar
    if (note == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Note not found!", color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.highlight)
            ) {
                Text("Back", color = themeColors.text)
            }
        }
        return
    }

    // Formulário de edição igual ao de adicionar, mas preenche dados iniciais
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Edit Note",
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = themeColors.text,
            fontFamily = FontFamily.Default
        )

        Spacer(Modifier.height(20.dp))

        TextField(
            value = title,
            onValueChange = {
                title = it
                showError = false
            },
            label = { Text("Title", color = themeColors.text) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = themeColors.text,
                fontSize = 16.sp
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = themeColors.noteBackground,
                focusedContainerColor = themeColors.noteBackground,
                focusedIndicatorColor = themeColors.action,
                unfocusedIndicatorColor = themeColors.noteBackground,
                focusedLabelColor = themeColors.text,
                unfocusedLabelColor = themeColors.text,
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        TextField(
            value = subtitle,
            onValueChange = {
                subtitle = it
                showError = false
            },
            label = { Text("Content", color = themeColors.text) },
            textStyle = LocalTextStyle.current.copy(
                color = themeColors.text,
                fontSize = 16.sp
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = themeColors.noteBackground,
                focusedContainerColor = themeColors.noteBackground,
                focusedIndicatorColor = themeColors.action,
                unfocusedIndicatorColor = themeColors.noteBackground,
                focusedLabelColor = themeColors.text,
                unfocusedLabelColor = themeColors.text,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = setReminder,
                onCheckedChange = {
                    setReminder = it
                    if (!it) reminderDate = null
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = themeColors.action,
                    checkedTrackColor = themeColors.highlight
                )
            )
            Spacer(Modifier.width(8.dp))
            Text("Set reminder date", color = themeColors.text)
        }

        if (setReminder) {
            Button(
                onClick = { openDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.action)
            ) {
                Text(
                    text = reminderDate?.let { formatDate(it) } ?: "Pick date",
                    color = themeColors.text
                )
            }
            if (openDialog.value) {
                val cal = java.util.Calendar.getInstance()
                if (reminderDate != null) cal.timeInMillis = reminderDate!!
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val c = java.util.Calendar.getInstance()
                        c.set(year, month, dayOfMonth, 0, 0, 0)
                        reminderDate = c.timeInMillis
                        openDialog.value = false
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                ).apply {
                    setOnCancelListener { openDialog.value = false }
                }.show()
            }
        }

        Spacer(Modifier.height(16.dp))

        if (showError) {
            Text(
                text = "Please fill in both fields!",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (title.isBlank() || subtitle.isBlank()) {
                        showError = true
                    } else {
                        // Atualiza nota na lista e guarda
                        val idx = notes.indexOfFirst { it.createdAt == createdAt }
                        if (idx != -1) {
                            val old = notes[idx]
                            notes[idx] = old.copy(
                                title = title,
                                subtitle = subtitle,
                                reminderAt = if (setReminder) reminderDate else null
                            )
                            NotesStorage.saveNotes(context, notes)
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.highlight)
            ) {
                Text("Save", color = themeColors.text)
            }
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = themeColors.action)
            }
        }
    }
}

//-------------------------------------
// FUNÇÕES UTILITÁRIAS E DATA CLASSES
//-------------------------------------

/**
 * Formata um timestamp para string "yyyy-MM-dd".
 */
fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

/**
 * Estrutura principal para armazenar uma nota.
 * @property title Título da nota.
 * @property subtitle Conteúdo.
 * @property createdAt Data de criação (milissegundos).
 * @property reminderAt Opcional: data do reminder (milissegundos).
 */
data class Notes(
    val title: String,
    val subtitle: String,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderAt: Long? = null
)

//------------------------------------------------
// PREVIEWS PARA REVISÃO VISUAL NO ANDROID STUDIO
//------------------------------------------------

@Preview(showBackground = true)
@Composable
fun NotesContentPreview() {
    NotesContent(
        data = Notes(
            title = "Título de Exemplo",
            subtitle = "Conteúdo da nota...",
            createdAt = System.currentTimeMillis(),
            reminderAt = System.currentTimeMillis() + 86400000
        ),
        themeColors = ThemesMap["Serenity"] ?: ThemesMap.values.first(),
        onAction = {},
        fontSizeSp = 16f,
        fontFamily = FontFamily.SansSerif
    )
}