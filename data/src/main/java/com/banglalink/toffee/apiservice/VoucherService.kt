package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PackVoucherMethodRequest
import com.banglalink.toffee.data.network.request.PremiumPackDetailRequest
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.response.VoucherPaymentMethodResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.VoucherPaymentBean
import javax.inject.Inject

class VoucherService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun loadData(packId: Int,voucherCode:String,packName:String): VoucherPaymentBean? {
        val response = tryIO {
            toffeeApi.getVoucherPayment(
                packId,
                preference.getDBVersionByApiName(ApiNames.CHECK_VOUCHER_STATUS),
                PackVoucherMethodRequest(
                    preference.customerId,
                    preference.password,
                    voucherCode,
                    packName
                )
            )
        }
        return response.response
    }
}