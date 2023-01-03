package com.banglalink.toffee.util

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageView.ScaleType.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.ActivityType
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.enums.Reaction.*
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.ui.widget.MultiTextButton
import com.banglalink.toffee.ui.widget.ReadMoreTextView
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class BindingUtil @Inject constructor(private val mPref: SessionPreference) {
    
    @BindingAdapter(value = ["loadImageFromUrl", "maintainRatio"], requireAll = false)
    fun bindImageFromUrl(view: ImageView, imageUrl: String?, maintainRatio: Boolean = true) {
        if (imageUrl.isNullOrEmpty()) {
            view.loadPlaceholder()
        } else {
            view.load(imageUrl) {
                setImageRequestParams()
                initListener(view, maintainRatio)
                size(min(360.px, 720), min(202.px, 405))
            }
        }
    }
    
    @BindingAdapter("loadSmallImageFromUrlRounded")
    fun bindSmallRoundImage(view: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            view.loadPlaceholder(isCircular = true)
        } else {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.load(imageUrl) {
                setImageRequestParams(true)
                size(min(30.px, 92), min(30.px, 92))
                transformations(CircleCropTransformation())
            }
        }
    }
    
    @BindingAdapter("loadImageFromUrlRounded")
    fun bindRoundImage(view: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            view.loadPlaceholder(isCircular = true)
        } else {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.load(imageUrl) {
                setImageRequestParams(true)
                transformations(CircleCropTransformation())
                size(min(80.px, 150), min(80.px, 150))
            }
        }
    }
    
    @BindingAdapter("loadImageFromUrlRoundedOrEmpty")
    fun bindRoundImageOrEmpty(view: ImageView, imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.load(imageUrl) {
                transformations(CircleCropTransformation())
                size(min(80.px, 150), min(80.px, 150))
            }
        }
    }
    
    @BindingAdapter("loadImageResource")
    fun loadImageFromResource(view: ImageView, image: Int) {
        view.load(image) {
            initListener(view)
            setImageRequestParams()
            size(min(320.px, 720), min(180.px, 405))
        }
    }
    
    @BindingAdapter("loadCategoryBackground")
    fun bindCategoryBackground(view: ImageView, category: Category?) {
        if (category?.thumbnailUrl.isNullOrBlank()) {
            view.loadPlaceholder()
        } else {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.load(category?.thumbnailUrl) {
                setImageRequestParams()
                size(min(120.px, 360), min(61.px, 184))
            }
        }
    }
    
    @BindingAdapter("loadCategoryIcon")
    fun bindCategoryIcon(view: ImageView, category: Category?) {
        if (category?.categoryIcon.isNullOrBlank()) {
            view.loadPlaceholder(isCircular = true)
        } else {
            view.load(category?.categoryIcon) {
                setImageRequestParams(true)
                size(min(30.px, 92), min(30.px, 92))
            }
        }
    }
    
    @BindingAdapter("loadChannelImage")
    fun bindChannel(view: ImageView, channelInfo: ChannelInfo?) {
        if (channelInfo?.isLinear == true) {
            if (channelInfo.channel_logo.isNullOrBlank()) {
                view.loadPlaceholder(isCircular = true)
            } else {
                view.load(channelInfo.channel_logo) {
                    initListener(view, false)
                    setImageRequestParams(true)
                    transformations(CircleCropTransformation())
                    size(min(80.px, 150), min(80.px, 150))
                }
            }
        } else {
            if (channelInfo?.landscape_ratio_1280_720.isNullOrBlank()) {
                view.loadPlaceholder()
            } else {
                view.load(channelInfo?.landscape_ratio_1280_720) {
                    initListener(view)
                    setImageRequestParams()
                    size(min(360.px, 720), min(202.px, 405))
                }
            }
        }
    }
    
    @BindingAdapter("loadPartnerImageFromUrl")
    fun bindPartnerImageFromUrl(view: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            view.loadPlaceholder()
        } else {
            view.load(imageUrl) {
//                transformations(
//                    CropCenterEndTransformation(4.1f)
//                )
                initListener(view, false)
                setImageRequestParams()
                size(min(288.px, 540), min(80.px, 150))
            }
        }
    }
    
    @BindingAdapter("loadSmallImageFromUrlRoundedFromDrawable")
    fun bindSmallRoundImageFromDrawable(view: ImageView, imageUrl:  Drawable?) {
        if (imageUrl==null) {
            view.loadPlaceholder(isCircular = true)
        } else {
            // view.scaleType = ImageView.ScaleType.CENTER_INSIDE
            view.load(imageUrl) {
                transformations(RoundedCornersTransformation(1000f))
                setImageRequestParams(true)
                size(min(30.px, 92), min(30.px, 92))
            }
        }
    }
    
    @BindingAdapter("bindDuration")
    fun bindDuration(view: TextView, channelInfo: ChannelInfo?) {
        try {
            view.text = channelInfo?.formattedDuration() ?: "00:00"
        } catch (ex: Exception) {
            view.text = "00:00"
            ToffeeAnalytics.logException(NullPointerException("Error getting duration info for id ${channelInfo?.id}, ${channelInfo?.program_name}"))
        }
    }
    
    @BindingAdapter("bindButtonState")
    fun bindButtonState(view: Button, isPressed: Boolean) {
        view.isPressed = isPressed
    }
    
    @BindingAdapter(value = ["bindSubscriptionStatus", "channelOwnerId"], requireAll = false)
    fun bindSubscriptionStatus(view: MultiTextButton, isSubscribed: Boolean, channelOwnerId: Int = 0) {
        view.setSubscriptionInfo(
            isSubscribed, null
        )
        view.isEnabled = mPref.customerId != channelOwnerId || !mPref.isVerifiedUser
    }
    
    @BindingAdapter("bindViewCount")
    fun bindViewCount(view: TextView, channelInfo: ChannelInfo?) {
        view.text = channelInfo?.formattedViewCount() ?: ""
    }
    
    @BindingAdapter("packageExpiryText")
    fun bindPackageExpiryText(view: TextView, mPackage: Package) {
        if (TextUtils.isEmpty(mPackage.expireDate)) {
            view.visibility = View.INVISIBLE
        } else {
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
        } else {
            autoRenewTv.visibility = View.INVISIBLE
        }
    }
    
    @BindingAdapter("bindVideoUploadTime")
    fun bindVideoUploadTime(tv: TextView, item: ChannelInfo) {
        if (item.created_at.isNullOrBlank()) {
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
        } else {
            validityTv.text = "Expires on $days"
        }
    }
    
    @BindingAdapter("discountText")
    fun bindDiscountText(discountTv: TextView, item: Package) {
        if (item.discount == 0) {
            discountTv.visibility = View.INVISIBLE
        } else {
            discountTv.visibility = View.VISIBLE
            val discountString = discountTv.context.getString(
                R.string.discount_formatted_text, item.discount
            )
            val str = SpannableStringBuilder(discountString)
            str.setSpan(
                StrikethroughSpan(), 0, discountString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            discountTv.text = str
        }
    }

/* TODO: Uncomment for subscription
    @BindingAdapter("togglePremiumIcon")
    fun bindPremiumIcon(imageView: ImageView, channelInfo: ChannelInfo) {
        if (!channelInfo.isExpired(mPref.getSystemTime())) {
            imageView.visibility = View.INVISIBLE
        } else if (channelInfo.isPurchased || channelInfo.subscription) {
            imageView.visibility = View.INVISIBLE
        } else {
            imageView.visibility = View.VISIBLE
        }
    }*/
    
    @BindingAdapter("bindActivityType")
    fun bindActivityType(view: TextView, item: UserActivities?) {
        view.text = when (item?.activityType) {
            ActivityType.REACTED.value -> "Reacted"
            ActivityType.REACTION_CHANGED.value -> "Reaction Changed"
            ActivityType.REACTION_REMOVED.value -> "Reaction Removed"
            ActivityType.WATCHED.value -> "Watched"
            ActivityType.PLAYLIST.value -> {
                when (item.activitySubType) {
                    Reaction.Add.value -> "Added to PlayList"
                    Reaction.Delete.value -> "Deleted from Playlist"
                    else -> ""
                }
            }
            else -> null
        }
    }
    
    @BindingAdapter("bindViewProgress")
    fun bindViewProgress(view: ProgressBar, item: ChannelInfo?) {
        if (item != null && item.viewProgressPercent() > 0) {
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
                R.drawable.ic_reaction_like_no_shadow
            }
            Love.value -> {
                reactionTitle = Love.name
                R.drawable.ic_reaction_love_no_shadow
            }
            HaHa.value -> {
                reactionTitle = HaHa.name
                R.drawable.ic_reaction_haha_no_shadow
            }
            Wow.value -> {
                reactionTitle = Wow.name
                R.drawable.ic_reaction_wow_no_shadow
            }
            Sad.value -> {
                reactionTitle = Sad.name
                R.drawable.ic_reaction_sad_no_shadow
            }
            Angry.value -> {
                reactionTitle = Angry.name
                R.drawable.ic_reaction_angry_no_shadow
            }
            Add.value -> R.drawable.ic_playlist
            Delete.value -> R.drawable.ic_playlist
            Watched.value -> R.drawable.ic_view
            else -> R.drawable.ic_reaction_love_empty
        }
        when (view) {
            is ImageView -> {
                view.setImageResource(reactionIcon)
                if (reaction == Add.value || reaction == Delete.value) {
                    view.setColorFilter(Color.parseColor("#829AB8"))
                }
            }
            is TextView -> {
                view.text = reactionTitle
                if (reaction == Love.value) view.setTextColor(Color.parseColor("#ff3988")) else view.setTextColor(
                    Color.parseColor("#829AB8")
                )
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
    
    @BindingAdapter(value = ["emoIcon", "iconPosition"], requireAll = true)
    fun bindEmoIcon(view: ImageView, item: ChannelInfo, iconPosition: Int) {
        val reactionCountList = listOf(
            Like to item.reaction?.like,
            Love to item.reaction?.love,
            Wow to item.reaction?.wow,
            HaHa to item.reaction?.haha,
            Sad to item.reaction?.sad,
            Angry to item.reaction?.angry
        ).sortedByDescending { (_, v) -> v }
        
        val icon = when (reactionCountList[iconPosition - 1].first) {
            Like -> R.drawable.ic_reaction_like_no_shadow
            Love -> R.drawable.ic_reaction_love_no_shadow
            Wow -> R.drawable.ic_reaction_wow_no_shadow
            HaHa -> R.drawable.ic_reaction_haha_no_shadow
            Sad -> R.drawable.ic_reaction_sad_no_shadow
            Angry -> R.drawable.ic_reaction_angry_no_shadow
            else -> R.drawable.ic_reactions_emo
        }
        view.setImageResource(icon)
    }
    
    @BindingAdapter("loadMyReactionBg")
    fun loadMyReactionBg(view: ImageView, isSetBg: Boolean) {
        if (isSetBg) {
            view.setBackgroundResource(R.drawable.reaction_round_bg)
        }
    }
    
    @BindingAdapter("loadUnseenCardBgColor")
    fun loadUnseenBgColor(view: CardView, isSeen: Boolean) {
        view.setCardBackgroundColor(
            ContextCompat.getColor(
                view.context, if (isSeen) R.color.cardBgColor else R.color.unseenCardColor
            )
        )
    }
    
    @BindingAdapter("onSafeClick")
    fun onSafeClick(view: View, listener: View.OnClickListener) {
        view.safeClick(listener)
    }
    
    @BindingAdapter("setContentDescription")
    fun setDescription(view: ReadMoreTextView, item: ChannelInfo?) {
        view.text = ""
        item?.let {
            it.getDescriptionDecoded()?.let {
                view.text = it
            }
        }
    }
    
    @BindingAdapter("setStartConstraint")
    fun setStartConstraint(view: View, item: Boolean) {
        val constraintLayout = view.layoutParams as ConstraintLayout.LayoutParams
        if (item) {
            constraintLayout.startToEnd = R.id.guideline3
        } else {
            constraintLayout.startToEnd = R.id.viewCount
            constraintLayout.endToStart = R.id.guideline3
        }
    }
}