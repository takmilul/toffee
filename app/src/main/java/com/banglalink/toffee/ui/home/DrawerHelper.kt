package com.banglalink.toffee.ui.home

import android.content.Intent
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
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.profile.ViewProfileActivity
import com.banglalink.toffee.ui.redeem.RedeemCodeActivity
import com.banglalink.toffee.ui.refer.ReferAFriendActivity
import com.banglalink.toffee.ui.subscription.MySubscriptionActivity
import com.banglalink.toffee.ui.subscription.PackageListActivity
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
//                activity.getNavController().navigate(R.id.menu_feed)
            }
        }

        setProfileInfo()
    }

    private fun setProfileInfo() {
        val header = binding.sideNavigation.getHeaderView(0)
        val profileName = header.findViewById(R.id.profile_name) as TextView
        activity.observe(mPref.customerNameLiveData) {
            when {
                it.isBlank() -> profileName.text =
                    activity.getString(R.string.profile)
                else -> {
                    profileName.text = mPref.customerName
                }
            }
        }
        val profilePicture = header.findViewById(R.id.profile_picture) as ImageView

        activity.observe(mPref.profileImageUrlLiveData) {
            profilePicture.loadProfileImage(it)
        }

        header.findViewById<LinearLayout>(R.id.menu_profile).setOnClickListener {
            activity.launchActivity<ViewProfileActivity>()
        }
//        profilePicture.setOnClickListener{
//            activity.launchActivity<ViewProfileActivity>()
//        }
//        profileName.setOnClickListener{
//            activity.launchActivity<ViewProfileActivity>()
//        }

//        val navBarClose = header.findViewById<ImageView>(R.id.nav_bar_close)
//        navBarClose.setOnClickListener{
//            binding.drawerLayout.closeDrawers()
//        }
    }

//    private fun generateNavMenu(): List<NavigationMenu> {
//        val navigationMenuList = ArrayList<NavigationMenu>()
//
//        val isBanglalinkNumber = mPref.isBanglalinkNumber
////        if(isBanglalinkNumber == "true"){
//            navigationMenuList.add(
//                NavigationMenu(
//                    ID_INTERNET_PACK,
//                    activity.getString(R.string.menu_internet_pack),
//                    R.drawable.ic_menu_internet_packs,
//                    ArrayList(),
//                    true
//                )
//            )
////        }
//
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_CHANNEL,
//                "TV Channels",
//                R.drawable.ic_menu_tv_normal,
//                ArrayList(),
//                true
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_RECENT,
//                "Recent",
//                R.drawable.ic_menu_activites,
//                ArrayList()
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_FAV,
//                "Favorites",
//                R.drawable.ic_menu_favorites,
//                ArrayList()
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_SUB_VIDEO,
//                activity.getString(R.string.menu_create_text),
//                R.mipmap.ic_menu_create,
//                ArrayList(),
//                true
//            )
//        )
//

//        navigationMenuList.add(
//            NavigationMenu(
//                ID_INVITE_FRIEND,
//                activity.getString(R.string.refer_a_friend_txt),
//                R.mipmap.ic_menu_invite,
//                ArrayList()
//            )
//        )

//        navigationMenuList.add(
//            NavigationMenu(
//                ID_REDEEM_CODE,
//                activity.getString(R.string.redeem_code_txt),
//                R.mipmap.ic_menu_redeem,
//                ArrayList()
//            )
//        )

//        navigationMenuList.add(
//            NavigationMenu(
//                ID_SETTINGS,
//                "Settings",
//                R.drawable.ic_side_menu_settings,
//                ArrayList()
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_ABOUT,
//                "About",
//                R.mipmap.ic_menu_about,
//                ArrayList()
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_FAQ,
//                activity.getString(R.string.menu_faqs_text),
//                R.drawable.ic_menu_faq,
//                ArrayList()
//            )
//        )
//        navigationMenuList.add(
//            NavigationMenu(
//                ID_LOGOUT,
//                "Logout",
//                R.mipmap.ic_menu_exit,
//                ArrayList()
//            )
//        )
//
//
//        return navigationMenuList
//    }

