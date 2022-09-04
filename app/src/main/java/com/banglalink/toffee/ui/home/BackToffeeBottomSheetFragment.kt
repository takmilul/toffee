package com.banglalink.toffee.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetBackToffeeBinding
import com.banglalink.toffee.extension.safeClick
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BackToffeeBottomSheetFragment : BottomSheetDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogBinding = BottomSheetBackToffeeBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val parent = dialogBinding.root.parent as View
        val bottomSheetBehavior = BottomSheetBehavior.from(parent)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        with(dialogBinding){
            okayButton.safeClick({
                dialog.dismiss()
            })
        }
        return dialog
    }

    override fun getTheme(): Int = R.style.SheetDialog
}