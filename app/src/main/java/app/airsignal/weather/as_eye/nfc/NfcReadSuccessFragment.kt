package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.airsignal.weather.as_eye.fragment.AddDeviceWifiPasswordFragment
import app.airsignal.weather.databinding.NfcReadSuccessFragmentBinding

class NfcReadSuccessFragment : Fragment() {
    private lateinit var mActivity: NfcMainActivity
    private lateinit var binding : NfcReadSuccessFragmentBinding

    private var serial = ""
    private var deviceId = ""
    private var alias = ""
    private var isMaster = "F"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NfcMainActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcReadSuccessFragmentBinding.inflate(inflater, null, false)

        binding.nfcReadSBtn.setOnClickListener {
            if (binding.nfcReadSBtn.isEnabled) {
                val bundle = Bundle()
                bundle.apply {
                    putString("serial", serial)
                    putString("deviceId", deviceId)
                    putString("alias", alias)
                    putString("isMaster", isMaster)
                }

                // 타겟 프래그먼트로 이동
                val targetFragment = AddDeviceWifiPasswordFragment()
                targetFragment.arguments = bundle

                mActivity.transactionFragment(targetFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}