package com.banglalink.toffee.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.dao.*
import com.banglalink.toffee.data.database.entities.*

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
        CdnChannelItem::class
    ],
    version = 9,
    exportSchema = false)

abstract class ToffeeDatabase: RoomDatabase() {
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
    abstract fun getReactionStatusDao(): ReactionStatusDao
    abstract fun getSubscriptionDao(): SubscriptionCountDao
    abstract fun getSubscriptionInfoDao(): SubscriptionInfoDao
    abstract fun getShareCountDao(): ShareCountDao
    abstract fun getDrmLicenseDao(): DrmLicenseDao
    abstract fun getSessionPrefDao(): SessionPrefDao
    abstract fun getCustomPlayerEventsDao(): PlayerEventsDao
    abstract fun getCdnChannelItemDao(): CdnChannelItemDao
    
    companion object {
        const val DB_NAME = "toffee-db"
    }
}