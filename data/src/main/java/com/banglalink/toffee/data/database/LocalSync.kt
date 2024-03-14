package com.banglalink.toffee.data.database

import com.banglalink.toffee.Constants.PLAY_CDN
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.CdnChannelItem
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.CdnChannelItemRepository
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ReactionCountRepository
import com.banglalink.toffee.data.repository.ShareCountRepository
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelSubscribeBean
import com.banglalink.toffee.model.ReactionStatus
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSync @Inject constructor(
    private val json: Json,
    private val reactionDao: ReactionDao,
    private val favoriteDao: FavoriteItemDao,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository,
    private val viewCountRepo: ViewCountRepository,
    private val userActivityRepo: UserActivitiesRepository,
    private val shareCountRepository: ShareCountRepository,
    private val reactionCountRepo: ReactionCountRepository,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    private val cdnChannelItemRepository: CdnChannelItemRepository,
    private val subscriptionInfoRepository: SubscriptionInfoRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) {
    /**
     * Sync data with local db. Provide [channelInfo] and [isFromCache] to sync data with local db.
     * If required to sync the contents with local db then provide [syncFlag] with the required flag. If [syncFlag] is not provided 
     * then it will not sync all the data with local db. You can provide multiple flags using 'or' to sync multiple data with local db.
     * @param channelInfo [ChannelInfo]: the item which needs to be synced with local db
     * @param syncFlag [Int]: the flag to sync the data with local db
     * @param isFromCache [Boolean]: if the data is from cache or not
     */
    suspend fun syncData(
        channelInfo: ChannelInfo,
        isFromCache: Boolean,
        syncFlag: Int = SYNC_FLAG_NONE,
    ) {
        val contentId = channelInfo.getContentId()
        
        if (channelInfo.isVOD) {
            Log.d("LocalSync", "VIEW_PROGRESS")
            channelInfo.viewProgress = viewProgressRepo.getProgressByContent(contentId.toLong())?.progress ?: 0L
        }
        if(syncFlag and SYNC_FLAG_VIEW_COUNT == SYNC_FLAG_VIEW_COUNT) {
            Log.d("LocalSync", "SYNC_FLAG_VIEW_COUNT")
            val viewCount = viewCountRepo.getViewCountByChannelId(contentId.toInt())
            channelInfo.view_count = viewCount?.toString() ?: "0"
        }
        if(syncFlag and SYNC_FLAG_SUB_COUNT == SYNC_FLAG_SUB_COUNT) {
            Log.d("LocalSync", "SYNC_FLAG_SUB_COUNT")
            channelInfo.subscriberCount = subscriptionCountRepository.getSubscriberCount(channelInfo.channel_owner_id)
        }
        if(syncFlag and SYNC_FLAG_SHARE_COUNT == SYNC_FLAG_SHARE_COUNT) {
            Log.d("LocalSync", "SYNC_FLAG_SHARE_COUNT")
            channelInfo.shareCount = shareCountRepository.getShareCountByContentId(contentId.toInt()) ?: 0L
        }
        if(syncFlag and SYNC_FLAG_REACT == SYNC_FLAG_REACT) {
            Log.d("LocalSync", "SYNC_FLAG_REACT")
            val reactionList = reactionCountRepo.getReactionStatusByChannelId(contentId.toLong())
            if (!reactionList.isNullOrEmpty()) {
                channelInfo.reaction = getReactionStatus(contentId, reactionList)
            }
        }
        if(syncFlag and SYNC_FLAG_CHANNEL_SUB == SYNC_FLAG_CHANNEL_SUB) {
            Log.d("LocalSync", "SYNC_FLAG_CHANNEL_SUB")
            if (preference.isVerifiedUser) {
                channelInfo.isSubscribed =
                    if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(
                            channelInfo.channel_owner_id,
                            preference.customerId
                        ) != null
                    ) 1 else 0
                val reactionInfo = reactionDao.getReactionByContentId(
                    preference.customerId,
                    contentId.toLong()
                )
                channelInfo.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
            } else {
                channelInfo.isSubscribed = 0
            }
        }
//        if(syncFlag and SYNC_FLAG_FAVORITE == SYNC_FLAG_FAVORITE) {
//            Log.d("LocalSync", "SYNC_FLAG_FAVORITE")
        if (channelInfo.isVOD) {
            val fav = favoriteDao.isFavorite(contentId.toLong())
            if (fav != null) {
                channelInfo.favorite = fav.toString()
            }
        }
//        }
//        if (syncFlag and SYNC_FLAG_TV_RECENT == SYNC_FLAG_TV_RECENT) {
//            Log.d("LocalSync", "SYNC_FLAG_TV_RECENT")
            if (channelInfo.isLive && !isFromCache) {
                tvChannelRepo.getRecentItemById(contentId.toLong(), if (channelInfo.isStingray ) 1 else 0,if (channelInfo.isFmRadio ) 1 else 0)?.let {
                    val dbRecentPayload = it.payload?.let { it1 -> json.decodeFromString<ChannelInfo>(it1) }
                    if (dbRecentPayload?.equals(channelInfo) == false) {
                        val isStingray = if (channelInfo.isStingray) 1 else 0
                        val isFmRadio = if (channelInfo.isStingray) 1 else 0
                        tvChannelRepo.updateRecentItemPayload(
                            contentId.toLong(),
                            isStingray,
                            isFmRadio,
                            channelInfo.view_count?.toLong() ?: 0L,
                            json.encodeToString(channelInfo)
                        )
                    }
                }
            }
//        }
//        if (syncFlag and SYNC_FLAG_FM_RADIO_RECENT == SYNC_FLAG_FM_RADIO_RECENT) {
//            Log.d("LocalSync", "SYNC_FLAG_FM_RADIO_RECENT")
            if (channelInfo.isFmRadio && !isFromCache) {
                tvChannelRepo.getRecentItemById(contentId.toLong(), 0, 1 )?.let {
                    val dbRecentPayload = it.payload?.let { it1 -> json.decodeFromString<ChannelInfo>(it1) }
                    if (dbRecentPayload?.equals(channelInfo) == false) {
                        val isStingray =  0
                        val isFmRadio = 1
                        tvChannelRepo.updateRecentItemPayload(
                            contentId.toLong(),
                            isStingray,
                            isFmRadio,
                            channelInfo.view_count?.toLong() ?: 0L,
                            json.encodeToString(channelInfo)
                        )
                    }
                }
            }
//        }
//        if (syncFlag and SYNC_FLAG_USER_ACTIVITY == SYNC_FLAG_USER_ACTIVITY) {
//            Log.d("LocalSync", "SYNC_FLAG_USER_ACTIVITY")
        if (!isFromCache) {
            userActivityRepo.getUserActivityById(contentId.toLong(), channelInfo.type ?: "VOD")?.let {
                val dbUserActivityPayload = it.payload?.let { it1 -> json.decodeFromString<ChannelInfo>(it1) }
                if (dbUserActivityPayload?.equals(channelInfo) == false) {
                    userActivityRepo.updateUserActivityPayload(
                        contentId.toLong(),
                        channelInfo.type ?: "VOD",
                        json.encodeToString(channelInfo)
                    )
                }
            }
        }
//        }
        /**
         * Sync cookie based content with local db. if the content is expired then update the local db with new content
         */
//        if (syncFlag and SYNC_FLAG_CDN_CONTENT == SYNC_FLAG_CDN_CONTENT) {
//            Log.d("LocalSync", "SYNC_FLAG_CDN_CONTENT")
            if (channelInfo.urlType == PLAY_CDN) {
                cdnChannelItemRepository.getCdnChannelItemByChannelId(contentId.toLong())?.let {
                    runCatching {
                        if (Utils.getDate(it.expiryDate).before(Utils.getDate(channelInfo.signedUrlExpiryDate ?: channelInfo
                            .signedCookieExpiryDate)) || (!channelInfo.equals(it) && !isFromCache)) {
                            cdnChannelItemRepository.updateCdnChannelItemByChannelId(
                                contentId.toLong(), 
                                channelInfo.signedUrlExpiryDate ?: channelInfo.signedCookieExpiryDate,
                                json.encodeToString(channelInfo)
                            )
                        } else {
                            channelInfo.signedUrlExpiryDate = it.expiryDate
                            channelInfo.hlsLinks = it.channelInfo?.hlsLinks
                            channelInfo.paidPlainHlsUrl = it.channelInfo?.paidPlainHlsUrl
                            channelInfo.signedCookieExpiryDate = it.expiryDate
                            channelInfo.signedCookie = it.channelInfo?.signedCookie
                        }
                    }
                } ?: run {
                    cdnChannelItemRepository.insert(CdnChannelItem(contentId.toLong(), channelInfo.urlType, channelInfo.signedUrlExpiryDate ?: channelInfo
                        .signedCookieExpiryDate, json.encodeToString(channelInfo)))
                }
            }
//        }
    }
    
    suspend fun syncUserChannel(userChannel: UserChannelInfo){
        if (preference.isVerifiedUser) {
            userChannel.isSubscribed =
                if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(userChannel.channelOwnerId, preference.customerId) != null) 1 else 0
        }
        else {
            userChannel.isSubscribed = 0
        }
        userChannel.subscriberCount = syncSubscriberCount(userChannel.channelOwnerId, userChannel.isSubscribed, userChannel.subscriberCount)
    }
    
    suspend fun syncSubscribedUserChannels(userChannel: UserChannelInfo){
        userChannel.subscriberCount = syncSubscriptionStatus(userChannel.channelOwnerId, userChannel.isSubscribed, userChannel.subscriberCount)
    }
    
    suspend fun updateOnSubscribeChannel(subscriptionInfo: MyChannelSubscribeBean){
        subscriptionInfo.subscriberCount = syncSubscriptionStatus(subscriptionInfo.channelId, subscriptionInfo.isSubscribed, subscriptionInfo
            .subscriberCount, true)
    }
    
    private suspend fun syncSubscriptionStatus(channelId: Int, isSubscribed: Int, subscriberCount: Long, isSubscriptionAction: Boolean = false): Long {
        if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(channelId, preference.customerId) == null) {
            if (isSubscribed == 1) {
                subscriptionInfoRepository.insert(SubscriptionInfo(null, channelId, preference.customerId))
            }
        } else {
            if (isSubscribed == 0) {
                subscriptionInfoRepository.deleteSubscriptionInfo(channelId, preference.customerId)
            }
        }
        return syncSubscriberCount(channelId, isSubscribed, subscriberCount, isSubscriptionAction)
    }
    
    private suspend fun syncSubscriberCount(channelId: Int, isSubscribed: Int, subscriberCount: Long, isSubscriptionAction: Boolean = false): Long {
        val dbSubscriberCount = subscriptionCountRepository.getSubscriberCount(channelId)
        var status = 0
        if (isSubscriptionAction) {
            status = if (isSubscribed == 0) -1 else isSubscribed
        }
//        compare api result with binary file result and update local db with updated value
//        if (subscriberCount > dbSubscriberCount) {
//            status += (subscriberCount - dbSubscriberCount).toInt()
//        }
        if (status != 0) {
            subscriptionCountRepository.updateSubscriptionCount(channelId, status)
        }
//        return subscriberCount + status // return this if above comparison code is applied
        return dbSubscriberCount + status
    }
    
    private fun getReactionStatus(contentId: String, rl: List<ReactionStatusItem>): ReactionStatus {
        val reactionStatus = ReactionStatus(0, contentId.toLong())
        rl.forEach {
            when(it.reactionType) {
                Reaction.Like.value -> {
                    reactionStatus.like = it.reactionCount
                }
                Reaction.Love.value -> {
                    reactionStatus.love = it.reactionCount
                }
                Reaction.HaHa.value -> {
                    reactionStatus.haha = it.reactionCount
                }
                Reaction.Wow.value -> {
                    reactionStatus.wow = it.reactionCount
                }
                Reaction.Sad.value -> {
                    reactionStatus.sad = it.reactionCount
                }
                Reaction.Angry.value -> {
                    reactionStatus.angry = it.reactionCount
                }
            }
        }
        return reactionStatus
    }

    companion object {
        const val SYNC_FLAG_NONE = 0
        const val SYNC_FLAG_ALL = 0x7FFFFFFF
        const val SYNC_FLAG_VIEW_COUNT = 1
        const val SYNC_FLAG_SHARE_COUNT = 2
        const val SYNC_FLAG_SUB_COUNT = 4
        const val SYNC_FLAG_REACT = 8
        const val SYNC_FLAG_CHANNEL_SUB = 16
        const val SYNC_FLAG_FAVORITE = 32
        const val SYNC_FLAG_TV_RECENT = 64
        const val SYNC_FLAG_USER_ACTIVITY = 128
        const val SYNC_FLAG_CDN_CONTENT = 256
        const val SYNC_FLAG_FM_RADIO_RECENT = 512
        const val SYNC_FLAG_FM_ACTIVITY = 1024
    }
}