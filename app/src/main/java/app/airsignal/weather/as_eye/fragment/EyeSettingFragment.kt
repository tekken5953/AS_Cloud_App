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
import app.airsignal.weather.databinding.EyeDetailLifeFragmentBinding
import app.airsignal.weather.databinding.EyeSettingFragmentBinding

class EyeSettingFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeSettingFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_setting_fragment, container, false)

        binding.aeSettingName.setOnClickListener {  }
        binding.aeSettingSerial.setOnClickListener {  }
        binding.aeSettingWifi.setOnClickListener {  }
        binding.aeSettingNotification.setOnClickListener {  }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}