package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.content.Context
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.DataPackOptionCallback
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder

class PaymentDataPackOptionAdapter(
    val context: Context,
    val mPref: SessionPreference,
    cb: DataPackOptionCallback<PackPaymentMethod>,
) : MyBaseAdapter<PackPaymentMethod>(cb) {
    
    var selectedPosition = -1
    private var selectedItem: PackPaymentMethod? = null
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return if (values[position].listTitle != null) {
            R.layout.list_item_data_pack_option_title
        } else {
            R.layout.list_item_data_pack_option
        }
    }
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        if (obj.listTitle != null) {
            if (obj.isDisabled == true) {
                val titleContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.dataPackOptionsTitleContainer)
                titleContainer.alpha = 0.3f
            }
            return
        } else {
            obj.let {
                val radioButton = holder.itemView.findViewById<RadioButton>(R.id.dataPackOptionRadioButton)
                val amountTextView = holder.itemView.findViewById<TextView>(R.id.dataPackOptionAmountTextView)
                val packOptionContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.packOptionContainerOne)
                
                if (it.isDisabled == true) {
                    radioButton.alpha = 0.3f
                    amountTextView.alpha = 0.3f
                    radioButton.setOnClickListener {
                        radioButton.isChecked = false
                        context.showToast(context.getString(string.only_for_bl_users))
                    }
                } else {
                    radioButton.isChecked = position == selectedPosition
                    packOptionContainer.background = if (radioButton.isChecked) ContextCompat.getDrawable(context, R.drawable.subscribe_bg_round_pass) else null
                }
            }
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: PackPaymentMethod?) {
        selectedItem = item
        notifyDataSetChanged()
    }
}