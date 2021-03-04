package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentEditRequest
import com.banglalink.toffee.data.network.response.ResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import javax.inject.Inject

class ContentEdit @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(
        contentId: Int,
        fileName: String,
        title: String?,
        description: String?,
        tags: String?,
        ageGroup: String?,
        categoryId: Long,
        subCategoryId: Int,
        oldContentBanner: String? = "NULL",
        base64Image: String? = "NULL"
    ): ResponseBean {
        val response = tryIO2 {
            toffeeApi.editContent(
                ContentEditRequest(
                    mPref.customerId,
                    mPref.password,
                    contentId,
                    title,
                    fileName,
                    description = description,
                    categoryId = categoryId.toInt(),
                    subCategoryId = subCategoryId,
                    ageRestriction = ageGroup,
                    videoTags = tags,
                    oldContentBanner = oldContentBanner,
                    contentBanner = base64Image,
                )
            )
        }
        return response.response
    }
}