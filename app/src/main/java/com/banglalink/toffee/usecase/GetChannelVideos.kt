package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import kotlinx.coroutines.coroutineScope


class GetChannelVideos(private val toffeeApi: ToffeeApi, private val contentRequest: ContentRequest, private val category: String, private val subCategory: String): SingleListRepository<ChannelInfo> {
    
    var mOffset: Int = 0
        private set
    private val limit = 10
    
    override suspend fun execute(): List<ChannelInfo> {
        return coroutineScope<List<ChannelInfo>>{
            val response = tryIO {
                toffeeApi.getContents(
                    ContentRequest(
                        contentRequest.categoryId,
                        contentRequest.subCategoryId,
                        contentRequest.type,
                        contentRequest.customerId,
                        contentRequest.password,
                        offset = mOffset,
                        limit = limit
                    )
                )
            }
        
            mOffset += response.response.count
            if (response.response.channels != null) {
                return@coroutineScope response.response.channels.map {
                    it.category = category
                    it.subCategoryId = contentRequest.subCategoryId
                    it.subCategory = subCategory
                    it.formatted_view_count = getFormattedViewsText(it.view_count)
                    it.formattedDuration = discardZeroFromDuration(it.duration)
                    it
                }
            }
            listOf()
        }
    }
}