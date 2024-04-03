package com.banglalink.toffee.ui.premium.payment

import android.content.res.Configuration
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder
import com.banglalink.toffee.ui.premium.PremiumViewModel


class PackPaymentMethodAdapter(
    val mPref: SessionPreference,
    val cPref: CommonPreference,
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

                if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty() || obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForNonBl ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            obj.paymentSubHeadlineOneForBl
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                }
                logo.hide()
                eligibleUser.hide()

                mPref.activePremiumPackList.value?.find {
                    it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
                }?.let { isTrialPackUsed = true }

                if (isTrialPackUsed || (mPref.isBanglalinkNumber != "true" && obj.data == null)) {
                    paymentMethodCard.alpha = 0.3f
                }
            }

            "VOUCHER" -> {
                passName.text = obj.paymentHeadline

                if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty() || obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForNonBl ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            obj.paymentSubHeadlineOneForBl
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                }

                var logoUrl = obj.paymentMethodLogoMobile
                val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO

                if (logoUrl?.contains("theme") == true) {
                    logoUrl = logoUrl.replace("theme", if (isLightMode) "light" else "dark")
                }
                logo.load(logoUrl) {
                    placeholder(R.drawable.placeholder)
                }

                eligibleUser.hide()
            }

            "blPack" -> {
                passName.text = obj.paymentHeadline

                if (obj.paymentSubHeadlineOneForPrepaid.isNullOrEmpty() || obj.paymentSubHeadlineOneForPostpaid.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForPrepaid.isNullOrEmpty()) {
                        price.text = (if (mPref.isPrepaid) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForPostpaid ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForPostpaid.isNullOrEmpty()) {
                        price.text = (if (mPref.isPrepaid) {
                            obj.paymentSubHeadlineOneForPrepaid ?: ""
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isPrepaid) obj.paymentSubHeadlineOneForPrepaid ?: "" else obj.paymentSubHeadlineOneForPostpaid ?: ""
                }

                var logoUrl = obj.paymentMethodLogoMobile
                val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO

                if (logoUrl?.contains("theme") == true) {
                    logoUrl = logoUrl.replace("theme", if (isLightMode) "light" else "dark")
                }
                logo.load(logoUrl) {
                    placeholder(R.drawable.placeholder)
                }
                eligibleUser.hide()
            }

            "bkash" -> {
                passName.text = obj.paymentHeadline

                if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty() || obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForNonBl ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            obj.paymentSubHeadlineOneForBl
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                }

                var logoUrl = obj.paymentMethodLogoMobile
                val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO

                if (logoUrl?.contains("theme") == true) {
                    logoUrl = logoUrl.replace("theme", if (isLightMode) "light" else "dark")
                }
                logo.load(logoUrl) {
                    placeholder(R.drawable.placeholder)
                }

                eligibleUser.hide()
            }

            "ssl" -> {
                passName.text = obj.paymentHeadline

                if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty() || obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForNonBl ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            obj.paymentSubHeadlineOneForBl
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                }

                var logoUrl = obj.paymentMethodLogoMobile
                val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO

                if (logoUrl?.contains("theme") == true) {
                    logoUrl = logoUrl.replace("theme", if (isLightMode) "light" else "dark")
                }
                logo.load(logoUrl) {
                    placeholder(R.drawable.placeholder)
                }
                eligibleUser.hide()
            }

            "nagad" -> {
                passName.text = obj.paymentHeadline

                if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty() || obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                    if (obj.paymentSubHeadlineOneForBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            price.hide()
                            ""
                        } else {
                            obj.paymentSubHeadlineOneForNonBl ?: ""
                        }).toString()
                    } else if(obj.paymentSubHeadlineOneForNonBl.isNullOrEmpty()) {
                        price.text = (if (mPref.isBanglalinkNumber.toBoolean()) {
                            obj.paymentSubHeadlineOneForBl
                        } else {
                            price.hide()
                            ""
                        }).toString()
                    } else {
                        price.hide()
                    }
                }
                else{
                    price.text = if (mPref.isBanglalinkNumber == "true") obj.paymentSubHeadlineOneForBl ?: "" else obj.paymentSubHeadlineOneForNonBl ?: ""
                }

                var logoUrl = obj.paymentMethodLogoMobile
                val isLightMode = cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO

                if (logoUrl?.contains("theme") == true) {
                    logoUrl = logoUrl.replace("theme", if (isLightMode) "light" else "dark")
                }
                logo.load(logoUrl) {
                    placeholder(R.drawable.placeholder)
                }
                eligibleUser.hide()
            }
        }
    }
}

