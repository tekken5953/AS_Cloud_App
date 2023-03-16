package com.example.airsignal_app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.example.airsignal_app.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onResume() {
        super.onResume()
        binding.mainScrollView.bringToFront()
    }

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
            binding.mainScrollView.bringToFront()
        }

        binding.mainNavView.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.side_menu_weather -> {
                        Toast.makeText(this@MainActivity, "날씨정보", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    R.id.side_menu_device -> {
                        val intent = Intent(this@MainActivity, MyDeviceActivity::class.java)
                        startActivity(intent)
                        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
                        }
                        return true
                    }
                    R.id.side_menu_setting -> {
                        Toast.makeText(this@MainActivity, "앱 설정", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            binding.mainScrollView.bringToFront()
        } else {
            super.onBackPressed()
        }
    }
}