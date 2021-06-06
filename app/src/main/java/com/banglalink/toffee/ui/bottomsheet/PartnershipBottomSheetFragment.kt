package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetPartnershipBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.BaseFragment

class PartnershipBottomSheetFragment :BaseFragment(){
    private var _binding: BottomSheetPartnershipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPartnershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            okeyBtn.safeClick({navigateToUploadPhoto()})
            learnMoreTv.safeClick({navigateToCreatorsPolicy()})
        }
    }

    private fun navigateToCreatorsPolicy()
    {
        val args = Bundle().apply {
            putString("myTitle", "Creators Policy")
            putString("url", mPref.creatorsPolicyUrl)
        }
        parentFragment?.parentFragment?.findNavController()?.navigate(R.id.termsAndConditionFragment, args)
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