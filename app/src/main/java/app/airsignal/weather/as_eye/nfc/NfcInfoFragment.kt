package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.NfcInfoFragmentBinding

class NfcInfoFragment : Fragment() {
    private lateinit var mActivity: AddEyeDeviceActivity
    private lateinit var binding : NfcInfoFragmentBinding

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}