package com.banglalink.toffee.ui.home

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.usecase.GetContents
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch
import java.lang.Exception

class CatchupViewModel(application: Application):BaseViewModel(application) {

    private val contentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val contentLiveData = contentMutableLiveData.toLiveData()

    private var category: String? = null
    private var categoryId: Int = 0
    private var subCategory: String? = null
    private var subCategoryID: Int = 0
    private var type: String? = null
    private var title: String? = null

    private val getContent by lazy {
        GetContents(RetrofitApiClient.toffeeApi)
    }

    fun parseBundle(arguments: Bundle){
        this.category = arguments.getString("category")
        this.categoryId = arguments.getInt("category-id")
        this.subCategory = arguments.getString("sub-category")
        this.subCategoryID = arguments.getInt("sub-category-id")
        this.title = arguments.getString("title")
        this.type = arguments.getString("type")
    }
    fun getContent(offset: Int){
        viewModelScope.launch {
            try{
                contentMutableLiveData.setSuccess(getContent.execute(category!!,categoryId,subCategory!!,subCategoryID,type!!,offset))
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
        this.subCategoryID = subCategoryID
        this.subCategory = subCategory
        this.category = category
        this.categoryId = categoryId
        this.title = title
        this.type = type
        getContent(0)
    }
}