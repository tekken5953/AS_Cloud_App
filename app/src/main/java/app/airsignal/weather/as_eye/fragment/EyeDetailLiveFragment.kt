package app.airsignal.weather.as_eye.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.EyeDetailLiveFragmentBinding
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.`object`.DataTypeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class EyeDetailLiveFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeDetailLiveFragmentBinding

    private lateinit var entireData: EyeDataModel.Measured

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
            Thread.sleep(1000)
            mActivity.loadAllData()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshData()
    }

    private fun refreshData() {
        entireData.let {
            binding.aeLiveRefreshTime.text = DataTypeParser.currentDateTimeString("hh시 mm분 ss초")
            binding.aeLiveTemp.fetchData(entireData.tempValue.toString())
            binding.aeLiveHumid.fetchData(entireData.humidValue.toString())
            binding.aeLiveLight.fetchData(entireData.lightValue.toString())
            binding.aeLiveNoise.fetchData(entireData.noiseValue.toString())
            binding.aeLivePM25.fetchData(entireData.pm2p5Value.roundToInt().toString())
            binding.aeLivePM10.fetchData(entireData.pm10p0Value.roundToInt().toString())
            binding.aeLiveCO2.fetchData(entireData.co2Value.roundToInt().toString())
            binding.aeLiveCO.fetchData(entireData.coValue.toString())
            binding.aeLiveTVOC.fetchData(entireData.tvocValue.toString())
            binding.aeLiveNO2.fetchData(entireData.no2Value.toString())
        }
    }

    fun onDataReceived(data: EyeDataModel.Measured?) {
        TimberUtil().d("eyetest","live data received : $data")
        data?.let {
            entireData = it
            refreshData()
        }
    }
}