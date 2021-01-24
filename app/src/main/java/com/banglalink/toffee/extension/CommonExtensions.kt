package com.banglalink.toffee.extension

import android.view.View
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.enums.InputType.*

private const val TITLE_PATTERN = ""
private const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-zA-Z0-9-]+\\.[a-zA-Z.]{2,18}"
private const val ADDRESS_PATTERN = ""
private const val DESCRIPTION_PATTERN = ""

fun String.isValid(type: InputType): Boolean{
    return when(type){
        TITLE -> TITLE_PATTERN.toRegex().matches(this)
        EMAIL -> EMAIL_PATTERN.toRegex().matches(this)
        ADDRESS -> ADDRESS_PATTERN.toRegex().matches(this)
        DESCRIPTION -> DESCRIPTION_PATTERN.toRegex().matches(this)
    }
}

fun View.show(){
    this.visibility = View.VISIBLE
}

fun View.hide(){
    this.visibility = View.GONE
}