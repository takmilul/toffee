package com.banglalink.toffee.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetDeteleProfileDataBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class DeleteProfileDataBottomSheetFragment : ChildDialogFragment() {
    
    private var _binding: BottomSheetDeteleProfileDataBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetDeteleProfileDataBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cancelButton.safeClick({ closeDialog() })
            deleteBtn.safeClick({
                observe(homeViewModel.accountDeleteLiveData) {
                    when (it) {
                        is Resource.Success -> {
                            mPref.deleteDialogLiveData.value = true
                            findNavController().navigateTo(R.id.completeDeleteProfileDataBottomSheetFragment)
                        }
                        is Resource.Failure -> {
                            requireContext().showToast(it.error.msg)
                        }
                    }
                }
                homeViewModel.accountDelete()
            })
        }
        
        binding.editDelete.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.editDelete.text.toString() == "DELETE") {
                    binding.deleteBtn.isEnabled = true
                } else if (binding.editDelete.text.toString() != "DELETE") {
                    binding.deleteBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(arg0: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}