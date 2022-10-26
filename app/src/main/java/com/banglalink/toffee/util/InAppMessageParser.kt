package com.banglalink.toffee.util

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.GetCategories
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppMessageParser @Inject constructor(
    private val categoryListApi: GetCategories
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
//    https://toffeelive.com?routing=internal&page=search&keyword=natok
//    https://toffeelive.com?routing=internal&page=ugc_channel&owner_id=6417560
//    https://toffeelive.com?routing=internal&page=categories&catid=1
//    https://toffeelive.com?routing=internal&page=categories&catid=9
//    https://toffeelive.com?routing=internal&page=categories&catid=2
//    https://toffeelive.com?routing=internal&page=featured_partner&id=5
//    https://toffeelive.com?routing=internal&page=playlist&listid=99&ownerid=594383
//    https://toffeelive.com/#video/0d52770e16b19486d9914c81061cf2da (For individual link)

    suspend fun parseUrlV2(url: String): RouteV2? {
        try {
            val link = Uri.parse(url)
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
                            }
                        )
                    }
                    "search" -> {
                        val keyword = link.getQueryParameter("keyword") ?: return null
                        return RouteV2(Uri.parse("app.toffee://search/${keyword}"),
                            "Search (${keyword})")
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
                        return RouteV2(R.id.menu_settings, "Settings")
                    }
                    "redeem" -> {
                        return RouteV2(R.id.menu_redeem, "Redeem Code")
                    }
                    "tv_channels" -> {
                        return RouteV2(R.id.menu_tv, "Tv Channels")
                    }
                    "explore" -> {
                        return RouteV2(R.id.menu_explore, "Explore")
                    }
                    "subscription" -> {
                        return RouteV2(R.id.menu_subscriptions, "Subscriptions")
                    }
                    "invite" -> {
                        return RouteV2(R.id.menu_invite, "Invite Friends")
                    }
                    "favorites" -> {
                        return RouteV2(R.id.menu_favorites, "Favorites")
                    }
                    "activities" -> {
                        return RouteV2(R.id.menu_activities, "Activities")
                    }
                    "profile" -> {
                        return RouteV2(R.id.profileFragment, "Profile")
                    }
                    "notification" -> {
                        return RouteV2(R.id.notificationDropdownFragment, "Notification")
                    }
                    "ugc_all_channel" -> {
                        return RouteV2(R.id.allUserChannelsFragment, "All UGC Channels")
                    }
                    "login" -> {
                        return RouteV2(R.id.loginDialog, "Login")
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
                        return RouteV2(partnerId, "Featured Partner")
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