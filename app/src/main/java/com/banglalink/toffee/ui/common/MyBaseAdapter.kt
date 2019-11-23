package com.foxrentacar.foxpress.ui.common

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.view.LayoutInflater


abstract class MyBaseAdapter<T: Any>(val callback:(T)->Unit={}) : RecyclerView.Adapter<MyViewHolder>() {

    protected val values: MutableList<T> = mutableListOf()


    var removedItemCount = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val obj = getObjForPosition(position)

        holder.itemView.setOnClickListener{callback(obj)}

        holder.bind(obj)
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition(position)
    }

    fun getObjForPosition(position: Int): T = values[position]

    protected abstract fun getLayoutIdForPosition(position: Int): Int




    override fun getItemCount():Int {
        return values.size
    }

//    fun getOffset():Int{
//        return values.size+removedItemCount
//    }

    fun add(item: T) = values.add(item)

    fun add(position: Int, item: T) = values.add(position, item)

    fun addAll(items: List<T>) {
        val positionStart = values.size
        values.addAll(items)
        notifyItemRangeInserted(positionStart, items.size)
    }

    fun remove(item: T){
        removedItemCount++
        val index = values.indexOf(item)
        values.removeAt(index)
        notifyItemRemoved(index)
    }

    fun remove(position: Int) {
        removedItemCount++
        values.removeAt(position)
        notifyItemRemoved(position)
    }


    fun getItem(position: Int): T? {
        return values[position]
    }

    fun getItems(): List<T> = values

    fun removeAll()  {
        clearOffset()
        values.clear()
    }

    fun clearOffset(){
        removedItemCount = 0
    }
}