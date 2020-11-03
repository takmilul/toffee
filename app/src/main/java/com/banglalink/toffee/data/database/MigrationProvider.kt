package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            
        }
    }

    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2)
    }
}