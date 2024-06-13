package com.banglalink.toffee.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.databinding.BottomSheetDeteleCompleteProfileDataBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class CompleteDeleteProfileDataBottomSheetFragment : ChildDialogFragment(){
    
    private var _binding: BottomSheetDeteleCompleteProfileDataBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BottomSheetDeteleCompleteProfileDataBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okayButton.safeClick({
            homeViewModel.logoutUser()
            homeViewModel.getCredential()
            mPref.deleteDialogLiveData.value = null
            closeDialog()
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}