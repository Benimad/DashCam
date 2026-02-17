package com.example.dashcam

import android.app.Application
import com.example.dashcam.data.database.AppDatabase

class DashCamApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
