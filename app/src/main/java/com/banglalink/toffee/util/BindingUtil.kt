package com.banglalink.toffee.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.shapes.OvalShape
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.widget.MultiTextButton
import de.hdodenhof.circleimageview.CircleImageView

const val crossFadeDurationInMills = 500

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (! imageUrl.isNullOrEmpty()) {
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
    if (! imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            transformations(CircleCropTransformation())
            crossfade(true)
            error(R.drawable.ic_home)
            crossfade(crossFadeDurationInMills)
        }
    }
}

@BindingAdapter("loadCategoryImage")
fun bindCategoryImage(view: ImageView, category: UgcCategory) {
    val gd = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        color = ColorStateList.valueOf(Color.parseColor(category.colorCode))
    }
    view.background = gd

    if(!category.categoryIcon.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_cat_music)
    } else {
        view.load(category.categoryIcon) {
            crossfade(true)
            error(R.drawable.ic_cat_movie)
            crossfade(crossFadeDurationInMills)
        }
    }
}


@BindingAdapter("loadChannelImage")
fun bindChannel(view: CircleImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        view.load(channelInfo.channel_logo) {
            crossfade(false)
        }
    }
    else {
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
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    }
    else {
        view.load(channelInfo.landscape_ratio_1280_720)
        {
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
            size(720, 405)
        }
    }
}

@BindingAdapter("loadUserChannelLogo")
fun bindUserChannelLogo(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.channel_logo.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_portrait)
    }
    else {
        view.load(channelInfo.channel_logo) {
//            fallback(R.drawable.ic_portrait)
//            placeholder(R.drawable.ic_portrait)
//            error(R.drawable.ic_portrait)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.channel_logo.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_portrait)
    }
    else {
        view.load(channelInfo.channel_logo) {
//            fallback(R.drawable.ic_portrait)
//            placeholder(R.drawable.ic_portrait)
//            error(R.drawable.ic_portrait)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: UgcUserChannelInfo) {
    if (channelInfo.profileUrl.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_portrait)
    }
    else {
        view.load(channelInfo.profileUrl) {
//            fallback(R.drawable.ic_portrait)
//            placeholder(R.drawable.ic_portrait)
//            error(R.drawable.ic_portrait)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("bindDuration")
fun bindDuration(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formattedDuration
}

@BindingAdapter("bindSubscriptionStatus")
fun bindSubscriptionStatus(view: MultiTextButton, channelInfo: ChannelInfo) {
    view.setSubscriptionInfo(
        channelInfo.subscription,
        null
    )
}

@BindingAdapter("bindSubscriptionStatus")
fun bindSubscriptionStatus(view: MultiTextButton, channelInfo: UgcUserChannelInfo) {
    view.setSubscriptionInfo(
        channelInfo.isSubscribed == 0,
        null
    )
}

@BindingAdapter("bindViewCount")
fun bindViewCount(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formatted_view_count
}

@BindingAdapter("packageExpiryText")
fun bindPackageExpiryText(view:TextView,mPackage:Package) {
    if(TextUtils.isEmpty(mPackage.expireDate)){
        view.visibility = View.INVISIBLE
    }
    else {
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
    }
    else {
        autoRenewTv.visibility = View.INVISIBLE
    }
}

@BindingAdapter("validityText")
fun bindValidityText(validityTv:TextView,item: Package){
    val days = Utils.formatValidityText(Utils.getDate(item.expireDate))
    if (item.isAutoRenewable == 1) {
        validityTv.text = "Auto renew on $days"
    }
    else {
        validityTv.text = "Expires on $days"
    }
}

@BindingAdapter("discountText")
fun bindDiscountText(discountTv:TextView,item:Package){
    if (item.discount == 0) {
        discountTv.visibility = View.INVISIBLE
    }
    else {
        discountTv.visibility = View.VISIBLE
        val discountString = discountTv.context.getString(
            R.string.discount_foramtted_text,
            item.discount
        )
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
fun bindPremiumIcon(imageView:ImageView,channelInfo:ChannelInfo) {
    if(!channelInfo.isExpired(Preference.getInstance().getSystemTime())){
        imageView.visibility = View.INVISIBLE
    }
    else if (channelInfo.isPurchased || channelInfo.subscription) {
        imageView.visibility = View.INVISIBLE
    }
    else {
        imageView.visibility = View.VISIBLE
    }
}


@BindingAdapter("loadImageResource")
fun loadImageFromResource(view: ImageView, image: Int) {
    view.setImageResource(image)
}


@BindingAdapter("loadCircleImageFromUrl")
fun bindCircleImageFromUrl(view: CircleImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            crossfade(false)
        }
    }
}
