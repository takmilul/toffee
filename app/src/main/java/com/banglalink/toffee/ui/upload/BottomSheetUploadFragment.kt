package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_bottom_sheet_upload, null)
        dialog.setContentView(view)
        val parent = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        bottomSheetBehavior.peekHeight=400


        return dialog
    }
    companion object {
        const val TAG = "CustomBottomSheetDialogFragment"
    }
}