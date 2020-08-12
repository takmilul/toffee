package com.banglalink.toffee.ui.common

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater


abstract class MyBaseAdapterV2<T: Any>(val callback: SingleListItemCallback<T>? = null)
    :RecyclerView.Adapter<MyViewHolderV2>() {

    protected val values: MutableList<T> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolderV2 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return MyViewHolderV2(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolderV2,
        position: Int
    ) {
        val obj = getObjForPosition(position)
        holder.bind(obj)
        holder.bindPosition(position)
        holder.bindCallBack(callback)
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition(position)
    }

    fun getObjForPosition(position: Int): T = values[position]

    protected abstract fun getLayoutIdForPosition(position: Int): Int




    override fun getItemCount():Int {
        return values.size
    }

    fun add(item: T) = values.add(item)

    fun add(position: Int, item: T) = values.add(position, item)

    fun addAll(items: List<T>) {
        val positionStart = values.size
        values.addAll(items)
        notifyItemRangeInserted(positionStart, items.size)
    }

    fun remove(item: T){
        val index = values.indexOf(item)
        values.removeAt(index)
        notifyItemRemoved(index)
    }

    fun remove(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    fun reloadItem(item: T) {
        val idx = values.indexOf(item)
        notifyItemChanged(idx)
    }

    fun reloadItem(pos: Int) {
        notifyItemChanged(pos)
    }

    fun getItem(position: Int): T? {
        return values[position]
    }

    fun getItems(): List<T> = values

    fun removeAll()  {
        values.clear()
        notifyDataSetChanged()
    }
}