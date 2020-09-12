package com.banglalink.toffee.ui.category

import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.model.NavCategory
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.ui.common.SingleListViewModel
import com.banglalink.toffee.usecase.GetCategoryNew

class CategoryViewModel: SingleListViewModel<NavCategory>() {
    override var enableToolbar: Boolean = false
    override var repo: SingleListRepository<NavCategory> = GetCategoryNew(RetrofitApiClient.toffeeApi)
}