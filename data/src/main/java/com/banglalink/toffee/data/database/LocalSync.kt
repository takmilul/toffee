package com.banglalink.toffee.data.database

import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ReactionStatus
import com.banglalink.toffee.model.UserChannelInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSync @Inject constructor(
    private val viewCountRepo: ViewCountRepository,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    private val reactionStatusRepo: ReactionStatusRepository,
    private val shareCountRepository: ShareCountRepository,
    private val reactionDao: ReactionDao,
    private val subscriptionInfoRepository: SubscriptionInfoRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
    private val favoriteDao: FavoriteItemDao,
    private val preference: SessionPreference
) {
    suspend fun syncData(channelInfo: ChannelInfo, syncFlag: Int = SYNC_FLAG_ALL) {
        if(syncFlag and SYNC_FLAG_VIEW_COUNT == SYNC_FLAG_VIEW_COUNT) {
            val viewCount = viewCountRepo.getViewCountByChannelId(channelInfo.id.toInt())
            channelInfo.view_count = viewCount?.toString() ?: channelInfo.view_count
        }
        // We always need to capture viewProgress
        channelInfo.viewProgress = viewProgressRepo.getProgressByContent(channelInfo.id.toLong())?.progress ?: 0L
        if(syncFlag and SYNC_FLAG_SUB_COUNT == SYNC_FLAG_SUB_COUNT) {
            channelInfo.subscriberCount =
                subscriptionCountRepository.getSubscriberCount(channelInfo.channel_owner_id).toInt()
        }
        if(syncFlag and SYNC_FLAG_SHARE_COUNT == SYNC_FLAG_SHARE_COUNT) {
            channelInfo.shareCount =
                shareCountRepository.getShareCountByContentId(channelInfo.id.toInt()) ?: channelInfo.shareCount
        }
        if(syncFlag and SYNC_FLAG_REACT == SYNC_FLAG_REACT) {
            val reactionList =
                reactionStatusRepo.getReactionStatusByChannelId(channelInfo.id.toLong())
            if (!reactionList.isNullOrEmpty()) {
                channelInfo.reaction = getReactionStatus(channelInfo, reactionList)
            }
        }
        if(syncFlag and SYNC_FLAG_CHANNEL_SUB == SYNC_FLAG_CHANNEL_SUB) {
            if (preference.isVerifiedUser) {
                channelInfo.isSubscribed =
                    if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(
                            channelInfo.channel_owner_id,
                            preference.customerId
                        ) != null
                    ) 1 else 0
                val reactionInfo = reactionDao.getReactionByContentId(
                    preference.customerId,
                    channelInfo.id.toLong()
                )
                channelInfo.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
            } else {
                channelInfo.isSubscribed = 0
            }
        }
        if(syncFlag and SYNC_FLAG_FAVORITE == SYNC_FLAG_FAVORITE) {
            val fav = favoriteDao.isFavorite(channelInfo.id.toLong())
            if (fav != null) {
                channelInfo.favorite = fav.toString()
            }
        }
    }

    suspend fun syncUserChannel(userChannel: UserChannelInfo){
        if (preference.isVerifiedUser) {
            userChannel.isSubscribed =
                if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(userChannel.channelOwnerId, preference.customerId) != null) 1 else 0
        }
        else {
            userChannel.isSubscribed = 0
        }
        userChannel.subscriberCount = maxOf(subscriptionCountRepository.getSubscriberCount(userChannel.channelOwnerId), userChannel.subscriberCount)
    }
    
    suspend fun syncSubscribedUserChannels(userChannel: UserChannelInfo){
        if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(userChannel.channelOwnerId, preference.customerId) == null) {
            if (userChannel.isSubscribed == 1) {
                subscriptionInfoRepository.insert(SubscriptionInfo(null, userChannel.channelOwnerId, preference.customerId))
            }
        } else {
            if (userChannel.isSubscribed == 0) {
                subscriptionInfoRepository.delete(SubscriptionInfo(null, userChannel.channelOwnerId, preference.customerId))
            }
        }
        userChannel.subscriberCount = maxOf(subscriptionCountRepository.getSubscriberCount(userChannel.channelOwnerId), userChannel.subscriberCount)
    }
    
    private fun getReactionStatus(channelInfo: ChannelInfo, rl: List<ReactionStatusItem>): ReactionStatus? {
        val reactionStatus = ReactionStatus(0, channelInfo.id.toLong())
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
        const val SYNC_FLAG_ALL = 0x7FFFFFFF
        const val SYNC_FLAG_VIEW_COUNT = 1
        const val SYNC_FLAG_SHARE_COUNT = 2
        const val SYNC_FLAG_SUB_COUNT = 4
        const val SYNC_FLAG_REACT = 8
        const val SYNC_FLAG_CHANNEL_SUB = 16
        const val SYNC_FLAG_FAVORITE = 32
    }
}