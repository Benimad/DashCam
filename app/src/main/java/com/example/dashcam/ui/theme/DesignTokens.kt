package com.example.dashcam.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

object DashCamAnimations {
    const val DURATION_FAST = 150
    const val DURATION_STANDARD = 300
    const val DURATION_MODERATE = 500
    const val DURATION_SLOW = 800
    const val DURATION_EMPHASIS = 1000
    
    val standardTween = tween<Float>(
        durationMillis = DURATION_STANDARD,
        easing = LinearEasing
    )
    
    val fastTween = tween<Float>(
        durationMillis = DURATION_FAST,
        easing = LinearEasing
    )
    
    val moderateTween = tween<Float>(
        durationMillis = DURATION_MODERATE,
        easing = LinearEasing
    )
}

object DashCamOpacity {
    const val DISABLED = 0.38f
    const val INACTIVE = 0.6f
    const val SECONDARY = 0.7f
    const val OVERLAY_LIGHT = 0.6f
    const val OVERLAY_MEDIUM = 0.7f
    const val OVERLAY_HEAVY = 0.8f
}

object DashCamConstants {
    const val RECORDING_BLINK_DURATION = 800
    const val CONTROL_HIDE_DELAY = 4000L
    const val ERROR_DISMISS_DELAY = 4000L
    const val TOAST_DURATION_SHORT = 2000
    const val TOAST_DURATION_LONG = 3500
    
    val VIDEO_QUALITY_HD = Pair(1280, 720)
    val VIDEO_QUALITY_FULL_HD = Pair(1920, 1080)
    
    const val PASSWORD_MIN_LENGTH = 6
    
    const val GRADIENT_TOP_HEIGHT = 300f
    const val GRADIENT_BOTTOM_HEIGHT = 400f
}

object DashCamAccessibility {
    const val MIN_TOUCH_TARGET_DP = 48
    const val RECOMMENDED_TOUCH_TARGET_DP = 56
    const val LARGE_TOUCH_TARGET_DP = 64
    
    const val CONTRAST_RATIO_NORMAL_TEXT = 4.5f
    const val CONTRAST_RATIO_LARGE_TEXT = 3.0f
    const val CONTRAST_RATIO_CRITICAL = 7.0f
    
    const val MIN_TEXT_SIZE_SP = 14
    const val RECOMMENDED_TEXT_SIZE_SP = 16
}
