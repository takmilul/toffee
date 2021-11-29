package com.banglalink.toffee.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.model.ChannelInfo

open class SimpleRecyclerAdapter<T: Any>(
    private var items: ArrayList<T> = ArrayList(),
    private val callback: BaseListItemCallback<T>? = null,
): RecyclerView.Adapter<BaseViewHolder>() {

    fun setItems(newItems: List<T>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T? {
        return items.getOrNull(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            holder.bind(obj, callback, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}