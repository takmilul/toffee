package com.banglalink.toffee.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.ui.player.ChannelInfo

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl){
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }

    }
}

@BindingAdapter("loadChannelImage")
fun bindChannel(view: ImageView, channelInfo: ChannelInfo) {
   if(channelInfo.isLive){
       view.load(channelInfo.channel_logo){
           transformations(CircleCropTransformation())
           memoryCachePolicy(CachePolicy.DISABLED)
           diskCachePolicy(CachePolicy.ENABLED)
       }
   }else{
       view.load(channelInfo.landscape_ratio_1280_720){
           memoryCachePolicy(CachePolicy.DISABLED)
           diskCachePolicy(CachePolicy.ENABLED)
           size(720,405)
       }
   }
}
@BindingAdapter("bindDuration")
fun bindDuration(view:TextView, channelInfo: ChannelInfo){
    view.text= discardZeroFromDuration(channelInfo.duration)
}
@BindingAdapter("bindViewCount")
fun bindViewCount(view:TextView, channelInfo: ChannelInfo){
    view.text = getFormattedViewsText(channelInfo.view_count)
}