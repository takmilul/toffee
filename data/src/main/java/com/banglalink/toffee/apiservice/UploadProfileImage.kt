package com.banglalink.toffee.apiservice

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.banglalink.toffee.data.network.request.UploadProfileImageRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.SubscriberPhotoBean
import com.banglalink.toffee.util.decodeSampledBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class UploadProfileImage @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {

    suspend fun execute(photoUri: Uri, context: Context): SubscriberPhotoBean {
        return withContext(Dispatchers.Default) {
            val imageBitmap = decodeSampledBitmap(context, photoUri)
            val bao = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 50, bao)
            val imageData = bao.toByteArray()
            val imageString = Base64.encodeToString(imageData, Base64.DEFAULT)

            val response = tryIO {
                toffeeApi.uploadPhoto(
                    UploadProfileImageRequest(
                        imageString,
                        preference.customerId,
                        preference.password
                    )
                )
            }
            response.response.userPhoto?.let {
                preference.userImageUrl = it
            }
            response.response
        }

    }


}