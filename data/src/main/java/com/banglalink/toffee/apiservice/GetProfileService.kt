package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Customer
import javax.inject.Inject

class GetProfileService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(): Customer? {
        val response = tryIO {
            toffeeApi.getCustomerProfile(
                ProfileRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        preference.customerName = response.response?.customer?.profile?.name ?: ""
        preference.userImageUrl = response.response?.customer?.profile?.photoUrl ?: ""
        return response.response?.customer
    }
}