package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
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
import app.airsignal.weather.util.OnSingleClickListener
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.`object`.DataTypeParser.getAverageTime
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.viewmodel.GetEyeDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class EyeDetailActivity : BaseEyeActivity<ActivityEyeDetailBinding>() {
    override val resID: Int get() = R.layout.activity_eye_detail

    companion object {
        const val FRAGMENT_REPORT = 0
        const val FRAGMENT_LIVE = 1
        const val FRAGMENT_SETTING = 2
        var currentFragment = -1
    }

    val aliasExtra by lazy {intent.getStringExtra("alias")}
    val serialExtra by lazy {intent.getStringExtra("serial")}
    val ssidExtra by lazy {intent.getStringExtra("ssid")}
    val createExtra by lazy {intent.getStringExtra("create_at")}
    val isMaster by lazy {intent.getBooleanExtra("is_master", false)}
    val modelName by lazy {intent.getStringExtra("model_name")}

    enum class AverageFlag(val flag: String) {
        HOURLY("hour"),
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly")
    }

    private lateinit var entireData: EyeDataModel.Entire

    private val dataViewModel by viewModel<GetEyeDataViewModel>()

    private val reportFragment = EyeDetailReportFragment()
    private val liveFragment = EyeDetailLiveFragment()
    private val settingFragment = EyeSettingFragment()

    private var lastRefreshTime = 0L

    private val fetch by lazy {dataViewModel.fetchData()}

    override fun onStart() {
        super.onStart()
        serialExtra?.let { sendApiData(it) }
    }

    private fun destroyObserver() {
        dataViewModel.cancelJob()
        fetch.removeObservers(this)
        TimberUtil().w("lifecycle_test", "아이 디테일 옵저버 제거")
    }

    override fun onDestroy() {
        super.onDestroy()
        lastRefreshTime = 0L
        currentFragment = -1
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.aeDetailTitle.text = aliasExtra
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

        binding.aeDetailBack.setOnClickListener { backToList() }

        binding.asDetailRefresh.setOnClickListener(object: OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                serialExtra?.let { sendApiData(it) }
            }
        })
    }

    private fun sendApiData(serial: String) {
        if (fetch.hasObservers()) { destroyObserver() }
        applyMeasuredData()
        TimberUtil().w("lifecycle_test","아이 디테일 옵저버 생성")
        dataViewModel.loadData(serial,AverageFlag.HOURLY.flag,getAverageTime(getCurrentTime()),getAverageTime(getCurrentTime()))
    }

    private fun setAnimation(transaction: FragmentTransaction, from: Int, to: Int) {
        var enterAnimation: Int? = null
        var exitAnimation: Int? = null
        when (to) {
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

    private fun transactionFragment(transaction: FragmentTransaction, frag: Fragment) {
        transaction.replace(R.id.aeDetailFrame, frag)
        transaction.commit()
    }

    private fun tabItemSelected(id: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        setAnimation(transaction, currentFragment, id)

        transactionFragment(
            transaction, when (id) {
                FRAGMENT_REPORT -> { reportFragment }
                FRAGMENT_LIVE -> { liveFragment }
                FRAGMENT_SETTING -> { settingFragment }
                else -> throw IllegalArgumentException("Invalid fragment id : $id")
            }
        )

        changeTabResource(id)

        currentFragment = id
    }

    private fun changeTabResource(id: Int) {
        val tabMap = mapOf(
            FRAGMENT_REPORT to binding.asDetailTabReport,
            FRAGMENT_LIVE to binding.asDetailTabLive,
            FRAGMENT_SETTING to binding.asDetailTabSetting
        )

        val selectedTab = tabMap[id] ?: throw IllegalArgumentException("Invalid fragment id: $id")

        tabMap.values.forEach { tab ->
            tab.background = if (tab == selectedTab) getDr(R.drawable.ae_detail_tap_enable) else null

            tab.setTextColor(
                if (tab == selectedTab) getColor(R.color.white)
                else getColor(R.color.ae_sub_color)
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
        } else { false }
    }

    private fun applyMeasuredData() {
        try {
            fetch.observe(this) { result ->
                result?.let { measured ->
                    when (measured) {
                        // 통신 성공
                        is BaseRepository.ApiState.Success -> {
                            hidePb()
                            val body = measured.data
                            entireData = body

                            val current = body.current
//                                if (isRefreshable()) {
                            current.let { currentData ->
                                reportFragment.onDataTransfer(
                                    EyeDataModel.ReportFragment(
                                        currentData.flags,
                                        currentData.CAIValue,
                                        currentData.CAILvl,
                                        currentData.virusValue,
                                        currentData.virusLvl,
                                        currentData.pm10p0Value,
                                        body.average,
                                        body.noiseRecent
                                    )
                                )

                                liveFragment.onDataTransfer(currentData)
                            }

                            if (currentFragment == -1) {
                                tabItemSelected(FRAGMENT_REPORT)
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                RDBLogcat.writeEyeMeasured(
                                    this@EyeDetailActivity, body.toString()
                                )
                            }
//                                }
                        }

                        is BaseRepository.ApiState.Error -> hidePb()
                        is BaseRepository.ApiState.Loading -> showPb()
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
                "eyetest", "IndexOutOfBoundsException $entireData ${e.stackTraceToString()}"
            )
            hidePb()
        }
    }

    fun showPb() {
        binding.aeDetailPb.bringToFront()
        binding.aeDetailPb.visibility = View.VISIBLE
        binding.eyeDetailContainer.isEnabled = false
        binding.aeDetailFrame.isEnabled = false
        blockTouch(true)
    }

    fun hidePb() {
        binding.aeDetailPb.visibility = View.GONE
        binding.eyeDetailContainer.isEnabled = true
        binding.aeDetailFrame.isEnabled = true
        blockTouch(false)
    }
}