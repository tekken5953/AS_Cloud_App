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
            mActivity.showPb()
            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                if (mActivity.isRefreshable()) {
                    mActivity.dataViewModel.loadData("AOA0000001F539")
                } else {
                    mActivity.hidePb()
                    Toast.makeText(requireContext(), "갱신은 30초 주기로 가능합니다", Toast.LENGTH_SHORT).show()
                }
            },1000)
        }

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
                binding.aeLivePM25.fetchData(entireData.pm2p5Value.roundToInt().toString(),getDataBackground(entireData.pm2p5Lvl))
                binding.aeLivePM10.fetchData(entireData.pm10p0Value.roundToInt().toString(),getDataBackground(entireData.pm10p0Lvl))
                binding.aeLiveCO2.fetchData(entireData.co2Value.roundToInt().toString(),getDataBackground(entireData.co2Lvl))
                binding.aeLiveCO.fetchData(entireData.coValue.toString(),getDataBackground(entireData.coLvl))
                binding.aeLiveTVOC.fetchData(entireData.tvocValue.toString(),getDataBackground(entireData.tvocLvl))
                binding.aeLiveNO2.fetchData(entireData.no2Value.toString(),getDataBackground(entireData.no2Lvl))
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    fun onDataReceived(data: EyeDataModel.Measured?) {
        TimberUtil().d("eyetest","live data received : $data")
        data?.let {
            entireData = it
        }
    }

    private fun getDataBackground(grade: Int): Int {
        return when(grade) {
            0 -> {R.drawable.ae_live_data_bg_good}
            1 -> {R.drawable.ae_live_data_bg_normal}
            2 -> {R.drawable.ae_live_data_bg_bad}
            3 -> {R.drawable.ae_live_data_bg_verybad}
            else -> {R.drawable.ae_live_data_bg_good}
        }
    }
}