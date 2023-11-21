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
import app.airsignal.weather.view.activity.MainActivity
import app.airsignal.weather.view.aseye.activity.EyeDetailActivity

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aeLiveTemp.fetchData("23")
        binding.aeLiveHumid.fetchData("48")
        binding.aeLiveLight.fetchData("23")
        binding.aeLiveNoise.fetchData("23")

        binding.aeLivePM25.fetchData("350")
        binding.aeLivePM10.fetchData("5")
        binding.aeLiveCO2.fetchData("1500")
        binding.aeLiveCO.fetchData("5")
        binding.aeLiveTVOC.fetchData("0.0153")
        binding.aeLiveNO2.fetchData("153")
    }
}