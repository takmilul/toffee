package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentUploadRequest
import com.banglalink.toffee.data.network.request.UgcFollowCategoryRequest
import com.banglalink.toffee.data.network.response.UgcResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.FollowCategoryBean
import javax.inject.Inject

class UgcContentUpload @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(
        channelId: Long,
        fileName: String,
        title: String?,
        description: String?,
        tags: String?,
        ageGroup: String?,
        categoryId: Long,
        base64Image: String? = null
    ): UgcResponseBean {
        val response = tryIO2 {
            toffeeApi.uploadContent(
                ContentUploadRequest(
                    mPref.customerId,
                    mPref.password,
                    channelId,
                    fileName,
                    title,
                    landscape_ratio_1280_720 = base64Image,
                    category_id = categoryId.toInt(),
                    description = description,
                    video_tags = tags,
                    age_restriction = ageGroup
                )
            )
        }
        return response.response
    }
}