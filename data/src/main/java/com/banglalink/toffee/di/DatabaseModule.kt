package com.banglalink.toffee.di

import android.content.Context
import androidx.room.Room
import com.banglalink.toffee.data.database.MigrationProvider
import com.banglalink.toffee.data.database.ToffeeDatabase
import com.banglalink.toffee.data.database.dao.*
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.repository.impl.*
import com.banglalink.toffee.data.storage.SessionPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext app: Context): ToffeeDatabase {
        return Room.databaseBuilder(app,
            ToffeeDatabase::class.java, ToffeeDatabase.DB_NAME)
            .addMigrations(*MigrationProvider.getMigrationList().toTypedArray())
//            .fallbackToDestructiveMigration()
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
    fun provideNotificationInfoRepository(notificationDao: NotificationDao, pref: SessionPreference): NotificationInfoRepository {
        return NotificationInfoRepositoryImpl(notificationDao, pref)
    }

    @Provides
    @Singleton
    fun provideTVChannelsDao(db: ToffeeDatabase): TVChannelDao {
        return db.getTVChannelsDao()
    }

    @Provides
    @Singleton
    fun provideTVChannelsRepository(db: ToffeeDatabase, tvChannelsDao: TVChannelDao): TVChannelRepository {
        return TVChannelRepositoryImpl(db, tvChannelsDao)
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
    fun providesContentViewProgressRepository(dao: ContentViewProgressDao, pref: SessionPreference): ContentViewPorgressRepsitory {
        return ContentViewPorgressRepsitoryImpl(dao, pref)
    }

    @Provides
    @Singleton
    fun providesContinueWatchingDao(db: ToffeeDatabase): ContinueWatchingDao {
        return db.getContinueWatchingDao()
    }

    @Provides
    @Singleton
    fun providesContinueWatchingRepository(dao: ContinueWatchingDao, pref: SessionPreference): ContinueWatchingRepository {
        return ContinueWatchingRepositoryImpl(dao, pref)
    }

    @Provides
    @Singleton
    fun providesViewCountRepository(dao: ViewCountDAO): ViewCountRepository {
        return ViewCountRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providesReactionStatusDao(db: ToffeeDatabase): ReactionCountDao {
        return db.getReactionCountDao()
    }

    @Provides
    @Singleton
    fun providesReactionStatusRepository(db: ToffeeDatabase, dao: ReactionCountDao): ReactionCountRepository {
        return ReactionCountRepositoryImpl(db, dao)
    }

    @Provides
    @Singleton
    fun providesSubscribeCount(db: ToffeeDatabase, dao: SubscriptionCountDao): SubscriptionCountRepository {
        return SubscriptionCountRepositoryImpl(db, dao)
    }
    @Provides
    @Singleton
    fun providesSubscriptionCountDao(db: ToffeeDatabase): SubscriptionCountDao {
        return db.getSubscriptionDao()
    }

    @Provides
    @Singleton
    fun providesSubscribeInfo(dao: SubscriptionInfoDao): SubscriptionInfoRepository {
        return SubscriptionInfoRepositoryImpl(dao)
    }
    
    @Provides
    @Singleton
    fun providesSubscriptionInfoDao(db: ToffeeDatabase): SubscriptionInfoDao {
        return db.getSubscriptionInfoDao()
    }
    
    @Provides
    @Singleton
    fun providesShareCountDao(db: ToffeeDatabase): ShareCountDao {
        return db.getShareCountDao()
    }

    @Provides
    @Singleton
    fun providesDrmLicenseDao(db: ToffeeDatabase): DrmLicenseDao {
        return db.getDrmLicenseDao()
    }

    @Provides
    @Singleton
    fun providesShareCountRepository(db: ToffeeDatabase, dao: ShareCountDao): ShareCountRepository {
        return ShareCountRepositoryImpl(db, dao)
    }

    @Singleton
    @Provides
    fun providesSessionPrefDao(db: ToffeeDatabase): SessionPrefDao {
        return db.getSessionPrefDao()
    }

    @Singleton
    @Provides
    fun providesSessionPrefRepository(dao: SessionPrefDao): SessionPrefRepository {
        return SessionPrefRepositoryImpl(dao)
    }

    @Singleton
    @Provides
    fun providesDrmLicenseRepository(dao: DrmLicenseDao): DrmLicenseRepository {
        return DrmLicenseRepositoryImpl(dao)
    }
    
    @Singleton
    @Provides
    fun providesCustomPlayerEventsDataDao(db: ToffeeDatabase): PlayerEventsDao {
        return db.getCustomPlayerEventsDao()
    }
    
    @Singleton
    @Provides
    fun providesCustomPlayerEventsDataRepository(db: ToffeeDatabase, dao: PlayerEventsDao): PlayerEventRepository {
        return PlayerEventRepositoryImpl(db, dao)
    }
    
    @Singleton
    @Provides
    fun providesCdnChannelItemDao(db: ToffeeDatabase): CdnChannelItemDao {
        return db.getCdnChannelItemDao()
    }
    
    @Singleton
    @Provides
    fun providesCdnChannelItemRepository(dao: CdnChannelItemDao): CdnChannelItemRepository {
        return CdnChannelItemRepositoryImpl(dao)
    }
    
    @Singleton
    @Provides
    fun providesBubbleConfigDao(db: ToffeeDatabase): BubbleConfigDao {
        return db.getBubbleConfigDao()
    }
    
    @Singleton
    @Provides
    fun providesBubbleConfigRepository(dao: BubbleConfigDao): BubbleConfigRepository {
        return BubbleConfigRepositoryImpl(dao)
    }
    
//    @Singleton
//    @Provides
//    fun providesPremiumPackDao(db: ToffeeDatabase): PremiumPackDao {
//        return db.getPremiumPackDao()
//    }
    
//    @Singleton
//    @Provides
//    fun providesPremiumPackRepository(dao: PremiumPackDao): PremiumPackRepository {
//        return PremiumPackRepositoryImpl(dao)
//    }
}