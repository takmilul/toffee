package com.banglalink.toffee.data.network.interceptor

import android.util.Log
import okhttp3.internal.immutableListOf

class GetTracker:IGetMethodTracker {

    private val getUrlList = immutableListOf(
        "/categories-v2/1/",
        "/contents-v5/1/",
        "/feature-contents-v2/1/",
        "/check-for-update-v2/Android/",
        "/app-home-page-content-toffee-v2/",
        "/ugc-most-popular-contents/1",
        "/ugc-category-wise-editors-choice/1",
        "/ugc-category-featured-contents/1",
        "/ugc-categories/1",
        "/ugc-popular-channel/1",
        "/ugc-channel-details/1",
        "/ugc-channel-all-content/1",
        "/ugc-playlist-names/1",
        "/ugc-content-by-playlist/1",
        "/ugc-popular-playlist-names/1",
        "/ugc-sub-category/1",
        "/ugc-all-user-channel/1",
        "/ugc-movie-category-details/1",
        "/ugc-movie-preview/1",
        "/ugc-coming-soon/1",
        "/ugc-latest-drama-serial/1",
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