package com.banglalink.toffee.common.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.banglalink.toffee.model.ChannelInfo

open class BasePagingDataAdapter<T: Any>(
    val callback: BaseListItemCallback<T>? = null,
    itemDiffUtil: DiffUtil.ItemCallback<T>
) :PagingDataAdapter<T, BaseViewHolder>(itemDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        adjustLayout(binding)
        return BaseViewHolder(binding)
    }

    open fun adjustLayout(binding: ViewDataBinding){}

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        obj?.let {
            if (it is ChannelInfo && it.isExpired) {
                with (holder.binding.root) {
                    isVisible = false
                    layoutParams = LinearLayout.LayoutParams(0, 0)
                }
            }
            holder.bind(obj, callback, position)
        }
    }

    fun getItemByIndex(idx: Int): T? {
        return getItem(idx)
    }
}