package com.banglalink.toffee.model

data class ChallengeResult(
    val posterUrl: String?,
    val viewCount: String?,
    val commentCount: String?,
    val reactionCount: String?,
    val logoUrl: String?,
    val ownerName: String?,
    val challengeTitle: String?,
    val challengeDescription: String?,
    val hashTag: String?,
    val rewardWinnerCount: String?,
    val remainingTime: String?,
    val isChallengeClosed: Boolean? = false
) 