package com.banglalink.toffee.ui.premium.payment

import android.content.Context
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder

class PaymentDataPackOptionAdapter(
    val context: Context,
    cb: BaseListItemCallback<PackPaymentMethod>,
) : MyBaseAdapter<PackPaymentMethod>(cb) {
    
    private var selectedItem: PackPaymentMethod? = null
    var selectedPosition = -1
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return if (values[position].listTitle != null) {
            R.layout.item_data_pack_option_title
        } else {
            R.layout.item_data_pack_option
        }
    }
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        if (obj.listTitle != null) {
            return
        } else {
            obj.let {
                val radioButton = holder.itemView.findViewById<RadioButton>(R.id.prepaidRadioButtonOne)
                val packOptionContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.packOptionContainerOne)
//                radioButton.isChecked = obj.dataPackId==selectedItem?.dataPackId
                radioButton.setOnCheckedChangeListener(null)
                
                radioButton.isChecked = position === selectedPosition
                packOptionContainer.background = if (radioButton.isChecked) ContextCompat.getDrawable(
                    context, R.drawable.subscribe_bg_round_pass
                ) else null
                
                radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                    selectedPosition = position
                    notifyDataSetChanged()
                }
            }
        }
    }
    
    fun setSelectedItem(item: PackPaymentMethod?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}