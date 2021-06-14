package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetPartnershipBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment

class PartnershipBottomSheetFragment :ChildDialogFragment(){
    private var _binding: BottomSheetPartnershipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPartnershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            okayBtn.safeClick({navigateToUploadPhoto()})
            learnMoreTv.safeClick({navigateToCreatorsPolicy()})
            cancelButton.safeClick({closeDialog()})
        }
    }

    private fun navigateToCreatorsPolicy()
    {
        val action = PartnershipBottomSheetFragmentDirections
            .actionPartnershipBottomSheetFragmentToHtmlPageViewDialog("Creators Policy",mPref.creatorsPolicyUrl)
        findNavController().navigate(action)
    }

    private fun navigateToUploadPhoto()
    {
        if (findNavController().currentDestination?.id != R.id.photoUploadBottomSheetFragment && findNavController().currentDestination?.id == R.id.partnershipBottomSheetFragment) {
            val action =
                PartnershipBottomSheetFragmentDirections.actionPartnershipBottomSheetFragmentToPhotoUploadBottomSheetFragment()
             findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}