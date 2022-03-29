package com.banglalink.toffee.ui.nativead

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.banglalink.toffee.R
import com.banglalink.toffee.enums.NativeAdType
import com.banglalink.toffee.enums.NativeAdType.LARGE
import com.banglalink.toffee.enums.NativeAdType.SMALL
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.util.BindingUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView


class NativeAdAdapter private constructor(
    private val mParam: Param,
    private val bindingUtil: BindingUtil,
) : RecyclerViewAdapterWrapper(mParam.adapter) {
    var currentNativeAd: NativeAd? = null
    companion object {
        private const val TYPE_FB_NATIVE_ADS = 900
        private const val DEFAULT_AD_ITEM_INTERVAL = 4
    }
    
    private fun convertAdPosition2OrgPosition(position: Int) = position - (position + 1) / (mParam.adItemInterval + 1)
    
    override fun getItemCount(): Int {
        val realCount = super.getItemCount()
        return realCount + realCount / mParam.adItemInterval
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (isAdPosition(position)) {
            mParam.adViewLayoutRes
        } else super.getItemViewType(convertAdPosition2OrgPosition(position))
    }
    
    private fun isAdPosition(position: Int) = (position + 1) % (mParam.adItemInterval + 1) == 0
    
    private fun onBindAdViewHolder(holder: ViewHolder) {
        val adHolder = holder as AdViewHolder
        if (mParam.forceReloadAdOnBind || !adHolder.loaded) {
            AdLoader.Builder(adHolder.context, mParam.adUnitId!!).forNativeAd { nativeAd ->
                if (mParam.isFinished) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                populateNativeAdView(nativeAd, adHolder)
                adHolder.loaded = true
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("admobnative", "error:" + loadAdError.message)
                    adHolder.adContainer.hide()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adHolder.adContainer.show()
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder().setVideoOptions(
                    VideoOptions.Builder().setStartMuted(true).setClickToExpandRequested(true)
                        .build()
                ).build()
            ).build().loadAd(AdRequest.Builder().build())
        }

    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == mParam.adViewLayoutRes) {
            onBindAdViewHolder(holder)
        } else {
            super.onBindViewHolder(holder, convertAdPosition2OrgPosition(position))
        }
    }
    
    private fun onCreateAdViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val adLayoutOutline = inflater.inflate(mParam.itemContainerLayoutRes, parent, false)
        val vg = adLayoutOutline.findViewById<ViewGroup>(mParam.itemContainerId)
        val adLayoutContent = inflater.inflate(mParam.adViewLayoutRes, parent, false) as NativeAdView
        vg.addView(adLayoutContent)
        return AdViewHolder(adLayoutOutline)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == mParam.adViewLayoutRes) {
            onCreateAdViewHolder(parent, viewType)
        } else super.onCreateViewHolder(parent, viewType)
    }
    
    private class AdViewHolder constructor(private val view: View) : ViewHolder(view) {
        var loaded: Boolean = false
        val context: Context get() = view.context
        var adContainer: NativeAdView = view.findViewById<View>(R.id.nativeAdview) as NativeAdView
        var duration: TextView = adContainer.findViewById(R.id.duration)
    }
    
    private class Param {
        var adItemInterval = 0
        @IdRes var itemContainerId = 0
        var forceReloadAdOnBind = false
        var adType: NativeAdType = LARGE
        var adUnitId: String? = null
        @LayoutRes var adViewLayoutRes = 0
        lateinit var adapter: Adapter<ViewHolder>
        @LayoutRes var itemContainerLayoutRes = 0
        var isFinished = false
    }
    
    class Builder private constructor(private val mParam: Param) {
        companion object {
            fun with(placementId: String?, wrapped: Adapter<ViewHolder>, nativeAdType: NativeAdType): Builder {
                return Builder(Param().apply {
                    adapter = wrapped
                    adType = nativeAdType
                    forceReloadAdOnBind = true
                    adUnitId = placementId
                    itemContainerId = R.id.ad_container
                    adItemInterval = DEFAULT_AD_ITEM_INTERVAL
                    itemContainerLayoutRes = R.layout.native_ad_container
                    adViewLayoutRes = if (nativeAdType == SMALL) R.layout.item_native_ad_small else R.layout.item_native_ad_large
                })
            }
        }

        fun destroyAd(){
            mParam.isFinished=true
        }

        fun adItemInterval(interval: Int): Builder {
            mParam.adItemInterval = interval
            return this
        }
        
        fun adLayout(@LayoutRes layoutContainerRes: Int, @IdRes itemContainerId: Int): Builder {
            mParam.itemContainerLayoutRes = layoutContainerRes
            mParam.itemContainerId = itemContainerId
            return this
        }
        
        fun build(bindingUtil: BindingUtil): NativeAdAdapter {
            return NativeAdAdapter(mParam, bindingUtil)
        }
        
        fun forceReloadAdOnBind(forced: Boolean): Builder {
            mParam.forceReloadAdOnBind = forced
            return this
        }
    }
    
    private fun populateNativeAdView(nativeAd: NativeAd, adContainerView: AdViewHolder) {
        val adView = adContainerView.adContainer as NativeAdView
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)
        adView.mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        
        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        
        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon.drawable)
            bindingUtil.bindSmallRoundImageFromDrawable((adView.iconView as ImageView), nativeAd.icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }
        
        if (nativeAd.mediaContent.hasVideoContent()) {
            val videoController = nativeAd.mediaContent.videoController
            val mediaAspectRatio: Float = nativeAd.mediaContent.aspectRatio
            val duration: Float = nativeAd.mediaContent.duration
            
            adContainerView.duration.show()
            adContainerView.duration.text = duration.toString()
        } else {
            adContainerView.duration.hide()
        }
        adView.setNativeAd(nativeAd)
    }
}