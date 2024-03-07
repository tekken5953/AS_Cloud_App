package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.as_eye.fragment.AddDeviceWifiPasswordFragment
import app.airsignal.weather.databinding.NfcReadSuccessFragmentBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.sp.SpDao.userEmail
import app.airsignal.weather.util.TimberUtil

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
                    Toast.makeText(requireContext(), "기기 등록이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    mActivity.nfcAdapter.disableForegroundDispatch(mActivity)
                    mActivity.finish()
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
            val deviceSerial = it.substring(it.lastIndexOf(":") + 1, it.lastIndex + 1)
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
}