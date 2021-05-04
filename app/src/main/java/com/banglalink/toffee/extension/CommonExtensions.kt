package com.banglalink.toffee.extension

import android.app.Activity
import android.content.res.Resources
import android.util.Patterns
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.enums.InputType.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.report.ReportPopupFragment
import java.text.SimpleDateFormat
import java.util.*

private const val TITLE_PATTERN = "^[\\w\\d_.-]+$"
//private const val EMAIL_PATTERN = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9-]+\\.[a-z]{2,4}$"
private const val ADDRESS_PATTERN = ""
private const val DESCRIPTION_PATTERN = ""
private const val PHONE_PATTERN = "^(?:\\+8801|01)(?:\\d{9})$"

fun String.isValid(type: InputType): Boolean{
    return when(type){
        TITLE -> this.isNotBlank() and TITLE_PATTERN.toRegex().matches(this)
        EMAIL -> this.isNotBlank() and Patterns.EMAIL_ADDRESS.toRegex().matches(this)
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

val Int.px: Int get() {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun Long.toFormattedDate(): String{
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.timeInMillis = this
    val dateGMT = cal.time
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return sdf.format(dateGMT)
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

fun Activity.handleShare(item: ChannelInfo) {
    if(this is HomeActivity) {
        getHomeViewModel().shareContentLiveData.postValue(item)
    }
}

fun Activity.handleFavorite(item: ChannelInfo, onAdded: (()->Unit)? = null, onRemoved: (()-> Unit)? = null) {
    checkVerification {
        if(this is HomeActivity) {
            getHomeViewModel().updateFavorite(item).observe(this, {
                when (it) {
                    is Resource.Success -> {
                        val channelInfo = it.data
                        when (channelInfo.favorite) {
                            "0" -> {
                                onRemoved?.invoke()
                                showToast("Content successfully removed from favorite list")
//                                handleFavoriteRemovedSuccessFully(channelInfo)
                            }
                            "1" -> {
                                onAdded?.invoke()
//                                handleFavoriteAddedSuccessfully(channelInfo)
                                showToast("Content successfully added to favorite list")
                            }
                        }
                    }
                    is Resource.Failure -> {
                        showToast(it.error.msg)
                    }
                }
            })
        }
    }
}