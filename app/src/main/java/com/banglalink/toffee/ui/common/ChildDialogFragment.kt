package com.banglalink.toffee.ui.common

import androidx.fragment.app.DialogFragment

open class ChildDialogFragment: BaseFragment() {
    fun closeDialog() {
        runCatching {
            parentFragment?.parentFragment?.let {
                if (it is DialogFragment) it.dismiss()
            }
        }
    }
}