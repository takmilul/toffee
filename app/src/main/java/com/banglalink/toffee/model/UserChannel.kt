package com.banglalink.toffee.model

data class UserChannel (
    val id: String,
    val posterUrl: String,
    val logo: String,
    var title: String,
    var description: String,
    val categoryList: List<String>,
    val selectedCategory: String,
    val subscriptionPriceList: List<String>,
    val selectedSubscriptionPrice: String
)