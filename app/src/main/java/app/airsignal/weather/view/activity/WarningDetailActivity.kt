package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import app.airsignal.weather.R
import app.airsignal.weather.adapter.WarningDetailAdapter
import app.airsignal.weather.databinding.ActivityWarningDetailBinding
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.`object`.GetAppInfo.getNotificationAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLastAddress
import app.airsignal.weather.util.`object`.SetSystemInfo

class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    private val warningList = ArrayList<String>()
    private val warningAdapter = WarningDetailAdapter(this, warningList)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        SetSystemInfo.setStatusBar(this)

        binding.warningListView.adapter = warningAdapter

        binding.warningBack.setOnClickListener {
            finish()
        }

        val dataList = intent.getStringArrayListExtra("warning")
        val regexAddress = AddressFromRegex(getUserLastAddress(this)).getWarningAddress()
            .replace("경기도","경기")

        // 수정 된 주소에 따른 적용
        binding.warningAddr.text =
            if (regexAddress != "Error") regexAddress
            else getNotificationAddress(this)

        // 기상 특보 추가
        dataList?.let {
            warningList.addAll(it)
            warningAdapter.notifyDataSetChanged()
        } ?: apply {
            warningAdapter.notifyDataSetChanged()
        }
    }

    // 지역명을 지역 코드로 변환
    private fun parseRegionToCode(region: String): Int {
        return when(region) {
            "서울,경기,인천" -> { 108 }
            "강원" -> { 105 }
            "충남" -> { 133 }
            "충북" -> { 131 }
            "전남" -> { 156 }
            "전북" -> { 146 }
            "경남" -> { 159 }
            "경북" -> { 143 }
            "제주" -> { 184 }
            "전국" -> { 109 }
            else -> { 109 }
        }
    }
}