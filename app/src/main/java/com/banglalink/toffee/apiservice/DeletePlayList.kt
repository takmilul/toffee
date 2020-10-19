package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.DeletePlayListRequest
import com.banglalink.toffee.data.network.request.UgcFollowCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.DeletePlayListBean
import com.banglalink.toffee.model.FollowCategoryBean
import javax.inject.Inject

class DeletePlayList @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(playListId: Int): DeletePlayListBean {
        val response = tryIO2 {
            toffeeApi.deletePlayList(
                DeletePlayListRequest(
                    playListId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}