package com.banglalink.toffee.ui.refer

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.ui.common.BaseViewModel

class ReferAFriendViewModel(@NonNull application: Application) : BaseViewModel(application) {

    private val referralCodeLiveData = MutableLiveData<String>()

    val referralCode: LiveData<String>
        get() {

            referralCodeLiveData.postValue("5421325")
            return referralCodeLiveData
        }

    fun share(context: Context, text: String, chooserText: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(sharingIntent, chooserText))
    }

    fun copy(context: Context, text: String): Boolean {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(text, text)
            clipboard.setPrimaryClip(clip)
            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return false
    }
}