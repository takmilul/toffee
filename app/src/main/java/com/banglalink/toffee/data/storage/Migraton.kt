package com.banglalink.toffee.data.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `channel_view_count` (`channel_id` INTEGER NOT NULL, `view_count` INTEGER NOT NULL, PRIMARY KEY(`channel_id`))")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_channel_view_count_channel_id` ON `channel_view_count` (`channel_id`)")
    }
}
