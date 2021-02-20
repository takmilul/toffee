package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.network.request.MoviesPreviewRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class MoviesPreviewService @Inject constructor(
        private val preference: Preference,
        private val toffeeApi: ToffeeApi,
        private val localSync: LocalSync,
//        private val viewCountDAO: ViewCountDAO,
//        private val viewProgressRepo: ContentViewPorgressRepsitory,
) {
    suspend fun loadData(type: String, categoryId: Int, subCategoryId: Int, limit: Int = 0, offset: Int = 0): List<ChannelInfo> {

        val request =  MoviesPreviewRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getMoviePreviews(
                type,
                categoryId,
                subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcMoviePreview"),
                request
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncData(it)
//                val viewCount = viewCountDAO.getViewCountByChannelId(it.id.toInt())
//                if(viewCount!=null){
//                    it.view_count= viewCount.toString()
//                }
//                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                it
            }
        } else emptyList()
    }
}