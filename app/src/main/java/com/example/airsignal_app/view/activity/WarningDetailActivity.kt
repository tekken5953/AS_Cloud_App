package com.example.airsignal_app.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.WarningDetailAdapter
import com.example.airsignal_app.databinding.ActivityWarningDetailBinding
import timber.log.Timber

class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    val warningList = ArrayList<String>()
    val warningAdapter by lazy { WarningDetailAdapter(this,warningList) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.warningListView.adapter = warningAdapter

        val dataList = intent.getStringArrayListExtra("warning")

        dataList!!.forEach { s ->
            Timber.tag("testtest").d(s)
            warningList.add(s)
            warningAdapter.notifyDataSetChanged()
        }
    }
}