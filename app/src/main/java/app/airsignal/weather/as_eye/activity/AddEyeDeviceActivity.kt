package app.airsignal.weather.as_eye.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.bluetooth.BleClient
import app.airsignal.weather.as_eye.fragment.AddDeviceSerialFragment
import app.airsignal.weather.databinding.ActivityAddEyeDeviceBinding
import app.airsignal.weather.databinding.IncludeEyeAddItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddEyeDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEyeDeviceBinding
    private lateinit var includedBinding: IncludeEyeAddItemBinding
    private lateinit var fragmentManager: FragmentManager

    lateinit var ble: BleClient

    override fun onStart() {
        super.onStart()
        transactionFragment(AddDeviceSerialFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_eye_device)
        includedBinding = binding.addEyeDeviceTop
        fragmentManager = supportFragmentManager

        includedBinding.includedEyeListBack.setOnClickListener {

        }

        ble = BleClient(this).getInstance()
    }

    fun transactionFragment(frag: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.addEyeDeviceFrame, frag)
        transaction.commit()
    }

    fun changeProgressWithAnimation(p: Int) {
        val progressAnimator: ObjectAnimator =
            ObjectAnimator.ofInt(binding.addEyeDevicePb, "progress", binding.addEyeDevicePb.progress, p)
        progressAnimator.duration = 1000
        progressAnimator.start()
    }

    fun changeTitleWithAnimation(tv: TextView, s: String, isFadeout: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            if (isFadeout) {
                tv.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@AddEyeDeviceActivity,
                        R.anim.fade_out
                    )
                )
                delay(250L)
            }
            tv.text = s
            tv.startAnimation(
                AnimationUtils.loadAnimation(
                    this@AddEyeDeviceActivity,
                    R.anim.fade_in
                )
            )
        }
    }

    fun showPb() {
        binding.addEyeDeviceLoading.visibility = View.VISIBLE
        binding.addEyeDeviceLoading.bringToFront()
    }

    fun hidePb() {
        binding.addEyeDeviceLoading.visibility = View.GONE
    }

    fun hideTopBar() {
        binding.addEyeTopContainer.visibility = View.GONE
        binding.addEyeTopContainer.animation = AnimationUtils.loadAnimation(this,R.anim.hide_bottom_to_top)
    }

    fun showTopBar() {
        binding.addEyeTopContainer.visibility = View.VISIBLE
        binding.addEyeTopContainer.animation = AnimationUtils.loadAnimation(this,R.anim.hide_bottom_to_top)
    }
}