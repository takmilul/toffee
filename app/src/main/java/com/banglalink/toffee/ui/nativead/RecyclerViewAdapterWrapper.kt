package com.banglalink.toffee.ui.nativead

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.ViewHolder

open class RecyclerViewAdapterWrapper(private val wrappedAdapter: Adapter<ViewHolder>) : Adapter<ViewHolder>() {
    init {
        wrappedAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }
            
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }
            
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }
            
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }
            
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return wrappedAdapter.onCreateViewHolder(parent, viewType)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        wrappedAdapter.onBindViewHolder(holder, position)
    }
    
    override fun getItemCount(): Int {
        return wrappedAdapter.itemCount
    }
    
    override fun getItemViewType(position: Int): Int {
        return wrappedAdapter.getItemViewType(position)
    }
    
    override fun setHasStableIds(hasStableIds: Boolean) {
        wrappedAdapter.setHasStableIds(hasStableIds)
    }
    
    override fun getItemId(position: Int): Long {
        return wrappedAdapter.getItemId(position)
    }
    
    override fun onViewRecycled(holder: ViewHolder) {
        wrappedAdapter.onViewRecycled(holder)
    }
    
    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return wrappedAdapter.onFailedToRecycleView(holder)
    }
    
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        wrappedAdapter.onViewAttachedToWindow(holder)
    }
    
    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        wrappedAdapter.onViewDetachedFromWindow(holder)
    }
    
    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        wrappedAdapter.registerAdapterDataObserver(observer)
    }
    
    override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
        wrappedAdapter.unregisterAdapterDataObserver(observer)
    }
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        wrappedAdapter.onAttachedToRecyclerView(recyclerView)
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        wrappedAdapter.onDetachedFromRecyclerView(recyclerView)
    }
}