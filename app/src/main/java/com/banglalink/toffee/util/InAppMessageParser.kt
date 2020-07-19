package com.banglalink.toffee.util

import android.net.Uri
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.ui.home.*
import java.lang.Exception

class InAppMessageParser {

//    https://toffeelive.com?routing=internal&page=musicvideo&catid=18&catname=Music Video
//    https://toffeelive.com?routing=internal&page=movie&catid=19&catname=Movie
//    https://toffeelive.com?routing=internal&page=drama&catid=25&catname=Drama
//    https://toffeelive.com?routing=internal&page=subscription
//    https://toffeelive.com?routing=internal&page=faq
//    https://toffeelive.com?routing=internal&page=submitvideo
//    https://toffeelive.com?routing=internal&page=invite
//    https://toffeelive.com?routing=internal&page=redeem
//    https://toffeelive.com?routing=internal&page=settings
//    https://toffeelive.com/#video/0d52770e16b19486d9914c81061cf2da (For individual link)


    fun parseUrl(url:String):Route?{
        try {
            val link = Uri.parse(url)
            val page = link.getQueryParameter("page")
            return page?.let {
                when(it){
                    "movie"->{
                        Route(it,null,link.getQueryParameter("catid")?.toInt(),link.getQueryParameter("catname"))
                    }
                    "drama"->{
                        (Route(it,null,link.getQueryParameter("catid")?.toInt(),link.getQueryParameter("catname")))
                    }
                    "musicvideo"->{
                        (Route(it,null,link.getQueryParameter("catid")?.toInt(),link.getQueryParameter("catname")))
                    }
                    "subscription"->{
                        (Route(it,ID_SUBSCRIPTIONS))
                    }
                    "submitvideo"->{
                        (Route(it,ID_SUB_VIDEO))
                    }
                    "invite"->{
                        (Route(it,ID_INVITE_FRIEND))
                    }
                    "redeem"->{
                        (Route(it,ID_REDEEM_CODE))
                    }
                    "settings"->{
                        (Route(it,ID_SETTINGS))
                    }
                    "faq"->{
                        (Route(it,ID_FAQ))
                    }
                    else->{
                        (Route(it,null))
                    }
                }
            }
        }catch (e:Exception){
            ToffeeAnalytics.logBreadCrumb("Cannot parse deep link url $url")
            ToffeeAnalytics.logException(e)
        }
        return null

    }

    data class Route(val url:String, val drawerId:Int?,val categoryId:Int?=null,val categoryName:String?=null)
}