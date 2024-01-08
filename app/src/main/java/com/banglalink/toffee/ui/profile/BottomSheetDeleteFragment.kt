package com.banglalink.toffee.ui.profile

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DeleteBottomSheetBinding
import com.banglalink.toffee.ui.home.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetDeleteFragment : BottomSheetDialogFragment() {
    
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    companion object {
        const val TAG = "BottomSheetDialog"
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogBinding = DeleteBottomSheetBinding.inflate(layoutInflater)
        
        val navHostFragment = childFragmentManager.findFragmentById(R.id.bottomSheetFragmentDeleteContainerView) as NavHostFragment
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
        if (mPref.deleteDialogLiveData.value == true) {
            mPref.deleteDialogLiveData.value = null
            homeViewModel.logoutUser()
            homeViewModel.getCredential()
        }
    }
    
    override fun getTheme(): Int = R.style.SheetDialog
}