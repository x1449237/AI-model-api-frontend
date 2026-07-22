package com.aifront.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aifront.data.model.VendorConfig
import com.aifront.ui.components.ModelTag
import com.aifront.ui.theme.*
import com.aifront.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorSelectScreen(
    chatViewModel: ChatViewModel,
    onVendorSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val vendors = VendorConfig.getAllVendors()
    var selectedTab by remember { mutableStateOf(0) }
    val domesticVendors = vendors.filter { it.country == "国内" }
    val foreignVendors = vendors.filter { it.country == "国外" }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("选择厂商", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
        )
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("国内厂商 (${domesticVendors.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("国外厂商 (${foreignVendors.size})") })
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val displayVendors = if (selectedTab == 0) domesticVendors else foreignVendors
            items(displayVendors) { vendor ->
                val modelCount = VendorConfig.getModelsByVendor(vendor.id).size
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onVendorSelected(vendor.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = if (vendor.country == "国内") Blue50 else Orange100) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.padding(12.dp), tint = if (vendor.country == "国内") Blue600 else Orange500)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(vendor.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(vendor.nameEn, fontSize = 13.sp, color = Gray500)
                            Text("${modelCount}个模型", fontSize = 12.sp, color = Gray400)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray400)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectScreen(
    vendorId: String,
    chatViewModel: ChatViewModel,
    onModelSelected: () -> Unit,
    onBack: () -> Unit
) {
    val vendor = VendorConfig.getVendorById(vendorId) ?: return
    val models = VendorConfig.getModelsByVendor(vendorId)
    val selectedModel by chatViewModel.selectedModel.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Column { Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(vendor.nameEn, fontSize = 12.sp, color = Gray500) } },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(models) { model ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { chatViewModel.selectModel(model); onModelSelected() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedModel?.id == model.id) Blue50 else White),
                    border = if (selectedModel?.id == model.id) CardDefaults.outlinedCardBorder() else null
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(model.displayName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            if (selectedModel?.id == model.id) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Blue600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(model.description, fontSize = 13.sp, color = Gray500)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (model.isLatest) ModelTag("最新", TagLatest)
                            if (model.isBestPerformance) ModelTag("性能最佳", TagBest)
                            if (model.supportsCode) ModelTag("支持代码", TagCode)
                            if (model.supportsImage) ModelTag("支持图像", TagImage)
                            if (model.contextLength > 0) ModelTag("${model.contextLength / 1000}K上下文", Gray600)
                        }
                    }
                }
            }
        }
    }
}