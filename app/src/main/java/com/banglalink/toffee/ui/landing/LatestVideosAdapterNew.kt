package com.banglalink.toffee.ui.landing

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.ContentReactionCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class LatestVideosAdapterNew(
    private val isAdActive: Boolean,
    private val adInterval: Int,
    val adUnitId: String?,
    val mPref: SessionPreference,
    val cb: ContentReactionCallback<ChannelInfo>,
) : PagingDataAdapter<ChannelInfo, ViewHolder>(ItemComparator()) {
    
    private var adPosition = 0
    private val AD_VIEW_SMALL = R.layout.item_native_ad_small
    private val AD_VIEW_LARGE = R.layout.item_native_ad_large
    private val ITEM_VIEW = R.layout.list_item_videos
    
    override fun getItemViewType(position: Int): Int {
        adPosition = if (position > adInterval + 1) position - 1 else position
        if(isAdActive && position != 0 && adPosition % adInterval == 0) {
            Log.i("View_", "AD: $position")
            return AD_VIEW_LARGE //if (nativeAdType == SMALL) R.layout.item_native_ad_small else R.layout.item_native_ad_large
        }
        Log.i("View_", "ITEM: $position")
        return ITEM_VIEW
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedItemView = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, ITEM_VIEW, parent, false
        )
        val inflatedAdViewLarge = layoutInflater.inflate(AD_VIEW_LARGE, parent, false)
        val inflatedAdViewSmall = layoutInflater.inflate(AD_VIEW_SMALL, parent, false)
        return when(viewType){
            AD_VIEW_LARGE -> NativeAdViewHolder(inflatedAdViewLarge, mPref)
            ITEM_VIEW -> BaseViewHolder(inflatedItemView)
            else ->  BaseViewHolder(inflatedItemView)
        }
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(getItemViewType(position) == AD_VIEW_LARGE) {
            CoroutineScope(IO).launch {
                val adViewHolder = holder as NativeAdViewHolder
                adViewHolder.loadAds(adUnitId!!)
            }
            return
        }
        val min = position / adInterval
        val obj = getItem(position)
        obj?.let {
            (holder as BaseViewHolder).bind(obj, cb, position)
        }
    }
    
    override fun getItemCount(): Int {
        val s = super.getItemCount()
        var t: Int = s + s / adInterval
        if (s > 0) t++ // +1 when list is not empty
        return t
    }
    
    fun getItemByIndex(idx: Int): ChannelInfo? {
        return getItem(idx)
    }
    
//    override fun onViewRecycled(holder: ViewHolder) {
//        if (holder is BaseViewHolder && holder.binding is ListItemVideosBinding) {
//            holder.binding.poster.setImageDrawable(null)
//        }
//        super.onViewRecycled(holder)
//    }
}