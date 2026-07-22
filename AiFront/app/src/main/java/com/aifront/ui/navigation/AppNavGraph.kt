package com.aifront.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.aifront.ui.screens.cluster.ClusterScreen
import com.aifront.ui.screens.codegen.CodeGenScreen
import com.aifront.ui.screens.history.HistoryScreen
import com.aifront.ui.screens.home.*
import com.aifront.ui.screens.imagegen.ImageGenScreen
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel
import com.aifront.viewmodel.SettingsViewModel

@Composable
fun AppNavGraph(
    chatViewModel: ChatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {
                HomeScreen(chatViewModel = chatViewModel, settingsViewModel = settingsViewModel,
                    onNavigateToVendor = { navController.navigate(Screen.VendorSelect.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.CodeGen.route) {
                CodeGenScreen(chatViewModel = chatViewModel, settingsViewModel = settingsViewModel,
                    onNavigateToVendor = { navController.navigate(Screen.VendorSelect.route) })
            }
            composable(Screen.ImageGen.route) {
                ImageGenScreen(chatViewModel = chatViewModel, settingsViewModel = settingsViewModel,
                    onNavigateToImageModel = { navController.navigate("model_select/image") })
            }
            composable(Screen.Cluster.route) {
                ClusterScreen(chatViewModel = chatViewModel, settingsViewModel = settingsViewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(chatViewModel = chatViewModel,
                    onConversationClick = { id -> chatViewModel.loadConversation(id); navController.navigate(Screen.Home.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true } },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel = settingsViewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.VendorSelect.route) {
                VendorSelectScreen(chatViewModel = chatViewModel,
                    onVendorSelected = { vendorId -> navController.navigate("model_select/$vendorId") },
                    onBack = { navController.popBackStack() })
            }
            composable("model_select/{vendorId}", arguments = listOf(navArgument("vendorId") { type = NavType.StringType })) { backStackEntry ->
                val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
                ModelSelectScreen(vendorId = vendorId, chatViewModel = chatViewModel,
                    onModelSelected = { navController.navigate(Screen.Home.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true } },
                    onBack = { navController.popBackStack() })
            }
            composable("model_select/image") {
                ImageModelSelectContent(chatViewModel = chatViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageModelSelectContent(chatViewModel: ChatViewModel, onBack: () -> Unit) {
    val imageModels = com.aifront.data.model.VendorConfig.getImageModels()
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("选择图片模型") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = White))
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(imageModels.size) { index ->
                val model = imageModels[index]
                val vendor = com.aifront.data.model.VendorConfig.getVendorById(model.vendorId)
                Card(modifier = Modifier.fillMaxWidth().clickable { chatViewModel.selectModel(model); onBack() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (selectedModel?.id == model.id) Blue50 else White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("${vendor?.name} - ${model.displayName}", fontWeight = FontWeight.Medium); Text(model.description, fontSize = 12.sp, color = Gray500) }
                        if (selectedModel?.id == model.id) Icon(Icons.Default.CheckCircle, null, tint = Blue600)
                    }
                }
            }
        }
    }
}