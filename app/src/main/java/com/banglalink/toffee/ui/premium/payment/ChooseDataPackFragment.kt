package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ButtomSheetChooseDataPackBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment

class ChooseDataPackFragment : ChildDialogFragment() {

    private var _binding: ButtomSheetChooseDataPackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetChooseDataPackBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){

            //BKASH SECTION
            bkashRadioButtonOne.setOnClickListener {
                bkashRadioButtonTwo.isChecked=false
                bkashRadioButtonThree.isChecked=false
                bkashRadioButtonFour.isChecked=false

                bkashConstraintOne.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                bkashConstraintTwo.setBackgroundResource(0)
                bkashConstraintThree.setBackgroundResource(0)
                bkashConstraintFour.setBackgroundResource(0)

                showPaymentOption()
            }
            bkashRadioButtonTwo.setOnClickListener {
                bkashRadioButtonOne.isChecked=false
                bkashRadioButtonThree.isChecked=false
                bkashRadioButtonFour.isChecked=false

                bkashConstraintOne.setBackgroundResource(0)
                bkashConstraintTwo.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                bkashConstraintThree.setBackgroundResource(0)
                bkashConstraintFour.setBackgroundResource(0)

                showPaymentOption()
            }
            bkashRadioButtonThree.setOnClickListener {
                bkashRadioButtonOne.isChecked=false
                bkashRadioButtonTwo.isChecked=false
                bkashRadioButtonFour.isChecked=false

                bkashConstraintOne.setBackgroundResource(0)
                bkashConstraintTwo.setBackgroundResource(0)
                bkashConstraintThree.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                bkashConstraintFour.setBackgroundResource(0)

                showPaymentOption()

            }
            bkashRadioButtonFour.setOnClickListener {
                bkashRadioButtonOne.isChecked=false
                bkashRadioButtonTwo.isChecked=false
                bkashRadioButtonThree.isChecked=false

                bkashConstraintOne.setBackgroundResource(0)
                bkashConstraintTwo.setBackgroundResource(0)
                bkashConstraintThree.setBackgroundResource(0)
                bkashConstraintFour.setBackgroundResource(R.drawable.subscribe_bg_round_pass)

                showPaymentOption()
            }


            //DATAPACK SECTION
            prepaidRadioButtonOne.setOnClickListener {
                prepaidRadioButtonTwo.isChecked=false
                postPaidRadioButtonOne.isChecked=false
                postPaidRadioButtonTwo.isChecked=false

                packOptionContainerOne.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                packOptionContainerTwo.setBackgroundResource(0)
                postOptionContainerOne.setBackgroundResource(0)
                postPackOptionContainerTwo.setBackgroundResource(0)

                showPaymentOption()
            }
            prepaidRadioButtonTwo.setOnClickListener {
                prepaidRadioButtonOne.isChecked=false
                postPaidRadioButtonOne.isChecked=false
                postPaidRadioButtonTwo.isChecked=false

                packOptionContainerOne.setBackgroundResource(0)
                packOptionContainerTwo.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                postOptionContainerOne.setBackgroundResource(0)
                postPackOptionContainerTwo.setBackgroundResource(0)

                showPaymentOption()
            }
            postPaidRadioButtonOne.setOnClickListener {
                prepaidRadioButtonOne.isChecked=false
                prepaidRadioButtonTwo.isChecked=false
                postPaidRadioButtonTwo.isChecked=false

                packOptionContainerOne.setBackgroundResource(0)
                packOptionContainerTwo.setBackgroundResource(0)
                postOptionContainerOne.setBackgroundResource(R.drawable.subscribe_bg_round_pass)
                postPackOptionContainerTwo.setBackgroundResource(0)

                showPaymentOption()
            }
            postPaidRadioButtonTwo.setOnClickListener {
                prepaidRadioButtonOne.isChecked=false
                prepaidRadioButtonTwo.isChecked=false
                postPaidRadioButtonOne.isChecked=false

                packOptionContainerOne.setBackgroundResource(0)
                packOptionContainerTwo.setBackgroundResource(0)
                postOptionContainerOne.setBackgroundResource(0)
                postPackOptionContainerTwo.setBackgroundResource(R.drawable.subscribe_bg_round_pass)

                showPaymentOption()
            }

            binding.backImg.safeClick({ findNavController().popBackStack() })
            binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showPaymentOption(){
        binding.termsAndConditionsOne.visibility=View.VISIBLE
        binding.termsAndConditionsTwo.visibility=View.VISIBLE
        binding.buyNow.visibility=View.VISIBLE

    }
    private fun showTermsAndConditionDialog() {
        findNavController().navigate(
            R.id.htmlPageViewDialog, bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to mPref.termsAndConditionUrl
        )
        )
    }
}