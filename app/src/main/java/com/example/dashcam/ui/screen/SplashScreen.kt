package com.example.dashcam.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dashcam.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToCamera: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        if (isLoggedIn) {
            onNavigateToCamera()
        } else {
            onNavigateToLogin()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(48.dp)
        ) {
            CarDashCamLogo()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "CarDashCam",
                style = MaterialTheme.typography.displayMedium,
                color = White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(DeepBlueLight)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "VOTRE SÉCURITÉ SUR LA ROUTE",
                style = MaterialTheme.typography.labelLarge,
                color = LightGrey,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(pulseAlpha),
                color = DeepBlueLight,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun CarDashCamLogo() {
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.size(160.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = DarkGrey
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .offset(y = 8.dp),
                    tint = LightGrey.copy(alpha = 0.3f)
                )
                
                Card(
                    modifier = Modifier
                        .size(70.dp)
                        .offset(x = 0.dp, y = (-12).dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = DeepBlueLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = White
                        )
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 90.dp, y = (-90).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(RecordingRed, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
