package com.example.stickynotes

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument

// --- Definição das rotas e ícones para o menu lateral (Drawer) ---
sealed class DrawerScreen(val route: String, val title: String, val icon: Int? = null) {
    object Home : DrawerScreen("home", "Home", R.drawable.baseline_home_24)
    object Search : DrawerScreen("search", "Search", R.drawable.baseline_search_24)
    object Theme : DrawerScreen("theme", "Theme", R.drawable.baseline_palette_24)
    object Settings : DrawerScreen("settings", "Settings", R.drawable.baseline_settings_24)
    object FAQs : DrawerScreen("faqs", "FAQs", R.drawable.baseline_contact_support_24)
    object Exit : DrawerScreen("exit", "Exit", R.drawable.baseline_exit_to_app_24)
}

// --- Activity principal da app StickyNotes ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A UI da aplicação é definida aqui
        setContent {
            // Contexto atual da app (usado para preferências, toast, etc)
            val context = LocalContext.current

            // Lê das SharedPreferences o tema escolhido pelo utilizador
            val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            var themeName by remember { mutableStateOf("Serenity") }
            LaunchedEffect(Unit) {
                themeName = prefs.getString("app_theme", "Serenity") ?: "Serenity"
            }

            // Vai buscar as cores do tema atual (ThemesMap é definido noutro ficheiro)
            val themeColors = ThemesMap[themeName] ?: ThemesMap.values.first()

            // Aplica o tema personalizado a toda a UI da app
            StickyNotesTheme(themeColors) {
                // Estado para o Drawer (menu lateral aberto/fechado)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                // Controlador de navegação entre ecrãs
                val navController = rememberNavController()
                // CoroutineScope para ações assíncronas (fechar drawer, delays, etc)
                val scope = rememberCoroutineScope()

                // Rota selecionada no Drawer
                var selectedRoute by remember { mutableStateOf(DrawerScreen.Home.route) }
                // Todas as opções do menu lateral, em lista
                val drawerScreens = listOf(
                    DrawerScreen.Home,
                    DrawerScreen.Search,
                    DrawerScreen.Theme,
                    DrawerScreen.Settings,
                    DrawerScreen.FAQs,
                    DrawerScreen.Exit
                )

                // Estado para confirmar saída da app (duplo clique em "Exit")
                var exitPending by remember { mutableStateOf(false) }

                // Cálculo da largura do Drawer, em função do ecrã do dispositivo
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                val drawerWidth = screenWidth * 0.6f

                // Obtém a rota atual, para atualizar o título da toolbar dinamicamente
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val toolbarTitle = getScreenTitle(currentRoute)

                // --- Estrutura do Drawer Modal ---
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Box(
                            modifier = Modifier
                                .width(drawerWidth)
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                            ) {
                                // Header do Drawer: nome da app
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                                // Linha separadora
                                Divider(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                    thickness = 1.dp
                                )
                                // Lista dos itens do menu lateral (scrolável)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.background)
                                        .verticalScroll(rememberScrollState())
                                        .padding(top = 8.dp, bottom = 12.dp)
                                ) {
                                    // Geração dinâmica dos itens do Drawer
                                    drawerScreens.forEach { screen ->
                                        NavigationDrawerItem(
                                            label = { Text(screen.title) },
                                            selected = selectedRoute == screen.route,
                                            onClick = {
                                                // Se for "Exit", exige duplo clique para fechar app
                                                if (screen.route == DrawerScreen.Exit.route) {
                                                    if (exitPending) {
                                                        finishAffinity()
                                                    } else {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Click again to confirm",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        exitPending = true
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(2000)
                                                            exitPending = false
                                                        }
                                                    }
                                                } else {
                                                    // Qualquer outra opção: navega para o ecrã correspondente
                                                    selectedRoute = screen.route
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                                        launchSingleTop = true
                                                    }
                                                    // Fecha o Drawer após navegação
                                                    scope.launch { drawerState.close() }
                                                    exitPending = false
                                                }
                                            },
                                            icon = {
                                                screen.icon?.let {
                                                    Icon(
                                                        painter = painterResource(id = it),
                                                        contentDescription = screen.title
                                                    )
                                                }
                                            },
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                                            ),
                                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    // --- Estrutura do topo da app (AppBar e navegação principal) ---
                    @OptIn(ExperimentalMaterial3Api::class)
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        toolbarTitle,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                                    }
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        // --- Navegação entre ecrãs principais da app ---
                        NavHost(
                            navController = navController,
                            startDestination = DrawerScreen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // Home: ecrã principal das notas
                            composable(DrawerScreen.Home.route) {
                                HomeScreen(navController)
                            }
                            // Search: pesquisa de notas
                            composable(DrawerScreen.Search.route) {
                                SearchScreen()
                            }
                            // Theme: seleção de tema
                            composable(DrawerScreen.Theme.route) {
                                ThemeScreen()
                            }
                            // Settings: definições
                            composable(DrawerScreen.Settings.route) {
                                SettingsScreen()
                            }
                            // FAQs: perguntas frequentes
                            composable(DrawerScreen.FAQs.route) {
                                FAQsScreen()
                            }
                            // Adicionar nova nota
                            composable("add") {
                                AddScreen(navController)
                            }
                            // Editar nota existente (passa parâmetro criado em "createdAt")
                            composable(
                                "edit/{createdAt}",
                                arguments = listOf(navArgument("createdAt") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val createdAt = backStackEntry.arguments?.getLong("createdAt") ?: 0L
                                EditScreen(navController, createdAt)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Função auxiliar que devolve o título do topo conforme a rota atual
fun getScreenTitle(route: String?): String {
    return when (route) {
        DrawerScreen.Home.route -> "My Notes"
        DrawerScreen.Search.route -> "Search"
        DrawerScreen.Theme.route -> "Theme"
        DrawerScreen.Settings.route -> "Settings"
        DrawerScreen.FAQs.route -> "FAQs"
        DrawerScreen.Exit.route -> "Exit"
        else -> ""
    }
}
