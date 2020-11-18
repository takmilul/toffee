package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.getFormattedViewsText

class GetFeatureContents(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    var mOffset: Int = 0
        private set
    private val limit = 100

    suspend fun execute(
        category: String,
        categoryId: Int,
        subcategory: String,
        subcategoryId: Int
    ): List<ChannelInfo> {


        val request =  FeatureContentRequest(
            categoryId,
            subcategoryId,
            "VOD",
            preference.customerId,
            preference.password,
            offset = mOffset,
            limit = limit
        )

        val response = tryIO2 {
            toffeeApi.getFeatureContentsV2(preference.getDBVersionByApiName("getFeatureContentsV2"),request)
        }

        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.category = category
                it.subCategoryId = subcategoryId
                it.subCategory = subcategory
                if(!it.created_at.isNullOrEmpty()) {
                    it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                }
                it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                it
            }
        }

        return listOf()
    }
}