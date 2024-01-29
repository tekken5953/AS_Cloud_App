package app.airsignal.weather.as_eye.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.fragment.AddDeviceWifiFragment
import app.airsignal.weather.databinding.ActivityAddEyeDeviceBinding
import app.airsignal.weather.databinding.IncludeEyeAddItemBinding

class AddEyeDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEyeDeviceBinding
    private lateinit var includedBinding: IncludeEyeAddItemBinding
    private lateinit var fragmentManager: FragmentManager

    override fun onStart() {
        super.onStart()
        transactionFragment(AddDeviceWifiFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_eye_device)
        includedBinding = binding.addEyeDeviceTop
        fragmentManager = supportFragmentManager

        includedBinding.includedEyeListBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount != 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    private fun transactionFragment(frag: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.addEyeDeviceFrame, frag)
        transaction.commit()
    }
}