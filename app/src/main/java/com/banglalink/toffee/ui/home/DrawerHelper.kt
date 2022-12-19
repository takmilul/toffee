package com.banglalink.toffee.ui.home

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ActivityHomeBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.openUrlToExternalApp
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.util.BindingUtil
import com.google.android.material.switchmaterial.SwitchMaterial
import com.suke.widget.SwitchButton

class DrawerHelper(
    private val activity: HomeActivity,
    private val mPref: SessionPreference,
    private var bindingUtil: BindingUtil,
    private val binding: ActivityHomeBinding,
) {

    lateinit var toggle: ActionBarDrawerToggle

    fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            activity,
            binding.drawerLayout,
            binding.tbar.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        //After instantiating your ActionBarDrawerToggle
        toggle.isDrawerIndicatorEnabled = false

//        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)
//        val parentAdapter =
//            ParentLevelAdapter(activity, generateNavMenu(), this, binding.navMenuList)
//        binding.navMenuList.setAdapter(parentAdapter)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (activity.getNavController().currentDestination?.id != R.id.menu_feed) {
                activity.getNavController().popBackStack(R.id.menu_feed, false)
            }
        }

        setProfileInfo()
        followUsAction()
    }
    
    private fun followUsAction() {
        val actionView = binding.sideNavigation.menu.findItem(R.id.menu_follow_us).actionView
        actionView?.findViewById<ImageView>(R.id.facebookButton)?.setOnClickListener {
            val fbAppUrl = "fb://page${mPref.facebookPageUrl.replaceBeforeLast("/", "").replace("Toffee.Bangladesh", "100869298504557", true)}"
            val doesHandledUrl = activity.openUrlToExternalApp(fbAppUrl)
            if (!doesHandledUrl) {
                activity.openUrlToExternalApp(mPref.facebookPageUrl)
            }
        }
        actionView?.findViewById<ImageView>(R.id.instagramButton)?.setOnClickListener {
            activity.openUrlToExternalApp(mPref.instagramPageUrl)
        }
        actionView?.findViewById<ImageView>(R.id.youtubeButton)?.setOnClickListener {
            activity.openUrlToExternalApp(mPref.youtubePageUrl)
        }
    }
    
    private fun setProfileInfo() {
        val header = binding.sideNavigation.getHeaderView(0)
        val profileName = header.findViewById(R.id.profile_name) as TextView
        val profileImageView = header.findViewById(R.id.profile_picture) as ImageView
        
        if (mPref.isVerifiedUser) {
            if (mPref.customerName.isNotBlank()) profileName.text = mPref.customerName
            if (!mPref.userImageUrl.isNullOrBlank()) bindingUtil.bindRoundImage(profileImageView, mPref.userImageUrl)
            
            activity.observe(mPref.customerNameLiveData) {
                when {
                    it.isBlank() -> profileName.text =
                        activity.getString(R.string.profile)
                    else -> {
                        profileName.text = mPref.customerName
                    }
                }
            }
            activity.observe(mPref.profileImageUrlLiveData) {
                bindingUtil.bindRoundImage(profileImageView, it)
            }
        }
        header.findViewById<LinearLayout>(R.id.menu_profile).setOnClickListener {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to "Profile"))
            activity.getNavController().let {
                if(it.currentDestination?.id != R.id.profileFragment) {
                    it.navigate(R.id.profileFragment)
                }
            }
            binding.drawerLayout.closeDrawers()
        }
    }

    fun handleMenuItemById(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_subscriptions -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_subscriptions)))
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.ic_menu_internet_packs -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_internet_pack)))
                binding.drawerLayout.closeDrawers()
                activity.launchActivity<Html5PlayerViewActivity> {
                    putExtra(
                        Html5PlayerViewActivity.CONTENT_URL,
                        mPref.internetPackUrl
                    )
                }
                return true
            }
            R.id.menu_creators_policy -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_creators_policy)))
                activity.getNavController().navigate(R.id.menu_creators_policy, bundleOf(
                    "myTitle" to "Creators Policy",
                    "url" to mPref.creatorsPolicyUrl
                ))
                binding.drawerLayout.closeDrawers()
                return true
            }
            R.id.menu_settings -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_settings)))
            }
            R.id.menu_logout -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_log_out)))
                binding.drawerLayout.closeDrawers()
                activity.handleExitApp()
                return true
            }
            R.id.menu_login -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_sign_in)))
                binding.drawerLayout.closeDrawers()
                activity.checkVerification()
                return true
            }
            R.id.menu_change_theme -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_dark_mode)))
                when (val switch = item.actionView) {
                    is SwitchButton -> {
                        switch.isChecked = !switch.isChecked
                    }
                    is SwitchMaterial -> {
                        switch.isChecked = !switch.isChecked
                    }
                }
                return true
            }
            R.id.menu_favorites -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_favorites)))
                ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_FAVORITES)
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_activities -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_activities)))
                ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_ACTIVITIES)
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_playlist -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.menu_playlists)))
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_invite -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.refer_a_friend_txt)))
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_redeem -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK,  bundleOf("selected_menu" to activity.getString(R.string.redeem_code_txt)))
                ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_REFERRAL)
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_follow_us -> {
                return false
            }
        }
        return run {
            activity.getNavController().navigate(item.itemId)
            binding.drawerLayout.closeDrawers()
            return@run true
//            if (NavigationUI.onNavDestinationSelected(item, activity.getNavController())) {
//                binding.drawerLayout.closeDrawers()
//                return@run true
//            }
//            false
        }
    }
}