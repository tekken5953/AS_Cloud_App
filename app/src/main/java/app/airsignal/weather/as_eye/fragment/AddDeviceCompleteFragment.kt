package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.os.HandlerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceCompleteBinding
import kotlinx.coroutines.*

class AddDeviceCompleteFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceCompleteBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentActivity.hidePb()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_complete, container, false)
        parentActivity.changeTitleWithAnimation(binding.addCompleteTitle, "기기로 등록 정보를\n전송중입니다",true)
        parentActivity.changeTitleWithAnimation(binding.addCompleteContent,"전송완료 후 기기가 재부팅됩니다",true)
        parentActivity.changeProgressWithAnimation(90)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentActivity.showPb()

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            parentActivity.hidePb()
            parentActivity.changeProgressWithAnimation(100)
            delay(400)
            binding.addCompleteTitle.visibility = View.GONE
            binding.addCompleteTitle.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)
            binding.addCompleteContent.visibility = View.GONE
            binding.addCompleteContent.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_out)

            withContext(Dispatchers.Main) {
                delay(1000)
                binding.addCompleteLinear.visibility = View.VISIBLE
                binding.addCompleteLinear.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in)
            }
        }
    }
}