package com.banglalink.toffee.apiservice

import com.banglalink.toffee.model.Earning

class GetEarningInfo {
    suspend fun execute(): Earning{
        return Earning("37,562.74", "2,370,562.74", "23,056.27")
    }
}