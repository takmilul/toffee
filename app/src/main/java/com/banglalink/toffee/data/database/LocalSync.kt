package com.banglalink.toffee.data.database

import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ReactionStatusRepository
import com.banglalink.toffee.data.repository.ShareCountRepository
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ReactionStatus
import com.banglalink.toffee.model.TrendingChannelInfo
import com.banglalink.toffee.model.UgcUserChannelInfo
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
    private val preference: Preference
) {
    suspend fun syncData(channelInfo: ChannelInfo) {
        val viewCount = viewCountRepo.getViewCountByChannelId(channelInfo.id.toInt())
        channelInfo.view_count= viewCount?.toString() ?: channelInfo.view_count
        channelInfo.viewProgress = viewProgressRepo.getProgressByContent(channelInfo.id.toLong())?.progress ?: 0L
        channelInfo.isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(channelInfo.channel_owner_id, preference.customerId) != null) 1 else 0
        channelInfo.subscriberCount = subscriptionCountRepository.getSubscriberCount(channelInfo.channel_owner_id).toInt()
        channelInfo.shareCount = shareCountRepository.getShareCountByContentId(channelInfo.id.toInt())
        val reactionList = reactionStatusRepo.getReactionStatusByChannelId(channelInfo.id.toLong())
        if(reactionList.isNotEmpty()) {
            channelInfo.reaction = getReactionStatus(channelInfo, reactionList)
        }
        val reactionInfo = reactionDao.getReactionByContentId(preference.customerId, channelInfo.id.toLong())
        channelInfo.myReaction = reactionInfo?.reactionType ?: Reaction.None.value
    }

    suspend fun syncUserChannel(userChannel: UgcUserChannelInfo){
        userChannel.isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(userChannel.channelOwnerId, preference.customerId) != null) 1 else 0
        userChannel.subscriberCount = subscriptionCountRepository.getSubscriberCount(userChannel.channelOwnerId)
    }
    
    suspend fun syncTrendingChannel(userChannel: TrendingChannelInfo){
        userChannel.isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(userChannel.channelOwnerId, preference.customerId) != null) 1 else 0
        userChannel.subscriberCount = subscriptionCountRepository.getSubscriberCount(userChannel.channelOwnerId)
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
}