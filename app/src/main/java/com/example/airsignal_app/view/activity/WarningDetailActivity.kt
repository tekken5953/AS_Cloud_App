package com.example.airsignal_app.view.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.ActivityWarningDetailBinding

class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    val warningList = ArrayList<String>()
    val warningAdapter by lazy { ArrayAdapter(this, android.R.layout.simple_list_item_1,warningList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.warningListView.adapter = warningAdapter

        val dataList = intent.getStringArrayListExtra("warning")

        dataList!!.forEach { s ->
            warningList.add(s)
            warningAdapter.notifyDataSetChanged()
        }
    }
}