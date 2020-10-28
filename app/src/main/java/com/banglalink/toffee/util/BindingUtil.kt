package com.banglalink.toffee.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.api.load
import coil.request.CachePolicy
import coil.request.CachePolicy.ENABLED
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.widget.MultiTextButton
import de.hdodenhof.circleimageview.CircleImageView

const val crossFadeDurationInMills = 500

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
        }
    } else {
        view.setImageResource(R.drawable.placeholder)
    }
}

@BindingAdapter("imageFromUrlRounded")
fun bindRoundImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            transformations(CircleCropTransformation())
            crossfade(false)
            error(R.drawable.ic_home)
//            crossfade(crossFadeDurationInMills)
        }
    }
    else{
        view.setImageResource(R.drawable.ic_profile_default)
    }
}

@BindingAdapter("loadCategoryImage")
fun bindCategoryImage(view: ImageView, category: UgcCategory) {
    val gd = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        color = ColorStateList.valueOf(Color.parseColor(category.colorCode))
    }
    view.background = gd

    if (category.categoryIcon.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_cat_music)
    }
    else {
        Log.e("CATEGORY", "$category")
        view.load(category.categoryIcon) {
            crossfade(false)
            error(R.drawable.ic_cat_movie)
//            crossfade(crossFadeDurationInMills)
        }
    }
}


@BindingAdapter("loadChannelImage")
fun bindChannel(view: CircleImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        if (channelInfo.channel_logo.isNullOrBlank()){
            view.setImageResource(R.drawable.ic_profile_default)
        }
        else {
            view.load(channelInfo.channel_logo) {
                crossfade(false)
            }
        }
    }
    else {
        if (channelInfo.landscape_ratio_1280_720.isNullOrBlank()){
            view.setImageResource(R.drawable.placeholder)
        }
        else {
            view.load(channelInfo.landscape_ratio_1280_720) {
                crossfade(false)
                size(720, 405)
            }
        }
    }
}

@BindingAdapter("loadChannelImage")
fun bindChannel(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        if (channelInfo.channel_logo.isNullOrBlank()){
            view.setImageResource(R.drawable.ic_profile_default)
        }
        else {
            view.load(channelInfo.channel_logo) {
                transformations(CircleCropTransformation())
                crossfade(false)
//            crossfade(crossFadeDurationInMills)
//            memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }
    }
    else {
        if (channelInfo.landscape_ratio_1280_720.isNullOrBlank()){
            view.setImageResource(R.drawable.placeholder)
        }
        else {
            view.load(channelInfo.landscape_ratio_1280_720)
            {
//            memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
//            crossfade(crossFadeDurationInMills)
                size(720, 405)
            }
        }
    }
}

@BindingAdapter("bindFeaturedImage")
fun bindFeatured(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.feature_image.isNullOrBlank()){
        view.setImageResource(R.drawable.placeholder)
    }
    else {
        view.load(channelInfo.feature_image)
        {
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
//        size(720, 405)
        }
    }
}

