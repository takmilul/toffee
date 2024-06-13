package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.DiscountApplyOnPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.data.network.response.SystemDiscount
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionsBinding
import com.banglalink.toffee.enums.PaymentMethodName
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PaymentMethodOptionsFragment : ChildDialogFragment(),
	BaseListItemCallback<PackPaymentMethodData> {

	private var _binding: FragmentPaymentMethodOptionsBinding? = null
	private var subType: String? = null
	var isTrialPackUsed = false
	var blTrialPackMethod: PackPaymentMethod? = null
	var nonBlTrialPackMethod: PackPaymentMethod? = null
	private lateinit var mAdapter: PackPaymentMethodAdapter

	private val binding get() = _binding!!
	private val viewModel by activityViewModels<PremiumViewModel>()

	var systemDiscount:SystemDiscount?=null

	/**
	 * discountApplyOnPaymentMethod will have the payment method's discount info which was clicked by the user. this data is send
	 * using bundle to next fragment.
	 */
	var discountApplyOnPaymentMethod: DiscountApplyOnPaymentMethod?=null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentPaymentMethodOptionsBinding.inflate(inflater, container, false)
		return binding.root
	}

	@SuppressLint("ResourceAsColor", "SetTextI18n")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		viewModel.selectedDataPackOption.value = null
		mAdapter = PackPaymentMethodAdapter(requireContext(), mPref, cPref.appThemeMode, viewModel, this)
		binding.packCardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
		binding.packCardRecyclerView.adapter = mAdapter
		
		viewModel.clickableAdInventories.value?.let {
			// navigating to destination by paymentMethodId from deep link
			when (it.paymentMethodId) {
				PaymentMethod.BKASH.value -> {
					findNavController().navigatePopUpTo(
						resId = R.id.paymentDataPackOptionsFragment,
						args = bundleOf("paymentName" to "bkash")
					)
				}

				PaymentMethod.NAGAD.value -> {
					findNavController().navigatePopUpTo(
						resId = R.id.paymentDataPackOptionsFragment,
						args = bundleOf("paymentName" to "nagad")
					)
				}

				PaymentMethod.BL_PACK.value -> {
					findNavController().navigatePopUpTo(
						resId = R.id.paymentDataPackOptionsFragment,
						args = bundleOf("paymentName" to "blPack")
					)

				}

				PaymentMethod.SSL.value -> {
					findNavController().navigatePopUpTo(
						resId = R.id.paymentDataPackOptionsFragment,
						args = bundleOf("paymentName" to "ssl")
					)
				}

				else -> {
					viewModel.clickableAdInventories.value = null
					requireActivity().showToast(getString(R.string.payment_method_invalid))
					mPref.refreshRequiredForClickableAd.value =
						true // refreshing pack details page to destroy this flow
					this@PaymentMethodOptionsFragment.closeDialog()
				}
			}
		} ?: run {
			viewModel.paymentMethod.value?.let { paymentTypes ->
				systemDiscount=paymentTypes.systemDiscount
				var paymentMethodList = listOf<PackPaymentMethodData>()
				/**
				 * When systemDiscount is not nullOrEmpty this means that this pack will have discounted payment.
				 */

				val isSystemDiscountApplicable = (
					(mPref.isBanglalinkNumber == "true" && (systemDiscount?.BL != null || systemDiscount?.BOTH != null)) ||
					(mPref.isBanglalinkNumber == "false" && (systemDiscount?.NONBL != null || systemDiscount?.BOTH != null))
				)

				if (isSystemDiscountApplicable) {
					binding.packSubTitle.show()
					if (mPref.isBanglalinkNumber=="true" && !paymentTypes.displayMessage?.top_promotion_msg_bl.isNullOrEmpty()){

						binding.packSubTitle.text= paymentTypes.displayMessage?.top_promotion_msg_bl
					}else if (mPref.isBanglalinkNumber!="true" && !paymentTypes.displayMessage?.top_promotion_msg_nonbl.isNullOrEmpty()){
						binding.packSubTitle.text= paymentTypes.displayMessage?.top_promotion_msg_nonbl
					}else{
						binding.packSubTitle.hide()
					}

					/**
					 * discountApplyOnPaymentMethod will have the payment method's discount info which was clicked by the user. This data is send
					 * using bundle to next fragment.
					 */

					if (mPref.isBanglalinkNumber=="true"){

						systemDiscount?.BL?.let {

							paymentMethodList = addPaymentMethodsToDisplay(paymentTypes, it.whichPaymentMethodDisplay)
							discountApplyOnPaymentMethod = systemDiscount?.BL?.discountApplyOnPaymentMethod
						} ?: run {

							systemDiscount?.BOTH?.let {

								paymentMethodList = addPaymentMethodsToDisplay(paymentTypes, it.whichPaymentMethodDisplay)
								discountApplyOnPaymentMethod = systemDiscount?.BOTH?.discountApplyOnPaymentMethod
							}
						}

					}
					else {

						systemDiscount?.NONBL?.let {

							paymentMethodList = addPaymentMethodsToDisplay(paymentTypes, it.whichPaymentMethodDisplay)
							discountApplyOnPaymentMethod = systemDiscount?.NONBL?.discountApplyOnPaymentMethod
						} ?: run {

							systemDiscount?.BOTH?.let {

								paymentMethodList = addPaymentMethodsToDisplay(paymentTypes, it.whichPaymentMethodDisplay)
								discountApplyOnPaymentMethod = systemDiscount?.BOTH?.discountApplyOnPaymentMethod
							}
						}
					}

					mAdapter.removeAll()
					mAdapter.addAll(paymentMethodList)
				} else {

					/**
					 * When systemDiscount is nullOrEmpty this means that this pack will not have any discounted payments.
					 */

					binding.packSubTitle.show()
					if (mPref.isBanglalinkNumber=="true" && !paymentTypes.displayMessage?.top_promotion_msg_bl.isNullOrEmpty()){

						binding.packSubTitle.text= paymentTypes.displayMessage?.top_promotion_msg_bl
					}else if (mPref.isBanglalinkNumber!="true" && !paymentTypes.displayMessage?.top_promotion_msg_nonbl.isNullOrEmpty()){
						binding.packSubTitle.text= paymentTypes.displayMessage?.top_promotion_msg_nonbl
					}else{
						binding.packSubTitle.hide()
					}

					var paymentMethodList = listOf<PackPaymentMethodData>()

					/**
					 * whichPaymentMethodDisplay this array list have all the payment method names that will be displayed. For
					 * discounted payments we get the array list inside SystemDiscount class. But in non discounted payments
					 * this list is not provided. We make the ArrayList from our end.
					 */
					var whichPaymentMethodDisplay: ArrayList<String> = arrayListOf()
					paymentTypes.free?.let { whichPaymentMethodDisplay.add(PaymentMethodString.FREE.value) }
					paymentTypes.voucher?.let { whichPaymentMethodDisplay.add(PaymentMethodString.VOUCHER.value) }
					paymentTypes.bl?.let { whichPaymentMethodDisplay.add(PaymentMethodString.BL.value) }
					paymentTypes.bkash?.let { whichPaymentMethodDisplay.add(PaymentMethodString.BKASH.value) }
					paymentTypes.ssl?.let { whichPaymentMethodDisplay.add(PaymentMethodString.SSL.value) }
					paymentTypes.nagad?.let { whichPaymentMethodDisplay.add(PaymentMethodString.NAGAD.value) }

					paymentMethodList = addPaymentMethodsToDisplay(paymentTypes, whichPaymentMethodDisplay)
					mAdapter.removeAll()
					mAdapter.addAll(paymentMethodList)
				}
			}
		}



	}

	private fun addPaymentMethodsToDisplay(paymentTypes: PackPaymentMethodBean, whichPaymentMethodDisplay: ArrayList<String>?): List<PackPaymentMethodData> {
		val packPaymentMethodList: MutableList<PackPaymentMethodData> = mutableListOf()
		whichPaymentMethodDisplay?.forEach {methods->
			when(methods){
				PaymentMethodString.FREE.value->{
					paymentTypes.free?.let {
						if (!it.data.isNullOrEmpty() && ((mPref.isBanglalinkNumber == "true" && !it.paymentHeadlineForBl.isNullOrEmpty()) || (mPref.isBanglalinkNumber == "false" && !it.paymentHeadlineForNonBl.isNullOrEmpty()))) {
							packPaymentMethodList.add(
								it.also {
									it.paymentMethodName = "free"
								}
							)
						}
					}
				}
				PaymentMethodString.VOUCHER.value->{
					paymentTypes.voucher?.let {
						if (!it.data.isNullOrEmpty() && !it.paymentHeadline.isNullOrEmpty()) {
							packPaymentMethodList.add(
								it.also {
									it.paymentMethodName = "VOUCHER"

								}
							)
						}
					}
				}
                PaymentMethodString.BL.value->{
                    paymentTypes.bl?.let {
                        if (
                            (
                                (mPref.isBanglalinkNumber == "true" &&
                                    (
                                        (mPref.isPrepaid && !it.prepaid.isNullOrEmpty()) ||
                                        (!mPref.isPrepaid && !it.postpaid.isNullOrEmpty()) ||
										!it.dcb.isNullOrEmpty()
                                    )
                                ) ||
                                (mPref.isBanglalinkNumber == "false" &&
                                    (!it.prepaid.isNullOrEmpty() || !it.postpaid.isNullOrEmpty() || !it.dcb.isNullOrEmpty())
                                )
                            ) &&
                            !it.paymentHeadline.isNullOrEmpty()
                        ) {
                            packPaymentMethodList.add(
                                it.also {
                                    it.paymentMethodName = "blPack"
                                }
                            )
                        }
                    }
                }
                PaymentMethodString.BKASH.value->{
                    paymentTypes.bkash?.let {
                        if (
                            (
                                (mPref.isBanglalinkNumber == "true" && !it.blPacks.isNullOrEmpty()) ||
                                (mPref.isBanglalinkNumber == "false" && !it.nonBlPacks.isNullOrEmpty())
                            ) &&
                            !it.paymentHeadline.isNullOrEmpty()
                        ) {
                            packPaymentMethodList.add(
                                it.also {
                                    it.paymentMethodName = "bkash"
                                }
                            )
                        }
                    }
                }
                PaymentMethodString.SSL.value->{
                    paymentTypes.ssl?.let {
                        if (
                            (
                                (mPref.isBanglalinkNumber == "true" && !it.blPacks.isNullOrEmpty()) ||
								(mPref.isBanglalinkNumber == "false" && !it.nonBlPacks.isNullOrEmpty())
                            ) &&
                            !it.paymentHeadline.isNullOrEmpty()
                        ){
                            packPaymentMethodList.add(
                                it.also {
                                    it.paymentMethodName = "ssl"
                                }
                            )
                        }
                    }
                }

                PaymentMethodString.NAGAD.value->{
                    paymentTypes.nagad?.let {
                        if (
                            (
                                (mPref.isBanglalinkNumber == "true" && !it.blPacks.isNullOrEmpty()) ||
                                (mPref.isBanglalinkNumber == "false" && !it.nonBlPacks.isNullOrEmpty())
                            ) &&
                            !it.paymentHeadline.isNullOrEmpty()
                        ) {
                            packPaymentMethodList.add(
                                it.also {
                                    it.paymentMethodName = "nagad"
                                }
                            )
                        }
                    }
                }
			}
		}
		return packPaymentMethodList.sortedBy { it.orderIndex }
	}
	override fun onItemClicked(item: PackPaymentMethodData) {
		super.onItemClicked(item)
		subType = when {
			(mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "prepaid"
			(mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "postpaid"
			(!(mPref.isBanglalinkNumber).toBoolean()) -> "N/A"
			else -> null
		}
			if (systemDiscount!=null){

				viewModel.selectedPackSystemDiscount.value=systemDiscount
			}

		when (item.paymentMethodName) {
			PaymentMethodName.FREE.value -> {
				mPref.selectedPaymentType.value = PaymentMethodName.FREE.value
				mPref.activePremiumPackList.value?.find {
					it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
				}?.let { isTrialPackUsed = true }

				if (isTrialPackUsed) {
					requireContext().showToast(getString(string.trial_already_availed_text))
				} else if (mPref.isBanglalinkNumber != "true" && item.data == null) {
					requireContext().showToast(getString(string.only_for_bl_users))
				} else if (mPref.isBanglalinkNumber != "false" && item.data == null) {
					requireContext().showToast(getString(string.only_for_non_bl_users))
				} else {
					findNavController().navigateTo(R.id.activateTrialPackFragment)
				}
			}

			PaymentMethodName.VOUCHER.value -> {
				mPref.selectedPaymentType.value = PaymentMethodName.VOUCHER.value
				//Send Log to FirebaseAnalytics
				ToffeeAnalytics.toffeeLogEvent(
					ToffeeEvents.PAYMENT_SELECTED,
					bundleOf(
						"pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
						"pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
						"provider" to "Banglalink",
						"type" to "coupon",
						"MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
						"subtype" to subType,
					)
				)
				findNavController().navigateTo(
					R.id.reedemVoucherCodeFragment,
					bundleOf("paymentName" to item.paymentMethodName)
				)
			}

			PaymentMethodName.BL.value -> {
				//Send Log to FirebaseAnalytics
				mPref.selectedPaymentType.value = PaymentMethodName.BL.value
				ToffeeAnalytics.toffeeLogEvent(
					ToffeeEvents.PAYMENT_SELECTED,
					bundleOf(
						"pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
						"pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
						"provider" to "Banglalink",
						"type" to "data pack",
						"MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
						"subtype" to subType,
					)
				)
				mPref.paymentDiscountPercentage.value=discountApplyOnPaymentMethod?.DCB

				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName,
						"discount" to discountApplyOnPaymentMethod?.DCB)
				)
			}

			PaymentMethodName.BKASH.value -> {
				mPref.selectedPaymentType.value = PaymentMethodName.BKASH.value
				//Send Log to FirebaseAnalytics
				ToffeeAnalytics.toffeeLogEvent(
					ToffeeEvents.PAYMENT_SELECTED,
					bundleOf(
						"pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
						"pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
						"provider" to "bKash",
						"type" to "wallet",
						"MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
						"subtype" to subType,
					)
				)
				mPref.paymentDiscountPercentage.value=discountApplyOnPaymentMethod?.BKASH
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName,
						"discount" to discountApplyOnPaymentMethod?.BKASH)
				)
			}

			PaymentMethodName.SSL.value -> {
				mPref.selectedPaymentType.value = PaymentMethodName.SSL.value
				//Send Log to FirebaseAnalytics
				ToffeeAnalytics.toffeeLogEvent(
					ToffeeEvents.PAYMENT_SELECTED,
					bundleOf(
						"pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
						"pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
						"provider" to "SSL Wireless",
						"type" to "aggregator",
						"MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
						"subtype" to subType,
					)
				)
				mPref.paymentDiscountPercentage.value=discountApplyOnPaymentMethod?.SSL
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName,
						"discount" to discountApplyOnPaymentMethod?.SSL)
				)
			}

			PaymentMethodName.NAGAD.value -> {

				mPref.selectedPaymentType.value = PaymentMethodName.NAGAD.value
				//Send Log to FirebaseAnalytics
				ToffeeAnalytics.toffeeLogEvent(
					ToffeeEvents.PAYMENT_SELECTED,
					bundleOf(
						"pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
						"pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
						"provider" to "nagad",
						"type" to "wallet",
						"MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
						"subtype" to subType,
					)
				)
				mPref.paymentDiscountPercentage.value=discountApplyOnPaymentMethod?.NAGAD
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName,
						"discount" to discountApplyOnPaymentMethod?.NAGAD,)
				)


			}
		}
	}



	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}