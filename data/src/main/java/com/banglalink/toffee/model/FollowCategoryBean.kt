package com.banglalink.toffee.model

data class FollowCategoryBean(
    val categoryId: Int,
    val isFollowed: Int,
    val message: String,
    val messageType: String,
    val systemTime: String
)