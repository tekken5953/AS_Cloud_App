package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.adapter.EyeLifeAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.EyeDetailLifeFragmentBinding

class EyeDetailLifeFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeDetailLifeFragmentBinding

    private val lifeList = ArrayList<EyeDataModel.Life>()
    private val lifeAdapter by lazy { EyeLifeAdapter(requireActivity(), lifeList) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_life_fragment, container, false)

        binding.aeLifeDataRv.adapter = lifeAdapter

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLifeItem("PM2.5","초미세먼지",20, R.color.ae_good_main,R.color.ae_good_sub)
        addLifeItem("PM10","미세먼지",50, R.color.ae_normal_main,R.color.ae_normal_sub)
        addLifeItem("CO2","이산화탄소",70, R.color.ae_bad_main,R.color.ae_bad_sub)
        addLifeItem("CO","일산화탄소",90, R.color.ae_very_bad_main,R.color.ae_very_bad_sub)
        addLifeItem("TVOC·NO2","총휘발성유기화합물\n이산화질소",10, R.color.ae_good_main,R.color.ae_good_sub)
        addLifeItem("TEMP·HUM","온도·습도",40, R.color.ae_normal_main,R.color.ae_normal_sub)
        lifeAdapter.notifyDataSetChanged()
    }

    fun onDataReceived(data: EyeDataModel.EyeReportAdapter?) {
    }


    private fun addLifeItem(nameEn: String, nameKr: String, lifeValue: Int, pbColor: Int, backColor: Int) {
        val item = EyeDataModel.Life(nameEn, nameKr,lifeValue,pbColor,backColor)
        lifeList.add(item)
    }
}