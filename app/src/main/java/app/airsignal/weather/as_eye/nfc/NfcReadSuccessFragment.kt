package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.HandlerCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.as_eye.fragment.AddDeviceWifiPasswordFragment
import app.airsignal.weather.databinding.NfcReadSuccessFragmentBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao.userEmail
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.util.TimberUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NfcReadSuccessFragment : Fragment() {
    private lateinit var mActivity: AddEyeDeviceActivity
    private lateinit var binding : NfcReadSuccessFragmentBinding

    private var serial = ""
    private var deviceId = ""
    private var alias = ""
    private var isMaster = "F"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcReadSuccessFragmentBinding.inflate(inflater, null, false)

        binding.nfcReadSBtn.setOnClickListener {
            if (binding.nfcReadSBtn.isEnabled) {
                if (binding.nfcReadSAliasEt.text.isNotBlank()) {
                    mActivity.showPb()
                    HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                        postDevice(SharedPreferenceManager(requireContext()).getString(userEmail),
                        EyeDataModel.PostDevice(serial,alias,isMaster))
                    }, 2000)
                } else {
                    if (binding.nfcReadSAliasEt.visibility == View.GONE)
                        binding.nfcReadSAliasEt.visibility = View.VISIBLE
                    binding.nfcReadSBtn.text = "등록"

                    binding.nfcReadSAliasEt.addTextChangedListener(object : TextWatcher{
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {}

                        override fun afterTextChanged(s: Editable?) {
                            binding.nfcReadSBtn.isEnabled = !s.isNullOrBlank()
                        }
                    })
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments?.getString("payload")
        TimberUtil().d("nfctest","receive payload is $args")
        args?.let {
            val sep = it.split(" ")
            val deviceSerial = sep[2].substring(sep[2].indexOf(":") + 1 , sep[2].lastIndex + 1)
            binding.nfcReadSSerial.text = deviceSerial
            serial = deviceSerial
            alias = binding.nfcReadSAliasEt.text.toString()
            deviceId = SharedPreferenceManager(requireContext()).getString(userEmail)
            isMaster = "F"
            if (binding.nfcReadSSerial.text.toString() != "") {
                binding.nfcReadSBtn.isEnabled = true
            }
        }
    }

    private fun postDevice(email: String, item: EyeDataModel.PostDevice) {
        HttpClient.getInstance(false).setClientBuilder().postDevice(
            email, item
        ).enqueue(
            object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try {
                        mActivity.hidePb()
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "기기 등록이 완료되었습니다", Toast.LENGTH_SHORT).show()
                            mActivity.nfcAdapter.disableForegroundDispatch(mActivity)
                            mActivity.finish()
                        }
                    } catch (e: Exception) {
                        TimberUtil().e("eyetest",e.stackTraceToString())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    mActivity.hidePb()
                    TimberUtil().e("eyetest",t.stackTraceToString())
                }
            }
        )
    }
}