package com.example.dashcam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dashcam.data.repository.UserRepository
import com.example.dashcam.data.repository.VideoRepository
import com.example.dashcam.ui.screen.*
import com.example.dashcam.ui.viewmodel.AuthViewModel
import com.example.dashcam.ui.viewmodel.CameraViewModel
import com.example.dashcam.ui.viewmodel.VideosViewModel
import com.example.dashcam.ui.viewmodel.ViewModelFactory
import com.example.dashcam.util.SessionManager

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Camera : Screen("camera")
    object VideosList : Screen("videos_list")
    object VideoPlayer : Screen("video_player/{videoId}") {
        fun createRoute(videoId: Long) = "video_player/$videoId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    userRepository: UserRepository,
    videoRepository: VideoRepository,
    sessionManager: SessionManager
) {
    val viewModelFactory = ViewModelFactory(userRepository, videoRepository, sessionManager)
    
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val cameraViewModel: CameraViewModel = viewModel(factory = viewModelFactory)
    val videosViewModel: VideosViewModel = viewModel(factory = viewModelFactory)
    
    val authState by authViewModel.authState.collectAsState()
    val cameraState by cameraViewModel.cameraState.collectAsState()
    val videosState by videosViewModel.videosState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                isLoggedIn = authState.isLoggedIn,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authState = authState,
                onLogin = { email, password ->
                    authViewModel.login(email, password)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onClearError = {
                    authViewModel.clearError()
                }
            )

            if (authState.isLoggedIn) {
                navController.navigate(Screen.Camera.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authState = authState,
                onRegister = { name, email, password ->
                    authViewModel.register(name, email, password)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onClearError = {
                    authViewModel.clearError()
                }
            )

            if (authState.isLoggedIn) {
                navController.navigate(Screen.Camera.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                cameraState = cameraState,
                viewModel = cameraViewModel,
                userName = authState.currentUser?.name ?: "Utilisateur",
                userId = authState.currentUser?.id ?: 0L,
                onNavigateToVideos = {
                    navController.navigate(Screen.VideosList.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.VideosList.route) {
            VideosListScreen(
                videosState = videosState,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVideoClick = { video ->
                    navController.navigate(Screen.VideoPlayer.createRoute(video.id))
                },
                onDeleteVideo = { video ->
                    videosViewModel.deleteVideo(video)
                }
            )
        }

        composable(
            route = Screen.VideoPlayer.route,
            arguments = listOf(
                navArgument("videoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            val video = videosState.videos.find { it.id == videoId }
            
            video?.let {
                VideoPlayerScreen(
                    video = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDeleteVideo = { videoToDelete ->
                        videosViewModel.deleteVideo(videoToDelete)
                    }
                )
            }
        }
    }
}
