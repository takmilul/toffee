package com.banglalink.toffee.ui.landing

import android.view.View
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.UgcUserChannelInfo

interface LandingPopularChannelCallback: BaseListItemCallback<UgcUserChannelInfo> {
    fun onSubscribeButtonClicked(view: View, info: UgcUserChannelInfo)
}