package com.banglalink.toffee.ui.home

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetContents
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.CancellationException

class CatchupViewModel(application: Application):BaseViewModel(application) {

    private val contentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val contentLiveData = contentMutableLiveData.toLiveData()

    private var category: String? = null
    private var categoryId: Int = 0
    private var subCategory: String? = null
    private var subCategoryID: Int = 0
    private var type: String? = null

    private val getContent by unsafeLazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    fun parseBundle(arguments: Bundle){
        this.category = arguments.getString("category")
        this.categoryId = arguments.getInt("category-id")
        this.subCategory = arguments.getString("sub-category")
        this.subCategoryID = arguments.getInt("sub-category-id")
        this.type = arguments.getString("type")
    }
    fun getContent(){
        viewModelScope.launch {
            try{
                contentMutableLiveData.setSuccess(getContent.execute(category!!,categoryId,subCategory!!,subCategoryID,type!!))
            }catch (e:Exception){
                contentMutableLiveData.setError(getError(e))
            }
        }
    }

    fun updateInfo(
        category: String,
        categoryId: Int,
        subCategory: String,
        subCategoryID: Int,
        type: String

    ) {
        viewModelScope.coroutineContext.cancelChildren(CancellationException("Cancelling ongoing requests"))
        this.subCategoryID = subCategoryID
        this.subCategory = subCategory
        this.category = category
        this.categoryId = categoryId
        this.type = type
        getContent()
    }
}