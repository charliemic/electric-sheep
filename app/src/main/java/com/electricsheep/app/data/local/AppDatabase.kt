package com.electricsheep.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.electricsheep.app.data.model.Mood

/**
 * Room database for local offline storage.
 * Handles migrations and provides access to DAOs.
 */
@Database(
    entities = [Mood::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
}

