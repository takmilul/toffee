package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UgcFollowCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.FollowCategoryBean
import javax.inject.Inject

class UgcFollowCategory @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(categoryId: Int, followStatus: Int): FollowCategoryBean {
        val response = tryIO2 {
            toffeeApi.followOnCategory(
                UgcFollowCategoryRequest(
                    categoryId,
                    followStatus,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}