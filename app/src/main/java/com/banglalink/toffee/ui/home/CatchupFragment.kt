package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.CommonChannelAdapter
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog

class CatchupFragment:HomeBaseFragment(){
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
        mAdapter.remove(channelInfo)
    }


    lateinit var mAdapter: CommonChannelAdapter
    lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var title: String? = null
    private var loading: Boolean = false

    lateinit var binding:FragmentCatchupBinding

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(CatchupViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.parseBundle(arguments!!)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_catchup,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressDialog = VelBoxProgressDialog(context!!)
        mAdapter= CommonChannelAdapter(this) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(it)
        }
        val linearLayoutManager = LinearLayoutManager(context)
        binding.listview.layoutManager = linearLayoutManager
        binding.listview.adapter = mAdapter
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
               loadItems(mAdapter.getOffset())
            }
        }
        // Adds the scroll listener to RecyclerView
        binding.listview.addOnScrollListener(scrollListener)
        viewModel.contentLiveData.observe(viewLifecycleOwner, Observer {
           when(it){
               is Resource.Success->{
                   if ( progressDialog.isShowing) progressDialog.dismiss()
                   loading = false
                   binding.progressBar.visibility = View.GONE
                   mAdapter.addAll(it.data)
               }
               is Resource.Failure->{
                   if (progressDialog.isShowing) progressDialog.dismiss()
                   binding.progressBar.visibility = View.GONE
                   Log.e("TAG","Failure")
               }
           }
        })
        progressDialog.show()
        loadItems(mAdapter.itemCount)
    }

    private fun loadItems(offset:Int){
        binding.progressBar.visibility = View.VISIBLE
        loading = true
        viewModel.getContent(offset)
    }
    companion object{
        fun createInstance(
            categoryID: Int,
            subCategoryID: Int,
            subCategory: String,
            category: String,
            title: String,
            type: String
        ): CatchupFragment {
            val catchupFragment = CatchupFragment()
            val bundle = Bundle()
            bundle.putInt("category-id", categoryID)
            bundle.putInt("sub-category-id", subCategoryID)
            bundle.putString("sub-category", subCategory)
            bundle.putString("category", category)
            bundle.putString("title", title)
            bundle.putString("type", type)
            catchupFragment.arguments = bundle
            return catchupFragment
        }
    }

    fun updateInfo(
        categoryId: Int,
        subCategoryID: Int,
        subCategory: String,
        category: String,
        title: String,
        type: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        loading = true
        viewModel.updateInfo(category,categoryId,subCategory,subCategoryID,type)
    }

}