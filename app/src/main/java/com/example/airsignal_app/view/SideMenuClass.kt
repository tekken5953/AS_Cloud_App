package com.example.airsignal_app.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.view.activity.MyDeviceActivity
import com.example.airsignal_app.view.activity.SettingActivity
import com.google.android.material.navigation.NavigationView

/**
 * @author : Lee Jae Young
 * @since : 2023-04-06 오후 5:03
 **/
class SideMenuClass(
    private val mContext: Activity,
    private val drawerLayout: DrawerLayout,
    private val navView: NavigationView,
    private val viewPagerLayout: RelativeLayout
) {
    private val sp by lazy { SharedPreferenceManager(mContext) }

    // 사이드 메뉴 닫기
    fun closeDrawerMenu() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // 사이드 메뉴 열기
    fun openMenu() {
        drawerLayout.openDrawer(GravityCompat.START)
        drawerLayout.bringToFront()
    }

    // 사이드 메뉴 세팅
    fun setUpSideMenu(menuIcon: ImageView, pb: ProgressBar) {
        // 사이드 메뉴 아이콘
        menuIcon.setOnClickListener {
            drawerLayout.apply {
                // 사이드 메뉴 열림
                openMenu()
                clipToPadding = false
            }
        }

        // 사이드메뉴 헤더에 있는 닫기 아이콘 이벤트처리
        navView.getHeaderView(0).findViewById<ImageView>(R.id.headerCancel)
            .setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

        navView.getHeaderView(0).findViewById<TableRow>(R.id.headerTr)
            .setOnClickListener {
                if (sp.getString(IgnoredKeyFile.lastLoginPlatform) == "")
                    EnterPage(mContext as Activity).toLogin()
            }

        // 로그인 이력이 없을 시 기본 메시지로 설정
        navView.getHeaderView(0).apply {
            Glide.with(mContext)
                .load(Uri.parse(SharedPreferenceManager(mContext).getString(IgnoredKeyFile.userProfile)))
                .into(findViewById(R.id.navHeaderProfileImg))

            if (sp.getString(IgnoredKeyFile.userEmail) != "") {
                SilentLogin().login(mContext, pb)
                findViewById<TextView>(R.id.navHeaderUserId).text = sp.getString(IgnoredKeyFile.userEmail)
            } else findViewById<TextView>(R.id.navHeaderUserId).text =
                mContext.getString(R.string.please_login)

            // 사이드 메뉴 생성 소멸에 따른 처리
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {
                }
                override fun onDrawerClosed(drawerView: View) {
                    viewPagerLayout.bringToFront()
                }

                override fun onDrawerStateChanged(newState: Int) {}
            })

            // 사이드 메뉴 아이템 클릭 리스너
            navView.setNavigationItemSelectedListener(object :
                NavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    closeDrawerMenu()
                    when (item.itemId) {
                        // 날씨정보
                        R.id.side_menu_weather -> {
                            return true
                        }
//                        // 내 기기
//                        R.id.side_menu_device -> {
//                            val intent = Intent(mContext, MyDeviceActivity::class.java)
//                            mContext.startActivity(intent)
//                            return true
//                        }
                        // 설정
                        R.id.side_menu_setting -> {
                            val intent = Intent(mContext, SettingActivity::class.java)
                            mContext.startActivity(intent)
                            return true
                        }
                        else -> {
                            return false
                        }
                    }
                }
            })
        }
    }
}