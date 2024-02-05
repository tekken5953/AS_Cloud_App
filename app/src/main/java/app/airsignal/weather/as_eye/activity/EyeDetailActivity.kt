package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.as_eye.fragment.EyeDetailLiveFragment
import app.airsignal.weather.as_eye.fragment.EyeDetailReportFragment
import app.airsignal.weather.as_eye.fragment.EyeSettingFragment
import app.airsignal.weather.dao.RDBLogcat
import app.airsignal.weather.databinding.ActivityEyeDetailBinding
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.viewmodel.GetEyeDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class EyeDetailActivity : AppCompatActivity() {
    companion object {
        const val FRAGMENT_REPORT = 0
        const val FRAGMENT_LIVE = 1
        const val FRAGMENT_SETTING = 2
        var currentFragment = -1
        const val TEST_SERIAL = "AOA0000001F539"
    }

    private lateinit var binding: ActivityEyeDetailBinding

    private lateinit var entireData: EyeDataModel.Measured

    private val dataViewModel by viewModel<GetEyeDataViewModel>()

    private val reportFragment = EyeDetailReportFragment()
    private val liveFragment = EyeDetailLiveFragment()
    private val settingFragment = EyeSettingFragment()

    private var lastRefreshTime = 0L

    override fun onStart() {
        super.onStart()
        dataViewModel.loadData(TEST_SERIAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        lastRefreshTime = 0L
        currentFragment = -1
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_detail)

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
        binding.asDetailTabSetting.setOnClickListener {
            if (currentFragment != FRAGMENT_SETTING)
                tabItemSelected(FRAGMENT_SETTING)
        }

        binding.aeDetailBack.setOnClickListener {
            backToList()
        }

        binding.asDetailRefresh.setOnClickListener {
            dataViewModel.loadData(TEST_SERIAL)
        }

        applyMeasuredData()
    }

    private fun setAnimation(transaction: FragmentTransaction, from: Int, to: Int) {
        var enterAnimation: Int? = null
        var exitAnimation: Int? = null
        when(to) {
            FRAGMENT_REPORT -> {
                if (from == FRAGMENT_LIVE) {
                    enterAnimation = R.anim.enter_from_start
                    exitAnimation = R.anim.exit_to_end
                } else if (from == FRAGMENT_SETTING) {
                    enterAnimation = R.anim.enter_from_start
                    exitAnimation = R.anim.exit_to_end
                }
            }
            FRAGMENT_LIVE -> {
                if (from == FRAGMENT_REPORT) {
                    enterAnimation = R.anim.enter_from_end
                    exitAnimation = R.anim.exit_to_start
                } else if (from == FRAGMENT_SETTING) {
                    enterAnimation = R.anim.enter_from_start
                    exitAnimation = R.anim.exit_to_end
                }
            }
            FRAGMENT_SETTING -> {
                if (from == FRAGMENT_LIVE) {
                    enterAnimation = R.anim.enter_from_end
                    exitAnimation = R.anim.exit_to_start
                } else if (from == FRAGMENT_REPORT) {
                    enterAnimation = R.anim.enter_from_end
                    exitAnimation = R.anim.exit_to_start
                }
            }
        }

        enterAnimation?.let { enter ->
            exitAnimation?.let { exit ->
                transaction.setCustomAnimations(enter, exit)
            }
        }
    }

    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        backToList()
    }

    private fun backToList() {
        val intent = Intent(this, EyeListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun transactionFragment(transaction: FragmentTransaction,frag: Fragment) {
        transaction.replace(R.id.aeDetailFrame, frag)
        transaction.commit()
    }

    private fun tabItemSelected(id: Int) {
        run {
            val transaction = supportFragmentManager.beginTransaction()
            val fromFragment = currentFragment
            currentFragment = id

            setAnimation(transaction,fromFragment, currentFragment)

            transactionFragment(transaction, when (id) {
                FRAGMENT_REPORT -> { reportFragment }
                FRAGMENT_LIVE -> { liveFragment }
                FRAGMENT_SETTING -> { settingFragment }
                else -> throw IllegalArgumentException("Invalid fragment id : $id")
            })

            changeTabResource(id)
        }
    }

    private fun changeTabResource(id: Int) {
        val tabMap = mapOf(
            FRAGMENT_REPORT to binding.asDetailTabReport,
            FRAGMENT_LIVE to binding.asDetailTabLive,
            FRAGMENT_SETTING to binding.asDetailTabSetting
        )

        val selectedTab = tabMap[id] ?: throw IllegalArgumentException("Invalid fragment id: $id")

        tabMap.values.forEach { tab ->
            tab.background = if (tab == selectedTab) {
                getDr(R.drawable.ae_detail_tap_enable)
            } else {
                null
            }

            tab.setTextColor(
                if (tab == selectedTab) {
                    getColor(R.color.white)
                } else {
                    getColor(R.color.ae_sub_color)
                }
            )
        }
    }

    private fun getDr(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    fun isRefreshable(): Boolean {
        return if (System.currentTimeMillis() - lastRefreshTime > 1000 * 30) {
            lastRefreshTime = System.currentTimeMillis()
            true
        } else {
            TimberUtil().i("eyetest", "Not Refreshable")
            false
        }
    }

    private fun applyMeasuredData() {
        try {
            if (!dataViewModel.fetchData().hasObservers()) {
                dataViewModel.fetchData().observe(this) { result ->
                    result?.let { measured ->
                        when (measured) {
                            // 통신 성공
                            is BaseRepository.ApiState.Success -> {
                                hidePb()
                                val body = measured.data
                                TimberUtil().i("eyetest", body.toString())
                                entireData = body

//                                if (isRefreshable()) {
                                    reportFragment.onDataTransfer(
                                        EyeDataModel.ReportFragment(
                                            body.flags,
                                            body.CAIValue,
                                            body.CAILvl,
                                            body.virusValue,
                                            body.virusLvl,
                                            body.pm10p0Value
                                        )
                                    )

                                    liveFragment.onDataTransfer(entireData)

                                    if (currentFragment == -1) {
                                        tabItemSelected(FRAGMENT_REPORT)
                                    }

                                CoroutineScope(Dispatchers.IO).launch {
                                    RDBLogcat.writeEyeMeasured(this@EyeDetailActivity,body.toString())
                                }
//                                }
                            }

                            is BaseRepository.ApiState.Error -> {
                                hidePb()
                                TimberUtil().e("eyetest", measured.errorMessage)
                            }

                            is BaseRepository.ApiState.Loading -> showPb()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            TimberUtil().e("eyetest", "IOException $entireData ${e.stackTraceToString()}")
            hidePb()
        } catch (e: NullPointerException) {
            TimberUtil().e("eyetest", "NullPointerException $entireData ${e.stackTraceToString()}")
            hidePb()
        } catch (e: IndexOutOfBoundsException) {
            TimberUtil().e(
                "eyetest", "IndexOutOfBoundsException $entireData ${e.stackTraceToString()}")
            hidePb()
        }
    }

    private fun showPb() {
        binding.aeDetailPb.bringToFront()
        binding.aeDetailPb.visibility = View.VISIBLE
        binding.eyeDetailContainer.isEnabled = false
        binding.aeDetailFrame.isEnabled = false
    }

    private fun hidePb() {
        binding.aeDetailPb.visibility = View.GONE
        binding.eyeDetailContainer.isEnabled = true
        binding.aeDetailFrame.isEnabled = true
    }
}