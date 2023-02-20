package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ButtomSheetChoosePackBinding
import com.banglalink.toffee.databinding.ButtomSheetEnableTrailBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.ChildDialogFragment

class TrailFragment : ChildDialogFragment(){

    private var _binding: ButtomSheetEnableTrailBinding ?=null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetEnableTrailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.enableNow.setOnClickListener {
            binding.progressBar.show()
            Toast.makeText(context,"Enabled", Toast.LENGTH_SHORT)
        }

        binding.backImg.safeClick( {findNavController().navigate(R.id.paymentPackages)})

        binding.termsAndConditionsTwo.safeClick({
            showTermsAndConditionDialog()
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTermsAndConditionDialog() {
        findNavController().navigate(R.id.htmlPageViewDialog, bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to mPref.termsAndConditionUrl
        )
        )
    }
}