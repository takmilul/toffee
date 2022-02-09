package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.util.Log
import com.banglalink.toffee.Constants
import okhttp3.internal.immutableListOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetTracker @Inject constructor():IGetMethodTracker {

    private val getUrlList = immutableListOf(
        "/categories-v2/${Constants.DEVICE_TYPE}/",
        "/ugc-contents-v5/${Constants.DEVICE_TYPE}/",
        "/feature-contents-v2/${Constants.DEVICE_TYPE}/",
        "/check-for-update-v2/Android/",
        "/ugc-app-home-page-content-toffee-v2/",
        "/ugc-most-popular-contents/${Constants.DEVICE_TYPE}",
        "/ugc-category-wise-editors-choice/${Constants.DEVICE_TYPE}",
        "/ugc-category-featured-contents/${Constants.DEVICE_TYPE}",
        "/ugc-categories/${Constants.DEVICE_TYPE}",
        "/ugc-popular-channel/${Constants.DEVICE_TYPE}",
        "/ugc-channel-details/${Constants.DEVICE_TYPE}",
        "/ugc-channel-all-content/${Constants.DEVICE_TYPE}",
        "/ugc-playlist-names/${Constants.DEVICE_TYPE}",
        "/ugc-content-by-playlist/${Constants.DEVICE_TYPE}",
        "/ugc-popular-playlist-names/${Constants.DEVICE_TYPE}",
        "/ugc-sub-category/${Constants.DEVICE_TYPE}",
        "/ugc-all-user-channel/${Constants.DEVICE_TYPE}",
        "/ugc-movie-category-details/${Constants.DEVICE_TYPE}",
        "/ugc-movie-preview/${Constants.DEVICE_TYPE}",
        "/ugc-coming-soon/${Constants.DEVICE_TYPE}",
        "/ugc-latest-drama-serial/${Constants.DEVICE_TYPE}",
        "/ugc-drama-serial-by-season/${Constants.DEVICE_TYPE}",
        "/ugc-partner-list/${Constants.DEVICE_TYPE}",
        "/ugc-terms-and-conditions/${Constants.DEVICE_TYPE}",
        "/ugc-channel-subscription-list/${Constants.DEVICE_TYPE}",
        "/ugc-inappropriate-head-list/${Constants.DEVICE_TYPE}",
        "/ugc-active-inactive-categories/${Constants.DEVICE_TYPE}",
        "/ugc-user-playlist-names/${Constants.DEVICE_TYPE}",
        "/ugc-content-by-user-playlist/${Constants.DEVICE_TYPE}",
        "/ugc-payment-method-list/${Constants.DEVICE_TYPE}",
        "/vast-tags-list/${Constants.DEVICE_TYPE}",
        "/ugc-feature-partner-list/${Constants.DEVICE_TYPE}",
        "/ugc-fireworks-list/${Constants.DEVICE_TYPE}",
        "/stingray-contents/${Constants.DEVICE_TYPE}"
    )

    override fun shouldConvertToGetRequest(urlEncodedFragmentString: String):Boolean {
        Log.i("PATH PATH",urlEncodedFragmentString)
        val resultString = getUrlList.find {
            urlEncodedFragmentString.contains(it)
        }
        Log.i("PATH",resultString?:"No String")
        return !resultString.isNullOrEmpty()
    }
}