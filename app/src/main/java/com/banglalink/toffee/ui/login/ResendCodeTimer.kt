package com.banglalink.toffee.ui.login

import android.os.CountDownTimer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.extension.toLiveData

class ResendCodeTimer(private val lifecycleOwner: LifecycleOwner, countDownTimeInMinute: Int) :
    CountDownTimer((countDownTimeInMinute * 60000).toLong(), 1000) {
    private val tickMutableLiveData = MutableLiveData<Long>()
    val tickLiveData = tickMutableLiveData.toLiveData()

    private val finishMutableLiveData = MutableLiveData<Unit>()
    val finishLiveData = finishMutableLiveData.toLiveData()

    override fun onFinish() {
        finishMutableLiveData.postValue(Unit)
    }

    override fun onTick(millis: Long) {
        tickMutableLiveData.postValue(millis)
    }

    fun cancelTimer() {
        cancel()
        finishMutableLiveData.removeObservers(lifecycleOwner)
        tickMutableLiveData.removeObservers(lifecycleOwner)
    }


}