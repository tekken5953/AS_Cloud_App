package com.example.airsignal_app.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Button
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastLoginPlatform
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.gps.GetLocation
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RequestPermissionsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

class RedirectPermissionActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        runBlocking {
            getGps().join()
            enterMainPage().join()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_redirect_permission)
        val btn = findViewById<Button>(R.id.permissionBtn)
        btn.setOnClickListener {
            if (!RequestPermissionsUtil(this).isLocationPermitted()) {
                if (VERSION.SDK_INT < 29) {
                    RequestPermissionsUtil(this).requestLocation()

                } else {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private suspend fun getGps() = CoroutineScope(coroutineContext).launch {
        GetLocation(this@RedirectPermissionActivity).getLocation()
    }

    private suspend fun enterMainPage() = CoroutineScope(coroutineContext).launch {
        if (RequestPermissionsUtil(this@RedirectPermissionActivity).isLocationPermitted()) {
            EnterPage(this@RedirectPermissionActivity)
                .toMain(
                    SharedPreferenceManager(this@RedirectPermissionActivity)
                        .getString(lastLoginPlatform)
                )
        }
    }
}