package com.example.ridehmidashboard

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridehmidashboard.ui.DashboardScreen
import com.example.ridehmidashboard.ui.DashboardViewModel
import com.example.ridehmidashboard.ui.theme.RideHmiDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // A dashboard should not dim or lock while riding
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RideHmiDashboardTheme {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                DashboardScreen(viewModel = dashboardViewModel)
            }
        }
    }
}
