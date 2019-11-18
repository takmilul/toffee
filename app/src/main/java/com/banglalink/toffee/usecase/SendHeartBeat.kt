package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class SendHeartBeat(private val coroutineScope: CoroutineScope,private val preference: Preference,private val toffeeApi: ToffeeApi):TimerTask() {
    override fun run() {
        coroutineScope.launch {
            execute()
        }
    }

    private suspend fun execute(){
        val response = tryIO {
            toffeeApi.sendHeartBeat(HeartBeatRequest(preference.customerId,preference.password,preference.latitude,preference.longitude))
        }
    }
}