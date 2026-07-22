package com.aifront

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aifront.ui.navigation.AppNavGraph
import com.aifront.ui.theme.AiFrontTheme
import com.aifront.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiFrontTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    color = White
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}