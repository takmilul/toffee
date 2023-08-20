package com.banglalink.toffee.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.dao.BubbleConfigDao
import com.banglalink.toffee.data.database.dao.CdnChannelItemDao
import com.banglalink.toffee.data.database.dao.ContentViewProgressDao
import com.banglalink.toffee.data.database.dao.ContinueWatchingDao
import com.banglalink.toffee.data.database.dao.DrmLicenseDao
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.HistoryItemDao
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.dao.PlayerEventsDao
import com.banglalink.toffee.data.database.dao.ReactionCountDao
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.dao.SessionPrefDao
import com.banglalink.toffee.data.database.dao.ShareCountDao
import com.banglalink.toffee.data.database.dao.SubscribedItemDao
import com.banglalink.toffee.data.database.dao.SubscriptionCountDao
import com.banglalink.toffee.data.database.dao.SubscriptionInfoDao
import com.banglalink.toffee.data.database.dao.TVChannelDao
import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.dao.UserActivitiesDao
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.database.entities.CdnChannelItem
import com.banglalink.toffee.data.database.entities.ContentViewProgress
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.database.entities.FavoriteItem
import com.banglalink.toffee.data.database.entities.HistoryItem
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.database.entities.PlayerEventData
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.database.entities.SessionPrefData
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.database.entities.SubscribedItem
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.database.entities.ViewCount
import com.banglalink.toffee.model.BubbleConfig

@Database(
    entities = [
        UploadInfo::class,
        ViewCount::class,
        NotificationInfo::class,
        ReactionInfo::class,
        HistoryItem::class,
        UserActivities::class,
        FavoriteItem::class,
        SubscribedItem::class,
        TVChannelItem::class,
        ContentViewProgress::class,
        ContinueWatchingItem::class,
        ReactionStatusItem::class,
        SubscriptionCount::class,
        SubscriptionInfo::class,
        ShareCount::class,
        DrmLicenseEntity::class,
        SessionPrefData::class,
        PlayerEventData::class,
        CdnChannelItem::class,
        BubbleConfig::class,
//        PremiumPackItem::class
    ],
    version = 16,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 15,
            to = 16,
//            spec = ToffeeDatabase.ToffeeMigrationSpec::class //This spec only needs in case of RenameColumn, RenameTable, DeleteColumn and DeleteTable. Otherwise room will automatically migrate database, no need to add specs here.
        )
    ]
)

abstract class ToffeeDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "toffee-db"
    }
    
//    @RenameColumn.Entries(
//        RenameColumn(tableName = "DrmLicenseEntity", fromColumnName = "channel_id", toColumnName = "channelId"),
//        RenameColumn(tableName = "DrmLicenseEntity", fromColumnName = "channel_name", toColumnName = "channelName"),
//    )
//    @DeleteTable(tableName = "PremiumPackItem")
//    class ToffeeMigrationSpec : AutoMigrationSpec
    
    abstract fun getUploadDao(): UploadDao
    abstract fun getViewCountDao(): ViewCountDAO
    abstract fun getNotificationDao(): NotificationDao
    abstract fun getReactionDao(): ReactionDao
    abstract fun getHistoryItemDao(): HistoryItemDao
    abstract fun getUserActivitiesDao(): UserActivitiesDao
    abstract fun getFavoriteItemsDao(): FavoriteItemDao
    abstract fun getSubscribedItemsDao(): SubscribedItemDao
    abstract fun getTVChannelsDao(): TVChannelDao
    abstract fun getContentViewProgressDao(): ContentViewProgressDao
    abstract fun getContinueWatchingDao(): ContinueWatchingDao
    abstract fun getReactionCountDao(): ReactionCountDao
    abstract fun getSubscriptionDao(): SubscriptionCountDao
    abstract fun getSubscriptionInfoDao(): SubscriptionInfoDao
    abstract fun getShareCountDao(): ShareCountDao
    abstract fun getDrmLicenseDao(): DrmLicenseDao
    abstract fun getSessionPrefDao(): SessionPrefDao
    abstract fun getCustomPlayerEventsDao(): PlayerEventsDao
    abstract fun getCdnChannelItemDao(): CdnChannelItemDao
    abstract fun getBubbleConfigDao(): BubbleConfigDao
//    abstract fun getPremiumPackDao(): PremiumPackDao
}