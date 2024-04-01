package com.banglalink.toffee.ui.premium.payment

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder
import com.banglalink.toffee.ui.premium.PremiumViewModel


class PackPaymentMethodAdapter(
    val mPref: SessionPreference,
    private val viewModel: PremiumViewModel,
cb: BaseListItemCallback<PackPaymentMethodData>,
) : MyBaseAdapter<PackPaymentMethodData>(cb) {
    var isTrialPackUsed = false
    var blTrialPackMethod: PackPaymentMethod? = null
    var nonBlTrialPackMethod: PackPaymentMethod? = null
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_payment_method
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        val paymentMethodCard = holder.itemView.findViewById<CardView>(R.id.paymentMethodCard)
        val passName = holder.itemView.findViewById<TextView>(R.id.passNameTextView)
        val price = holder.itemView.findViewById<TextView>(R.id.premiumPackPriceTextView)
        val eligibleUser = holder.itemView.findViewById<TextView>(R.id.packEligibleUserTextView)
        val logo = holder.itemView.findViewById<ImageView>(R.id.passBrandImageView)

        when(obj.paymentMethodName) {
            "free" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                logo.hide()
                eligibleUser.hide()

                mPref.activePremiumPackList.value?.find {
                    it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
                }?.let { isTrialPackUsed = true }

                if (isTrialPackUsed || (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null)) {
                    paymentMethodCard.alpha = 0.3f
                }
            }

            "VOUCHER" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                logo.setImageResource(R.drawable.ic_gift_voucher)
                eligibleUser.hide()
            }

            "blPack" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isPrepaid) obj.paymentSubHeadlineOneForPrepaid ?: "" else obj.paymentSubHeadlineOneForPostpaid ?: ""
                logo.setImageResource(R.drawable.ic_banglalink_logo)
                eligibleUser.hide()
            }

            "bkash" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                logo.setImageResource(R.drawable.bkash_logo_new)
                eligibleUser.hide()
            }

            "ssl" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                logo.setImageResource(R.drawable.ic_ssl_icon)
                eligibleUser.hide()
            }

            "nagad" -> {
                passName.text = obj.paymentHeadline
                price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                logo.setImageResource(R.drawable.ic_nagad_logo)
                eligibleUser.hide()
            }
        }
    }

}

