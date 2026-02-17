package com.example.dashcam.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dashcam.data.dao.UserDao
import com.example.dashcam.data.dao.VideoDao
import com.example.dashcam.data.entity.User
import com.example.dashcam.data.entity.Video

@Database(
    entities = [User::class, Video::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE videos ADD COLUMN userId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX index_videos_timestamp ON videos(timestamp)")
                database.execSQL("CREATE INDEX index_videos_userId ON videos(userId)")
                database.execSQL("CREATE UNIQUE INDEX index_users_email ON users(email)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dashcam_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
