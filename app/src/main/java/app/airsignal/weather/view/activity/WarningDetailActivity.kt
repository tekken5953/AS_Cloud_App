package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import app.airsignal.weather.R
import app.airsignal.weather.adapter.WarningDetailAdapter
import app.airsignal.weather.address.AddressFromRegex
import app.airsignal.weather.databinding.ActivityWarningDetailBinding
import app.airsignal.weather.db.sp.GetAppInfo.getNotificationAddress
import app.airsignal.weather.db.sp.GetAppInfo.getUserLastAddress
import app.airsignal.weather.db.sp.GetAppInfo.getWarningFixed
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.`object`.DataTypeParser.setStatusBar
import app.airsignal.weather.viewmodel.GetWarningViewModel
import org.angmarch.views.OnSpinnerItemSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*


class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    private val warningList = ArrayList<String>()
    private val warningAdapter = WarningDetailAdapter(this, warningList)
    private val warningViewModel by viewModel<GetWarningViewModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        setStatusBar(this)

        applyWarning()

        binding.warningListView.adapter = warningAdapter

        binding.warningBack.setOnClickListener { finish() }

        val regexAddress = if (intent.extras?.getBoolean("isMain") == true) {
            AddressFromRegex(getUserLastAddress(this)).getWarningAddress()
        } else { getWarningFixed(this) }

        // 수정 된 주소에 따른 적용
        val regexAddr =
            if (regexAddress != "Error") regexAddress
            else getNotificationAddress(this)

        warningViewModel.loadDataResult(parseRegionToCode(regexAddr))

        binding.warningNoResult.setOnClickListener {
            binding.warningAddr.selectedIndex = 0
            warningViewModel.loadDataResult(109)
        }

        val dataset: List<String> = resources.getStringArray(R.array.warning_address_list).asList()
        binding.warningAddr.attachDataSource(dataset)

        binding.warningAddr.onSpinnerItemSelectedListener =
            OnSpinnerItemSelectedListener { _, _, position, _ ->
                warningViewModel.loadDataResult(parseRegionToCode(dataset[position])) }
    }

    // 앱 버전 뷰모델 데이터 호출
    @SuppressLint("NotifyDataSetChanged")
    private fun applyWarning() {
        try {
            warningViewModel.fetchData().observe(this) { result ->
                warningList.clear()
                result?.let { warning ->
                    when (warning) {
                        is BaseRepository.ApiState.Success -> {
                            warning.data.content?.let { content ->
                                if (content.isNotEmpty()) {
                                    hideNoResult()
                                    warningList.addAll(content.map { it.replace("○", "").trim()})
                                    warningAdapter.notifyItemRangeInserted(0, warningList.size)
                                } else showNoResult()
                            } ?: showNoResult()
                        }
                        is BaseRepository.ApiState.Error -> {
                            showNoResult()
                            warningAdapter.notifyDataSetChanged()
                        }
                        is BaseRepository.ApiState.Loading -> {
                            binding.warningPb.visibility = View.VISIBLE
                        }
                    }
                } ?: run {
                    showNoResult()
                    warningAdapter.notifyDataSetChanged()
                }
            }
        } catch (e: IOException) {
            showNoResult()
            warningAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNoResult() {
        val noResultTextView = binding.warningNoResult
        val isNationwide = binding.warningAddr.text.toString() == "전국"

        noResultTextView.run {
            this.text = if (isNationwide) "현재 전국의 기상특보가 없습니다."
            else "현재 지역의 기상 특보가 없습니다.\n전국으로 검색하시겠습니까?"

            this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                if (!isNationwide) ResourcesCompat.getDrawable(resources, R.drawable.search, null)
                else null, null
            )

            this.isClickable = !isNationwide

            if (!isNationwide) this.bringToFront()

            TextViewCompat.setCompoundDrawableTintList(this,
                ColorStateList.valueOf(getColor(R.color.theme_text_color)))

            binding.warningNoResult.visibility = View.VISIBLE
            binding.warningPb.visibility = View.GONE
        }
    }

    private fun hideNoResult() {
        binding.warningNoResult.isClickable = false
        binding.warningPb.visibility = View.GONE
        binding.warningNoResult.visibility = View.GONE
    }

    // 지역명을 지역 코드로 변환
    private fun parseRegionToCode(region: String): Int {
        val fullName = parseRegionFullName(region)
        val regionMap = mapOf(
            setOf("서울시", "경기도", "인천 광역시") to 108,
            "강원도" to 105,
            "충청남도" to 133,
            "충청북도" to 131,
            "전라남도" to 156,
            "전라북도" to 146,
            "경상남도" to 159,
            "경상북도" to 143,
            "제주도" to 184
        )
        return regionMap[fullName] ?: 109
    }

    // 지역명을 전체 명칭으로 변환
    private fun parseRegionFullName(region: String): String {
        val regionMap = mapOf("서울" to "서울시",
        "경기" to "경기도",
        "인천" to "인천 광역시",
        "강원" to "강원도",
        "충남" to "충청남도",
        "충북" to "충청북도",
        "전남" to "전라남도",
        "전북" to "전라북도",
        "경남" to "경상남도",
        "경북" to "경상북도",
        "제주" to "제주도")
        return regionMap[region] ?: region
    }

    private fun parseStringToIndex(region: String): Int {
        val fullName = parseRegionFullName(region)
        val regionMap = mapOf(
            "서울시" to 1,
            "경기도" to 2,
            "인천 광역시" to 3,
            "강원도" to 4,
            "충청남도" to 5,
            "충청북도" to 6,
            "전라남도" to 7,
            "전라북도" to 8,
            "경상남도" to 9,
            "경상북도" to 10,
            "제주도" to 11
        )
        return regionMap[fullName] ?: 0
    }
}