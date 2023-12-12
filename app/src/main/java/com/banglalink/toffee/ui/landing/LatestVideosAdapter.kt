package com.banglalink.toffee.ui.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ListItemVideosBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.Utils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class LatestVideosAdapter(
    private val isAdActive: Boolean,
    private val adInterval: Int,
    private val adUnitId: String?,
    private val mPref: SessionPreference,
    private val bindingUtil: BindingUtil,
    val callback: ContentReactionCallback<ChannelInfo>,
) : PagingDataAdapter<ChannelInfo, ViewHolder>(ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_videos
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is BaseViewHolder && holder.binding is ListItemVideosBinding) {
            if (isAdActive && position > 0 && position % adInterval == 0) {
                holder.binding.nativeAdLarge.root.show()
                holder.binding.nativeAdLarge.nativeAdview.hide()
                holder.binding.nativeAdLarge.placeholder.root.show()
                
                CoroutineScope(IO).launch {
                    loadAds(adUnitId!!, holder.binding)
                }
            } else {
                holder.binding.nativeAdSmall.root.hide()
                holder.binding.nativeAdLarge.root.hide()
            }
            val obj = getItem(position)
            obj?.let {
                holder.bind(obj, callback, position)
            }
        }
    }
    
    private fun loadAds(adUnitId: String, itemView: ListItemVideosBinding) {
        AdLoader.Builder(itemView.root.context, adUnitId)
            .forNativeAd { nativeAd ->
                CoroutineScope(Main).launch {
                    populateNativeAdView(nativeAd, itemView)
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    CoroutineScope(Main).launch {
                        val placeholder = itemView.nativeAdLarge.placeholder.root
                        placeholder.hide()
                        placeholder.stopShimmer()
                        itemView.nativeAdSmall.root.hide()
                        itemView.nativeAdLarge.root.hide()
                    }
                }
                override fun onAdClicked() {
                    super.onAdClicked()
                    mPref.isDisablePip.postValue(true)
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder().setVideoOptions(
                    VideoOptions.Builder().setStartMuted(true).setClickToExpandRequested(true).build()
                ).build()
            ).build()
            .loadAd(AdRequest.Builder().build())
    }
    
    private fun populateNativeAdView(nativeAd: NativeAd, adContainerView: ListItemVideosBinding) {
        val adView = adContainerView.nativeAdLarge.nativeAdview
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        
        (adView.headlineView as TextView).text = nativeAd.headline
        
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            bindingUtil.bindSmallRoundImageFromDrawable((adView.iconView as ImageView), nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }
        
        nativeAd.mediaContent?.let {
            adView.mediaView?.mediaContent = it
            adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            if (nativeAd.mediaContent?.hasVideoContent() == true) {
                val duration: String = Utils.getDuration(it.duration.toInt())
                adContainerView.duration.show()
                adContainerView.duration.text = duration
            } else {
                adContainerView.duration.hide()
            }
        }
        adView.setNativeAd(nativeAd)
        val placeholderSmall = adContainerView.nativeAdSmall.placeholder.root
        val placeholderLarge = adContainerView.nativeAdLarge.placeholder.root
        adView.show()
        placeholderSmall.hide()
        placeholderLarge.hide()
        placeholderSmall.stopShimmer()
        placeholderLarge.stopShimmer()
    }
    
    override fun onViewRecycled(holder: ViewHolder) {
        if (holder is BaseViewHolder && holder.binding is ListItemVideosBinding) {
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}