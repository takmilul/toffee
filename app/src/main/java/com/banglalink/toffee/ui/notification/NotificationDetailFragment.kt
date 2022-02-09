package com.banglalink.toffee.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.databinding.FragmentNotificationDetailBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class NotificationDetailFragment : BaseFragment(), OnClickListener {
    private var notificationInfo: NotificationInfo? = null
    private var _binding: FragmentNotificationDetailBinding ? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = NotificationDetailFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = NotificationDetailFragmentArgs.fromBundle(requireArguments())
        notificationInfo = args.notificationInfo
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNotificationDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.data = notificationInfo
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.posterView.safeClick({
            mPref.shareableUrlLiveData.value = notificationInfo?.resourceUrl
        })
        binding.detailTextView.safeClick({
            mPref.shareableUrlLiveData.value = notificationInfo?.resourceUrl
        })
        binding.backButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        findNavController().navigateUp()
    }

}