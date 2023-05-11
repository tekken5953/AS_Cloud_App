package com.example.airsignal_app.view.activity

import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
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
        Log.d("RedirectPage","onResume Re")
        runBlocking {
            getGps().join()
            enterMainPage().join()
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
    private suspend fun getGps() = CoroutineScope(coroutineContext).launch {
        GetLocation(this@RedirectPermissionActivity).getLocation()
        Log.d("RedirectPage","getGps")
    }

    private suspend fun enterMainPage() = CoroutineScope(coroutineContext).launch {
        if (RequestPermissionsUtil(this@RedirectPermissionActivity).isLocationPermitted()) {
            EnterPage(this@RedirectPermissionActivity)
                .toMain(SharedPreferenceManager(this@RedirectPermissionActivity)
                    .getString(lastLoginPlatform))
        }
        Log.d("RedirectPage","enterMainPage")
    }
}