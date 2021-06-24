package com.banglalink.toffee.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.banglalink.toffee.data.database.ToffeeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Qualifier

@Qualifier
annotation class InMemoryRoomDB

@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [DatabaseModule::class]
//)
@InstallIn(SingletonComponent::class)
object TestDatabaseModule {

    @Provides
    @InMemoryRoomDB
    fun providesDatabase(@ApplicationContext context: Context): RoomDatabase {
        return Room.inMemoryDatabaseBuilder(context, ToffeeDatabase::class.java)
            .build()
    }
}