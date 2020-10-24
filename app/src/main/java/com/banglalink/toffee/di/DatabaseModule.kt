package com.banglalink.toffee.di

import android.content.Context
import androidx.room.Room
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.*
import com.banglalink.toffee.data.repository.HistoryRepository
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.repository.impl.HistoryRepositoryImpl
import com.banglalink.toffee.data.repository.impl.NotificationInfoRepositoryImpl
import com.banglalink.toffee.data.repository.impl.UploadInfoRepositoryImpl
import com.banglalink.toffee.data.repository.impl.UserActivitiesRepositoryImpl
import com.banglalink.toffee.data.storage.ViewCountDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext app: Context): ToffeeDatabase {
        return Room.databaseBuilder(app,
            ToffeeDatabase::class.java, ToffeeDatabase.DB_NAME)
//            .addMigrations(ToffeeDatabase.MIGRATION_1_2, ToffeeDatabase.MIGRATION_2_3) // TODO: Add migration provider
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesUploadDao(db: ToffeeDatabase): UploadDao {
        return db.getUploadDao()
    }

    @Provides
    @Singleton
    fun providesViewCountDao(db: ToffeeDatabase): ViewCountDAO {
        return db.getViewCountDao()
    }

    @Provides
    @Singleton
    fun providesHistoryDao(db: ToffeeDatabase): HistoryItemDao {
        return db.getHistoryItemDao()
    }

    @Provides
    @Singleton
    fun providesHistoryRepo(dao: HistoryItemDao): HistoryRepository {
        return HistoryRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providesUserActivitiesDao(db: ToffeeDatabase): UserActivitiesDao {
        return db.getUserActivitiesDao()
    }

    @Provides
    @Singleton
    fun providesUserActivitiesRepo(dao: UserActivitiesDao): UserActivitiesRepository {
        return UserActivitiesRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providesUploadInfoRepository(uploadDao: UploadDao): UploadInfoRepository {
        return UploadInfoRepositoryImpl(uploadDao)
    }
    
    @Provides
    @Singleton
    fun provideNotificationInfoDao(db: ToffeeDatabase): NotificationDao {
        return db.getNotificationDao()
    }
    
    @Provides
    @Singleton
    fun provideNotificationInfoRepository(notificationDao: NotificationDao): NotificationInfoRepository {
        return NotificationInfoRepositoryImpl(notificationDao)
    }
    
    @Provides
    @Singleton
    fun provideReactionInfoDao(db: ToffeeDatabase): ReactionDao {
        return db.getReactionDao()
    }
}