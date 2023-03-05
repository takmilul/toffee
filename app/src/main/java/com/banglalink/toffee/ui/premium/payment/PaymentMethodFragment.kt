package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentPaymentMethodBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PaymentMethodFragment : BottomSheetDialogFragment() {
    
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController
    
    companion object {
        const val TAG = "BottomSheetDialog"
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP && navController.currentDestination?.id != R.id.paymentPackages) {
                navController.popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        val dialogBinding = FragmentPaymentMethodBinding.inflate(layoutInflater)
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