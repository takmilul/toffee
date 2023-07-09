package com.banglalink.toffee.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.databinding.FragmentNotificationDetailBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.BaseFragment

class NotificationDetailFragment : BaseFragment(), OnClickListener {
    
    private val binding get() = _binding!!
    private var notificationInfo: NotificationInfo? = null
    private var _binding: FragmentNotificationDetailBinding? = null
    
    companion object {
        const val ARG_NOTIFICATION_INFO = "notificationInfo"
        
        @JvmStatic
        fun newInstance() = NotificationDetailFragment()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationInfo = arguments?.getParcelable(ARG_NOTIFICATION_INFO)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.data = notificationInfo
        return binding.root
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}