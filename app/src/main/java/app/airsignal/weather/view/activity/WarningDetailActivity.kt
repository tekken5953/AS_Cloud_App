package app.airsignal.weather.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.airsignal.weather.R
import app.airsignal.weather.adapter.WarningDetailAdapter
import app.airsignal.weather.databinding.ActivityWarningDetailBinding
import app.airsignal.weather.repo.BaseRepository
import app.airsignal.weather.util.AddressFromRegex
import app.airsignal.weather.util.EnterPageUtil
import app.airsignal.weather.util.LinearLayoutManagerWrapper
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.`object`.GetAppInfo.getNotificationAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLastAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getWarningFixed
import app.airsignal.weather.util.`object`.SetSystemInfo
import app.airsignal.weather.vmodel.GetWarningViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class WarningDetailActivity : BaseActivity<ActivityWarningDetailBinding>() {
    override val resID: Int get() = R.layout.activity_warning_detail

    private val warningList = ArrayList<String>()
    private val warningAdapter = WarningDetailAdapter(this, warningList)
    private val warningViewModel by viewModel<GetWarningViewModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        SetSystemInfo.setStatusBar(this)

        applyWarning()

        binding.warningListView.adapter = warningAdapter

        binding.warningBack.setOnClickListener { finish() }

        binding.warningMainLayout.setOnClickListener {
            if (binding.warningAddr.isShowing) {
                binding.warningAddr.dismiss()
            }
        }

        val regexAddress = if (intent.extras?.getBoolean("isMain") == true) {
            AddressFromRegex(getUserLastAddress(this)).getWarningAddress()
        } else {
            getWarningFixed(this)
        }
        // 수정 된 주소에 따른 적용
        val regexAddr =
            if (regexAddress != "Error") regexAddress
            else getNotificationAddress(this)

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
        warningViewModel.fetchData().observe(this) { result ->
            result?.let { warning ->
                when (warning) {
                    is BaseRepository.ApiState.Success -> {
                        binding.warningPb.visibility = View.GONE
                        warning.data.content?.let { content ->
                            if (content.isNotEmpty()) {
                                binding.warningNoResult.visibility = View.GONE
                                warningList.clear()
                                warningList.addAll(content.map { it.replace("○", "").trim()})
                                warningAdapter.notifyItemRangeInserted(0,warningList.size)
                            } else {
                                binding.warningNoResult.visibility = View.VISIBLE
                            }
                        } ?: apply {
                            binding.warningNoResult.visibility = View.VISIBLE
                        }
                    }
                    is BaseRepository.ApiState.Error -> {
                        binding.warningPb.visibility = View.GONE
                        binding.warningNoResult.visibility = View.VISIBLE
                    }
                    is BaseRepository.ApiState.Loading -> {
                        binding.warningPb.visibility = View.VISIBLE
                    }
                }
            } ?: apply {
                binding.warningPb.visibility = View.GONE
                binding.warningNoResult.visibility = View.VISIBLE
            }
        }
    }

    // 지역명을 지역 코드로 변환
    private fun parseRegionToCode(region: String): Int {
        return when (parseRegionFullName(region)) {
            "서울시", "경기도", "인천 광역시" -> {
                108
            }
            "강원도" -> {
                105
            }
            "충청남도" -> {
                133
            }
            "충청북도" -> {
                131
            }
            "전라남도" -> {
                156
            }
            "전라북도" -> {
                146
            }
            "경상남도" -> {
                159
            }
            "경상북도" -> {
                143
            }
            "제주도" -> {
                184
            }
            else -> {
                109
            }
        }
    }

    // 지역명을 전체 명칭으로 변환
    private fun parseRegionFullName(region: String): String {
        return when (region) {
            "서울" -> {
                "서울시"
            }
            "경기" -> {
                "경기도"
            }
            "인천" -> {
                "인천 광역시"
            }
            "강원" -> {
                "강원도"
            }
            "충남" -> {
                "충청남도"
            }
            "충북" -> {
                "충청북도"
            }
            "전남" -> {
                "전라남도"
            }
            "전북" -> {
                "전라북도"
            }
            "경남" -> {
                "경상남도"
            }
            "경북" -> {
                "경상북도"
            }
            "제주" -> {
                "제주도"
            }
            "전국" -> {
                "전국"
            }
            else -> {
                region
            }
        }
    }

    private fun parseStringToIndex(region: String): Int {
        return when (parseRegionFullName(region)) {
            "서울시" -> {
                1
            }
            "경기도" -> {
                2
            }
            "인천 광역시" -> {
                3
            }
            "강원도" -> {
                4
            }
            "충청남도" -> {
                5
            }
            "충청북도" -> {
                6
            }
            "전라남도" -> {
                7
            }
            "전라북도" -> {
                8
            }
            "경상남도" -> {
                9
            }
            "경상북도" -> {
                10
            }
            "제주도" -> {
                11
            }
            else -> {
                0
            }
        }
    }
}