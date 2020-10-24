package com.banglalink.toffee.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.dao.*
import com.banglalink.toffee.data.database.entities.*
import com.banglalink.toffee.data.storage.ViewCountDAO
import com.banglalink.toffee.data.storage.ViewCountDataModel

@Database(
    entities = [
        UploadInfo::class,
        ViewCountDataModel::class,
        NotificationInfo::class,
        ReactionInfo::class,
        HistoryItem::class,
        UserActivities::class,
    ],
    version = 1,
    exportSchema = false)
abstract class ToffeeDatabase: RoomDatabase() {
    abstract fun getUploadDao(): UploadDao
    abstract fun getViewCountDao(): ViewCountDAO
    abstract fun getNotificationDao(): NotificationDao
    abstract fun getReactionDao(): ReactionDao
    abstract fun getHistoryItemDao(): HistoryItemDao
    abstract fun getUserActivitiesDao(): UserActivitiesDao

    companion object {
        const val DB_NAME = "toffee-db"
    }
}