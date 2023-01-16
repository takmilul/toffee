package com.banglalink.toffee.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.FIT_CENTER
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import coil.request.ImageRequest
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.FavoriteItem
import com.banglalink.toffee.di.NetworkModule
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.enums.InputType.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.Utils
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch
import java.util.*

private const val TITLE_PATTERN = "^[\\w\\d_.-]+$"
private const val EMAIL_PATTERN = "^[a-zA-Z0-9._-]{1,256}+@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}+\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,25}+(?:\\.[a-zA-Z]{1,4})?$"
private const val ADDRESS_PATTERN = ""
private const val DESCRIPTION_PATTERN = ""
private const val PHONE_PATTERN = "^(?:\\+8801|01)(\\d{9})$"

fun String.isValid(type: InputType): Boolean{
    return when(type){
        TITLE -> this.isNotBlank() and TITLE_PATTERN.toRegex().matches(this)
        EMAIL -> this.isNotBlank() and EMAIL_PATTERN.toRegex().matches(this)
        ADDRESS -> this.isNotBlank() and ADDRESS_PATTERN.toRegex().matches(this)
        DESCRIPTION -> this.isNotBlank() and DESCRIPTION_PATTERN.toRegex().matches(this)
        PHONE -> this.isNotBlank() and PHONE_PATTERN.toRegex().matches(this)
    }
}

fun View.show(){
    this.visibility = View.VISIBLE
}

fun View.hide(){
    this.visibility = View.GONE
}

fun View.invisible(){
    this.visibility = View.INVISIBLE
}

val Int.dp: Int get() {
    return (this/Resources.getSystem().displayMetrics.density).toInt()
}

val Float.dp: Float get() {
    return (this/Resources.getSystem().displayMetrics.density)
}

val Int.sp: Int get() {
    return (this/Resources.getSystem().displayMetrics.scaledDensity).toInt()
}

val Float.sp: Float get() {
    return (this/Resources.getSystem().displayMetrics.scaledDensity)
}

val Int.px: Int get() {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

val Float.px: Float get() {
    return (this * Resources.getSystem().displayMetrics.density)
}

fun Activity.checkVerification(block: (()-> Unit)? = null) {
    if (this is HomeActivity && !mPref.isVerifiedUser) {
        this.getNavController().navigate(R.id.loginDialog)
    } else {
        block?.invoke()
    }
}

fun Activity.handleReport(item: ChannelInfo) {
    checkVerification {
        val fragment =
            item.duration?.let { durations ->
                ReportPopupFragment.newInstance(
                    -1,
                    durations, item.id
                )
            }
        fragment?.show((this as FragmentActivity).supportFragmentManager, "report_video")
    }
}

fun Activity.handleAddToPlaylist(item: ChannelInfo, isUserPlaylist: Int = 1) {
    checkVerification {
        if (this is HomeActivity) {
            val args = Bundle().also {
                it.putInt(MyChannelAddToPlaylistFragment.CHANNEL_OWNER_ID, mPref.customerId)
                it.putParcelable(MyChannelAddToPlaylistFragment.CHANNEL_INFO, item)
                it.putInt(MyChannelAddToPlaylistFragment.IS_USER_PLAYLIST, isUserPlaylist)
            }
            this.getNavController().navigate(R.id.myChannelAddToPlaylistFragment, args)
        }
    }
}

fun Activity.handleShare(item: ChannelInfo) {
    if(this is HomeActivity) {
        getHomeViewModel().shareContentLiveData.postValue(item)
    }
}

fun Activity.handleUrlShare(url: String) {
    ToffeeAnalytics.logEvent(ToffeeEvents.SHARE_CLICK)
    if(this is HomeActivity) {
        getHomeViewModel().shareUrlLiveData.postValue(url)
    }
}

fun Activity.handleFavorite(item: ChannelInfo, favoriteDao: FavoriteItemDao, onAdded: (()->Unit)? = null, onRemoved: (()-> Unit)? = null) {
    checkVerification {
        ToffeeAnalytics.logEvent(ToffeeEvents.ADD_TO_FAVORITE)
        if(this is HomeActivity) {
            getHomeViewModel().updateFavorite(item).observe(this) {
                when (it) {
                    is Resource.Success -> {
                        val isFavorite = it.data.isFavorite
                        item.favorite = if (isFavorite == 1) "1" else "0"
                        lifecycleScope.launch {
                            favoriteDao.insert(
                                FavoriteItem(
                                    channelId = item.id.toLong(),
                                    isFavorite = isFavorite
                                )
                            )
                        }
                        when (isFavorite) {
                            0 -> {
                                onRemoved?.invoke()
                                showToast("Content successfully removed from favorite list")
                            }
                            1 -> {
                                onAdded?.invoke()
                                showToast("Content successfully added to favorite list")
                            }
                        }
                    }
                    is Resource.Failure -> {
                        showToast(it.error.msg)
                    }
                }
            }
        }
    }
}

fun ViewGroup.showLoadingAnimation(isLoading: Boolean) {
    this.forEach {
        if (it is ShimmerFrameLayout) {
            if (isLoading && !it.isShimmerStarted) {
                it.startShimmer()
            }
            else {
                it.stopShimmer()
            }
        }
    }
}

fun View.validateInput(messageTextView: TextView, messageResource: Int, messageColorResource: Int, viewBackgroundResource: Int) {
    messageTextView.setTextColor(
        ContextCompat.getColor(
            context,
            messageColorResource
        )
    )
    messageTextView.text = context.getString(messageResource)
    this.setBackgroundResource(viewBackgroundResource)
}

fun Context.openUrlToExternalApp(url: String): Boolean {
    return try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return true
    }
    catch (e: Exception) {
        Log.e("EXT_APP", "Url is not valid")
        false
    }
}

fun ImageView.loadPlaceholder(isCircular: Boolean = false) {
    scaleType = CENTER_CROP
    if (isCircular) {
        setImageResource(R.drawable.ic_profile)
    } else {
        setImageResource(R.drawable.placeholder)
    }
}

fun ImageRequest.Builder.initListener(view: ImageView, maintainRatio: Boolean = true) {
    listener(onStart = {
        view.scaleType = CENTER_CROP
    }, onError = { _, _ ->
        view.scaleType = CENTER_CROP
    }, onSuccess = { _, _ ->
        view.scaleType = if (maintainRatio) FIT_CENTER else CENTER_CROP
    })
}

fun ImageRequest.Builder.setImageRequestParams(isCircular: Boolean = false) {
    if (isCircular) {
        error(R.drawable.ic_profile)
        fallback(R.drawable.ic_profile)
        placeholder(R.drawable.ic_profile)
    } else {
        error(R.drawable.placeholder)
        fallback(R.drawable.placeholder)
        placeholder(R.drawable.placeholder)
    }
}

fun String.isTestEnvironment(): Boolean = !this.contains("https://mapi.toffeelive.com/")

fun Activity.showDebugMessage(message: String, length: Int = Toast.LENGTH_SHORT) {
    if (this is HomeActivity && BuildConfig.DEBUG && NetworkModule.isDebugMessageActive) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            showDisplayMessageDialog(this, message)
        } else {
            showToast(message, length)
        }
    }
}

fun String.isExpiredFrom(comparedDate: Date): Boolean {
    return try {
        Utils.getDate(this).before(comparedDate)
    } catch (e: Exception) {
        false
    }
}