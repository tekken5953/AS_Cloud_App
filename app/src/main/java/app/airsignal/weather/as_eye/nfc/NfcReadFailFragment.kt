package app.airsignal.weather.as_eye.nfc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.airsignal.weather.databinding.NfcReadFailFragmentBinding

class NfcReadFailFragment : Fragment() {
    private lateinit var mActivity: NfcMainActivity
    private lateinit var binding : NfcReadFailFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NfcMainActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NfcReadFailFragmentBinding.inflate(inflater, null, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}