package app.airsignal.weather.view.widget

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.R
import app.airsignal.weather.view.perm.RequestPermissionsUtil

class WidgetPermActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_perm)

        val perm = RequestPermissionsUtil(this)

        if (!perm.isBackgroundRequestLocation()) {
            perm.requestBackgroundLocation()
            Toast.makeText(this, "권한을 항상허용으로 변경 뒤 재설치 해주세요", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}