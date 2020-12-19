package com.banglalink.toffee.util

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.widget.MultiTextButton

const val crossFadeDurationInMills = 500

@BindingAdapter("loadImageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (imageUrl.isNullOrEmpty()) {
        view.setImageResource(R.drawable.placeholder)
    }
    else {
        view.load(imageUrl) {
            fallback(R.drawable.placeholder)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
//            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
//            crossfade(crossFadeDurationInMills)
        }
    }
}

@BindingAdapter("loadImageFromUrlRounded")
fun bindRoundImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        view.load(imageUrl) {
            transformations(CircleCropTransformation())
            crossfade(false)
            fallback(R.drawable.ic_profile)
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
//            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
//            crossfade(crossFadeDurationInMills)
        }
    }
    else {
        view.setImageResource(R.drawable.ic_profile)
    }
}

@BindingAdapter("loadImageResource")
fun loadImageFromResource(view: ImageView, image: Int) {
    view.setImageResource(image)
}

@BindingAdapter("loadCategoryBackground")
fun bindCategoryBackground(view: ImageView, category: UgcCategory) {
    view.load(category.thumbnailUrl) {
        crossfade(false)
//            memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
        size(624, 320)
    }
}

@BindingAdapter("loadCategoryImage")
fun bindCategoryImage(view: ImageView, category: UgcCategory) {
    if (category.categoryIcon.isNullOrBlank()) {
        view.setImageResource(R.drawable.ic_cat_music)
    }
    else {
        view.load(category.categoryIcon) {
            crossfade(false)
            fallback(R.drawable.ic_cat_movie)
            placeholder(R.drawable.ic_cat_movie)
            error(R.drawable.ic_cat_movie)
//            memoryCachePolicy(CachePolicy.ENABLED)
            diskCachePolicy(CachePolicy.ENABLED)
//            crossfade(crossFadeDurationInMills)
        }
    }
}

@BindingAdapter("loadChannelImage")
fun bindChannel(view: ImageView, channelInfo: ChannelInfo) {
    if (channelInfo.isLive) {
        if (channelInfo.channel_logo.isNullOrBlank()) {
            view.setImageResource(R.drawable.ic_profile)
        }
        else {
            view.load(channelInfo.channel_logo) {
                transformations(CircleCropTransformation())
                crossfade(false)
                fallback(R.drawable.ic_profile)
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
//            crossfade(crossFadeDurationInMills)
//                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }
    }
    else {
        if (channelInfo.landscape_ratio_1280_720.isNullOrBlank()) {
            view.setImageResource(R.drawable.placeholder)
        }
        else {
            view.load(channelInfo.landscape_ratio_1280_720)
            {
//                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
                fallback(R.drawable.placeholder)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
//            crossfade(crossFadeDurationInMills)
                size(720, 405)
            }
        }
    }
}

@BindingAdapter("bindDuration")
fun bindDuration(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formattedDuration()
}

@BindingAdapter("bindButtonState")
fun bindButtonState(view: Button, isPressed: Boolean){
    view.isPressed = isPressed
}

@BindingAdapter("bindSubscriptionStatus")
fun bindSubscriptionStatus(view: MultiTextButton, isSubscribed: Boolean) {
    view.setSubscriptionInfo(
        isSubscribed,
        null
    )
}

@BindingAdapter("bindViewCount")
fun bindViewCount(view: TextView, channelInfo: ChannelInfo) {
    view.text = channelInfo.formatted_view_count()
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

@BindingAdapter("bindVideoUploadTime")
fun bindVideoUploadTime(tv: TextView, item: ChannelInfo) {
    if(item.created_at.isNullOrBlank()) {
        tv.text = "1 year ago"
    } else {
        tv.text = Utils.getDateDiffInDayOrHour(Utils.getDate(item.created_at)) + " ago"
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

@BindingAdapter("bindActivityType")
fun bindActivityType(view: TextView, item: UserActivities) {
    view.text = when (item.activityType) {
        ActivityType.REACT.value -> "Reacted"
        ActivityType.PLAYLIST.value -> {
            when(item.activitySubType) {
                Reaction.Add.value -> "Added to PlayList"
                Reaction.Delete.value -> "Deleted from Playlist"
                else -> ""
            }
        }
        else -> null
    }
}

@BindingAdapter("bindViewProgress")
fun bindViewProgress(view: ProgressBar, item: ChannelInfo) {
    if(item.viewProgressPercent() > 0) {
        view.visibility = View.VISIBLE
        view.progress = item.viewProgressPercent()
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("loadReactionEmo")
fun loadReactionEmo(view: View, reaction: Int) {
    var reactionTitle = "React"
    val reactionIcon = when (reaction) {
        Like.value -> {
            reactionTitle = Like.name
            R.drawable.ic_reaction_like
        }
        Love.value -> {
            reactionTitle = Love.name
            R.drawable.ic_reaction_love_filled
        }
        HaHa.value -> {
            reactionTitle = HaHa.name
            R.drawable.ic_reaction_haha
        }
        Wow.value -> {
            reactionTitle = Wow.name
            R.drawable.ic_reaction_wow
        }
        Sad.value -> {
            reactionTitle = Sad.name
            R.drawable.ic_reaction_sad
        }
        Angry.value -> {
            reactionTitle = Angry.name
            R.drawable.ic_reaction_angry
        }
        Add.value -> R.drawable.ic_playlist
        Delete.value -> R.drawable.ic_playlist
        else -> R.drawable.ic_reaction_love_empty
    }
    when (view) {
        is ImageView -> view.setImageResource(reactionIcon)
        is TextView -> {
            view.text = reactionTitle
            view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
        }
    }
}

@BindingAdapter("bindEmoCount")
fun bindEmoCount(view: TextView, item: ChannelInfo) {
    var react = item.reaction?.run {
        like + love + haha + wow + sad + angry
    } ?: 0L
    if (item.myReaction > 0) react++
    view.text = Utils.getFormattedViewsText(react.toString())
}

@BindingAdapter("loadMyReactionBg")
fun loadMyReactionBg(view: ImageView, isSetBg: Boolean){
    if (isSetBg){
        view.setBackgroundResource(R.drawable.teal_round_bg)
    }
}

@BindingAdapter("loadUnseenCardBgColor")
fun loadUnseenBgColor(view: CardView, isSeen: Boolean){
    if (!isSeen){
        view.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.unseenCardColor))
    }
}

@BindingAdapter("contentNameMargin")
fun setContentMargin(view: TextView, isMyChannel: Boolean){
    if (isMyChannel){
        (view.layoutParams as MarginLayoutParams).marginStart = Utils.dpToPx(16)
    }
    else{
        (view.layoutParams as MarginLayoutParams).marginStart = Utils.dpToPx(8)
    }
}