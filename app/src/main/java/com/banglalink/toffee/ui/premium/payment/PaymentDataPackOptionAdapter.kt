package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.px
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
            if (position > 0) {
                val optionTitle = holder.itemView.findViewById<ConstraintLayout>(R.id.dataPackOptionsTitleContainer)
                optionTitle.setPadding(0, 32.px, 0, 0)
            }
            return
        } else {
            obj.let {
                val radioButton = holder.itemView.findViewById<RadioButton>(R.id.dataPackOptionRadioButton)
                val packOptionContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.packOptionContainerOne)
                val priceTv = holder.itemView.findViewById<TextView>(R.id.dataPackOptionAmountTextView)
                val priceBeforeDiscount = holder.itemView.findViewById<TextView>(R.id.dataPackPriceBeforeDiscountTv)
//                radioButton.isChecked = obj.dataPackId==selectedItem?.dataPackId
                radioButton.setOnCheckedChangeListener(null)

                radioButton.isChecked = position === selectedPosition
                packOptionContainer.background = if (radioButton.isChecked) ContextCompat.getDrawable(context, R.drawable.subscribe_bg_round_pass) else null

                radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                    selectedPosition = position

                    notifyDataSetChanged()
                }

                Log.d("TAG", "paymentDiscountPercentage: "+mPref.paymentDiscountPercentage.value.toString())
                if (mPref.paymentDiscountPercentage.value.isNullOrEmpty()){
                    priceBeforeDiscount.visibility= View.GONE
                    priceTv.text=" BDT "+obj.packPrice.toString()
                }else{
                    priceBeforeDiscount.visibility= View.VISIBLE
                    var discountedPrice = calculateDiscountedPrice(obj.packPrice!!.toDouble(),mPref.paymentDiscountPercentage.value!!)
                    priceTv.text="BDT "+discountedPrice.toInt()
                    priceBeforeDiscount.text="BDT "+obj.packPrice.toString()
                    priceBeforeDiscount.paintFlags = priceBeforeDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                }
            }
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: PackPaymentMethod?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    fun calculateDiscountedPrice(originalPrice: Double, discountPercent: String): Double {
        val discountPercentage = discountPercent.toDouble()
        val discountAmount = originalPrice * (discountPercentage / 100)
        val discountedPrice = originalPrice - discountAmount

        val integerPart = discountedPrice.toInt()
        val decimalPart = discountedPrice - integerPart

         if (decimalPart > 0) {
          return  integerPart + 1.0
        } else {
          return  discountedPrice
        }


    }
}