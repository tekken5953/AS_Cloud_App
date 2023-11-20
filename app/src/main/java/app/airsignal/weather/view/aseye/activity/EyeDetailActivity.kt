package app.airsignal.weather.view.aseye.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.adapter.WarningViewPagerAdapter
import app.airsignal.weather.databinding.ActivityEyeDetailBinding
import app.airsignal.weather.view.aseye.fragment.EyeDetailLifeFragment
import app.airsignal.weather.view.aseye.fragment.EyeDetailLiveFragment
import app.airsignal.weather.view.aseye.fragment.EyeDetailReportFragment
import java.util.ArrayList

class EyeDetailActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_REPORT = 0
        const val FRAGMENT_LIVE = 1
        const val FRAGMENT_LIFE = 2
        var currentFragment = 0
    }

    private lateinit var binding: ActivityEyeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_detail)

        tabItemSelected(FRAGMENT_REPORT)

        val nameExtra = intent.getStringExtra("name")
        val serialExtra = intent.getStringExtra("serial")
        binding.aeDetailTitle.text = nameExtra
        binding.asDetailSerial.text = serialExtra

        binding.asDetailTabReport.setOnClickListener {
            if (currentFragment != FRAGMENT_REPORT)
                tabItemSelected(FRAGMENT_REPORT)
        }
        binding.asDetailTabLive.setOnClickListener {
            if (currentFragment != FRAGMENT_LIVE)
                tabItemSelected(FRAGMENT_LIVE)
        }
        binding.asDetailTabLife.setOnClickListener {
            if (currentFragment != FRAGMENT_LIFE)
                tabItemSelected(FRAGMENT_LIFE)
        }

        binding.aeDetailBack.setOnClickListener {
            finish()
        }
    }

    private fun transactionFragment(frag: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.aeDetailFrame, frag)
        if (!supportFragmentManager.isStateSaved) {
            transaction.commit()
        }
    }

    private fun tabItemSelected(id: Int) {
        when (id) {
            FRAGMENT_REPORT -> {
                currentFragment = id
                transactionFragment(EyeDetailReportFragment())
                changeTabResource(id)
            }
            FRAGMENT_LIVE -> {
                currentFragment = id
                transactionFragment(EyeDetailLiveFragment())
                changeTabResource(id)
            }
            FRAGMENT_LIFE -> {
                currentFragment = id
                transactionFragment(EyeDetailLifeFragment())
                changeTabResource(id)
            }
        }
    }

    private fun changeTabResource(id: Int) {
        when (id) {
            FRAGMENT_REPORT -> {
                binding.asDetailTabReport.run {
                    background = getDr(R.drawable.ae_detail_tap_enable)
                    setTextColor(getColor(R.color.white))
                }
                binding.asDetailTabLive.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
                binding.asDetailTabLife.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
            }
            FRAGMENT_LIVE -> {
                binding.asDetailTabLive.run {
                    background = getDr(R.drawable.ae_detail_tap_enable)
                    setTextColor(getColor(R.color.white))
                }
                binding.asDetailTabReport.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
                binding.asDetailTabLife.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
            }
            FRAGMENT_LIFE -> {
                binding.asDetailTabLife.run {
                    background = getDr(R.drawable.ae_detail_tap_enable)
                    setTextColor(getColor(R.color.white))
                }
                binding.asDetailTabLive.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
                binding.asDetailTabReport.run {
                    background = null
                    setTextColor(getColor(R.color.ae_sub_color))
                }
            }
        }
    }

    private fun getDr(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources,id,null)
    }
}