//    override fun onMenuClick(menu: NavigationMenu?) {
//       menu?.let {
//           handleMenuItemById(it.id)
//       }
//    }

    fun handleMenuItemById(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.menu_activities -> {
////                with(activity.getNavController()) {
////                    if(currentDestination?.id != R.id.menu_activities) {
////                        navigate(R.id.menu_activities)
////                    }
////                }
//                binding.drawerLayout.closeDrawers()
//            }
//            R.id.menu_tv -> {
//                with(activity.getNavController()) {
//                    if(currentDestination?.id != R.id.menu_tv) {
//                        navigate(R.id.menu_tv)
//                    }
//                }
//                binding.drawerLayout.closeDrawers()
//            }
//            R.id.menu_favorites -> {
//                with(activity.getNavController()) {
//                    if(currentDestination?.id != R.id.menu_favorites) {
//                        navigate(R.id.menu_favorites)
//                    }
//                }
//                binding.drawerLayout.closeDrawers()
//            }
//            ID_SUB_VIDEO -> {
//                activity.launchActivity<HtmlPageViewActivity> {
//                    putExtra(
//                        HtmlPageViewActivity.TITLE_KEY,
//                        activity.getString(R.string.menu_create_text)
//                    )
//                    putExtra(HtmlPageViewActivity.CONTENT_KEY, MICRO_SITE_URL)
//                }
//                binding.drawerLayout.closeDrawers()
//            }
//            R.id.menu_subscriptions -> {
//                binding.drawerLayout.closeDrawers()
//                if (mPref.isSubscriptionActive == "true") {
//                    activity.launchActivity<PackageListActivity>()
//                } else {
//                    activity.launchActivity<MySubscriptionActivity>()
//                }
//                return true
//            }
            R.id.ic_menu_internet_packs -> {
                binding.drawerLayout.closeDrawers()
                activity.launchActivity<Html5PlayerViewActivity> {
                    putExtra(
                        Html5PlayerViewActivity.CONTENT_URL,
                        INTERNET_PACK_URL
                    )
                }
                return true
            }
            R.id.menu_creators_policy -> {
                val intent = Intent(activity, HtmlPageViewActivity::class.java).apply {
                    putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.PRIVACY_POLICY_URL)
                    putExtra(HtmlPageViewActivity.TITLE_KEY, "Creators Policy")
                }
                activity.startActivity(intent)
            }
//            R.id.menu_settings -> {
//                activity.launchActivity<SettingsActivity>()
//                binding.drawerLayout.closeDrawers()
//                return true
//            }
//            ID_ABOUT -> {
//                activity.launchActivity<AboutActivity>()
//                binding.drawerLayout.closeDrawers()
//
//            }
//            ID_FAQ -> {
//                activity.launchActivity<HtmlPageViewActivity> {
//                    putExtra(HtmlPageViewActivity.CONTENT_KEY, FAQ_URL)
//                    putExtra(HtmlPageViewActivity.TITLE_KEY, activity.getString(R.string.menu_faqs_text))
//                }
//                binding.drawerLayout.closeDrawers()
//
//            }
            R.id.menu_logout -> {
                activity.handleExitApp()
                return true
            }
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
            R.id.menu_invite -> {
                activity.launchActivity<ReferAFriendActivity>()
                binding.drawerLayout.closeDrawers()
            }
            R.id.menu_redeem -> {
                activity.launchActivity<RedeemCodeActivity>()
                binding.drawerLayout.closeDrawers()
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

//    private fun getCurrentContentFragment(): Fragment? {
//        return activity.supportFragmentManager.findFragmentById(R.id.content_viewer)
//    }
//
//    override fun onSubCategoryClick(
//        subcategory: NavSubcategory?,
//        category: NavCategory?,
//        parent: NavigationMenu?
//    ) {
//        //Do nothing
//    }
//
//    override fun onCategoryClick(category: NavCategory, parent: NavigationMenu) {
//        handleCategoryClick(parent.id,category.id,category.categoryName)
//        binding.drawerLayout.closeDrawers()
//    }

//    fun handleCategoryClick(parentId:Int,categoryId:Int,categoryName:String){
//        val currentFragment = getCurrentContentFragment()
//        if (parentId == ID_VIDEO && currentFragment!=null) {
//            if (CatchupFragment::class.java.name != currentFragment.tag) {
//                activity.loadFragmentById(R.id.content_viewer,CatchupFragment.createInstance(
//                    categoryId,
//                    0,
//                    "",
//                    "",
//                    categoryName,
//                    "VOD"
//                ), CatchupFragment::class.java.name)
//            } else {
//                val catchupFragment = currentFragment as CatchupFragment
//                catchupFragment.updateInfo(
//                   categoryId,
//                    0,
//                    "",
//                    "",
//                    categoryName,
//                    "VOD"
//                )
//            }
//            activity.minimizePlayer()
//        }
//    }
}