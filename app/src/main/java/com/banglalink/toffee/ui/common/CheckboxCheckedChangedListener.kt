package com.banglalink.toffee.ui.common

import android.view.View

interface CheckboxCheckedChangedListener<T : Any>: SingleListItemCallback<T> {
    fun onCheckedChanged(view: View, item: T, position: Int) {}
}