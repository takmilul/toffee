package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class QueryPaymentRequest(
  @SerializedName("paymentID")
  var paymentID: String? = null,
  )