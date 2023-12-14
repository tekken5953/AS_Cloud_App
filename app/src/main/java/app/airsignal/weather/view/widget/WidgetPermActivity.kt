package app.airsignal.weather.view.widget

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.R
import app.airsignal.weather.view.perm.RequestPermissionsUtil

class WidgetPermActivity : AppCompatActivity() {
    private val perm by lazy {RequestPermissionsUtil(this)}

    override fun onResume() {
        super.onResume()
        if (!perm.isBackgroundRequestLocation()) {
            perm.requestBackgroundLocation()
            Toast.makeText(this, "권한을 항상허용으로 변경 뒤 재설치 해주세요", Toast.LENGTH_SHORT).show()
        } else {
            val id = intent.extras?.getInt("id")
            when(intent.extras?.getString("sort")) {
                "22" -> WidgetProvider().processUpdate(this,id!!)
                "42" -> WidgetProvider42().processUpdate(this,id!!)
            }
            finish()
        }
    }
}