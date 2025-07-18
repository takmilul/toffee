package com.banglalink.toffee.ui.report

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.apiservice.GetOffenseService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.OffenseType
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ReportPopupFragmentViewModel @Inject constructor(
    private val reportApiService:GetOffenseService
) : ViewModel()  {

    val reports = MutableLiveData<List<Category>>()
    val exitDialogue = SingleLiveEvent<Boolean>()

    fun loadReportList(): Flow<PagingData<OffenseType>> {
        return reportRepo.getList()
    }
    private val reportRepo by lazy {
        BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                reportApiService, ApiNames.GET_OFFENCE_LIST, BrowsingScreens.REPORT_PAGE
            )
        })
    }


}