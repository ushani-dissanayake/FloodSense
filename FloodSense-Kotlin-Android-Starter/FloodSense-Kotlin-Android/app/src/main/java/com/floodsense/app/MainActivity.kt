package com.floodsense.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.floodsense.app.ui.FloodSenseApp
import com.floodsense.app.ui.FloodSenseViewModel
import com.floodsense.app.ui.theme.FloodSenseTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        setContent {
            val floodSenseViewModel: FloodSenseViewModel = viewModel()
            val themeMode by floodSenseViewModel.themeMode.collectAsState()

            FloodSenseTheme(themeMode = themeMode) {
                FloodSenseApp(viewModel = floodSenseViewModel)
            }
        }
    }
}
