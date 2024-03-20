package app.airsignal.weather.as_eye.nfc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.NfcInfoFragmentBinding
import app.airsignal.weather.util.LoggerUtil
import app.airsignal.weather.util.TimberUtil


class NfcInfoFragment : Fragment() {
    private lateinit var mActivity: AddEyeDeviceActivity
    private lateinit var binding : NfcInfoFragmentBinding

    fun handleNfcIntent(intent: Intent) {
        processIntent(intent)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcInfoFragmentBinding.inflate(inflater, null, false)

        binding.nfcInfoTitle.setOnClickListener {
            mActivity.transactionFragment(NfcReadSuccessFragment())
        }

        binding.nfcInfoTitle.setOnLongClickListener {
            mActivity.transactionFragment(NfcReadFailFragment())
            true
        }

        mActivity.changeProgressWithAnimation(50)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scaleAnimatorX = ObjectAnimator.ofFloat(binding.nfcInfoDeviceImg, "scaleX", 1.0f, 1.1f, 1.0f)
        val scaleAnimatorY = ObjectAnimator.ofFloat(binding.nfcInfoDeviceImg, "scaleY", 1.0f, 1.1f, 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleAnimatorX, scaleAnimatorY)
        animatorSet.duration = 1500
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animatorSet.start()
            }
        })
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
    }

    private fun processIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            @Suppress("DEPRECATION") val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                binding.nfcInfoProgress.text = "스캔 성공"
                binding.nfcInfoProgress.setTextColor(requireContext().getColor(R.color.main_blue_color))
                for (message in rawMessages) {
                    val ndefMessage = message as NdefMessage
                    val records = ndefMessage.records
                    val sb = StringBuilder()
                    for (record in records) {
                        val mPayload = record.payload.toString(Charsets.UTF_8)
                        sb.append(mPayload.replace("ko",""))
                    }

                    HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                        processNfcPayload(sb.toString())
                    },1500)
                }
            } else {
                binding.nfcInfoProgress.text = "스캔 실패"
                binding.nfcInfoProgress.setTextColor(requireContext().getColor(R.color.red))
                HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                    processNfcPayload(null)
                },1500)
            }
        }
    }

    private fun processNfcPayload(payload: String?) {
        payload?.let {
            TimberUtil().d("nfctest","NFC에서 읽은 데이터: $it")
            val space = it.replace("\u0002"," ").replace("ko"," ")
            val bundle = Bundle()
            bundle.putString("payload",space)
            mActivity.transactionFragment(NfcReadSuccessFragment(),bundle)
        } ?: run { mActivity.transactionFragment(NfcReadFailFragment()) }
    }
}