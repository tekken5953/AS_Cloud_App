package com.example.airsignal_app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        binding.mainSideMenuIv.setOnClickListener {
            binding.mainDrawerLayout.apply {
                openDrawer(GravityCompat.START)
                clipToPadding = false
                bringToFront()
            }
        }

        binding.mainNavView.getHeaderView(0).findViewById<ImageView>(R.id.headerCancel).setOnClickListener {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) { binding.mainScrollView.bringToFront() }
            override fun onDrawerStateChanged(newState: Int) {}
        })

        binding.mainNavView.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                closeDrawerMenu()
                when (item.itemId) {
                    R.id.side_menu_weather -> {
                        Toast.makeText(this@MainActivity, "날씨정보", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    R.id.side_menu_device -> {
                        val intent = Intent(this@MainActivity, MyDeviceActivity::class.java)
                        startActivity(intent)
                        return true
                    }
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

    private fun closeDrawerMenu() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}