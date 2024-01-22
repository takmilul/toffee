package com.banglalink.toffee.ui.premium

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder

class SubscriptionHistoryFooterAdapter(private val baseAdapter: MyBaseAdapter<*>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val footerViewType = 1
    private var footerText: String = ""

    fun setFooterText(text: String) {
        footerText = text
        notifyItemChanged(itemCount - 1) // Notify the footer item to update its text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == footerViewType) {
            val footerView = LayoutInflater.from(parent.context).inflate(R.layout.subscription_history_footer_layout, parent, false)
            FooterViewHolder(footerView)
        } else {
            baseAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FooterViewHolder) {
            holder.historyShowingText.text = footerText
        } else {
            baseAdapter.onBindViewHolder(holder as MyViewHolder, position)
        }
    }

    override fun getItemCount(): Int {
        return baseAdapter.itemCount + 1
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == baseAdapter.itemCount) { // Return the view type for the footer view
            footerViewType
        } else { // Return the view type for the regular view
            baseAdapter.getItemViewType(position)
        }
    }
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val historyShowingText: TextView = itemView.findViewById(R.id.historyShowingText)
    }
}
