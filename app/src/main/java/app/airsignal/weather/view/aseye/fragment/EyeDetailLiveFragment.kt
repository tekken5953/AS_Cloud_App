package app.airsignal.weather.view.aseye.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.databinding.EyeDetailLiveFragmentBinding
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.view.activity.MainActivity
import app.airsignal.weather.view.aseye.activity.EyeDetailActivity
import app.airsignal.weather.view.aseye.dao.EyeDataModel
import java.util.Random

class EyeDetailLiveFragment : Fragment() {
    lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeDetailLiveFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_live_fragment, container, false)

        binding.aeLiveRefreshIcon.setOnClickListener {
            Thread.sleep(2000)
            fetchData()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchData()
    }
    fun onDataReceived(data: EyeDataModel.EyeReportModel?) {
    }

    private fun fetchData() {
        binding.aeLiveRefreshTime.text = DataTypeParser.currentDateTimeString("hh시 mm분 ss초")
        binding.aeLiveTemp.fetchData(Random().nextInt(50).toString())
        binding.aeLiveHumid.fetchData(Random().nextInt(50).toString())
        binding.aeLiveLight.fetchData(Random().nextInt(50).toString())
        binding.aeLiveNoise.fetchData(Random().nextInt(50).toString())

        binding.aeLivePM25.fetchData(Random().nextInt(5000).toString())
        binding.aeLivePM10.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveCO2.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveCO.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveTVOC.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveNO2.fetchData(Random().nextInt(5000).toString())
    }
}