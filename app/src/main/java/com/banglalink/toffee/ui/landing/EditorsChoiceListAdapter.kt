package com.banglalink.toffee.ui.landing

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ListItemEditorsChoice2Binding
import com.banglalink.toffee.databinding.ListItemEditorsChoiceBinding
import com.banglalink.toffee.model.ChannelInfo

class EditorsChoiceListAdapter(
    cb: ProviderIconCallback<ChannelInfo>
):BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {

    override fun getItemViewType(position: Int): Int {
        return if(position % 2  == 0)
            R.layout.list_item_editors_choice
        else
            R.layout.list_item_editors_choice2
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if(holder.binding is ListItemEditorsChoiceBinding){
            holder.binding.poster.setImageDrawable(null)
        }
        else if (holder.binding is ListItemEditorsChoice2Binding){
            holder.binding.poster.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }
}