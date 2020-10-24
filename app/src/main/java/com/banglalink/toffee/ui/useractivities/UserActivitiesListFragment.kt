package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivitiesListFragment: BaseListFragment<UserActivities>(),
    BaseListItemCallback<UserActivities> {

    override val mViewModel by viewModels<UserActivitiesListViewModel>()
    override val mAdapter by lazy { UserActivitiesListAdapter(this) }
    private val homeViewModel by activityViewModels<HomeViewModel>()

    companion object {
        fun newInstance(): UserActivitiesListFragment {
            return UserActivitiesListFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTopPanel(view)
    }

    private fun initTopPanel(view: View) {
        val topPanel = view.findViewById<MotionLayout>(R.id.top_panel)
        if(topPanel != null && topPanel is MotionLayout) {
            topPanel.findViewById<ImageButton>(R.id.back_button)?.let {backButton->
                backButton.setOnClickListener {
                    topPanel.transitionToStart()
                }
            }
        }
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_activities_empty, "You don't have any activities yet")
    }

    override fun onItemClicked(item: UserActivities) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item.channelInfo)
    }
}