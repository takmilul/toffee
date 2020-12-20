package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    fun getMigrationList(): List<Migration> {
        return listOf()
    }
}