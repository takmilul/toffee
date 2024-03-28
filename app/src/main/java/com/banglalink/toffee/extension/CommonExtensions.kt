package com.banglalink.toffee.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.FIT_CENTER
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import coil.request.ImageRequest
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.FavoriteItem
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.di.NetworkModule
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.enums.InputType.ADDRESS
import com.banglalink.toffee.enums.InputType.DESCRIPTION
import com.banglalink.toffee.enums.InputType.EMAIL
import com.banglalink.toffee.enums.InputType.PHONE
import com.banglalink.toffee.enums.InputType.TITLE
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.mychannel.MyChannelAddToPlaylistFragment
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.util.Date

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

val CommonPreference.appTheme: String
    get() = if (this.appThemeMode == Configuration.UI_MODE_NIGHT_YES) "dark" else "light"

fun View.show(){
    this.visibility = View.VISIBLE
}

fun View.hide(){
    this.visibility = View.GONE
}

fun View.invisible(){
    this.visibility = View.INVISIBLE
}

/**
 * Converts left hand side [Int] type value from [px] to [dp]
 */
val Int.dp: Int get() {
    return (this/Resources.getSystem().displayMetrics.density).toInt()
}

/**
 * Converts left hand side [Float] type value from [px] to [dp]
 */
val Float.dp: Float get() {
    return (this/Resources.getSystem().displayMetrics.density)
}

/**
 * Converts left hand side [Int] type value from [px] to [sp]
 */
val Int.sp: Int get() {
    return (this/Resources.getSystem().displayMetrics.scaledDensity).toInt()
}

/**
 * Converts left hand side [Float] type value from [px] to [sp]
 */
val Float.sp: Float get() {
    return (this/Resources.getSystem().displayMetrics.scaledDensity)
}

/**
 * Converts left hand side [Int] type value from [dp] to [px]
 */
val Int.px: Int get() {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

/**
 * Converts left hand side [Float] type value from [dp] to [px]
 */
val Float.px: Float get() {
    return (this * Resources.getSystem().displayMetrics.density)
}

fun Boolean.toInt() = if (this) 1 else 0

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Activity.checkVerification(
    currentDestinationId: Int? = null,
    doActionBeforeReload: Boolean = false,
    shouldReloadAfterLogin: Boolean = true, 
    block: (()-> Unit)? = null
) {
    if (this is HomeActivity && !mPref.isVerifiedUser && this.getNavController().currentDestination?.id != R.id.loginDialog) {
        mPref.doActionBeforeReload.value = doActionBeforeReload
        mPref.shouldReloadAfterLogin.value = shouldReloadAfterLogin
        mPref.preLoginDestinationId.value = currentDestinationId ?: this.getNavController().currentDestination?.id
        this.getNavController().navigate(R.id.loginDialog)
        mPref.postLoginEventAction.value = block
    } else {
        block?.invoke()
    }
}

fun Activity.handleReport(item: ChannelInfo) {
    if (this is HomeActivity){
        if (!mPref.isVerifiedUser)
        {
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "report_content",
                    "method" to "mobile"
                )
            )
        }
    }
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
    if (this is HomeActivity){
        if (!mPref.isVerifiedUser)
        {
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "add_to_playlist",
                    "method" to "mobile"
                )
            )
        }
    }
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
    if (this is HomeActivity){
        if (!mPref.isVerifiedUser)
        {
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "add_to_favorites",
                    "method" to "mobile"
                )
            )
        }
    }
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
        val newUrl = if (url.substringAfter("//").subSequence(0, 2) != "www") {
            url.replaceFirst("//", "//www.")
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl).normalizeScheme())
        startActivity(intent)
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
    if (this is HomeActivity && BuildConfig.DEBUG && NetworkModule.IS_DEBUG_MESSAGE_ACTIVE) {
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

fun String.hexToResourceName(resources: Resources): String {
    val id = BigInteger(this.removePrefix("0x"), 16).toInt()
    val resourceName = resources.getResourceName(id)
    Log.i("RES_", resourceName)
    return resourceName
}

fun List<ActivePack>?.getPurchasedPack(selectedPackId: Int?, isVerifiedUser: Boolean, systemDate: Date): ActivePack? {
    return if (isVerifiedUser) {
        selectedPackId?.let { packId ->
            this?.find {
                try {
                    it.packId == packId && it.isActive && systemDate.before(Utils.getDate(it.expiryDate))
                } catch (e: Exception) {
                    false
                }
            }
        }
    } else null
}

inline fun List<ActivePack>?.checkContentPurchase(contentId: String, systemDate: Date, onSuccess: () -> Unit, onFailure: () -> Unit) {
    if (!this.isNullOrEmpty()) {
        this.find {
            try {
                (it.contents?.contains(contentId.toInt()) == true) && it.isActive && systemDate.before(Utils.getDate(it.expiryDate))
            } catch (e: Exception) {
                false
            }
        }?.let {
            onSuccess.invoke()
        } ?: onFailure.invoke()
    } else {
        onFailure.invoke()
    }
}

fun List<ActivePack>?.isContentPurchased(contentId: String?, systemDate: Date): Boolean {
    return if (!this.isNullOrEmpty() && contentId != null) {
        this.find {
            try {
                (it.contents?.contains(contentId.toInt()) == true) && it.isActive && systemDate.before(Utils.getDate(it.expiryDate))
            } catch (e: Exception) {
                false
            }
        }?.let {
            true
        } ?: false
    } else {
        false
    }
}

fun NavController.navigateTo(@IdRes resId: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    this.navigate(resId, args, navOptions ?: navOptions { 
        launchSingleTop = true
    })
}

fun NavController.navigateTo(deepLink: Uri, navOptions: NavOptions? = null) {
    this.navigate(deepLink, navOptions ?: navOptions { 
        launchSingleTop = true
    })
}

fun NavController.navigatePopUpTo(
    @IdRes resId: Int,
    args: Bundle? = null,
    inclusive: Boolean? = true,
    @IdRes popUpTo: Int? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(resId, args, navOptions ?: navOptions { 
        launchSingleTop = true
        popUpTo(popUpTo ?: resId) {
            inclusive?.let {
                this.inclusive = inclusive
            }
        }
    })
}

inline fun <T> Fragment.checkIfFragmentAttached(operation: Context.() -> T?): T? {
    if (isAdded && context != null) {
        return operation(requireContext())
    }
    return null
}