package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.databinding.ButtomSheetPackPaymentOptionBinding
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class PackPaymentOptionFragment : ChildDialogFragment() {

    private var _binding: ButtomSheetPackPaymentOptionBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetPackPaymentOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        viewModel.paymentMethod.value?.let {paymentTypes->

            with(binding) {
                val isBlNumber = if (mPref.isBanglalinkNumber == "true")
                {
                    trailTitle.setText(paymentTypes.free?.get(1)?.packDetails)
                    trailDetails.setText("Includes 15 days extra for Banglalink subscribers")
                } else {
                    trailTitle.setText(paymentTypes.free?.get(0)?.packDetails)
                    trailDetails.setText("Extra 15 days for Banglalink users")
                }


                blPackPrice.setText("Starting from BDT "+ paymentTypes.bl?.minimumPrice?.toString())
                bkashPackPrice.setText("Starting from BDT "+ paymentTypes.bkash?.minimumPrice?.toString())


                trailCard.setOnClickListener {
                   if (mPref.isBanglalinkNumber == "true")   viewModel.selectedPaymentMethod.postValue(paymentTypes.free?.get(1))
                    else viewModel.selectedPaymentMethod.postValue(paymentTypes.free?.get(0))

                    mPref.paymentName.value="trail"
                    findNavController().navigate(R.id.action_payment_to_trail)
                }
                blPackCard.setOnClickListener {
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bl?.pREPAID)
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bl?.pOSTPAID)
                   // mPref.paymentName.value="blPack"
                    viewModel.selectedPaymentMethod.postValue(paymentTypes.bl?.pREPAID?.get(0))
                    findNavController().navigate(R.id.action_payment_to_pack, bundleOf("paymentName" to "blPack"))
                }
                bkashPackCard.setOnClickListener {
                    viewModel.selectedPaymentMethod.postValue(paymentTypes.bkash?.dataPacks?.get(0))
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bkash?.dataPacks)
                    //mPref.paymentName.value="bKash"
                    findNavController().navigate(R.id.action_payment_to_pack,bundleOf("paymentName" to "bKash"))

                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}