package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE UserActivities ADD COLUMN customerId INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2)
    }
}