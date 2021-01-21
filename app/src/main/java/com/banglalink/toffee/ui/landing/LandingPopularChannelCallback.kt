package com.banglalink.toffee.ui.landing

import android.view.View
import com.banglalink.toffee.common.paging.BaseListItemCallback

interface LandingPopularChannelCallback<T: Any>: BaseListItemCallback<T> {
    fun onSubscribeButtonClicked(view: View, info: T)
}