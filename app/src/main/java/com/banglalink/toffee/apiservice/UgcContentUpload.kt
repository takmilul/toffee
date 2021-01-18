package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentUploadRequest
import com.banglalink.toffee.data.network.request.UgcFollowCategoryRequest
import com.banglalink.toffee.data.network.response.UgcContentUploadResponseBean
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
        fileName: String,
        title: String?,
        description: String?,
        tags: String?,
        ageGroup: String?,
        categoryId: Long,
        subcategoryId: Long,
        base64Image: String? = null,
        duration: String? = null,
        isHorizontal: Int,
    ): UgcContentUploadResponseBean {
        val response = tryIO2 {
            toffeeApi.uploadContent(
                ContentUploadRequest(
                    mPref.customerId,
                    mPref.password,
                    title,
                    fileName,
                    description = description,
                    categoryId = categoryId.toInt(),
                    subCategoryId = subcategoryId.toInt(),
                    ageRestriction = ageGroup,
                    videoTags = tags,
                    contentBanner = base64Image,
                    duration = duration,
                    isHorizontal = isHorizontal
                )
            )
        }
        return response.response
    }
}