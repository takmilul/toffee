package com.banglalink.toffee.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationProvider {
    // migration sql query location(project view): data/build/generated/source/kapt/mobileDebug[main]/com.banglalink .toffee/data/database/ToffeeDatabase_Impl
    
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

    private val MIGRATION_3_4 = object: Migration(3,4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `DrmLicenseEntity` (`channelId` INTEGER NOT NULL, `contentId` TEXT NOT NULL, `data` BLOB NOT NULL, `expiryTime` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DrmLicenseEntity_channelId` ON `DrmLicenseEntity` (`channelId`)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `SessionPrefData` (`prefName` TEXT NOT NULL, `prefValue` TEXT, PRIMARY KEY(`prefName`))")
        }
    }
    
    private val MIGRATION_4_5 = object: Migration(4,5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `TVChannelItem` ADD COLUMN `isStingray` INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    private val MIGRATION_5_6 = object: Migration(5,6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `PlayerEventData` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` TEXT, `isInternetAvailable` INTEGER, `networkType` TEXT, `ispOrTelecomOperator` TEXT, `remoteHost` TEXT, `remoteIp` TEXT, `latencyClientToCdn` TEXT, `playerEventId` INTEGER, `playerEvent` TEXT, `contentId` TEXT, `contentTitle` TEXT, `contentProviderId` TEXT, `contentProviderName` TEXT, `contentCategoryId` INTEGER NOT NULL, `contentCategoryName` TEXT, `contentDuration` TEXT, `seasonName` TEXT, `seasonNo` INTEGER, `episodeName` TEXT, `episodeNo` TEXT, `contentType` TEXT, `isDrm` INTEGER, `playingUrl` TEXT, `affiliate` TEXT, `agent` TEXT, `errorMessage` TEXT, `errorCause` TEXT, `adId` TEXT, `adEvent` TEXT, `adPosition` TEXT, `adIsLive` TEXT, `adCreativeId` TEXT, `adFirstCreativeId` TEXT, `adFirstAdId` TEXT, `adFirstAdSystem` TEXT, `adSystem` TEXT, `adTechnology` TEXT, `adIsSlate` TEXT, `adErrorMessage` TEXT, `appVersion` TEXT NOT NULL, `osName` TEXT NOT NULL, `userId` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `deviceManufacturer` TEXT NOT NULL, `deviceModel` TEXT NOT NULL, `msisdn` TEXT NOT NULL, `osVersion` TEXT NOT NULL, `city` TEXT NOT NULL, `region` TEXT NOT NULL, `country` TEXT NOT NULL, `lat` TEXT NOT NULL, `lon` TEXT NOT NULL, `clientIp` TEXT NOT NULL, `deviceType` TEXT NOT NULL, `applicationType` TEXT NOT NULL, `dateTime` TEXT NOT NULL, `statusCode` INTEGER NOT NULL)")
        }
    }
    
    private val MIGRATION_6_7 = object: Migration(6,7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `PlayerEventData`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `PlayerEventData` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` TEXT, `isInternetAvailable` INTEGER, `networkType` TEXT, `ispOrTelecomOperator` TEXT, `remoteHost` TEXT, `remoteIp` TEXT, `latencyClientToCdn` TEXT, `playerEventId` INTEGER, `playerEvent` TEXT, `contentId` TEXT, `contentTitle` TEXT, `contentProviderId` TEXT, `contentProviderName` TEXT, `contentCategoryId` INTEGER, `contentCategoryName` TEXT, `contentDuration` TEXT, `seasonName` TEXT, `seasonNo` INTEGER, `episodeName` TEXT, `episodeNo` TEXT, `contentType` TEXT, `isDrm` INTEGER, `playingUrl` TEXT, `affiliate` TEXT, `agent` TEXT, `errorMessage` TEXT, `errorCause` TEXT, `adId` TEXT, `adEvent` TEXT, `adPosition` TEXT, `adIsLive` TEXT, `adCreativeId` TEXT, `adFirstCreativeId` TEXT, `adFirstAdId` TEXT, `adFirstAdSystem` TEXT, `adSystem` TEXT, `adTechnology` TEXT, `adIsSlate` TEXT, `adErrorMessage` TEXT, `appVersion` TEXT NOT NULL, `osName` TEXT NOT NULL, `userId` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `deviceManufacturer` TEXT NOT NULL, `deviceModel` TEXT NOT NULL, `msisdn` TEXT NOT NULL, `osVersion` TEXT NOT NULL, `city` TEXT NOT NULL, `region` TEXT NOT NULL, `country` TEXT NOT NULL, `lat` TEXT NOT NULL, `lon` TEXT NOT NULL, `clientIp` TEXT NOT NULL, `deviceType` TEXT NOT NULL, `applicationType` TEXT NOT NULL, `dateTime` TEXT NOT NULL, `statusCode` INTEGER NOT NULL)")
        }
    }
    
    private val MIGRATION_7_8 = object: Migration(7,8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `PlayerEventData`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `PlayerEventData` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` TEXT, `contentPlayingSessionId` TEXT, `contentPlayingSessionSequenceId` TEXT, `appLifeCycleId` TEXT, `isForeground` TEXT, `isInternetAvailable` INTEGER, `networkType` TEXT, `ispOrTelecomOperator` TEXT, `remoteHost` TEXT, `remoteIp` TEXT, `latencyClientToCdn` TEXT, `playerEventId` INTEGER, `playerEvent` TEXT, `contentId` TEXT, `contentTitle` TEXT, `contentProviderId` TEXT, `contentProviderName` TEXT, `contentCategoryId` INTEGER, `contentCategoryName` TEXT, `contentDuration` TEXT, `seasonName` TEXT, `seasonNo` INTEGER, `episodeName` TEXT, `episodeNo` TEXT, `contentType` TEXT, `isDrm` INTEGER, `playingUrl` TEXT, `affiliate` TEXT, `agent` TEXT, `errorMessage` TEXT, `errorCause` TEXT, `adId` TEXT, `adEvent` TEXT, `adPosition` TEXT, `adIsLive` TEXT, `adCreativeId` TEXT, `adFirstCreativeId` TEXT, `adFirstAdId` TEXT, `adFirstAdSystem` TEXT, `adSystem` TEXT, `adTechnology` TEXT, `adIsSlate` TEXT, `adErrorMessage` TEXT, `appVersion` TEXT NOT NULL, `osName` TEXT NOT NULL, `userId` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `deviceManufacturer` TEXT NOT NULL, `deviceModel` TEXT NOT NULL, `msisdn` TEXT NOT NULL, `osVersion` TEXT NOT NULL, `city` TEXT NOT NULL, `region` TEXT NOT NULL, `country` TEXT NOT NULL, `lat` TEXT NOT NULL, `lon` TEXT NOT NULL, `clientIp` TEXT NOT NULL, `deviceType` TEXT NOT NULL, `applicationType` TEXT NOT NULL, `statusCode` INTEGER NOT NULL, `dateTime` TEXT NOT NULL)")
        }
    }
    
    private val MIGRATION_8_9 = object: Migration(8,9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `CdnChannelItem` (`channelId` INTEGER NOT NULL, `urlType` INTEGER NOT NULL, `expiryDate` TEXT, `payload` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `createTime` INTEGER NOT NULL, `updateTime` INTEGER NOT NULL)")
        }
    }
    
    private val MIGRATION_9_10 = object: Migration(9,10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `BubbleConfig` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `isBubbleActive` INTEGER NOT NULL, `imageType` TEXT, `adIconUrl` TEXT, `bubbleText` TEXT, `adForwardUrl` TEXT, `isGlobalCountDownActive` INTEGER NOT NULL, `countDownEndTime` TEXT, `type` TEXT, `matchStartTime` TEXT, `venue` TEXT, `poweredBy` TEXT, `poweredByIconUrl` TEXT, `receiveTime` INTEGER NOT NULL, `homeScore` TEXT, `homeCountryName` TEXT, `homeCountryFlag` TEXT, `awayScore` TEXT, `awayCountryName` TEXT, `awayCountryFlag` TEXT)")
        }
    }
    
    private val MIGRATION_10_11 = object: Migration(10,11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `BubbleConfig`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `BubbleConfig` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `isBubbleActive` INTEGER NOT NULL, `imageType` TEXT, `adIconUrl` TEXT, `bubbleText` TEXT, `adForwardUrl` TEXT, `isGlobalCountDownActive` INTEGER NOT NULL, `countDownEndTime` TEXT, `type` TEXT, `matchStartTime` TEXT, `venue` TEXT, `poweredBy` TEXT, `poweredByIconUrl` TEXT, `receiveTime` INTEGER NOT NULL, `homeScore` TEXT, `homeCountryName` TEXT, `homeCountryFlag` TEXT, `awayScore` TEXT, `awayCountryName` TEXT, `awayCountryFlag` TEXT)")
        }
    }
    
    private val MIGRATION_11_12 = object: Migration(11,12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS `BubbleConfig`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `BubbleConfig` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `isBubbleActive` INTEGER NOT NULL, `imageType` TEXT, `adIconUrl` TEXT, `bubbleText` TEXT, `adForwardUrl` TEXT, `isGlobalCountDownActive` INTEGER NOT NULL, `countDownEndTime` TEXT, `type` TEXT, `matchStartTime` TEXT, `venue` TEXT, `poweredBy` TEXT, `poweredByIconUrl` TEXT, `receiveTime` INTEGER NOT NULL, `homeScore` TEXT, `homeCountryName` TEXT, `homeCountryFlag` TEXT, `awayScore` TEXT, `awayCountryName` TEXT, `awayCountryFlag` TEXT)")
        }
    }
    
    fun getMigrationList(): List<Migration> {
        return listOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, 
            MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
    }
}