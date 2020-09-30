package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Challenge
import com.banglalink.toffee.ui.common.SingleListRepository
import kotlinx.coroutines.coroutineScope

class GetChallenges(private val preference: Preference, private val toffeeApi: ToffeeApi, private val contentRequest: ContentRequest, private val category: String, private val subCategory: String) :
    SingleListRepository<Challenge> {

    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<Challenge> {
        val response = tryIO2 {
            toffeeApi.getContents(contentRequest.categoryId,
                mOffset, contentRequest.type,
                preference.getDBVersionByApiName("getContentsV5"),
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
            val challenges = mutableListOf<Challenge>()
            response.response.channels.map {
                challenges.add(Challenge(it.poster_url_mobile, "25:46", "#CricketScoreChallenge", "Join & win prizes!", "24d : 16h remaining", it.logo_mobile_url, "Shakib Al Hasan"))
            }
            return challenges
        }
            return listOf()
    }
}