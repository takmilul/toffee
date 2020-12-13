package com.banglalink.toffee.data.network.request

data class MoviesPreviewRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcMoviePreview")