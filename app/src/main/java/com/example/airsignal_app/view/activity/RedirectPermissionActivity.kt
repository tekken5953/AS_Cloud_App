package com.example.airsignal_app.view.activity

import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.airsignal_app.R
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLoginPlatform
import com.google.firebase.database.FirebaseDatabase


class RedirectPermissionActivity : BaseActivity() {
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }

    override fun onResume() {
        super.onResume()
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (RequestPermissionsUtil(this).isLocationPermitted()) {
                enterMainPage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redirect_permission)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        FirebaseDatabase.getInstance()
        LoggerUtil().getInstance()

        val btn = findViewById<Button>(R.id.permissionBtn)
        btn.setOnClickListener {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (!RequestPermissionsUtil(this).isLocationPermitted()) {
                    RequestPermissionsUtil(this).requestLocation()
                }
            } else {
               GetLocation(this).requestSystemGPSEnable()
            }
        }
    }

    private fun enterMainPage() {
        if (RequestPermissionsUtil(this@RedirectPermissionActivity).isLocationPermitted()) {
            EnterPageUtil(this@RedirectPermissionActivity)
                .toMain(
                    getUserLoginPlatform(this)
                )
        }
    }
}