package com.banglalink.toffee.ui.common

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

open class ChildDialogFragment: Fragment() {
    fun closeDialog() {
        parentFragment?.parentFragment?.let {
            if(it is DialogFragment) it.dismiss()
        }
    }
}