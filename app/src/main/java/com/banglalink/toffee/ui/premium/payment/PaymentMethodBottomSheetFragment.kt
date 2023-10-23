package com.banglalink.toffee.ui.premium.payment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentPaymentMethodBottomSheetBinding
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // disabling payment flow of clickable ad inventories
        viewModel.isLoggedInFromPaymentOptions.value?.let {
            if (!it){
                viewModel.clickableAdInventories.value = null
            }
        }
    }
    
    override fun getTheme(): Int = R.style.SheetDialog
}