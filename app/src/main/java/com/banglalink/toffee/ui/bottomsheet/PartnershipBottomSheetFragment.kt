package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
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
            cancelButton.safeClick({closeDialog()})
            learnMoreTv.safeClick({navigateToCreatorsPolicy()})
            okayBtn.safeClick({findNavController().navigate(R.id.photoUploadBottomSheetFragment)})
        }
    }

    private fun navigateToCreatorsPolicy() {
        ToffeeAnalytics.logEvent(ToffeeEvents.UGC_LEARN_MORE)
        val args = Bundle().apply {
            putString("myTitle", "Creators Policy")
            putString("url", mPref.creatorsPolicyUrl)
            putBoolean("isHideBackIcon", false)
            putBoolean("isHideCloseIcon", true)
        }
        findNavController().navigate(R.id.htmlPageViewDialog, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}