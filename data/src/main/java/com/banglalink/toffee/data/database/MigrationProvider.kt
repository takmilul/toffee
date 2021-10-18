package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `SubscriptionInfo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `channelId` INTEGER NOT NULL, `customerId` INTEGER NOT NULL, `dateTime` INTEGER NOT NULL)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `SubscriptionCount` (`channelId` INTEGER NOT NULL, `status` INTEGER NOT NULL, PRIMARY KEY(`channelId`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ShareCount` (`contentId` INTEGER NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`contentId`))")
        }
    }
    
    private val MIGRATION_2_3 = object : Migration(2,3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `reaction_status_item`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `ReactionStatusItem` (`contentId` INTEGER NOT NULL, `reactionType` INTEGER NOT NULL, `reactionCount` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ReactionStatusItem_contentId_reactionType` ON `ReactionStatusItem` (`contentId`, `reactionType`)")
        }
    }

    private val MIGRAION_3_4 = object: Migration(3,4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `DrmLicenseEntity` (`channelId` INTEGER NOT NULL, `contentId` TEXT NOT NULL, `data` BLOB NOT NULL, `expiryTime` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DrmLicenseEntity_channelId` ON `DrmLicenseEntity` (`channelId`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `SessionPrefData` (`prefName` TEXT NOT NULL, `prefValue` TEXT, PRIMARY KEY(`prefName`))")
        }
    }
    
    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2, MIGRATION_2_3, MIGRAION_3_4)
    }
}