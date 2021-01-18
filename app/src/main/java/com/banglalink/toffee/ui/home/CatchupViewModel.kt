package com.banglalink.toffee.ui.home

import com.banglalink.toffee.ui.common.BaseViewModel

class CatchupViewModel:BaseViewModel() {

    /*private var category: String? = null
    private var categoryId: Int = 0
    private var subCategory: String? = null
    private var subCategoryID: Int = 0
    private var type: String? = null

    private val getContent by unsafeLazy {
        GetContents(Preference.getInstance(),RetrofitApiClient.toffeeApi)
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
    }*/
}