package com.banglalink.toffee.ui.premium.payment

import android.widget.CompoundButton
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.ListItemChoosePackBinding
import com.banglalink.toffee.model.ChannelInfo


class ChoosePackAdapter ( cb: ProviderIconCallback<ChannelInfo>
): BasePagingDataAdapter<ChannelInfo>(cb, ItemComparator()) {
    var selectedPosition = -1
    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_choose_pack
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if(holder.binding is ListItemChoosePackBinding){
            holder.binding.radioButton.setOnCheckedChangeListener(null)
            holder.binding.radioButton.setChecked(position === selectedPosition)
            holder.binding.radioButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                selectedPosition = position
                notifyDataSetChanged()
            })
            }
        }



}