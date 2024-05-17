package app.airsignal.weather.view.widget

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.CutCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel

class WidgetPermActivity : AppCompatActivity() {
    private val perm by lazy {RequestPermissionsUtil(this)}

    override fun onResume() {
        super.onResume()
        if (!perm.isBackgroundRequestLocation()) {
            perm.requestBackgroundLocation()
            Toast.makeText(this, "권한을 항상 허용으로 변경 뒤 재설치 해주세요", Toast.LENGTH_SHORT).show()
        } else {
            val id = intent.extras?.getInt("id")
            when(intent.extras?.getString("sort")) {
                BaseWidgetProvider.WIDGET_22 -> WidgetProvider().processUpdate(this, id)
                BaseWidgetProvider.WIDGET_42 -> WidgetProvider42().processUpdate(this, id)
            }
            finish()
        }
    }
}