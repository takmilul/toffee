package com.banglalink.toffee.ui.report

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.ContentUpload
import com.banglalink.toffee.apiservice.GetCategories
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import com.banglalink.toffee.model.ReportListModel
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.upload.EditUploadInfoViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

class ReportPopupFragmentViewModel @ViewModelInject constructor(
    private val categoryApi: GetCategories,
) : ViewModel()  {

    lateinit var reportList:List<ReportListModel>
    val reports = MutableLiveData<List<Category>>()
    init {
        load()
    }

    fun getReportList(){

        reportList=listOf(ReportListModel(1,"Violence"),
            ReportListModel(2,"Safety"),
            ReportListModel(3,"Fraud,Spam And Fake Information"),
            ReportListModel(4,"Hate Speech"),
            ReportListModel(5,"Nudity"))
    }

    private fun load() {
        viewModelScope.launch {
            reports.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
            if (reports.value.isNullOrEmpty()) {

                return@launch
            }

        }
    }


}