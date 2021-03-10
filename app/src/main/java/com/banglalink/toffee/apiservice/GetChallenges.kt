package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Challenge
import javax.inject.Inject

class GetChallenges @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) :
    BaseApiService<Challenge> {

    override suspend fun loadData(offset: Int, limit: Int): List<Challenge> {
        val response = tryIO2 {
            toffeeApi.getContents(
                "VOD",
                0, 0, offset, 30,
                preference.getDBVersionByApiName("getUgcContentsV5"),
                ContentRequest(
                    0,
                    0,
                    "VOD",
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }

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