package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.WarningDetailAdapter
import com.example.airsignal_app.dao.StaticDataObject.THEME_LIGHT
import com.example.airsignal_app.databinding.ActivityWarningDetailBinding
import com.example.airsignal_app.util.AddressFromRegex
import com.example.airsignal_app.util.`object`.GetAppInfo.getNotificationAddress
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLastAddress
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserTheme
import com.example.airsignal_app.util.`object`.SetSystemInfo

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
}