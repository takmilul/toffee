package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Transaction
import javax.inject.Inject
import kotlin.random.Random

class GetTransaction @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi): BaseApiService<Transaction> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<Transaction> {
        val response = tryIO2 {
            toffeeApi.getContents(
                0,
                offset, "VOD",
                preference.getDBVersionByApiName("getContentsV5"),
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
            /*return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }*/
            val transactionList: MutableList<Transaction> = mutableListOf()

            val dateList = List(10){Random.nextInt(1, 28)}
            val monthList = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
            val amountList = List(10){Random.nextInt(50000, 1000000)}
            val remainingList = List(10){ Random.nextInt(60000, 80000)}
            if (offset < limit) {
                repeat(10) {
                    transactionList.add(Transaction("Deposit", "${dateList[it]} ${monthList.random()}", "+ ৳ ${amountList[it]}", "${remainingList[it]}"))
                }
                return transactionList
            }
        }
        return listOf()
    }
}