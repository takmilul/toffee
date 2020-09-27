package com.banglalink.toffee.ui.home

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.AppDatabase
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetContents
import com.banglalink.toffee.usecase.GetViewCount
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.cancelChildren
import java.util.concurrent.CancellationException

class CatchupViewModel(application: Application):BaseViewModel(application) {

    private var category: String? = null
    private var categoryId: Int = 0
    private var subCategory: String? = null
    private var subCategoryID: Int = 0
    private var type: String? = null

    private val getContent by unsafeLazy {
        GetContents(Preference.getInstance(),RetrofitApiClient.toffeeApi, GetViewCount(AppDatabase.getDatabase().viewCountDAO()))
    }

    fun parseBundle(arguments: Bundle){
        this.category = arguments.getString("category")
        this.categoryId = arguments.getInt("category-id")
        this.subCategory = arguments.getString("sub-category")
        this.subCategoryID = arguments.getInt("sub-category-id")
        this.type = arguments.getString("type")
    }
    fun getContent():LiveData<Resource<List<ChannelInfo>>>{
        return resultLiveData {
            getContent.execute(category!!,categoryId,subCategory!!,subCategoryID,type!!)
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