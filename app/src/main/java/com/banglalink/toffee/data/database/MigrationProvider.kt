package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `SubscriptionInfo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `channelId` INTEGER NOT NULL, `customerId` INTEGER NOT NULL, `dateTime` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `SubscriptionCount` (`channelId` INTEGER NOT NULL, `status` INTEGER NOT NULL, PRIMARY KEY(`channelId`))")
        }
    }
    
    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2)
    }
}