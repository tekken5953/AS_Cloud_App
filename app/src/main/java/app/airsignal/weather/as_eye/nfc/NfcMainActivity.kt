package app.airsignal.weather.as_eye.nfc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.AddEyeDeviceActivity

class NfcMainActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_INFO = 0
        const val FRAGMENT_READ_SUCCESS = 1
        const val FRAGMENT_READ_FAIL = 2
        var currentFragment = -1
    }

    private val infoFragment = NfcInfoFragment()
    private val readSuccessFragment = NfcReadSuccessFragment()
    private val readFailFragment = NfcReadFailFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_main_activity)

        setFragmentTransaction(FRAGMENT_INFO)
    }

    fun transactionFragment(frag: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nfcMainFrame, frag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun setFragmentTransaction(id: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment = id

        transactionFragment(
            when (id) {
                FRAGMENT_INFO -> {
                    infoFragment
                }
                FRAGMENT_READ_SUCCESS -> {
                    readSuccessFragment
                }
                FRAGMENT_READ_FAIL -> {
                    readFailFragment
                }
                else -> throw IllegalArgumentException("Invalid fragment id : $id")
            }
        )
    }
}