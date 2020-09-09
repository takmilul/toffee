package com.banglalink.toffee.ui.points

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPointsViewModel(): ViewModel() {
    private val points:MutableLiveData<String> = MutableLiveData()
    private val redeemMsg: MutableLiveData<String> = MutableLiveData()
    private val progress: MutableLiveData<Int> = MutableLiveData()
    
    fun getPoints(): LiveData<String>{
        points.postValue("16,000")
        return points
    }
    
    fun getProgress(): LiveData<Int>{
        val point = points.value?.replace(",", "")?.toInt() ?: 0
        
        when(point){
            in 0..5000 -> progress.postValue(1)
            in 5001..10000 -> progress.postValue(((point.times(25)).div(10000)))
            in 10001..20000 -> progress.postValue(((point.times(50)).div(20000)))
            in 20001..30000 -> progress.postValue(((point.times(75)).div(30000)))
            else -> progress.postValue(100)
        }
        Log.d("TAG", "getProgress: ${progress.value}")
        return progress
    }
    
    fun getRedeemMsg(): LiveData<String>{
        redeemMsg.postValue("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")
        return redeemMsg
    }
}