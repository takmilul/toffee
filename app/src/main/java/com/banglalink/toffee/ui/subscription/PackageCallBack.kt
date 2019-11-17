package com.banglalink.toffee.ui.subscription

import com.banglalink.toffee.model.Package

interface PackageCallBack {
    fun onSubscribeClick(mPackage:Package)
    fun onShowChannelClick(mPackage: Package)
}