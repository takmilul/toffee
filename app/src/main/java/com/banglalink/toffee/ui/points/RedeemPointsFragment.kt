package com.banglalink.toffee.ui.points

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.banglalink.toffee.onboarding.OnBoarding
import com.banglalink.toffee.util.unsafeLazy

class RedeemPointsFragment : Fragment() {
    
    private lateinit var mAdapter: RedeemPointsAdapter
    private var _binding: FragmentRedeemPointsBinding ? = null
    private val binding get() = _binding!!

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val viewModel by unsafeLazy { 
        ViewModelProviders.of(this).get(RedeemPointsViewModel::class.java)
    }

    companion object {
        fun createInstance() = RedeemPointsFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRedeemPointsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = RedeemPointsAdapter(requireActivity(), {
            onItemClicked(it)
        }, {view1, view2 -> getItemView(view1, view2)})
        scrollListener = object : EndlessRecyclerViewScrollListener(LinearLayoutManager(context)) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadList()
            }
        }
        loadList()
        binding.listView.layoutManager = LinearLayoutManager(context)
        binding.listView.addOnScrollListener(scrollListener)
        binding.listView.setHasFixedSize(true)
        binding.listView.setItemViewCacheSize(10)
        binding.listView.adapter = mAdapter
    }
    
    private fun getItemView(view1: View, view2: View) {
        
        val viewList = arrayOf(view1, view2, binding.progressBar)
        val titleList = arrayOf("Get Notified from Channels", "Live Badge", "Title Default")
        val contentList = arrayOf(
            "Turn on the 'bell' button to get\n notifications from your subscribed channels",
            "Live badge will appear when a channel\n is broadcasting live.",
            "This is a default content"
        )
        
        OnBoarding(activity as Activity, 3, false)
            .build(viewList, titleList, contentList)
            
    }
    
    fun loadList() {
        observe(viewModel.setRedeemPoints()) {
            when (it) {
                is Resource.Success -> {
                    val itemCount = mAdapter.itemCount
                    if (it.data.redeemPoints.isEmpty() && itemCount == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                    }
                    else {
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