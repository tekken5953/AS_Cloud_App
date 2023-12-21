package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.databinding.ActivityEyeDetailBinding
import app.airsignal.weather.as_eye.customview.EyeSettingView
import app.core_as_eye.dao.EyeDataModel
import app.airsignal.weather.as_eye.fragment.EyeDetailLifeFragment
import app.airsignal.weather.as_eye.fragment.EyeDetailLiveFragment
import app.airsignal.weather.as_eye.fragment.EyeDetailReportFragment
import app.airsignal.weather.view.custom_view.ShowDialogClass

class EyeDetailActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_REPORT = 0
        const val FRAGMENT_LIVE = 1
        const val FRAGMENT_LIFE = 2
        var currentFragment = 0
    }

    private lateinit var binding: ActivityEyeDetailBinding

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_detail)

        tabItemSelected(FRAGMENT_REPORT)

        val nameExtra = intent.getStringExtra("name")
        val serialExtra = intent.getStringExtra("serial")
        binding.aeDetailTitle.text = nameExtra
        binding.asDetailSerial.text = serialExtra

        binding.asDetailTabReport.setOnClickListener {
            if (currentFragment != FRAGMENT_REPORT)
                tabItemSelected(FRAGMENT_REPORT)
        }
        binding.asDetailTabLive.setOnClickListener {
            if (currentFragment != FRAGMENT_LIVE)
                tabItemSelected(FRAGMENT_LIVE)
        }
        binding.asDetailTabLife.setOnClickListener {
            if (currentFragment != FRAGMENT_LIFE)
                tabItemSelected(FRAGMENT_LIFE)
        }

        binding.aeDetailBack.setOnClickListener { finish() }

        binding.aeDetailSetting.setOnClickListener {
            val settingView =
                LayoutInflater.from(this).inflate(R.layout.dialog_ae_setting, null, false)
            val settingBack = settingView.findViewById<ImageView>(R.id.aeSettingBack)
            ShowDialogClass(this).setBackPressed(settingBack).show(settingView, true)
            val settingName = settingView.findViewById<EyeSettingView>(R.id.aeSettingName)
            val settingNoti = settingView.findViewById<EyeSettingView>(R.id.aeSettingNotification)
            val settingSerial = settingView.findViewById<EyeSettingView>(R.id.aeSettingSerial)
            val settingWifi = settingView.findViewById<EyeSettingView>(R.id.aeSettingWifi)
            settingName.fetchData("사무실")
            settingSerial.fetchData("AS-442421")
            settingWifi.fetchData("A8:81:7C:5A:3D:57")

            settingName.setOnClickListener { }
            settingNoti.setOnClickListener { }
        }
    }

    private fun transactionFragment(frag: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.aeDetailFrame, frag)
        if (!supportFragmentManager.isStateSaved) { transaction.commit() }
    }

    private fun tabItemSelected(id: Int) {
        when (id) {
            FRAGMENT_REPORT -> {
                val reportFragment = EyeDetailReportFragment()
                val data = EyeDataModel.EyeReportAdapter("test","test")
                currentFragment = id
                reportFragment.onDataReceived(data)
                transactionFragment(reportFragment)
                changeTabResource(id)
            }
            FRAGMENT_LIVE -> {
                val liveFragment = EyeDetailLiveFragment()
                val data = EyeDataModel.EyeReportAdapter("test","test")
                currentFragment = id
                liveFragment.onDataReceived(data)
                transactionFragment(liveFragment)
                changeTabResource(id)
            }
            FRAGMENT_LIFE -> {
                val lifeFragment = EyeDetailLifeFragment()
                val data = EyeDataModel.EyeReportAdapter("test","test")
                lifeFragment.onDataReceived(data)
                currentFragment = id
                transactionFragment(lifeFragment)
                changeTabResource(id)
            }
            else -> throw IllegalArgumentException("Invalid fragment id : $id")
        }
    }

    private fun changeTabResource(id: Int) {
        val tabMap = mapOf (
            FRAGMENT_REPORT to binding.asDetailTabReport,
            FRAGMENT_LIVE to binding.asDetailTabLive,
            FRAGMENT_LIFE to binding.asDetailTabLife
        )

        val selectedTab = tabMap[id] ?: throw IllegalArgumentException("Invalid fragment id: $id")

        tabMap.values.forEach { tab ->
            tab.background = if (tab == selectedTab) {getDr(R.drawable.ae_detail_tap_enable)}
            else { null }

            tab.setTextColor(if (tab == selectedTab) {getColor(R.color.white)}
            else { getColor(R.color.ae_sub_color) })
        }
    }

    private fun getDr(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources,id,null)
    }
}