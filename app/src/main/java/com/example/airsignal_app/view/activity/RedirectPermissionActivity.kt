package com.example.airsignal_app.view.activity

import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.EnterPageUtil
import com.example.airsignal_app.util.LoggerUtil
import com.example.airsignal_app.util.RequestPermissionsUtil


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

        LoggerUtil().getInstance()
        setContentView(R.layout.activity_redirect_permission)
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
                    SharedPreferenceManager(this@RedirectPermissionActivity)
                        .getString(lastLoginPlatform)
                )
        }
    }
}