package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Customer

class GetProfile(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute():Customer{
        val response = tryIO { toffeeApi.getCustomerProfile(ProfileRequest(preference.customerId,preference.password)) }
        preference.userImageUrl = response.response.customer.profile.photoUrl?:""
        preference.balance = response.response.balance
        response.response.customer.profile.name?.let {
            preference.customerName = it
        }
        return response.response.customer
    }
}