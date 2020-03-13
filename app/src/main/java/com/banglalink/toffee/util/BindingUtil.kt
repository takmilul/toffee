package com.banglalink.toffee.util

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Package
import java.util.*

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
        }
    }
}

@BindingAdapter("imageFromUrlRounded")
fun bindRoundImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            transformations(CircleCropTransformation())
            crossfade(true)
        }
    }
}

@BindingAdapter("loadCategoryImage")
fun bindCategoryImage(view: ImageView, category : Category) {
    view.setImageResource(category.icon)
}

@BindingAdapter("loadCategoryName")
fun bindCategoryName(view: TextView, category : Category) {
    view.text = category.name
}

@BindingAdapter("loadChannelImage")
fun bindChannel(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        view.load(channelInfo.channel_logo) {
            transformations(CircleCropTransformation())
            crossfade(true)
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    } else {
        view.load(channelInfo.landscape_ratio_1280_720) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            size(720, 405)
        }
    }
}

@BindingAdapter("bindDuration")
fun bindDuration(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formattedDuration
}

@BindingAdapter("bindViewCount")
fun bindViewCount(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formatted_view_count
}

@BindingAdapter("packageExpiryText")
fun bindPackageExpiryText(view:TextView,mPackage: Package){
    if(TextUtils.isEmpty(mPackage.expireDate)){
        view.visibility = View.INVISIBLE
    }else{
        view.visibility = View.VISIBLE
    }

    val days = Utils.getCountOfDays(Calendar.getInstance().time, Utils.getDate(mPackage.expireDate))
    view.text = view.context.getString(R.string.days_left, days)
}

@BindingAdapter("autoRenewText")
fun bindAutoRenewText(autoRenewTv:TextView,item: Package){
    if (item.isAutoRenewable) {
        val days = Utils.getCountOfDays(Calendar.getInstance().time, Utils.getDate(item.expireDate))
        autoRenewTv.text = autoRenewTv.context.getString(
            R.string.auto_renew_formatted_text,
            days,
            "Days"
        )
        autoRenewTv.visibility = View.VISIBLE
    } else {
        autoRenewTv.visibility = View.INVISIBLE
    }
}

@BindingAdapter("discountText")
fun bindDiscountText(discountTv:TextView,item:Package){
    if (item.discount == 0) {
        discountTv.visibility = View.INVISIBLE
    } else {
        discountTv.visibility = View.VISIBLE
        val discountString = discountTv.context.getString(R.string.discount_foramtted_text, item.discount)
        val str = SpannableStringBuilder(discountString)
        str.setSpan(
            StrikethroughSpan(),
            0,
            discountString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        discountTv.text = str
    }
}