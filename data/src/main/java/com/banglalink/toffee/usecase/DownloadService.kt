package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ReactionCountRepository
import com.banglalink.toffee.data.repository.ShareCountRepository
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadService @Inject constructor(
    private val dbApi: DbApi,
    private val mPref: SessionPreference,
    @ApplicationContext private val mContext: Context,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val viewCountRepository: ViewCountRepository,
    private val shareCountRepository: ShareCountRepository,
    private val reactionCountRepository: ReactionCountRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) {
    
    companion object {
        const val TAG = "DB_Download_Log"
    }
    
    fun populateViewCountDb(url: String) {
        appScope.launch {
            DownloadViewCountDb(dbApi, mPref, viewCountRepository).execute(mContext, url)
        }
    }
    
    fun populateReactionStatusDb(url: String) {
        appScope.launch {
            DownloadReactionStatusDb(dbApi, reactionCountRepository).execute(mContext, url)
        }
    }
    
    fun populateSubscriptionCountDb(url: String) {
        appScope.launch {
            DownloadSubscriptionCountDb(dbApi, subscriptionCountRepository).execute(mContext, url)
        }
    }
    
    fun populateShareCountDb(url: String) {
        appScope.launch {
            DownloadShareCountDb(dbApi, shareCountRepository).execute(mContext, url)
        }
    }
}