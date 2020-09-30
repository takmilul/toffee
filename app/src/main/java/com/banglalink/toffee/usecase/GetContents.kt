package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import kotlinx.coroutines.coroutineScope

class GetContents(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    var mOffset: Int = 0
        private set
    private val limit = 30

    suspend fun execute(
        category: String,
        categoryId: Int,
        subcategory: String,
        subcategoryId: Int,
        type: String
    ): List<ChannelInfo> {

        val response = tryIO2 {
            toffeeApi.getContents(categoryId,mOffset,type,preference.getDBVersionByApiName("getContentsV5"),
                ContentRequest(
                    categoryId,
                    subcategoryId,
                    type,
                    Preference.getInstance().customerId,
                    Preference.getInstance().password,
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
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }
        return listOf()
    }
}