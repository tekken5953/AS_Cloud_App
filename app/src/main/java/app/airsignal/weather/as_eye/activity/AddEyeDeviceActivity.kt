package app.airsignal.weather.as_eye.activity

import android.animation.ObjectAnimator
import android.app.StatusBarManager
import android.hardware.lights.LightState
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.bluetooth.BleClient
import app.airsignal.weather.as_eye.fragment.AddDeviceSerialFragment
import app.airsignal.weather.databinding.ActivityAddEyeDeviceBinding
import app.airsignal.weather.databinding.IncludeEyeAddItemBinding
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import kotlinx.coroutines.*

class AddEyeDeviceActivity : BaseEyeActivity<ActivityAddEyeDeviceBinding>() {
    override val resID: Int get() = R.layout.activity_add_eye_device

    private lateinit var includedBinding: IncludeEyeAddItemBinding
    private lateinit var fragmentManager: FragmentManager

    lateinit var ble: BleClient

    override fun onStart() {
        super.onStart()
        transactionFragment(AddDeviceSerialFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        window.statusBarColor = getColor(R.color.white)
        includedBinding = binding.addEyeDeviceTop
        fragmentManager = supportFragmentManager

        includedBinding.includedEyeListCancel.setOnClickListener {
            createCancelDialog()
        }

        ble = BleClient(this).getInstance()
    }

    private fun createCancelDialog() {
        val dialog = MakeDoubleDialog(this)
        val show = dialog.make(getString(R.string.eye_add_cancel),getString(R.string.yes),getString(R.string.no),R.color.red)
        show.first.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ble.destroyBle()

                withContext(Dispatchers.Main) {
                    finish()
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
                }
            }
        }
        show.second.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun transactionFragment(frag: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.addEyeDeviceFrame, frag)
        transaction.commit()
    }

    fun transactionFragment(oldFrag: Fragment, newFrag: Fragment, bundle: Bundle) {
        val transaction = fragmentManager.beginTransaction()
        oldFrag.arguments = bundle
        transaction.replace(R.id.addEyeDeviceFrame, newFrag)
        transaction.commit()
    }

    fun changeProgressWithAnimation(p: Int) {
        val progressAnimator: ObjectAnimator =
            ObjectAnimator.ofInt(binding.addEyeDevicePb, "progress", binding.addEyeDevicePb.progress, p)
        progressAnimator.duration = 700
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

    override fun onBackPressed() {
        createCancelDialog()
    }
}