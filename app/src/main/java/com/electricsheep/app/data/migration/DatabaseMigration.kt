package com.electricsheep.app.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.electricsheep.app.util.Logger

/**
 * Database migration manager.
 * All migrations are forward-only (never rollback).
 * Migrations are applied sequentially based on database version.
 */
object DatabaseMigrations {
    
    /**
     * Get all migrations for the database.
     * Add new migrations here as the database schema evolves.
     * 
     * Migration naming: MIGRATION_<from_version>_<to_version>
     */
    fun getMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2 // Add userId column to moods table
            // Migration from version 2 to 3
            // MIGRATION_2_3,
            // Add future migrations here
        )
    }
    
    /**
     * Migration from version 1 to 2: Add userId column to moods table
     * This migration adds user scoping to existing mood data.
     * For existing data, we'll set userId to a placeholder value.
     * In production, you may want to handle this differently (e.g., prompt user to sign in).
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Logger.info("DatabaseMigration", "Applying migration 1->2: Adding userId column")
            try {
                // Room guarantees this migration runs exactly once when upgrading from version 1 to 2
                // No need to check if column exists - Room's migration system handles this
                
                // SQLite doesn't support adding NOT NULL column with DEFAULT in one step
                // Step 1: Add column as nullable
                database.execSQL("ALTER TABLE moods ADD COLUMN userId TEXT")
                
                // Step 2: Set placeholder value for existing rows
                database.execSQL("UPDATE moods SET userId = 'placeholder_user' WHERE userId IS NULL")
                
                // Step 3: SQLite doesn't support changing column constraints directly
                // We keep it nullable and handle validation in application code
                // In production, you might want to recreate the table with NOT NULL constraint
                
                // Step 4: Create index on userId for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_moods_userId ON moods(userId)")
                
                Logger.info("DatabaseMigration", "Migration 1->2 completed successfully")
            } catch (e: Exception) {
                Logger.error("DatabaseMigration", "Migration 1->2 failed", e)
                throw e
            }
        }
    }
}

/**
 * Migration validation and safety checks
 */
object MigrationValidator {
    /**
     * Validate that migration is forward-only
     */
    fun validateMigration(fromVersion: Int, toVersion: Int) {
        require(toVersion > fromVersion) {
            "Migrations must be forward-only. Cannot migrate from $fromVersion to $toVersion"
        }
    }
    
    /**
     * Validate migration sequence is complete
     */
    fun validateMigrationSequence(currentVersion: Int, targetVersion: Int, migrations: List<Migration>) {
        val migrationVersions = migrations.map { it.startVersion to it.endVersion }.sortedBy { it.first }
        
        // Check for gaps in migration sequence
        var expectedVersion = currentVersion
        for ((from, to) in migrationVersions) {
            require(from == expectedVersion) {
                "Migration sequence has gap. Expected migration from $expectedVersion, found from $from"
            }
            expectedVersion = to
        }
        
        require(expectedVersion == targetVersion) {
            "Migration sequence incomplete. Last migration goes to $expectedVersion, but target is $targetVersion"
        }
    }
}

