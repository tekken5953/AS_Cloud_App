package com.example.airsignal_app.view.activity

import android.os.Build.VERSION
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RequestPermissionsUtil
import timber.log.Timber
import kotlin.system.exitProcess

class RedirectPermissionActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        Log.d("RedirectPage","onResume Re")
        if (RequestPermissionsUtil(this).isLocationPermitted()) {
            EnterPage(this).toMain(SharedPreferenceManager(this).getString(lastLoginPlatform))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RedirectPage","onCreate Re")
        setContentView(R.layout.activity_redirect_permission)
        val btn = findViewById<Button>(R.id.permissionBtn)
        btn.setOnClickListener {
            if (!RequestPermissionsUtil(this).isLocationPermitted()) {
                Log.d("RedirectPage","isNotLocationPermitted SDK is ${VERSION.SDK_INT}")
                RequestPermissionsUtil(this).requestLocation()
            }
        }
    }
}