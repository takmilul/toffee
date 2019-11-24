package com.banglalink.toffee.ui.refer

class ReferCode {
    var referalCode : String? = null
    fun isEmpty(): Boolean{
        if (referalCode != null && !referalCode!!.isEmpty())
            return false
        return true
    }
}