package com.aifront.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "对话", Icons.Default.Chat)
    object CodeGen : Screen("code_gen", "代码", Icons.Default.Code)
    object ImageGen : Screen("image_gen", "图片", Icons.Default.Image)
    object Cluster : Screen("cluster", "集群", Icons.Default.Compare)
    object History : Screen("history", "历史", Icons.Default.History)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
    object VendorSelect : Screen("vendor_select", "选择厂商", Icons.Default.Business)
    object ModelSelect : Screen("model_select/{vendorId}", "选择模型", Icons.Default.Psychology)

    companion object {
        val bottomNavItems = listOf(Home, CodeGen, ImageGen, Cluster, History)
    }
}