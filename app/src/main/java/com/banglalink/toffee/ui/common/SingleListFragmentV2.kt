package com.banglalink.toffee.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentCommonSingleListV2Binding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Resource
import kotlinx.android.synthetic.main.fragment_common_single_list_v2.*
import kotlinx.android.synthetic.main.fragment_top_panel_search_collapsed.*

abstract class SingleListFragmentV2<T: Any> : Fragment() {

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    lateinit var binding: FragmentCommonSingleListV2Binding

    protected lateinit var mAdapter: MyBaseAdapterV2<T>
    protected lateinit var mViewModel: SingleListViewModel<T>

    companion object {
        const val ARG_TITLE = "arg-title"
    }

    abstract fun initAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_common_single_list_v2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        arguments?.getString(ARG_TITLE)?.let {
            activity?.title = it
        }

        binding.listview.adapter = mAdapter
        scrollListener = object : EndlessRecyclerViewScrollListener(binding.listview.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                mViewModel.loadData()
            }
        }

        binding.listview.addOnScrollListener(scrollListener)
        binding.listview.setHasFixedSize(true)
        binding.listview.setItemViewCacheSize(10)

        observeList()

        mViewModel.loadData()

//        search_list.setOnClickListener {
//            val constraintSet = ConstraintSet()
//            val panel = top_panel as ConstraintLayout
//            constraintSet.clone(requireContext(), R.layout.fragment_top_panel_search_expanded)
//            val transition = TransitionSet()
//            transition.ordering = TransitionSet.ORDERING_TOGETHER
//            transition
//                .addTransition(ChangeBounds())
//                .addTransition(AutoTransition())
////                .addTransition(fadeIn)
//            transition.duration = 2000
//            TransitionManager.beginDelayedTransition(panel, transition)
//            constraintSet.applyTo(panel)
//        }
//
//        back_button.setOnClickListener {
//            val constraintSet = ConstraintSet()
//            val panel = top_panel as ConstraintLayout
//            constraintSet.clone(requireContext(), R.layout.fragment_top_panel_search_collapsed)
//            val transition = ChangeBounds()
//            transition.duration = 2000
//            TransitionManager.beginDelayedTransition(panel, transition)
//            constraintSet.applyTo(panel)
//        }
    }

    private fun observeList() {
        mViewModel.listData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    val itemCount = mAdapter.itemCount
                    if(it.data.isEmpty() && itemCount == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        binding.emptyView.visibility = View.GONE
                    }
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    scrollListener.resetState()
                    activity?.showToast(it.error.msg)
                }
            }
        })

        mViewModel.showProgress.observe(viewLifecycleOwner, Observer {
            if(it) {
                showProgress()
            } else{
                hideProgress()
            }
        })
    }

    private fun showProgress() {
        binding.progress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progress.visibility = View.GONE
    }

    override fun onDestroyView() {
        binding.listview.adapter = null
        binding.listview.clearOnScrollListeners()
        binding.unbind()
        super.onDestroyView()
    }
}