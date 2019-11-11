package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo

class GetContents(private val toffeeApi: ToffeeApi) {

    suspend fun execute(
        category: String,
        categoryId: Int,
        subcategory: String,
        subcategoryId: Int,
        type: String,
        offset: Int,
        limit: Int = 10
    ): List<ChannelInfo> {


        val response = tryIO {
            toffeeApi.getContents(
                ContentRequest(
                    categoryId,
                    subcategoryId,
                    type,
                    Preference.getInstance().customerId,
                    Preference.getInstance().password,
                    offset = offset,
                    limit = limit
                )
            )
        }
//        if (response.response.channels != null) {
//            return response.response.channels.map {
//                it.category = category
//                it.subCategoryId = subcategoryId
//                it.subCategory = subcategory
//                it
//            }
//        }

        return response.response.channels ?:listOf()
    }
}