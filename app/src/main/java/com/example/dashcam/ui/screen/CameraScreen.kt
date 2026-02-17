package com.example.dashcam.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.dashcam.ui.viewmodel.CameraState
import com.example.dashcam.ui.viewmodel.CameraViewModel
import com.example.dashcam.util.FormatUtils
import kotlinx.coroutines.delay
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    cameraState: CameraState,
    viewModel: CameraViewModel,
    userName: String,
    userId: Long,
    onNavigateToVideos: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showExitDialog by remember { mutableStateOf(false) }
    
    BackHandler {
        showExitDialog = true
    }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionsDeniedPermanently by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
        
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionsDeniedPermanently = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    LaunchedEffect(cameraState.isRecording) {
        while (cameraState.isRecording) {
            viewModel.updateRecordingDuration()
            delay(1000)
        }
    }

    LaunchedEffect(cameraState.savedVideoPath) {
        cameraState.savedVideoPath?.let {
            Toast.makeText(context, "Vidéo enregistrée avec succès", Toast.LENGTH_SHORT).show()
            viewModel.clearSavedPath()
        }
    }
    
    LaunchedEffect(cameraState.errorMessage) {
        cameraState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && hasAudioPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                isRecording = cameraState.isRecording,
                userId = userId
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (cameraState.isRecording) {
                        RecordingIndicator(duration = cameraState.recordingDuration)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prêt",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashcamFloatingButton(
                            icon = Icons.Default.VideoLibrary,
                            contentDescription = "Galerie",
                            onClick = onNavigateToVideos
                        )
                        DashcamFloatingButton(
                            icon = Icons.Default.ExitToApp,
                            contentDescription = "Déconnexion",
                            onClick = onLogout,
                            backgroundColor = Color.White.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 0f,
                            endY = 400f
                        )
                    )
                    .padding(bottom = 40.dp, top = 120.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (!cameraState.isRecording) {
                        Text(
                            text = "Appuyez pour enregistrer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DashcamRecordButton(
                        isRecording = cameraState.isRecording,
                        onClick = {
                            if (cameraState.isRecording) {
                                viewModel.stopRecording()
                            } else {
                                viewModel.startRecording(context)
                            }
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Permissions requises",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "L'application a besoin d'accéder à votre caméra et microphone pour enregistrer des vidéos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        if (permissionsDeniedPermanently) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(
                                    text = "Ouvrir les paramètres",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Veuillez activer les permissions dans les paramètres de l'application",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(
                                    text = "Accorder les permissions",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Quitter l'application",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("Êtes-vous sûr de vouloir quitter l'application ?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            (context as? android.app.Activity)?.finish()
                        }
                    ) {
                        Text("Quitter")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun RecordingIndicator(duration: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Row(
        modifier = Modifier
            .background(
                Color.Black.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .alpha(alpha)
                .background(
                    Color(0xFFFF1744),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = FormatUtils.formatDuration(duration),
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DashcamFloatingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f)
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun DashcamRecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    val buttonSize = 100.dp
    val innerSize = if (isRecording) 40.dp else 80.dp
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier.size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer {
                    scaleX = if (isRecording) scale else 1f
                    scaleY = if (isRecording) scale else 1f
                },
            onClick = onClick,
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(
                width = 5.dp,
                color = if (isRecording) Color(0xFFFF1744) else Color.White
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(innerSize),
                    shape = if (isRecording) 
                        RoundedCornerShape(8.dp) 
                    else 
                        CircleShape,
                    color = if (isRecording) Color(0xFFFF1744) else Color.White
                ) {}
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel,
    isRecording: Boolean,
    userId: Long
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val qualitySelector = QualitySelector.from(Quality.HD)
                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                val videoCapture = VideoCapture.withOutput(recorder)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )

                    previewView.tag = videoCapture
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            
            previewView
        },
        modifier = modifier,
        update = { previewView ->
            if (isRecording && viewModel.cameraState.value.recordingDuration == 0L) {
                val videoCapture = previewView.tag as? VideoCapture<Recorder>
                videoCapture?.let { vc ->
                    val outputFile = viewModel.getOutputFile(context)
                    val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()

                    val recording = vc.output
                        .prepareRecording(context, fileOutputOptions)
                        .withAudioEnabled()
                        .start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Finalize -> {
                                    if (event.hasError()) {
                                        Toast.makeText(
                                            context,
                                            "Erreur d'enregistrement: ${event.error}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.saveVideoToDatabase(outputFile, userId)
                                    }
                                }
                            }
                        }
                    viewModel.setRecording(recording)
                }
            }
        }
    )
}
