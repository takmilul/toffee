package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo

class GetFeatureContents(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    var mOffset: Int = 0
        private set
    private val limit = 10

    suspend fun execute(
        category: String,
        categoryId: Int,
        subcategory: String,
        subcategoryId: Int,
        type: String
    ): List<ChannelInfo> {


        val response = tryIO {
            toffeeApi.getFeatureContents(
                FeatureContentRequest(
                    categoryId,
                    subcategoryId,
                    type,
                    preference.customerId,
                    preference.password,
                    offset = mOffset,
                    limit = limit
                )
            )
        }

        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.category = category
                it.subCategoryId = subcategoryId
                it.subCategory = subcategory
                it
            }
        }

        return listOf()
    }
}