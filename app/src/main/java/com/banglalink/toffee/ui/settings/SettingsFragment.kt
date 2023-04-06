package com.banglalink.toffee.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.databinding.FragmentSettingsBinding
import com.banglalink.toffee.enums.BubbleType.FIFA
import com.banglalink.toffee.enums.BubbleType.RAMADAN
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var userActivitiesRepository: UserActivitiesRepository
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Settings"
        binding.bubbleSwitch.isVisible = mPref.isBubbleActive && mPref.bubbleType == FIFA.value
        binding.bubbleDivider.isVisible = mPref.isBubbleActive && mPref.bubbleType == FIFA.value
        binding.bubbleRamadanSwitch.isVisible = mPref.isBubbleActive && mPref.bubbleType == RAMADAN.value
        binding.ramadanBubbleDivider.isVisible = mPref.isBubbleActive && mPref.bubbleType == RAMADAN.value
        binding.prefClearWatch.isVisible = mPref.isVerifiedUser
        binding.clearWatchDivider.isVisible = mPref.isVerifiedUser
        binding.watchOnlyWifiToggleBtn.setOnCheckedChangeListener { _, _ -> handleWatchOnlyWifiToggleBtn() }
        binding.bubbleToggleBtn.setOnCheckedChangeListener { _, _ -> handleBubbleToggleBtn() }
        binding.bubbleRamadanToggleBtn.setOnCheckedChangeListener { _, _ -> handleRamadanBubbleToggleBtn() }
        binding.notificationSwitch.setOnCheckedChangeListener { _, _ -> handleNotificationChange() }
        binding.prefFloatingWindow.setOnCheckedChangeListener { _, _ -> handleFloatingWindowPrefChange() }
        initializeSettings()
        setPrefItemListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun initializeSettings() {
        binding.isBubbleEnabled = mPref.isBubbleEnabled
        binding.isRamadanBubbleEnabled = mPref.isBubbleRamadanEnabled
        binding.watchWifiOnly = mPref.watchOnlyWifi()
        binding.enableNotification = mPref.isNotificationEnabled()
        binding.enableFloatingWindow = mPref.isEnableFloatingWindow
    }
    
    private fun handleNotificationChange() {
        mPref.setNotificationEnabled(binding.notificationSwitch.isChecked)
        binding.enableNotification = mPref.isNotificationEnabled()
    }
    
    private fun handleFloatingWindowPrefChange() {
        mPref.isEnableFloatingWindow = binding.prefFloatingWindow.isChecked
        binding.enableFloatingWindow = mPref.isEnableFloatingWindow
    }
    
    private fun handleWatchOnlyWifiToggleBtn() {
        mPref.setWatchOnlyWifi(binding.watchOnlyWifiToggleBtn.isChecked)
        binding.watchWifiOnly = mPref.watchOnlyWifi()
    }

    private fun handleBubbleToggleBtn() {
        mPref.isBubbleEnabled = binding.bubbleToggleBtn.isChecked
        binding.isBubbleEnabled = binding.bubbleToggleBtn.isChecked
        mPref.startBubbleService.value = binding.bubbleToggleBtn.isChecked
    }

    private fun handleRamadanBubbleToggleBtn() {
        mPref.isBubbleEnabled = binding.bubbleRamadanToggleBtn.isChecked
        binding.isBubbleEnabled = binding.bubbleRamadanToggleBtn.isChecked
        mPref.startBubbleService.value = binding.bubbleRamadanToggleBtn.isChecked
    }
    
    private fun setPrefItemListener() {
        binding.prefClearWatch.setOnClickListener { onClickClearWatchHistory() }
//        binding.prefPrivacy.setOnClickListener { onClickPrivacyPolicy() }
//        binding.prefTerms.setOnClickListener { onClickTermsAndConditions() }
        binding.prefAbout.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.AboutFragment && findNavController().currentDestination?.id == R.id.menu_settings) {
                val action = SettingsFragmentDirections.actionSettingsFragmentToAboutFragment()
                findNavController().navigate(action)
            }
        }
    }
    
    private fun onClickClearWatchHistory() {
        ToffeeAlertDialogBuilder(requireContext()).apply {
            setTitle("Clear Activities")
            setText("Are you sure that you want to clear Activities?")
            setPositiveButtonListener("Clear") {
                lifecycleScope.launch {
                    userActivitiesRepository.deleteAll()
                    requireContext().showToast("Activities cleared")
                    it?.dismiss()
                }
            }
        }.create().show()
    }
    
//    private fun onClickTermsAndConditions() {
//        if (findNavController().currentDestination?.id != R.id.termsAndConditionFragment && findNavController().currentDestination?.id == R.id.menu_settings) {
//            val action = SettingsFragmentDirections.actionSettingsFragmentToTermsAndConditions("Terms & Conditions", mPref.termsAndConditionUrl)
//            findNavController().navigate(action)
//        }
//    }
//
//    private fun onClickPrivacyPolicy() {
//        if (findNavController().currentDestination?.id != R.id.privacyPolicyFragment && findNavController().currentDestination?.id == R.id.menu_settings) {
//            val action = SettingsFragmentDirections.actionSettingsFragmentToPrivacyPolicy("Privacy Policy", mPref.privacyPolicyUrl)
//            findNavController().navigate(action)
//        }
//    }
}