package com.banglalink.toffee.ui.player

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.banglalink.toffee.data.repository.PlayerEventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PlayerEventWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val playerEventRepository: PlayerEventRepository
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result {
        return try {
            playerEventRepository.sendAllRemainingEventToPubSubAndRemove()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}