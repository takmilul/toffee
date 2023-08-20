package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentReedemVoucherCodeBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.toInt
import com.banglalink.toffee.extension.validateInput
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import javax.inject.Inject

class RedeemVoucherCodeFragment : ChildDialogFragment() {

    private lateinit var binding: FragmentReedemVoucherCodeBinding
    private var voucherCode: String = ""
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReedemVoucherCodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null)
        binding.backImg.safeClick({
            findNavController().popBackStack()
        })

        binding.redeemVoucherBtn.safeClick({
            voucherCode = binding.giftVoucherCode.text.toString().trim()

            if (voucherCode.isBlank()) {
                binding.giftVoucherCode.validateInput(
                    binding.tvGiftVoucherCodeError,
                    R.string.voucher_code_not_valid,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
                binding.tvGiftVoucherCodeError.visibility = View.VISIBLE
                binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(),R.drawable.ic_not_verified), null)
            }
            else{
                viewModel.voucherValidate(viewModel.selectedPremiumPack.value!!.id, voucherCode, viewModel.selectedPremiumPack.value!!.packTitle.toString())
            }
        })

        observeVoucher()
        observeVoucherPurchase()
    }

    private fun observeVoucher() {
        observe(viewModel.voucherPaymentState) {
            when(it) {

                is Resource.Success -> {

                    if (it.data?.isValidVoucher==true){

                        val selectedPremiumPack = viewModel.selectedPremiumPack.value!!

                        val dataPackPurchaseRequest = DataPackPurchaseRequest(

                            customerId = mPref.customerId,
                            password = mPref.password,
                            isBanglalinkNumber = (mPref.isBanglalinkNumber == "true").toInt(),
                            packId = selectedPremiumPack.id,
                            paymentMethodId = 12,
                            packTitle = selectedPremiumPack.packTitle,
                            contentList = selectedPremiumPack.contentId,
                            packCode = "",
                            packDetails = "",
                            packPrice = 0,
                            packDuration = it.data?.campaignsDuration ?: 0,

                            voucher = voucherCode,
                            partnerType = it.data?.partnerType,
                            partnerName = it.data?.partnerName,
                            partnerId =  it.data?.partnerId,
                            partnerCampaignsName = it.data?.partnerCampaignsName,
                            partnerCampaignsId = it.data?.partnerCampaignsId,
                            campaignsExpireDate = it.data?.campaignsExpireDate
                        )
                        Log.d("TAG", "purchaseWithVoucher: "+dataPackPurchaseRequest.toString())
                        viewModel.purchaseDataPackVoucher(dataPackPurchaseRequest)
                    }else{
                        binding.giftVoucherCode.validateInput(
                            binding.tvGiftVoucherCodeError,
                            R.string.voucher_code_not_valid,
                            R.color.pink_to_accent_color,
                            R.drawable.error_single_line_input_text_bg
                        )
                        binding.tvGiftVoucherCodeError.visibility = View.VISIBLE
                        binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(),R.drawable.ic_not_verified), null)

                    }



                }
                is Resource.Failure -> {

                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun observeVoucherPurchase() {
        observe(viewModel.packPurchaseResponseVoucher) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {

                    if (it.data.status == PaymentStatusDialog.SUCCESS) {
                        mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 0),
                            PaymentStatusDialog.ARG_STATUS_TITLE to "Gift Voucher Redemption is Successful",
                            PaymentStatusDialog.ARG_STATUS_MESSAGE to "Please wait while we redirect you"
                        )
                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    }
                    else if (it.data.status == PaymentStatusDialog.UN_SUCCESS){
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 0),
                            PaymentStatusDialog.ARG_STATUS_TITLE to "Voucher Activation Failed!",
                            PaymentStatusDialog.ARG_STATUS_MESSAGE to "Due to some technical error, the Voucher activation failed. Please retry."
                        )
                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    }
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

}