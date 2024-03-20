package app.airsignal.weather.as_eye.activity

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.bluetooth.BleClient
import app.airsignal.weather.as_eye.fragment.AddDeviceSerialFragment
import app.airsignal.weather.as_eye.nfc.NfcInfoFragment
import app.airsignal.weather.databinding.ActivityAddEyeDeviceBinding
import app.airsignal.weather.databinding.IncludeEyeAddItemBinding
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import kotlinx.coroutines.*


class AddEyeDeviceActivity : BaseEyeActivity<ActivityAddEyeDeviceBinding>() {
    override val resID: Int get() = R.layout.activity_add_eye_device

    private lateinit var includedBinding: IncludeEyeAddItemBinding
    private lateinit var fragmentManager: FragmentManager

    lateinit var ble: BleClient

    lateinit var nfcAdapter: NfcAdapter

    override fun onResume() {
        super.onResume()
        enableNfc()
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter.isEnabled) {
            nfcAdapter.disableForegroundDispatch(this)
        }
    }

    override fun onStart() {
        super.onStart()
        transactionFragment(AddDeviceSerialFragment())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LoggerUtil().w("testtest", "processIntent : ${intent.action}")
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.addEyeDeviceFrame)
            if (currentFragment is NfcInfoFragment) {
                currentFragment.handleNfcIntent(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        window.statusBarColor = getColor(R.color.white)
        includedBinding = binding.addEyeDeviceTop
        fragmentManager = supportFragmentManager

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        includedBinding.includedEyeListCancel.setOnClickListener {
            createCancelDialog()
        }

        ble = BleClient(this).getInstance()
    }

    fun createCancelDialog() {
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

    fun transactionFragment(frag: Fragment, bundle: Bundle) {
        val transaction = fragmentManager.beginTransaction()
        frag.arguments = bundle
        transaction.replace(R.id.addEyeDeviceFrame, frag)
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
        blockTouch(true)
        binding.addEyeDeviceLoading.bringToFront()
    }

    fun hidePb() {
        blockTouch(false)
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

    private fun enableNfc() {
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.nfc_disabled_msg), Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent(Settings.ACTION_NFC_SETTINGS))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            nfcAdapter.enableForegroundDispatch(
                this,
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                ),
                null,
                null
            )
        }
    }
}