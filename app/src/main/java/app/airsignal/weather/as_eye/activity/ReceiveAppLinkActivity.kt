package app.airsignal.weather.as_eye.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.airsignal.weather.R
import app.airsignal.weather.databinding.ActivityReceiveAppLinkBinding
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils

class ReceiveAppLinkActivity : BaseEyeActivity<ActivityReceiveAppLinkBinding>() {
    override val resID: Int
        get() = R.layout.activity_receive_app_link

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAppLink()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        handleAppLink()
    }

    private fun handleAppLink() {
        val action: String? = intent?.action
        val data: Uri? = intent.data

        ToastUtils(this).showMessage("data is $data")
        TimberUtil().d("testtest", "action is $action data is $data")
        binding.appLinkText.text = data.toString()
        if (action == Intent.ACTION_VIEW) {
            val token: String? = data?.getQueryParameter("token")
            val sort: String? = data?.getQueryParameter("sort")
            TimberUtil().d("testtest", "token is $token, sort is $sort")
        } else {
            TimberUtil().d("testtest", "another action is $action")
        }
    }
}