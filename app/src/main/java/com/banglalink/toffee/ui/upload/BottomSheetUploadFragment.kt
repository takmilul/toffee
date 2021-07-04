package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.UploadBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {

    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController

    companion object {
        const val TAG = "BottomSheetDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogBinding = UploadBottomSheetBinding.inflate(layoutInflater)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.bottomSheetFragmentContainerView) as NavHostFragment
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