package com.banglalink.toffee.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.entities.UploadInfo

@Database(entities = [UploadInfo::class], version = 1, exportSchema = false)
abstract class ToffeeDatabase: RoomDatabase() {
    abstract fun getUploadDao(): UploadDao

    companion object {
        const val DB_NAME = "toffee-db"
    }
}