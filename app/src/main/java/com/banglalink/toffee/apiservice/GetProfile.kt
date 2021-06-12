package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Customer
import javax.inject.Inject

class GetProfile @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
){
    suspend operator fun invoke(): Customer{
        val response = tryIO2 {
            toffeeApi.getCustomerProfile(
                ProfileRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        preference.userImageUrl = response.response.customer.profile.photoUrl ?: ""
        preference.balance = response.response.balance
        return response.response.customer
    }
}