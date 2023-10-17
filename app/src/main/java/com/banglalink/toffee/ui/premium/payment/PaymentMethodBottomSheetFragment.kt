package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentPaymentMethodBottomSheetBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PaymentMethodBottomSheetFragment : BottomSheetDialogFragment() {
    
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    companion object {
        const val TAG = "BottomSheetDialog"
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnKeyListener { _, keyCode, keyEvent ->
            viewModel.clickableAdInventories.value?.let {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP && navController.currentDestination?.id == R.id.paymentDataPackOptionsFragment) {
                    dialog.dismiss() // Dismiss the BottomSheetDialog
                    viewModel.clickableAdInventories.value = null
                }
            } ?: run {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP && navController.currentDestination?.id != R.id.paymentMethodOptions) {
                    navController.popBackStack()
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }
        val dialogBinding = FragmentPaymentMethodBottomSheetBinding.inflate(layoutInflater)
        val navHostFragment = childFragmentManager.findFragmentById(R.id.bottomSheetFragmentPayments) as NavHostFragment
        navController = navHostFragment.navController
        
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        
        val parent = dialogBinding.root.parent as View
        val bottomSheetBehavior = BottomSheetBehavior.from(parent)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }
    
    override fun getTheme(): Int = R.style.SheetDialog
}