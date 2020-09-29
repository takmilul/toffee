package com.banglalink.toffee.data.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE `channel_view_count` (`channel_id` INTEGER,`view_count` INTEGER, " +
                "PRIMARY KEY(`channel_id`))")
        database.execSQL("CREATE UNIQUE INDEX index_channel_view_count_channel_id ON channel_view_count(channel_id)");
    }
}
