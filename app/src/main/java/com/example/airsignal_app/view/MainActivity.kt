package com.example.airsignal_app.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.IgnoredKeyFile.CHECK_GPS_BACK
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        // 사이드 메뉴 아이콘
        binding.mainSideMenuIv.setOnClickListener {
            binding.mainDrawerLayout.apply {
                // 사이드 메뉴 열림
                openDrawer(GravityCompat.START)
                clipToPadding = false
                bringToFront()
            }
        }

        RequestPermissionsUtil(this).requestLocation()

        CoroutineScope(Dispatchers.IO).launch {
            createWorkManager()
        }

        // 사이드메뉴 헤더에 있는 닫기 아이콘 이벤트처리
        binding.mainNavView.getHeaderView(0).findViewById<ImageView>(R.id.headerCancel)
            .setOnClickListener {
                binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            }

        // 사이드 메뉴 생성소멸에 따른 처리
        binding.mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                binding.mainScrollView.bringToFront()
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        // 사이드 메뉴 아이템 클릭 리스너
        binding.mainNavView.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                closeDrawerMenu()
                when (item.itemId) {
                    // 날씨정보
                    R.id.side_menu_weather -> {
                        return true
                    }
                    // 내 기기
                    R.id.side_menu_device -> {
                        val intent = Intent(this@MainActivity, MyDeviceActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                    // 설정
                    R.id.side_menu_setting -> {
                        val intent = Intent(this@MainActivity, SettingActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        })

        binding.mainGpsTitleTv.setOnClickListener {
            @SuppressLint("InflateParams")
            val searchLayout: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_search_address,null)
            RefreshUtils(this).showDialog(searchLayout, true)

            val searchListView: ListView = searchLayout.findViewById(R.id.searchAddressListView)
            val searchItem = ArrayList<String>()
            val allTextArray = resources.getStringArray(R.array.address)
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchItem)
            val searchView: SearchView = searchLayout.findViewById(R.id.searchAddressView)
            searchListView.adapter = adapter

            searchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        searchItem.clear()
                        allTextArray.forEach { allList ->
//                                val formatText = if (allList.contains("특별시")) {
//                                    allList.replace("특별","")
//                                } else if (allList.contains("광역시")) {
//                                    allList.replace("광역","")
//                                } else if (allList.contains("특별자치도")) {
//                                    allList.replace("특별자치","")
//                                } else {
//                                    allList
//                                }
                            if (allList.contains(newText)) {
                                searchItem.add(allList)
                            }
                        }

                    } else {
                        searchItem.clear()
                    }
                    adapter.notifyDataSetChanged()
                    return true
                }
            })
        }
    }

    // 사이드메뉴 닫기
    private fun closeDrawerMenu() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // 사이드 메뉴 열려있으면 닫고 닫혀있으면 종료
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // 백그라운드에서 GPS를 불러오기 위한 WorkManager
    private fun createWorkManager() {
        val workManager = WorkManager.getInstance(this)
        val workRequest =
            PeriodicWorkRequest.Builder(GetLocation::class.java, 15, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_GPS_BACK,
            ExistingPeriodicWorkPolicy.REPLACE, workRequest
        )

    }
}