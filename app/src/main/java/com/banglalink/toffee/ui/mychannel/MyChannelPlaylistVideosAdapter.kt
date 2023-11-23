package com.banglalink.toffee.ui.mychannel

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ListItemMyChannelPlaylistVideosBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyChannelPlaylistVideosAdapter(
    private val isAdActive: Boolean,
    private val adInterval: Int,
    private val adUnitId: String?,
    private val mPref: SessionPreference,
    private val bindingUtil: BindingUtil?,
    callback: BaseListItemCallback<ChannelInfo>?,
    private var selectedItem: ChannelInfo? = null,
) : BasePagingDataAdapter<ChannelInfo>(callback, ItemComparator()) {
    
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_my_channel_playlist_videos
    }
    
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder.binding is ListItemMyChannelPlaylistVideosBinding) {
            if (isAdActive && position > 0 && position % adInterval == 0) {
                holder.binding.nativeAdSmall.root.show()
                CoroutineScope(Dispatchers.IO).launch {
                    loadAds(adUnitId!!, holder.binding)
                }
            } else {
                holder.binding.nativeAdSmall.root.hide()
            }
            val obj = getItem(position)
            obj?.let {
                holder.bind(obj, callback, position, selectedItem)
            }
        }
    }
    
    private fun loadAds(adUnitId: String, itemView: ListItemMyChannelPlaylistVideosBinding) {
        val placeholder = itemView.nativeAdSmall.placeholder.root
        placeholder.show()
        AdLoader.Builder(itemView.root.context, adUnitId)
            .forNativeAd { nativeAd ->
                CoroutineScope(Dispatchers.Main).launch {
                    populateNativeAdView(nativeAd, itemView)
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        placeholder.hide()
                        placeholder.stopShimmer()
                        itemView.nativeAdSmall.root.hide()
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
    
    private fun populateNativeAdView(nativeAd: NativeAd, adContainerView: ListItemMyChannelPlaylistVideosBinding) {
        val adView = adContainerView.nativeAdSmall.nativeAdview
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
            bindingUtil?.bindSmallRoundImageFromDrawable((adView.iconView as ImageView), nativeAd.icon?.drawable)
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
        adView.show()
        placeholderSmall.hide()
        placeholderSmall.stopShimmer()
    }
    
    fun setSelectedItem(item: ChannelInfo?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}