@BindingAdapter("loadUserChannelLogo")
fun bindUserChannelLogo(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.channel_logo.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_profile_default)
    }
    else {
        view.load(channelInfo.channel_logo) {
            fallback(R.drawable.ic_profile_default)
//            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadPlayListLogo")
fun loadPlayListLogo(view: ImageView, playlistInfo: MyChannelPlaylist) {
    if (playlistInfo.logoMobileUrl.isNullOrBlank()) {
        view.setImageResource(R.drawable.placeholder)
    }
    else {
        view.load(playlistInfo.logoMobileUrl) {
            fallback(R.drawable.placeholder)
//            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.placeholder)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.channelProfileUrl.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_profile_default)
    }
    else {
        view.load(channelInfo.channelProfileUrl) {
            fallback(R.drawable.ic_profile_default)
//            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: MyChannelPlaylist) {
    if (channelInfo.channelLogo.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_profile_default)
    }
    else {
        view.load(channelInfo.channelLogo) {
            fallback(R.drawable.ic_profile_default)
//            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
//        size(Utils.dpToPx(imageHeight), Utils.dpToPx(imageHeight))
        }
    }
}

@BindingAdapter("loadChannelLogo")
fun bindChannelLogo(view: ImageView, channelInfo: UgcUserChannelInfo) {
    if (channelInfo.profileUrl.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_profile_default)
    }
    else {
        view.load(channelInfo.profileUrl) {
            fallback(R.drawable.ic_profile_default)
            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
//            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
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
        channelInfo.isSubscribed == 1,
        null
    )
}

@BindingAdapter("bindSubscriptionStatus")
fun bindSubscriptionStatus(view: MultiTextButton, isSubscribed: Int) {
    view.setSubscriptionInfo(
        isSubscribed == 1,
        null
    )
}

@BindingAdapter("bindViewCount")
fun bindViewCount(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formatted_view_count
}

@BindingAdapter("packageExpiryText")
fun bindPackageExpiryText(view: TextView, mPackage: Package) {
    if (TextUtils.isEmpty(mPackage.expireDate)) {
        view.visibility = View.INVISIBLE
    }
    else {
        view.visibility = View.VISIBLE
    }

    val days = Utils.getDateDiffInDayOrHour(Utils.getDate(mPackage.expireDate))
    view.text = "$days left"
}

@BindingAdapter("autoRenewText")
fun bindAutoRenewText(autoRenewTv: TextView, item: Package) {
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
fun bindValidityText(validityTv: TextView, item: Package) {
    val days = Utils.formatValidityText(Utils.getDate(item.expireDate))
    if (item.isAutoRenewable == 1) {
        validityTv.text = "Auto renew on $days"
    }
    else {
        validityTv.text = "Expires on $days"
    }
}

@BindingAdapter("discountText")
fun bindDiscountText(discountTv: TextView, item: Package) {
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
fun bindPremiumIcon(imageView: ImageView, channelInfo: ChannelInfo) {
    if (!channelInfo.isExpired(Preference.getInstance().getSystemTime())) {
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
    if (imageUrl.isNullOrEmpty()) {
        view.setImageResource(R.drawable.ic_profile_default)
    } else {
        view.load(imageUrl) {
            fallback(R.drawable.ic_profile_default)
            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
            diskCachePolicy(ENABLED)
            crossfade(false)
        }
    }
}

@BindingAdapter("bindActivityType")
fun bindActivityType(view: TextView, item: UserActivities) {
    view.text = when (item.activityType) {
        ActivityType.REACT.value -> "Reacted"
        ActivityType.PLAYLIST.value -> "Added to PlayList"
        else -> null
    }
}

@BindingAdapter("loadTextLeftDrawable")
fun loadLeftDrawable(view: TextView, resourceId: Int){
    view.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0)
}

@BindingAdapter("bindActivityLogo")
fun bindActivityLogo(view: ImageView, item: UserActivities) {
    val reactLogo = when (item.activitySubType) {
        Like.value -> R.drawable.ic_reaction_like
        Love.value -> R.drawable.ic_reaction_love
        HaHa.value -> R.drawable.ic_reaction_haha
        Wow.value -> R.drawable.ic_reaction_wow
        Sad.value -> R.drawable.ic_reaction_sad
        Angry.value -> R.drawable.ic_reaction_angry
        else -> R.drawable.ic_like_emo
    }
    view.setImageResource(reactLogo)
}

@BindingAdapter("bindEmoCount")
fun bindEmoCount(view: TextView, item: ChannelInfo) {
    var react = item.reaction?.run {
        like + love + haha + wow + sad + angry
    } ?: 0L
    println("react count before inc: $react")
    if (item.userReaction > 0) react++
    println(item.userReaction)
    println("react count after inc: $react")
    view.text = Utils.getFormattedViewsText(react.toString())
}

@BindingAdapter("bindReaction", "bindReactionCount", requireAll = true)
fun bindReactionCount(view: TextView, reaction: Reaction, item: ChannelInfo) {
    val reactionCount = when (reaction) {
        Like -> {
            if (item.userReaction == Like.value)
                item.reaction?.like?.plus(1)
            else
                item.reaction?.like
        }
        Love -> {
            if (item.userReaction == Love.value)
                item.reaction?.love?.plus(1)
            else
                item.reaction?.love
        }
        HaHa -> {
            if (item.userReaction == HaHa.value)
                item.reaction?.haha?.plus(1)
            else
                item.reaction?.haha
        }
        Wow -> {
            if (item.userReaction == Wow.value)
                item.reaction?.wow?.plus(1)
            else
                item.reaction?.wow
        }
        Sad -> {
            if (item.userReaction == Sad.value)
                item.reaction?.sad?.plus(1)
            else
                item.reaction?.sad
        }
        Angry -> {
            if (item.userReaction == Angry.value)
                item.reaction?.angry?.plus(1)
            else
                item.reaction?.angry
        }
        None -> {
            0L
        }
    }
    view.text = Utils.getFormattedViewsText(reactionCount.toString())
}