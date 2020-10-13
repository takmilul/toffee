package com.banglalink.toffee.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.storage.ViewCountDAO
import com.banglalink.toffee.data.storage.ViewCountDataModel

@Database(
    entities = [
        UploadInfo::class,
        ViewCountDataModel::class,
        NotificationInfo::class
    ],
    version = 1,
    exportSchema = false)
abstract class ToffeeDatabase: RoomDatabase() {
    abstract fun getUploadDao(): UploadDao
    abstract fun getViewCountDao(): ViewCountDAO
    abstract fun getNotificationDao(): NotificationDao

    companion object {
        const val DB_NAME = "toffee-db"
    }
}