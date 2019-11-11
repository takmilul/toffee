package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.data.storage.Preference

data class ViewingContentRequest(
    val type: String,
    val contentId: Int,
    val customerId: Int=Preference.getInstance().customerId,
    val password: String=Preference.getInstance().password,
    val lat: String=Preference.getInstance().latitude,
    val lon: String=Preference.getInstance().longitude
):BaseRequest("viewingContent")