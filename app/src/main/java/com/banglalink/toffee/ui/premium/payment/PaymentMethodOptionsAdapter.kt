package com.banglalink.toffee.ui.premium.payment

import android.content.res.Configuration
import coil.load
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PackPaymentMethodAdapter(
    private val mPref: SessionPreference,
    private val cPref: CommonPreference,
    private val viewModel: PremiumViewModel,
    cb: BaseListItemCallback<PackPaymentMethodData>,
) : MyBaseAdapter<PackPaymentMethodData>(cb) {

    private val isBanglalinkNumber: Boolean
        get() = mPref.isBanglalinkNumber == "true"

    override fun getLayoutIdForPosition(position: Int): Int = R.layout.list_item_payment_method

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        val paymentMethodCard = holder.itemView.findViewById<CardView>(R.id.paymentMethodCard)
        val passName = holder.itemView.findViewById<TextView>(R.id.passNameTextView)
        val price = holder.itemView.findViewById<TextView>(R.id.premiumPackPriceTextView)
        val logo = holder.itemView.findViewById<ImageView>(R.id.passBrandImageView)
        val eligibleUser = holder.itemView.findViewById<TextView>(R.id.packEligibleUserTextView)

        when (obj.paymentMethodName) {
            "free" -> {
                passName.text = obj.paymentHeadline
                setPriceText(obj.paymentSubHeadlineOneForBl, obj.paymentSubHeadlineOneForNonBl, price)
                logo.hide()
                eligibleUser.hide()
                if (isTrialPackUsed() || (!isBanglalinkNumber && obj.data == null)) {
                    paymentMethodCard.alpha = 0.3f
                }
            }

            "VOUCHER" -> {
                passName.text = obj.paymentHeadline
                setPriceText(obj.paymentSubHeadlineOneForBl, obj.paymentSubHeadlineOneForNonBl, price)
                loadLogo(obj.paymentMethodLogoMobile, logo)
                eligibleUser.hide()
            }

            "blPack" -> {
                passName.text = obj.paymentHeadline
                setBlPackPriceText(obj.paymentSubHeadlineOneForPrepaid, obj.paymentSubHeadlineOneForPostpaid, price)
                loadLogo(obj.paymentMethodLogoMobile, logo)
                eligibleUser.hide()
            }

            "bkash", "ssl", "nagad" -> {
                passName.text = obj.paymentHeadline
                setPriceText(obj.paymentSubHeadlineOneForBl, obj.paymentSubHeadlineOneForNonBl, price)
                loadLogo(obj.paymentMethodLogoMobile, logo)
                eligibleUser.hide()
            }
        }
    }

    private fun isTrialPackUsed(): Boolean {
        return mPref.activePremiumPackList.value?.any { it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed } == true
    }

    private fun setPriceText(blPrice: String?, nonBlPrice: String?, priceTextView: TextView) {
        if (isBanglalinkNumber) {
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

    private fun setBlPackPriceText(prepaidPrice: String?, postpaidPrice: String?, priceTextView: TextView) {
        if (mPref.isPrepaid) {
            priceTextView.text = prepaidPrice ?: ""
            if (prepaidPrice.isNullOrEmpty()) {
                priceTextView.hide()
            }
        } else {
            priceTextView.text = postpaidPrice ?: ""
            if (postpaidPrice.isNullOrEmpty()) {
                priceTextView.hide()
            }
        }
    }

    private fun loadLogo(logoUrl: String?, logoImageView: ImageView) {
        if (!logoUrl.isNullOrEmpty()) {
            var replacedLogoUrl = logoUrl
            val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO
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
