package com.banglalink.toffee.util

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.GetCategoriesService
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppMessageParser @Inject constructor(
    private val categoryListApi: GetCategoriesService,
    private var mPref: SessionPreference
) {

//    https://toffeelive.com?routing=internal&page=musicvideo&catid=18&catname=Music Video
//    https://toffeelive.com?routing=internal&page=movie&catid=19&catname=Movie
//    https://toffeelive.com?routing=internal&page=drama&catid=25&catname=Drama
//    https://toffeelive.com?routing=internal&page=subscription
//    https://toffeelive.com?routing=internal&page=faq
//    https://toffeelive.com?routing=internal&page=submitvideo
//    https://toffeelive.com?routing=internal&page=invite
//    https://toffeelive.com?routing=internal&page=redeem
//    https://toffeelive.com?routing=internal&page=settings
//    https://toffeelive.com?routing=internal&page=login
//    https://toffeelive.com?routing=internal&page=fm-radio
//    https://toffeelive.com?routing=internal&page=search&keyword=natok
//    https://toffeelive.com?routing=internal&page=ugc_channel&owner_id=6417560
//    https://toffeelive.com?routing=internal&page=categories&catid=1
//    https://toffeelive.com?routing=internal&page=categories&catid=9
//    https://toffeelive.com?routing=internal&page=categories&catid=2
//    https://toffeelive.com?routing=internal&page=featured_partner&id=5
//    https://toffeelive.com?routing=internal&page=playlist&listid=99&ownerid=594383
//    https://toffeelive.com/#video/0d52770e16b19486d9914c81061cf2da (For individual link)
//    https://toffeelive.com?routing=internal&page=pack_from_ad&packId=6&paymentMethodId=10&showBlPacks=true

    suspend fun parseUrlV2(url: String): RouteV2? {
        try {
            val link = Uri.parse(url)
            val navOptions = navOptions { launchSingleTop = true }
            val path = link.lastPathSegment
            val page = link.getQueryParameter("page")
            page?.let { name ->
                when(name) {
                    "ugc_channel" -> {
                        val channelId = link.getQueryParameter("owner_id") ?: "0"
                        return RouteV2(
                            Uri.parse("app.toffee://ugc_channel/${channelId}/false"),
                            "Ugc Channel (${channelId})"
                        )
                    }
                    "categories" -> {
                        val catId = link.getQueryParameter("catid")?.toLongOrNull() ?: return null
                        val catList = categoryListApi.loadData(0, 0).filter { it.id ==  catId }
                        if(catList.isEmpty()) return null
                        val destId = when(catId.toInt()) {
                            CategoryType.MOVIE.value -> {
                                R.id.movieFragment
                            }
                            CategoryType.MUSIC.value -> {
                                R.id.musicDetailsFragmant
                            }
                            CategoryType.DRAMA_SERIES.value -> {
                                R.id.dramaSeriesFragment
                            }
                            else -> {
                                R.id.categoryDetailsFragment
                            }
                        }
                        return RouteV2(
                            destId,
                            "Category (${catList[0].categoryName})",
                            Bundle().also {
                                it.putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, catList[0])
                                it.putString(CategoryDetailsFragment.ARG_TITLE, catList[0].categoryName)
                            },
                            navOptions
                        )
                    }
                    "search" -> {
                        val keyword = link.getQueryParameter("keyword") ?: return null
                        return RouteV2(Uri.parse("app.toffee://search/${keyword}"),
                            "Search (${keyword})", null, navOptions)
                    }
//                    "playlist" -> {
//                        val playlistId = link.getQueryParameter("listid") ?: return null
//                        val ownerId = link.getQueryParameter("ownerid") ?: return null
//
//                    }
//                    "episodelist" -> {
//
//                    }
                    "settings" -> {
                        return RouteV2(R.id.menu_settings, "Settings", null, navOptions)
                    }
                    "redeem" -> {
                        return RouteV2(R.id.menu_redeem, "Redeem Code", null, navOptions)
                    }
                    "tv_channels" -> {
                        return RouteV2(R.id.menu_tv, "Tv Channels", null, navOptions)
                    }
                    "explore" -> {
                        return RouteV2(R.id.menu_explore, "Explore", null, navOptions)
                    }
                    "subscription" -> {
                        return RouteV2(R.id.menu_subscriptions, "Subscriptions", null, navOptions)
                    }
                    "invite" -> {
                        return RouteV2(R.id.menu_invite, "Invite Friends", null, navOptions)
                    }
                    "favorites" -> {
                        return RouteV2(R.id.menu_favorites, "Favorites", null, navOptions)
                    }
                    "activities" -> {
                        return RouteV2(R.id.menu_activities, "Activities", null, navOptions)
                    }
                    "profile" -> {
                        return RouteV2(R.id.profileFragment, "Profile", null, navOptions)
                    }
                    "notification" -> {
                        return RouteV2(R.id.notificationDropdownFragment, "Notification", null, navOptions)
                    }
                    "ugc_all_channel" -> {
                        return RouteV2(R.id.allUserChannelsFragment, "All UGC Channels", null, navOptions)
                    }
                    "login" -> {
                        return RouteV2(R.id.loginDialog, "Login", null, navOptions)
                    }
                    "home" -> {
                        return RouteV2(R.id.menu_feed, "Home", options = navOptions { 
                            popUpTo(R.id.menu_feed) {
                                inclusive = true
                            }
                        })
                    }
                    "featured_partner" -> {
                        val partnerId = link.getQueryParameter("id")?.toIntOrNull() ?: 0
                        return RouteV2(partnerId, "Featured Partner", null, navOptions)
                    }
                    "pack_from_ad" ->{
                        val packId = link.getQueryParameter("packId")?.toIntOrNull()
                        val paymentMethodId = link.getQueryParameter("paymentMethodId")?.toIntOrNull() ?: -1
                        val showBlPacks = link.getQueryParameter("showBlPacks").toBoolean()

                        if (packId == null){
                            return RouteV2(R.id.menu_feed, "Home", null, navOptions)
                        } else {
                            return RouteV2(
                                R.id.packDetailsFragment,
                                "Pack Details",
                                bundleOf(
                                    "openPlanDetails" to true,
                                    "packId" to packId,
                                    "paymentMethodId" to paymentMethodId,
                                    "showBlPacks" to showBlPacks
                                ),
                                navOptions
                            )
                        }
                    }
                    "fm-radio" -> {
                        return RouteV2(R.id.fmRadioFragment, "FM Radio", null, navOptions)
                    }
                    "kabbik" -> {
                        return RouteV2(R.id.audioBookLandingFragment, "Kabbik", null, navOptions)
                    }
                    else -> null
                }
            }
            
            path?.let {
                when (it) {
                    "fm-radio" -> {
                        return RouteV2(R.id.fmRadioFragment, "FM Radio", null, navOptions)
                    }
                    "kabbik" -> {
                        return RouteV2(R.id.audioBookLandingFragment, "Kabbik", null, navOptions)
                    }
                    "tvsignin" -> {
                        if (mPref.isQrCodeEnable) {
                            val code = link.getQueryParameter("code")
                            return RouteV2(
                                R.id.menu_active_tv,
                                "Activate Tv",
                                bundleOf(
                                    "code" to code?.ifBlank { null },
                                ),
                                navOptions
                            )
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
            ToffeeAnalytics.logBreadCrumb("Cannot parse deep link url $url")
            ToffeeAnalytics.logException(e)
        }
        return null
    }

    data class RouteV2(
        val destId: Any,
        val name: String,
        val args: Bundle? = null,
        val options: NavOptions? = null,
        val navExtra: Navigator.Extras? = null
    )
}