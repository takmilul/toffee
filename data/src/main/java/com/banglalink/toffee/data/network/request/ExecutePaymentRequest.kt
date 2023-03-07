package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ExecutePaymentRequest(
  @SerializedName("paymentID")
  var paymentID: String? = null,
  )