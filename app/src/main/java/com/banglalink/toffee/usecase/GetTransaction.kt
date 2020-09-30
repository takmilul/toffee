package com.banglalink.toffee.usecase

import com.banglalink.toffee.model.Transaction
import com.banglalink.toffee.ui.common.SingleListRepository
import kotlin.random.Random

class GetTransaction: SingleListRepository<Transaction> {
    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<Transaction>{
        val transactionList: MutableList<Transaction> = mutableListOf()
        
        val dateList = List(10){Random.nextInt(1, 28)}
        val monthList = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        val amountList = List(10){Random.nextInt(50000, 1000000)}
        val remainingList = List(10){ Random.nextInt(60000, 80000)}
        if (mOffset < limit) {
            mOffset += limit
            repeat(10) {
                transactionList.add(Transaction("Deposit", "${dateList[it]} ${monthList.random()}", "+ ৳ ${amountList[it]}", "${remainingList[it]}"))
            }
            return transactionList
        }
        return listOf()
    }
}