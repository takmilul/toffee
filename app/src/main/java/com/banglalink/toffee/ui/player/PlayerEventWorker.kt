package com.banglalink.toffee.ui.player

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.banglalink.toffee.data.repository.PlayerEventRepository
import javax.inject.Inject

class PlayerEventWorker @Inject constructor(
    appContext: Context,
    params: WorkerParameters,
    private val playerEventRepository: PlayerEventRepository,
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result {
        return try {
            playerEventRepository.sendTopEventToPubSubAndRemove()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}