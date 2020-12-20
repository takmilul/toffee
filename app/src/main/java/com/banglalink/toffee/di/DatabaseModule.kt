package com.banglalink.toffee.di

import android.content.Context
import androidx.room.Room
import com.banglalink.toffee.data.database.MigrationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.*
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.repository.impl.*
import com.banglalink.toffee.data.storage.Preference
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
//            .addMigrations(*MigrationProvider.getMigrationList().toTypedArray())
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
    fun provideTVChannelsDao(db: ToffeeDatabase): TVChannelDao {
        return db.getTVChannelsDao()
    }

    @Provides
    @Singleton
    fun provideTVChannelsRepository(tvChannelsDao: TVChannelDao): TVChannelRepository {
        return TVChannelRepositoryImpl(tvChannelsDao)
    }
    
    @Provides
    @Singleton
    fun provideReactionInfoDao(db: ToffeeDatabase): ReactionDao {
        return db.getReactionDao()
    }

    @Provides
    @Singleton
    fun providesFavoritesItemDao(db: ToffeeDatabase): FavoriteItemDao {
        return db.getFavoriteItemsDao()
    }

    @Provides
    @Singleton
    fun providesSubscribedItemDao(db: ToffeeDatabase): SubscribedItemDao {
        return db.getSubscribedItemsDao()
    }
    
    @Provides
    @Singleton
    fun providesContentViewProgressDao(db: ToffeeDatabase): ContentViewProgressDao {
        return db.getContentViewProgressDao()
    }

    @Provides
    @Singleton
    fun providesContentViewProgressRepository(dao: ContentViewProgressDao, pref: Preference): ContentViewPorgressRepsitory {
        return ContentViewPorgressRepsitoryImpl(dao, pref)
    }

    @Provides
    @Singleton
    fun providesContinueWatchingDao(db: ToffeeDatabase): ContinueWatchingDao {
        return db.getContinueWatchingDao()
    }

    @Provides
    @Singleton
    fun providesContinueWatchingRepository(dao: ContinueWatchingDao, pref: Preference): ContinueWatchingRepository {
        return ContinueWatchingRepositoryImpl(dao, pref)
    }
 }