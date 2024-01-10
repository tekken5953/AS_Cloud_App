package app.airsignal.weather.as_eye.fragment

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.EyeDetailLiveFragmentBinding
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.`object`.DataTypeParser
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshData()
    }

    private fun refreshData() {
        try {
            entireData.let {
                binding.aeLiveRefreshTime.text = DataTypeParser.currentDateTimeString("hh시 mm분 ss초")
                binding.aeLiveTemp.fetchData(entireData.tempValue.toString())
                binding.aeLiveHumid.fetchData(entireData.humidValue.toString())
                binding.aeLiveLight.fetchData(entireData.lightValue.toString())
                binding.aeLiveNoise.fetchData(entireData.noiseValue.toString())
                val pm25d = getDataBackground(entireData.pm2p5Lvl)
                val pm10d = getDataBackground(entireData.pm10p0Lvl)
                val co2d = getDataBackground(entireData.co2Lvl)
                val cod = getDataBackground(entireData.coLvl)
                val tvocd = getDataBackground(entireData.tvocLvl)
                val no2d = getDataBackground(entireData.no2Lvl)
                binding.aeLivePM25.fetchData(entireData.pm2p5Value.roundToInt().toString(),pm25d.first,pm25d.second)
                binding.aeLivePM10.fetchData(entireData.pm10p0Value.roundToInt().toString(),pm10d.first,pm10d.second)
                binding.aeLiveCO2.fetchData(entireData.co2Value.roundToInt().toString(),co2d.first,co2d.second)
                binding.aeLiveCO.fetchData(entireData.coValue.toString(),cod.first,cod.second)
                binding.aeLiveTVOC.fetchData(entireData.tvocValue.toString(),tvocd.first,tvocd.second)
                binding.aeLiveNO2.fetchData(entireData.no2Value.toString(),no2d.first,no2d.second)
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    fun onDataTransfer(data: EyeDataModel.Measured?) {
        TimberUtil().d("eyetest","live data received : $data")
        data?.let {
            entireData = it
            if (this@EyeDetailLiveFragment.isVisible) {
                refreshData()
            }
        }
    }

    private fun getDataBackground(grade: Int): Pair<Int,Int> {
        return when(grade) {
            0 -> {Pair(R.drawable.ae_live_data_bg_good,R.drawable.live_smile_good)}
            1 -> {Pair(R.drawable.ae_live_data_bg_normal,R.drawable.live_smile_normal)}
            2 -> {Pair(R.drawable.ae_live_data_bg_bad,R.drawable.live_smile_bad)}
            3 -> {Pair(R.drawable.ae_live_data_bg_verybad,R.drawable.live_smile_verybad)}
            else -> {Pair(R.drawable.ae_live_data_bg_good,R.drawable.live_smile_good)}
        }
    }
}