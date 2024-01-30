package app.airsignal.weather.as_eye.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.FragmentAddDeviceBleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddDeviceBleFragment : Fragment() {
    private lateinit var parentActivity: AddEyeDeviceActivity
    private lateinit var binding : FragmentAddDeviceBleBinding
    private val animatorSet by lazy {AnimatorSet()}
    private var isAnimationEnabled = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) parentActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_device_ble, container, false)
        parentActivity.changeProgressWithAnimation(50)

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            binding.addBleTitle.text = "AS-Eye를\n검색하고 있습니다"
            startTextAnimation()
            delay(5000)
            binding.addBleTitle.text = "AS-Eye를 찾았습니다"
            delay(1000)
            changeModelVisibility(true)
            delay(4000)
            binding.addBleTitle.text = "블루투스를 연결중입니다"
            delay(4000)
            stopTextAnimation()
            binding.addBleTitle.text = "AS-Eye와 연결이\n완료되었습니다"
            delay(3000)
            parentActivity.transactionFragment(AddDeviceWifiFragment())
        }
    }
    private fun changeModelVisibility(b: Boolean) {
        binding.addBleModelContainer.visibility = if (b) View.VISIBLE else View.INVISIBLE
        binding.addBleModelContainer.animation = AnimationUtils.loadAnimation(requireContext(),R.anim.fade_in)
    }

    private fun changeTitle(s: String) {
        parentActivity.changeTitleWithAnimation(binding.addBleTitle,s,true)
    }

    private fun startScan() {
    }

    private fun cancelScan() {
    }

    private fun successToConnect() {
    }

    private fun failToConnect() {
    }

    private fun connectDevice() {
    }

    private fun startTextAnimation() {
        if (isAnimationEnabled) {
            // Fade-out 애니메이션 설정
            val fadeOutAnimator = ObjectAnimator.ofFloat(binding.addBleTitle, "alpha", 1f, 0.4f)
            fadeOutAnimator.duration = 400
            fadeOutAnimator.interpolator = AccelerateInterpolator()

            // Fade-in 애니메이션 설정
            val fadeInAnimator = ObjectAnimator.ofFloat(binding.addBleTitle, "alpha", 0.4f, 1f)
            fadeInAnimator.duration = 400
            fadeInAnimator.interpolator = AccelerateInterpolator()

            // 애니메이션 반복 설정
            animatorSet.playSequentially(fadeOutAnimator, fadeInAnimator)
            animatorSet.duration = 1800
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    startTextAnimation()
                }

                override fun onAnimationCancel(animation: Animator) {
                    binding.addBleTitle.alpha = 1f
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })

            // 애니메이션 시작
            animatorSet.start()
        }
    }

    private fun stopTextAnimation() {
        isAnimationEnabled = false
        animatorSet.cancel()
        binding.addBleTitle.alpha = 1f
    }
}