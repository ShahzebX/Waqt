package com.example.waqt.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.waqt.ui.navigation.WaqtNavGraph
import com.example.waqt.ui.theme.WaqtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaqtTheme {
                WaqtNavGraph()
            }
        }
    }
}
