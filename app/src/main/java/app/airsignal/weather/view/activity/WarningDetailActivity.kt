package app.airsignal.weather.view.activity

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import app.airsignal.weather.R
import app.airsignal.weather.adapter.WarningDetailAdapter
import app.airsignal.weather.databinding.ActivityWarningDetailBinding
import app.airsignal.weather.db.sp.GetAppInfo
import app.airsignal.weather.db.sp.GetSystemInfo
import app.airsignal.weather.location.AddressFromRegex
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.utils.controller.ScreenController
import app.airsignal.weather.viewmodel.GetWarningViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.*

class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    private val context: Context by inject()
    private val isKorea get() = GetSystemInfo.getLocale(context) == Locale.KOREA

    private enum class CityCode(val code: Int, val title: String, val titleShort: String) {
        ENTIRE(109, "전국", "전국"),
        SEOUL(108, "서울시", "서울"),
        GYEONGGI(108, "경기도", "경기"),
        INCHEON(108, "인천 광역시", "인천"),
        GANGWON(105, "강원도", "강원"),
        CHUNG_BUK(131, "충청북도", "충북"),
        CHUNG_NAM(133, "충청남도", "충남"),
        JEON_NAM(156, "전라남도", "전남"),
        JEON_BUK(146, "전라북도", "전북"),
        GYEONG_NAM(159, "경상남도", "경남"),
        GYEONG_BUK(143, "경상북도", "경북"),
        JEJU(184, "제주도", "제주"),
        JEJU_FULL(184, "제주특별자치도", "제주")
    }

    private val warningList = ArrayList<String>()
    private val warningAdapter = WarningDetailAdapter(this, warningList)
    private val warningViewModel by viewModel<GetWarningViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        ScreenController(this).setStatusBar()

        applyWarning()

        binding.warningListView.adapter = warningAdapter

        binding.warningListView.itemAnimator = null

        binding.warningBack.setOnClickListener { finish() }

        binding.warningMainLayout.setOnClickListener {
            if (binding.warningAddr.isShowing) binding.warningAddr.dismiss()
        }

        val regexAddress =
            if (intent.extras?.getBoolean("isMain") == true)
                AddressFromRegex(GetAppInfo.getUserLastAddress(this)).getWarningAddress()
            else GetAppInfo.getWarningFixed(this)

        // 수정 된 주소에 따른 적용
        val regexAddr = if (regexAddress != "Error") regexAddress else GetAppInfo.getNotificationAddress(this)

        binding.warningAddr.selectItemByIndex(parseStringToIndex(regexAddr))
        warningViewModel.loadDataResult(parseRegionToCode(regexAddr))

        binding.warningAddr.setOnSpinnerItemSelectedListener<String> { _, _, _, newText ->
            warningViewModel.loadDataResult(parseRegionToCode(newText))
        }

        binding.warningNoResult.setOnClickListener {
            binding.warningAddr.selectItemByIndex(0)
            warningViewModel.loadDataResult(109)
        }
    }

    // 앱 버전 뷰모델 데이터 호출
    private fun applyWarning() {
        kotlin.runCatching {
            warningList.clear()

            warningViewModel.getResultData.observe(this) { result ->
                result?.let { warning ->
                    when (warning) {
                        is BaseRepository.ApiState.Success ->
                            warning.data.content?.let { content ->
                                if (content.isNotEmpty()) {
                                    hideNoResult()

                                    binding.warningTime.text =
                                        if (isKorea) "${warning.data.time?.format(DateTimeFormatter.ofPattern("HH : mm"))} 기준"
                                        else "${warning.data.time?.format(DateTimeFormatter.ofPattern("HH : mm"))} KST"

                                    warningList.addAll(content.map { it.replace("○", "").trim() })
                                    warningAdapter.notifyItemRangeInserted(0, warningList.size)
                                } else showNoResult()
                            } ?: showNoResult()
                        is BaseRepository.ApiState.Error -> showNoResult()
                        is BaseRepository.ApiState.Loading -> binding.warningPb.visibility = View.VISIBLE
                        else -> {}
                    }
                } ?: run { showNoResult() }
            }
        }.onFailure { exception -> if (exception == IOException()) showNoResult() }
    }

    private fun showNoResult() {
        val isNationwide = binding.warningAddr.text.toString() == CityCode.ENTIRE.title

        binding.warningNoResult.run {
            this.text = if (isNationwide) context.getString(R.string.no_entire_summary)
            else context.getString(R.string.no_region_summary)

            this.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null,
                if (!isNationwide) ResourcesCompat.getDrawable(resources, R.drawable.search, null)
                else null, null
            )

            this.isClickable = !isNationwide

            if (!isNationwide) this.bringToFront()

            TextViewCompat.setCompoundDrawableTintList(
                this, ColorStateList.valueOf(getColor(R.color.theme_text_color))
            )

            binding.warningNoResult.visibility = View.VISIBLE
            binding.warningPb.visibility = View.GONE

            warningAdapter.notifyItemRangeChanged(0, warningList.lastIndex)
        }
    }

    private fun hideNoResult() {
        binding.warningNoResult.isClickable = false
        binding.warningPb.visibility = View.GONE
        binding.warningNoResult.visibility = View.GONE
    }

    // 지역명을 지역 코드로 변환
    private fun parseRegionToCode(region: String): Int =
        CityCode.values().find { it.title == parseRegionFullName(region) }?.code ?: CityCode.ENTIRE.code

    // 지역명을 전체 명칭으로 변환
    private fun parseRegionFullName(region: String): String =
        CityCode.values().find { region == it.titleShort }?.title ?: region

    private fun parseStringToIndex(region: String): Int =
        CityCode.values().find { it.title == parseRegionFullName(region) }?.ordinal ?: 0
}