package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE UserActivities ADD COLUMN customerId INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `TVChannelItem` (`channelId` INTEGER NOT NULL, `type` TEXT NOT NULL, `priority` INTEGER NOT NULL, `categoryName` TEXT NOT NULL, `payload` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ContentViewProgress` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `customerId` INTEGER NOT NULL, `contentId` INTEGER NOT NULL, `progress` INTEGER NOT NULL, `watchTime` INTEGER NOT NULL)")
        }
    }

    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
    }
}