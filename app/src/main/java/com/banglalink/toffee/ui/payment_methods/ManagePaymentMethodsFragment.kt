package com.banglalink.toffee.ui.payment_methods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.banglalink.toffee.databinding.FragmentManagePaymentMethodsBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.BaseFragment

class ManagePaymentMethodsFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        _binding = FragmentManagePaymentMethodsBinding.inflate(inflater, container, false)
//        return binding.root
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ManagePaymentMethodsScreen()
            }
        }
    }
}