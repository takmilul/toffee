package com.banglalink.toffee.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentRedeemPointsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.RedeemPoints
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy

class RedeemPointsFragment : Fragment() {
    
    private lateinit var mAdapter: RedeemPointsAdapter
    private lateinit var binding: FragmentRedeemPointsBinding
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    
    private val viewModel by unsafeLazy { 
        ViewModelProviders.of(this).get(RedeemPointsViewModel::class.java)
    }
    
    companion object {
        fun createInstance() = RedeemPointsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_redeem_points, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        mAdapter = RedeemPointsAdapter(){
            onItemClicked(it)
        }
        binding.listView.adapter = mAdapter
        scrollListener = object : EndlessRecyclerViewScrollListener(LinearLayoutManager(context)) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadList()
            }
        }
        loadList()
        binding.listView.addOnScrollListener(scrollListener)
        binding.listView.setHasFixedSize(true)
        binding.listView.setItemViewCacheSize(10)
    }
    
    fun loadList() {
        observe(viewModel.setRedeemPoints()) {
            when (it) {
                is Resource.Success -> {
                    val itemCount = mAdapter.itemCount
                    if (it.data.redeemPoints.isEmpty() && itemCount == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                    }
                    mAdapter.addAll(it.data.redeemPoints)
                }
                is Resource.Failure -> {
                    scrollListener.resetState()
                    context?.showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun onItemClicked(item: RedeemPoints) {
        observe(viewModel.redeemPoints()){
            when(it){
                is Resource.Success -> {
                    fragmentManager?.beginTransaction()
                        ?.replace(R.id.content_viewer, RedeemPointsSuccessFragment.createInstance(it.data.message))
                        ?.addToBackStack(RedeemPointsSuccessFragment::class.java.name)
                        ?.commit()
                }
                is Resource.Failure -> {
                    context?.showToast(it.error.msg)
                }
            }
        }
        
        
    }
    
}