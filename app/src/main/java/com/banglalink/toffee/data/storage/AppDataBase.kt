package com.banglalink.toffee.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChannelDataModel::class,ViewCountDataModel::class], version = 2)
abstract class AppDatabase: RoomDatabase() {

    abstract fun channelDAO(): ChannelDAO
    abstract fun viewCountDAO(): ViewCountDAO

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME: String = "toffee_database"

        fun getDatabase(): AppDatabase {
            return INSTANCE!!
        }


        fun init(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }
    }
}