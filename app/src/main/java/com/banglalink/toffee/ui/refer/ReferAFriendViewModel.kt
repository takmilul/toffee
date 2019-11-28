package com.banglalink.toffee.ui.refer

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetMyReferralCode
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class ReferAFriendViewModel(@NonNull application: Application) : BaseViewModel(application) {

    private val referralCodeLiveData = MutableLiveData<Resource<String>>()

    val referralCode: LiveData<Resource<String>> = referralCodeLiveData.toLiveData()

    private val getMyReferralCode by unsafeLazy {
        GetMyReferralCode(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        getMyReferralCode()
    }

    fun getMyReferralCode(){
        viewModelScope.launch {
            try{
                referralCodeLiveData.setSuccess(getMyReferralCode.execute())
            }catch (e:Exception){
                referralCodeLiveData.setError(getError(e))
            }
        }
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