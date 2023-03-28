package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.HomeViewPagerAdapter
import com.example.airsignal_app.dao.AdapterModel
import com.example.airsignal_app.dao.StaticDataObject.CHECK_GPS_BACKGROUND
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.SharedPreferenceManager
import com.example.airsignal_app.util.ShowDialogClass
import com.example.airsignal_app.util.ToastUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val addressList = ArrayList<AdapterModel.ViewPagerItem>()
    private val viewPagerAdapter = HomeViewPagerAdapter(this, addressList)
    private var isBackPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        initializing()

        setUpSideMenu()

        binding.mainSearchAddressIv.setOnClickListener {
            @SuppressLint("InflateParams")
            val searchLayout: View =
                LayoutInflater.from(this).inflate(R.layout.dialog_search_address, null)
            ShowDialogClass(this).show(searchLayout, true)

            val searchListView: ListView = searchLayout.findViewById(R.id.searchAddressListView)
            val searchItem = ArrayList<String>()
            val allTextArray = resources.getStringArray(R.array.address)
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, searchItem)
            val searchView: SearchView = searchLayout.findViewById(R.id.searchAddressView)
            searchListView.adapter = adapter

            // 서치 뷰 텍스트 변환 콜벡
            searchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

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

            // 검색주소 리스트
            searchListView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    Logger.t("searchView").d("$position : ${searchItem[position]}")
                    ToastUtils(this).customDurationMessage(
                        "$position : ${searchItem[position]}",
                        500
                    )
                }
        }
    }

    // 사이드 메뉴 닫기
    fun closeDrawerMenu() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun openMenu(menu: DrawerLayout) {
        menu.openDrawer(GravityCompat.START)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initializing() {
        if (!RequestPermissionsUtil(this).isLocationPermitted()) {
            RequestPermissionsUtil(this).requestLocation()
        }

        // 워크 매니저 생성
        CoroutineScope(Dispatchers.IO).launch { createWorkManager() }

        binding.mainViewPager.apply {
            adapter = viewPagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로모드
            offscreenPageLimit = 3  // 최대 3개

            // 뷰 페이저 페이지 변환 중 리스너
            setPageTransformer { page, position ->
                //TODO
            }

            // 뷰 페이저 페이지 전환 후 리스너
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // 페이지 변환 시 주소 텍스트 변경
                    binding.mainGpsTitleTv.text = addressList[position].address
                }
            })

            // 탭레이아웃 연동
            TabLayoutMediator(binding.mainTabLayout, this@apply) { tab, position ->
            }.attach()
        }

        addViewPagerItem()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addViewPagerItem() {
        // Add Item
        addViewPagerLayout("address1","0","0","0","0","0","0","0",0,2)
        addViewPagerLayout("address2","0","0","0","0","0","0","0",3,1)
        addViewPagerLayout("address3","0","0","0","0","0","0","0",1,3)
        viewPagerAdapter.notifyDataSetChanged()
    }

    // 뷰 페이저 아이템 추가
    private fun addViewPagerLayout(
        address: String,
        temp: String,
        sunrise: String,
        sunSet: String,
        sky: String,
        humid: String,
        wind: String,
        rainPer: String,
        pm2p5Grade: Int,
        pm10Grade: Int,
    ) {
        val item = AdapterModel.ViewPagerItem(
            address = address,
            temp = temp,
            sunRise = sunrise,
            sunSet = sunSet,
            sky = sky,
            humid = humid,
            wind = wind,
            rainPer = rainPer,
            pm2p5Grade = pm2p5Grade,
            pm10Grade = pm10Grade,
        )
        addressList.add(item)
    }

    // 사이드 메뉴 세팅
    private fun setUpSideMenu() {
        // 사이드 메뉴 아이콘
        binding.mainSideMenuIv.setOnClickListener {
            binding.mainDrawerLayout.apply {
                // 사이드 메뉴 열림
                openMenu(this)
                clipToPadding = false
                bringToFront()
            }
        }

        // 사이드메뉴 헤더에 있는 닫기 아이콘 이벤트처리
        binding.mainNavView.getHeaderView(0).findViewById<ImageView>(R.id.headerCancel)
            .setOnClickListener {
                binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            }

        binding.mainNavView.getHeaderView(0).apply {
            findViewById<TextView>(R.id.navHeaderUserId)
                .text = SharedPreferenceManager(this@MainActivity).getString("user_email")
            Glide.with(context)
                .load(Uri.parse(SharedPreferenceManager(context).getString("user_profile")))
                .into(findViewById(R.id.navHeaderProfileImg))

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

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (binding.mainViewPager.currentItem == 0) {
                val toast = ToastUtils(this)
                if (!isBackPressed) {
                    toast.customDurationMessage("버튼을 한번 더 누르면 앱이 종료됩니다", 2)
                    isBackPressed = true
                } else {
                    finishAffinity()  // 해당 어플리케이션의 루트 액티비티를 종료
                    System.runFinalization() // 현재 구동중인 쓰레드가 다 종료되면 종료
                    exitProcess(0) // 현재의 액티비티를 종료
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    isBackPressed = false
                }, 2000)
            } else {
                binding.mainViewPager.currentItem = 0
            }
        }
    }
}