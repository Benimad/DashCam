package com.example.dashcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.dashcam.data.repository.UserRepository
import com.example.dashcam.data.repository.VideoRepository
import com.example.dashcam.ui.navigation.NavGraph
import com.example.dashcam.ui.theme.DashCamTheme
import com.example.dashcam.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = (application as DashCamApplication).database
        val userRepository = UserRepository(database.userDao())
        val videoRepository = VideoRepository(database.videoDao())
        val sessionManager = SessionManager.getInstance(this)
        
        setContent {
            DashCamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        userRepository = userRepository,
                        videoRepository = videoRepository,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}