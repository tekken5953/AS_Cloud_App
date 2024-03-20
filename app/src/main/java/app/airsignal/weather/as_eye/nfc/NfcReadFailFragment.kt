package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity
import app.airsignal.weather.databinding.NfcReadFailFragmentBinding

class NfcReadFailFragment : Fragment() {
    private lateinit var mActivity: AddEyeDeviceActivity
    private lateinit var binding : NfcReadFailFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEyeDeviceActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcReadFailFragmentBinding.inflate(inflater, null, false)

        binding.nfcReadFCancelBtn.setOnClickListener {
            mActivity.createCancelDialog()
        }

        binding.nfcReadFRetryBtn.setOnClickListener {
            mActivity.transactionFragment(NfcInfoFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity.changeTitleWithAnimation(binding.nfcReadFTitle,"불러오기를 실패했습니다", false)
        mActivity.changeTitleWithAnimation(binding.nfcReadFSubTitle,"스마트폰의 NFC 활성화를 확인해주세요", false)
    }
}