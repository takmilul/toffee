package com.banglalink.toffee.ui.home

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ActivityHomeBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.openUrlToExternalApp
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.bytesEqualTo
import com.google.android.material.switchmaterial.SwitchMaterial
import com.suke.widget.SwitchButton

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class DrawerHelper(
    private val activity: HomeActivity,
    private val mPref: SessionPreference,
    private var bindingUtil: BindingUtil,
    private val binding: ActivityHomeBinding,
) {
    
    lateinit var toggle: ActionBarDrawerToggle
    
    fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.tbar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        //After instantiating your ActionBarDrawerToggle
        toggle.isDrawerIndicatorEnabled = false

//        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home)
//        val parentAdapter = ParentLevelAdapter(activity, generateNavMenu(), this, binding.navMenuList)
//        binding.navMenuList.setAdapter(parentAdapter)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        
        toggle.toolbarNavigationClickListener = View.OnClickListener {
            val icon = binding.tbar.toolbar.navigationIcon
            val isBackIconVisible = ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back)?.bytesEqualTo(icon)
            if (isBackIconVisible == true && activity.getNavController().currentDestination?.id != R.id.premiumPackListFragment) {
                activity.getNavController().popBackStack()
            } else {
                activity.getNavController().navigatePopUpTo(R.id.menu_feed)
            }
        }
        
        setProfileInfo()
        setToffeePremium()
        followUsAction()
    }
    
    private fun followUsAction() {
        val actionView = binding.sideNavigation.menu.findItem(R.id.menu_follow_us).actionView
        actionView?.findViewById<ImageView>(R.id.facebookButton)?.setOnClickListener {
            val fbAppUrl =
                "fb://page${mPref.facebookPageUrl.replaceBeforeLast("/", "").replace("Toffee.Bangladesh", "100869298504557", true)}"
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
        val profileImageView = header.findViewById(R.id.profile_picture) as ImageView
        if (mPref.isVerifiedUser) {
            activity.observe(mPref.profileImageUrlLiveData) {
                when (it) {
                    is String -> bindingUtil.bindRoundImage(profileImageView, it)
                    is Int -> bindingUtil.loadImageFromResource(profileImageView, it)
                }
            }
            if (!mPref.userImageUrl.isNullOrBlank()) mPref.profileImageUrlLiveData.postValue(mPref.userImageUrl)
        }
        header.findViewById<LinearLayout>(R.id.menu_account).setOnClickListener {
            activity.getNavController().let {
                if (it.currentDestination?.id != R.id.accountFragment) {
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.MENU_SELECTED,
                        bundleOf(
                            "menu" to "Account",
                            "screen" to activity.title
                        )
                    )
                    it.navigateTo(R.id.accountFragment)
                }
            }
            binding.drawerLayout.closeDrawers()
        }
    }
    
    private fun setToffeePremium() {

        val header = binding.sideNavigation.getHeaderView(0)
        header.findViewById<LinearLayout>(R.id.menu_toffee_premium).setOnClickListener {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.toffee_premium)))
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.MENU_SELECTED,
                bundleOf(
                    "menu" to activity.getString(R.string.toffee_premium),
                    "screen" to activity.title
                )
            )
            activity.getNavController().navigateTo(R.id.premiumPackListFragment)
            binding.drawerLayout.closeDrawers()
        }
    }
    
    fun handleMenuItemById(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ic_menu_internet_packs -> {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.MENU_CLICK,
                    bundleOf("selected_menu" to activity.getString(R.string.menu_internet_pack))
                )
                binding.drawerLayout.closeDrawers()
                activity.launchActivity<Html5PlayerViewActivity> {
                    putExtra(
                        Html5PlayerViewActivity.CONTENT_URL, mPref.internetPackUrl
                    )
                }
                return true
            }
            R.id.menu_creators_policy -> {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.MENU_CLICK,
                    bundleOf("selected_menu" to activity.getString(R.string.menu_creators_policy))
                )
                activity.getNavController().navigateTo(
                    resId = R.id.menu_creators_policy,
                    args = bundleOf(
                        "myTitle" to "Creators Policy", "url" to mPref.creatorsPolicyUrl
                    )
                )
                binding.drawerLayout.closeDrawers()
                return true
            }
            R.id.menu_policies -> {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.MENU_CLICK,
                    bundleOf("selected_menu" to activity.getString(R.string.menu_policies))
                )
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_policies),
                        "screen" to activity.title
                    )
                )
                activity.getNavController().navigateTo(R.id.menu_policies)
                binding.drawerLayout.closeDrawers()
                return true
            }
            R.id.menu_faq -> {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.MENU_CLICK,
                    bundleOf("selected_menu" to activity.getString(R.string.menu_faq))
                )
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_faq),
                        "screen" to activity.title
                    )
                )
                activity.getNavController().navigateTo(
                    resId = R.id.menu_faq,
                    args = bundleOf(
                        "myTitle" to "FAQ", "url" to mPref.faqUrl
                    )
                )
                binding.drawerLayout.closeDrawers()
                return true
            }
            R.id.menu_settings -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.menu_settings)))
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_settings),
                        "screen" to activity.title
                    )
                )
            }
            R.id.menu_logout -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.menu_log_out)))
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_sign_out),
                        "screen" to activity.title
                    )
                )
                binding.drawerLayout.closeDrawers()
                activity.handleExitApp()
                return true
            }
            R.id.menu_login -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.menu_sign_in)))
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_sign_in),
                        "screen" to activity.title
                    )
                )
                binding.drawerLayout.closeDrawers()
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "menu",
                            "method" to "mobile"
                        )
                    )
                }
                activity.checkVerification()
                return true
            }
            R.id.menu_change_theme -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.menu_dark_mode)))
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.menu_dark_mode),
                        "screen" to activity.title
                    )
                )
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
            R.id.menu_invite -> {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.MENU_CLICK,
                    bundleOf("selected_menu" to activity.getString(R.string.refer_a_friend_txt))
                )
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.refer_a_friend_txt),
                        "screen" to activity.title
                    )
                )

                if (!mPref.isVerifiedUser) {
                    if (!mPref.isVerifiedUser){
                        ToffeeAnalytics.toffeeLogEvent(
                            ToffeeEvents.LOGIN_SOURCE,
                            bundleOf(
                                "source" to "referral",
                                "method" to "mobile"
                            )
                        )
                    }
                    activity.checkVerification()
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_redeem -> {
                ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to activity.getString(R.string.redeem_code_txt)))
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.MENU_SELECTED,
                    bundleOf(
                        "menu" to activity.getString(R.string.redeem_code_txt),
                        "screen" to activity.title
                    )
                )
                ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_REFERRAL)
                if (!mPref.isVerifiedUser) {
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN_SOURCE,
                        bundleOf(
                            "source" to "referral",
                            "method" to "mobile"
                        )
                    )
                    activity.checkVerification{
                    }
                    binding.drawerLayout.closeDrawers()
                    return true
                }
            }
            R.id.menu_follow_us -> {
                return false
            }
        }
        return run {
            activity.getNavController().navigateTo(item.itemId)
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