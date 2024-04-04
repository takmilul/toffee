package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionsBinding
import com.banglalink.toffee.enums.PaymentMethodName
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
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
		mAdapter = PackPaymentMethodAdapter(mPref, cPref.appThemeMode, viewModel, this)
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
				val packPaymentMethodList: MutableList<PackPaymentMethodData> = mutableListOf()
				paymentTypes.free?.let {
					if (!it.data.isNullOrEmpty() && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "free"
							}
						)
					}
				}
				paymentTypes.voucher?.let {
					if (!it.data.isNullOrEmpty() && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "VOUCHER"
							}
						)
					}
				}
				paymentTypes.bl?.let {
					if ((!it.prepaid.isNullOrEmpty() || !it.prepaid.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "blPack"
							}
						)
					}
				}
				paymentTypes.bkash?.let {
					if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "bkash"
							}
						)
					}
				}
				paymentTypes.ssl?.let {
					if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "ssl"
							}
						)
					}
				}
				paymentTypes.nagad?.let {
					if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()) {
						packPaymentMethodList.add(
							it.also {
								it.paymentMethodName = "nagad"
							}
						)
					}
				}

				mAdapter.removeAll()
				mAdapter.addAll(packPaymentMethodList.sortedBy { it.orderIndex })
			}
		}

	}

	override fun onItemClicked(item: PackPaymentMethodData) {
		super.onItemClicked(item)
		subType = when {
			(mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "prepaid"
			(mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "postpaid"
			(!(mPref.isBanglalinkNumber).toBoolean()) -> "N/A"
			else -> null
		}

		when (item.paymentMethodName) {
			PaymentMethodName.FREE.value -> {
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
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName)
				)
			}

			PaymentMethodName.BKASH.value -> {
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
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName)
				)
			}

			PaymentMethodName.SSL.value -> {
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
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName)
				)
			}

			PaymentMethodName.NAGAD.value -> {
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
				findNavController().navigateTo(
					R.id.paymentDataPackOptionsFragment,
					bundleOf("paymentName" to item.paymentMethodName)
				)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}