package com.banglalink.toffee.ui.home

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.NavigationUI
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.suke.widget.SwitchButton


class DrawerHelper(
    private val activity: HomeActivity,
    private val mPref: SessionPreference,
    private val binding: ActivityMainMenuBinding,
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
    }

    private fun setProfileInfo() {
        val header = binding.sideNavigation.getHeaderView(0)
        val profileName = header.findViewById(R.id.profile_name) as TextView
        val profilePicture = header.findViewById(R.id.profile_picture) as ImageView

        if (mPref.isVerifiedUser) {
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
                profilePicture.loadProfileImage(it)
            }
        }
        header.findViewById<LinearLayout>(R.id.menu_profile).setOnClickListener {
            activity.getNavController().navigate(R.id.profileFragment)
            binding.drawerLayout.closeDrawers()
        }
    }

    fun handleMenuItemById(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.menu_tv -> {
//                with(activity.getNavController()) {
//                    if(currentDestination?.id != R.id.menu_tv) {
//                        navigate(R.id.menu_tv)
//                    }
//                }
//                binding.drawerLayout.closeDrawers()
//            }
            R.id.menu_subscriptions -> {
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.ic_menu_internet_packs -> {
                binding.drawerLayout.closeDrawers()
                activity.launchActivity<Html5PlayerViewActivity> {
                    putExtra(
                        Html5PlayerViewActivity.CONTENT_URL,
                        mPref.internetPackUrl
                    )
                }
                return true
            }
//            R.id.menu_creators_policy -> {
//                val intent = Intent(activity, HtmlPageViewActivity::class.java).apply {
//                    putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.PRIVACY_POLICY_URL)
//                    putExtra(HtmlPageViewActivity.TITLE_KEY, "Creators Policy")
//                }
//                activity.startActivity(intent)
//            }
//            R.id.menu_settings -> {
//                activity.launchActivity<SettingsActivity>()
//                binding.drawerLayout.closeDrawers()
//                return true
//            }
            R.id.menu_logout -> {
                binding.drawerLayout.closeDrawers()
                activity.handleExitApp()
                return true
            }
//            R.id.menu_verfication -> {
//                binding.drawerLayout.closeDrawers()
//                activity.checkVerification()
//                return true
//            }
            R.id.menu_change_theme -> {
                when (val switch = item.actionView) {
                    is SwitchButton -> {
                        switch.isChecked = !switch.isChecked
                    }
                    is SwitchMaterial -> {
                        switch.isChecked = !switch.isChecked
                    }
                }
            }
            R.id.menu_favorites -> {
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_activities -> {
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_invite -> {
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_redeem -> {
                if (!mPref.isVerifiedUser) {
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
        }
        return run {
            if (NavigationUI.onNavDestinationSelected(item, activity.getNavController())) {
                binding.drawerLayout.closeDrawers()
                return@run true
            }
            false
        }
    }
}