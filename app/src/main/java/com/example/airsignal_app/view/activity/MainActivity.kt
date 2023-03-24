package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager.PageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.HomeViewPagerAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.dao.StaticDataObject.CHECK_GPS_BACKGROUND
import com.example.airsignal_app.util.ToastUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val addressList = ArrayList<AdapterModel.ViewPagerItem>()
    private val vAdapter = HomeViewPagerAdapter(this,addressList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        initializing()

        setUpSideMenu()

        binding.mainGpsTitleTv.setOnClickListener {
            @SuppressLint("InflateParams")
            val searchLayout: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_search_address,null)
            RefreshUtils(this).showDialog(searchLayout, true)

            val searchListView: ListView = searchLayout.findViewById(R.id.searchAddressListView)
            val searchItem = ArrayList<String>()
            val allTextArray = resources.getStringArray(R.array.address)
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, searchItem)
            val searchView: SearchView = searchLayout.findViewById(R.id.searchAddressView)
            searchListView.adapter = adapter

            searchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean { return false }
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isNotEmpty()) {
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

            searchListView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    Logger.t("searchView").d("$position : ${searchItem[position]}")
                    ToastUtils(this).customDurationMessage("$position : ${searchItem[position]}",500)
                }
        }
    }

    // 사이드 메뉴 닫기
    private fun closeDrawerMenu() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // 사이드 메뉴 열려있으면 닫고 닫혀있으면 종료
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
        if (!RequestPermissionsUtil(this).isLocationPermitted())
        { RequestPermissionsUtil(this).requestLocation() }

        CoroutineScope(Dispatchers.IO).launch { createWorkManager() }

        binding.mainViewPager.apply {
            adapter = vAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            setPageTransformer { page, position ->
                //TODO
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.mainGpsTitleTv.text = addressList[position].item
                }
            })

            TabLayoutMediator(binding.mainTabLayout, this@apply) { tab, position ->
                //TODO
            }.attach()
        }

        addLayout("address1")
        addLayout("address2")
        addLayout("address3")
        vAdapter.notifyDataSetChanged()
    }

    private fun addLayout(title: String) {
        val item = AdapterModel.ViewPagerItem(title)
        addressList.add(item)
    }

    private fun setUpSideMenu() {
        // 사이드 메뉴 아이콘
        binding.mainSideMenuIv.setOnClickListener {
            binding.mainDrawerLayout.apply {
                // 사이드 메뉴 열림
                openDrawer(GravityCompat.START)
                clipToPadding = false
                bringToFront()
            }
        }

        // 사이드메뉴 헤더에 있는 닫기 아이콘 이벤트처리
        binding.mainNavView.getHeaderView(0).findViewById<ImageView>(R.id.headerCancel)
            .setOnClickListener {
                binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            }

        // 사이드 메뉴 생성 소멸에 따른 처리
        binding.mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                binding.viewPagerLayout.bringToFront()
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
    }

    // 백그라운드에서 GPS 를 불러오기 위한 WorkManager
    private fun createWorkManager() {
        val workManager = WorkManager.getInstance(this)
        val workRequest =
            PeriodicWorkRequest.Builder(GetLocation::class.java, 15, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_GPS_BACKGROUND,
            ExistingPeriodicWorkPolicy.KEEP, workRequest
        )
    }
}