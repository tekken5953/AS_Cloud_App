package com.example.airsignal_app.view.activity

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RequestPermissionsUtil


class RedirectPermissionActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        if (RequestPermissionsUtil(this).isLocationPermitted()) {
            enterMainPage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_redirect_permission)
        val btn = findViewById<Button>(R.id.permissionBtn)
        btn.setOnClickListener {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (!RequestPermissionsUtil(this).isLocationPermitted()) {
                    RequestPermissionsUtil(this).requestLocation()
//                    if (VERSION.SDK_INT < 29) {
//                        RequestPermissionsUtil(this).requestLocation()
//                    } else {
//                        val intent = Intent(
//                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                            Uri.fromParts("package", packageName, null)
//                        )
//                        intent.addCategory(Intent.CATEGORY_DEFAULT)
////                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        startActivity(intent)
//                    }
                }
            } else {
                Toast.makeText(this, "핸드폰 GPS를 켜주세요", Toast.LENGTH_SHORT).show()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun enterMainPage() {
        if (RequestPermissionsUtil(this@RedirectPermissionActivity).isLocationPermitted()) {
            EnterPage(this@RedirectPermissionActivity)
                .toMain(
                    SharedPreferenceManager(this@RedirectPermissionActivity)
                        .getString(lastLoginPlatform)
                )
        }
    }
}