package com.banglalink.toffee.ui.premium.payment

import android.content.Context
import android.content.res.Configuration
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.PaymentMethodName
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PackPaymentMethodAdapter(
    private val context: Context, // Add context here
    private val mPref: SessionPreference,
    private val appThemeMode: Int,
    private val viewModel: PremiumViewModel,
    cb: BaseListItemCallback<PackPaymentMethodData>,
) : MyBaseAdapter<PackPaymentMethodData>(cb) {
    var blTrialPackMethod: PackPaymentMethod? = null
    var nonBlTrialPackMethod: PackPaymentMethod? = null
    private val isBanglalinkNumber: Boolean
        get() = mPref.isBanglalinkNumber == "true"
    
    override fun getLayoutIdForPosition(position: Int): Int = R.layout.list_item_payment_method
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        val paymentMethodCardView = holder.itemView.findViewById<CardView>(R.id.paymentMethodCard)
        val passNameTextView = holder.itemView.findViewById<TextView>(R.id.passNameTextView)
        val priceTextView = holder.itemView.findViewById<TextView>(R.id.premiumPackPriceTextView)
        val logoImageView = holder.itemView.findViewById<ImageView>(R.id.passBrandImageView)
        val eligibleUserTextView = holder.itemView.findViewById<TextView>(R.id.packEligibleUserTextView)
        
        passNameTextView.text = if (obj.paymentMethodName == PaymentMethodName.FREE.value) {
            if (isBanglalinkNumber)
                obj.paymentHeadlineForBl
            else
                obj.paymentHeadlineForNonBl
        } else {
            obj.paymentHeadline
        }
        
        when (obj.paymentMethodName) {
            PaymentMethodName.FREE.value -> {
                obj.data?.forEach {
                    if (it.isNonBlFree == 1) {
                        nonBlTrialPackMethod = it
                    } else {
                        blTrialPackMethod = it
                    }
                }
                setPriceText(
                    obj.paymentSubHeadlineOneForBl,
                    obj.paymentSubHeadlineOneForNonBl,
                    isBanglalinkNumber,
                    priceTextView
                )
                logoImageView.hide()

                if (isBanglalinkNumber) {
                    if (obj.data != null) {
                        viewModel.selectedDataPackOption.value = blTrialPackMethod
                    } else {
                        paymentMethodCardView.alpha = 0.3f
                    }
                } else {
                    if (obj.data != null) {
                        viewModel.selectedDataPackOption.value = nonBlTrialPackMethod
//                        priceTextView.setTextColor(ContextCompat.getColor(context, R.color.trial_extra_text_color))
                    } else {
                        paymentMethodCardView.alpha = 0.3f
//                        priceTextView.setTextColor(ContextCompat.getColor(context, R.color.trial_extra_text_color))
                    }
                }

                if (isTrialPackUsed() || (!isBanglalinkNumber && obj.data == null)) {
                    paymentMethodCardView.alpha = 0.3f
                }
            }
            
            PaymentMethodName.VOUCHER.value -> {
                setPriceText(
                    obj.paymentSubHeadlineOneForBl,
                    obj.paymentSubHeadlineOneForNonBl,
                    isBanglalinkNumber,
                    priceTextView
                )
                loadLogo(obj.paymentMethodLogoMobile, logoImageView)
            }
            
            PaymentMethodName.BL.value -> {
                setPriceText(
                    obj.paymentSubHeadlineOneForPrepaid,
                    obj.paymentSubHeadlineOneForPostpaid,
                    mPref.isPrepaid,
                    priceTextView
                )
                loadLogo(obj.paymentMethodLogoMobile, logoImageView)
            }
            
            PaymentMethodName.BKASH.value,
            PaymentMethodName.NAGAD.value,
            PaymentMethodName.SSL.value -> {
                setPriceText(
                    obj.paymentSubHeadlineOneForBl,
                    obj.paymentSubHeadlineOneForNonBl,
                    isBanglalinkNumber,
                    priceTextView
                )
                loadLogo(obj.paymentMethodLogoMobile, logoImageView)
            }
        }
    }
    
    private fun isTrialPackUsed(): Boolean {
        return mPref.activePremiumPackList.value?.any { it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed } == true
    }
    
    private fun setPriceText(blPrice: String?, nonBlPrice: String?, status: Boolean, priceTextView: TextView) {
        if (status) {
            priceTextView.text = blPrice ?: ""
            if (blPrice.isNullOrEmpty()) {
                priceTextView.hide()
            }
        } else {
            priceTextView.text = nonBlPrice ?: ""
            if (nonBlPrice.isNullOrEmpty()) {
                priceTextView.hide()
            }
        }
    }
    
    private fun loadLogo(logoUrl: String?, logoImageView: ImageView) {
        if (!logoUrl.isNullOrEmpty()) {
            var replacedLogoUrl = logoUrl
            val isLightMode = appThemeMode == Configuration.UI_MODE_NIGHT_NO
            if (replacedLogoUrl.contains("theme")) {
                replacedLogoUrl = replacedLogoUrl.replace("theme", if (isLightMode) "light" else "dark")
            }
            logoImageView.load(replacedLogoUrl) {
                placeholder(R.drawable.placeholder)
            }
        } else {
            logoImageView.setImageResource(R.drawable.placeholder)
        }
    }
}
