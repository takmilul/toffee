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
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.ui.widget.MultiTextButton
import com.suke.widget.SwitchButton
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

const val crossFadeDurationInMills = 500

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
        }
    }
}

@BindingAdapter("imageFromUrlRounded")
fun bindRoundImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            transformations(CircleCropTransformation())
            crossfade(true)
            error(R.drawable.ic_home)
            crossfade(crossFadeDurationInMills)
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
fun bindChannel(view: CircleImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        view.load(channelInfo.channel_logo) {
            crossfade(false)
        }
    } else {
        view.load(channelInfo.landscape_ratio_1280_720) {
            crossfade(true)
            crossfade(crossFadeDurationInMills)
            size(720, 405)
        }
    }
}

@BindingAdapter("loadChannelImage")
fun bindChannel(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        view.load(channelInfo.channel_logo) {
            transformations(CircleCropTransformation())
            crossfade(true)
            crossfade(crossFadeDurationInMills)
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    } else {
        view.load(channelInfo.landscape_ratio_1280_720) {
            fallback(R.drawable.ic_portrait)
            placeholder(R.drawable.ic_portrait)
            error(R.drawable.ic_portrait)
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
            size(720, 405)
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: ChannelInfo) {
    view.load(channelInfo.channel_logo) {
        fallback(R.drawable.ic_portrait)
        placeholder(R.drawable.ic_portrait)
        error(R.drawable.ic_portrait)
        memoryCachePolicy(CachePolicy.DISABLED)
        diskCachePolicy(CachePolicy.ENABLED)
        crossfade(true)
        crossfade(crossFadeDurationInMills)
        size(32, 32)
    }
}

@BindingAdapter("bindDuration")
fun bindDuration(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formattedDuration
}

@BindingAdapter("bindSubscriptionStatus")
fun bindSubscriptionStatus(view: MultiTextButton, channelSubscriptionInfo: ChannelSubscriptionInfo) {
    view.setSubscriptionInfo(channelSubscriptionInfo.subscriptionStatus,
        channelSubscriptionInfo.subscriptionAmount)
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

    val days = Utils.getDateDiffInDayOrHour(Utils.getDate(mPackage.expireDate))
    view.text = "$days left"
}

@BindingAdapter("autoRenewText")
fun bindAutoRenewText(autoRenewTv:TextView,item: Package){
    if (item.isAutoRenewable == 1) {
        val days = Utils.getDateDiffInDayOrHour(Utils.getDate(item.expireDate))
        autoRenewTv.text = "Auto renew in $days"
        autoRenewTv.visibility = View.VISIBLE
    } else {
        autoRenewTv.visibility = View.INVISIBLE
    }
}

@BindingAdapter("validityText")
fun bindValidityText(validityTv:TextView,item: Package){
    val days = Utils.formatValidityText(Utils.getDate(item.expireDate))
    if (item.isAutoRenewable == 1) {
        validityTv.text = "Auto renew on $days"
    } else {
        validityTv.text = "Expires on $days"
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

@BindingAdapter("togglePremiumIcon")
fun bindPremiumIcon(imageView: ImageView,channelInfo:ChannelInfo){
    if(!channelInfo.isExpired(Preference.getInstance().getSystemTime())){
        imageView.visibility = View.INVISIBLE
    }
    else if(channelInfo.isPurchased||channelInfo.subscription){
        imageView.visibility = View.INVISIBLE
    }
    else{
        imageView.visibility = View.VISIBLE
    }
}