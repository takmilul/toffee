package com.banglalink.toffee.ui.widget

import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.annotation.DrawableRes
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.ui.widget.ToffeeFieldTextWatcher.FieldType.*

class ToffeeFieldTextWatcher(editText: EditText, type: FieldType) : OnFocusChangeListener {
    
    private var validLength = 0
    private var editText: EditText? = null
    private var fieldType: FieldType? = null
    @DrawableRes private var leftDrawable = 0
    @DrawableRes private var rightDrawable = 0
    @DrawableRes private var leftDrawableSelected = 0
    
    init {
        when (type) {
            NAME_FIELD -> {
                validLength = 4
                leftDrawable = drawable.ic_user
                leftDrawableSelected = drawable.ic_user
                rightDrawable = drawable.ic_field_tick
                this.editText = editText
                fieldType = type
            }
            EMAIL_FIELD -> {
                leftDrawable = drawable.ic_email
                leftDrawableSelected = drawable.ic_email
                rightDrawable = drawable.ic_field_tick
                this.editText = editText
                fieldType = type
            }
            ADDRESS_FIELD -> {
                leftDrawable = drawable.ic_location
                leftDrawableSelected = drawable.ic_location
                rightDrawable = drawable.ic_field_tick
                this.editText = editText
                fieldType = type
            }
        }
    }
    
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            editText!!.setCompoundDrawablesWithIntrinsicBounds(leftDrawableSelected, 0, 0, 0)
        } else {
            editText!!.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0)
        }
    }
    
    enum class FieldType(val levelCode: Int) {
        NAME_FIELD(1), EMAIL_FIELD(2), ADDRESS_FIELD(3);
    }
    
    private val isValid: Boolean
        get() = if (fieldType == NAME_FIELD) {
            editText!!.text.length > validLength
        } else true
}