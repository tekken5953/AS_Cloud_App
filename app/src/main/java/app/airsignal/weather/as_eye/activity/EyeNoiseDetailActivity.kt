package app.airsignal.weather.as_eye.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.os.HandlerCompat
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.NoiseDetailAdapter
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.databinding.ActivityEyeNoiseDetailBinding
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.viewmodel.NoiseDataViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.LocalDateTime

class EyeNoiseDetailActivity : BaseEyeActivity<ActivityEyeNoiseDetailBinding>() {
    override val resID: Int get() = R.layout.activity_eye_noise_detail

    private enum class NoiseValueSort(val index: Int, val title: String, val resId: Int?) {
        NO_DECIBEL(0, "없음", null),
        TODAY(1, "오늘", R.id.radioToday),
        LAST_24(2, "24시간", R.id.radio24Hours),
        THIS_WEEK(3, "이번 주", R.id.radioTWeek),
        THIS_MONTH(4, "이번 달", R.id.radioTMonth),
        THIS_YEAR(5, "올해", R.id.radioTYear),
        ENTIRE(6, "전체", R.id.radioEntire),
        CUSTOM(7, "직접 입력", R.id.radioSelect)
    }

    private var currentSort = -1

    private var startCalendar: LocalDate = LocalDate.now()
    private var endCalendar: LocalDate = LocalDate.now()

    private val noiseList = ArrayList<AdapterModel.NoiseDetailItem>()
    private val noiseAdapter by lazy { NoiseDetailAdapter(this, noiseList) }

    private val dateFilteredArray = ArrayList<AdapterModel.NoiseDetailItem>()

    private val noiseViewModel by viewModel<NoiseDataViewModel>()

    private val fetch by lazy {noiseViewModel.fetchData()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.apply {
            noiseDetailRv.adapter = noiseAdapter
            noiseDetailBack.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_top_to_bottom)
            }

            noiseFilterDbContainer.setOnClickListener { createFilterDialog() }
            noiseFilterDateContainer.setOnClickListener { createFilterDialog() }

            noiseFilterClear.setOnClickListener {
                binding.noiseFilterByDateValue.text = parsingLanguage(NoiseValueSort.THIS_WEEK.title)
                binding.noiseFilterByDbValue.text = parsingLanguage(NoiseValueSort.NO_DECIBEL.title)
                clearFilter()
            }
        }

        TimberUtil().d("noisetest","hasObservers is ${fetch.hasObservers()}")
        if (!fetch.hasActiveObservers()) {
            applyNoiseViewModel()
        }

        if (currentSort == -1) {
            currentSort = NoiseValueSort.THIS_WEEK.index
            noiseViewModel.loadDataResult(intent?.extras?.getString("serial").toString(),NoiseValueSort.THIS_WEEK.index,null,null)
        }
    }

    private fun createFilterDialog() {
        val noiseDbFilterBuilder = AlertDialog.Builder(this@EyeNoiseDetailActivity)
        val noiseDbFilterView = LayoutInflater.from(this@EyeNoiseDetailActivity)
            .inflate(R.layout.dialog_noise_filter_db, binding.noiseDetailRoot, false)
        noiseDbFilterBuilder.setView(noiseDbFilterView)
        val dialog = noiseDbFilterBuilder.create()

        val filterBtn =
            noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbBtn)
        val rgTop = noiseDbFilterView.findViewById<RadioGroup>(R.id.dialogNoiseDbRgTop)
        val rgBottom = noiseDbFilterView.findViewById<RadioGroup>(R.id.dialogNoiseDbRgBottom)
        val datePickerStart = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbDatePickerStart)
        val datePickerEnd = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbDatePickerEnd)
        val datePicker = noiseDbFilterView.findViewById<DatePicker>(R.id.dialogNoiseDbDatePicker)
        val seekBar = noiseDbFilterView.findViewById<AppCompatSeekBar>(R.id.dialogNoiseDbSeek)
        val seekBarValue = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbValue)
        val datePickerContainer= noiseDbFilterView.findViewById<LinearLayout>(R.id.datePickerContainer)
        val filterBack = noiseDbFilterView.findViewById<ImageView>(R.id.dialogNoiseBack)

        filterBack.setOnClickListener {
            dialog.dismiss()
        }

        val oldDb = binding.noiseFilterByDbValue.text.toString().replace("dB","")
        seekBar.progress = if (oldDb != "없음") oldDb.toInt() else 0
        seekBarValue.text = if (oldDb != "없음") oldDb else "0"

        val currentTime = LocalDateTime.now()
        datePickerStart.text = "${currentTime.year}-${insertDateZero(currentTime.monthValue)}-${insertDateZero(currentTime.dayOfMonth)}"
        datePickerEnd.text = "${currentTime.year}-${insertDateZero(currentTime.monthValue)}-${insertDateZero(currentTime.dayOfMonth)}"

        val dateSort = parseDateValueToResId(binding.noiseFilterByDateValue.text.toString())
        dateSort?.let {
            checkRb(it, rgTop, rgBottom)

            datePickerContainer.visibility = if (it == R.id.radioSelect) View.VISIBLE else View.GONE
        } ?: checkRb(NoiseValueSort.THIS_WEEK.resId, rgTop, rgBottom)

        rgTop.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                rgBottom.clearCheck()
                rgTop.check(checkedId)
            }
        }
        rgBottom.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                rgTop.clearCheck()
                rgBottom.check(checkedId)
            }

            if (checkedId == R.id.radioSelect) {
                datePickerContainer.visibility = View.VISIBLE
                datePickerContainer.startAnimation(AnimationUtils.loadAnimation(this@EyeNoiseDetailActivity, R.anim.fade_in))
            } else {
                datePickerContainer.visibility = View.GONE
            }
        }

        datePickerStart.setOnClickListener {
            if (!datePickerStart.isActivated) {
                datePickerStart.setTextColor(getColor(R.color.progress_color))
                datePickerEnd.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerEnd.isActivated = false
                datePickerStart.isActivated = true
            } else {
                datePickerStart.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerStart.isActivated = false
            }
        }

        datePickerEnd.setOnClickListener {
            if (!datePickerEnd.isActivated) {
                datePickerEnd.setTextColor(getColor(R.color.progress_color))
                datePickerStart.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerStart.isActivated = false
                datePickerEnd.isActivated = true
            } else {
                datePickerEnd.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerEnd.isActivated = false
            }
        }

        filterBtn.setOnClickListener {
            dialog.dismiss()
            val checkedTopId = rgTop.checkedRadioButtonId
            val checkedBottomId = rgBottom.checkedRadioButtonId
            val sort = if (checkedTopId == -1 && checkedBottomId != -1) {
                paredSort(checkedBottomId)
            } else if (checkedTopId != -1 && checkedBottomId == -1) {
                paredSort(checkedTopId)
            } else {
                NoiseValueSort.THIS_WEEK.index
            }

            HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                try {
                    binding.noiseFilterByDbValue.text =
                        if (seekBarValue.text.toString() != "") "${seekBarValue.text}dB" else "없음"
                    binding.noiseFilterByDateValue.text =
                        if (sort != -1) parsingLanguage(NoiseValueSort.values()[sort].title) else ""
                    filtering(sort)
                } catch (e: NumberFormatException) {
                    e.stackTraceToString()
                    clearFilter()
                }
            }, 500)
        }

        datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            if (datePickerStart.isActivated && !datePickerEnd.isActivated) {
                startCalendar = LocalDate.of(year,monthOfYear + 1,dayOfMonth)
                datePickerStart.text = "${year}-${insertDateZero(monthOfYear + 1)}-${insertDateZero(dayOfMonth)}"
            } else if (datePickerEnd.isActivated && !datePickerStart.isActivated) {
                endCalendar = LocalDate.of(year,monthOfYear + 1,dayOfMonth)
                datePickerEnd.text = "${year}-${insertDateZero(monthOfYear + 1)}-${insertDateZero(dayOfMonth)}"
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                seekBarValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.show()
    }

    private fun insertDateZero(i: Int): String {
        return if (i < 10) "0$i" else i.toString()
    }

    private fun parseDateValueToResId(value: String): Int? {
        return when (parsingLanguage(value)) {
            getString(R.string.today) -> NoiseValueSort.TODAY.resId
            getString(R.string.t_week) -> NoiseValueSort.THIS_WEEK.resId
            getString(R.string.this_year) -> NoiseValueSort.THIS_YEAR.resId
            getString(R.string.this_month) -> NoiseValueSort.THIS_MONTH.resId
            getString(R.string.direct_input) -> NoiseValueSort.CUSTOM.resId
            getString(R.string.entire) -> NoiseValueSort.ENTIRE.resId
            getString(R.string.hour_24) -> NoiseValueSort.LAST_24.resId
            else -> null
        }
    }

    private fun isTop(resId: Int?): Boolean {
        return when (resId) {
            NoiseValueSort.TODAY.resId, NoiseValueSort.THIS_WEEK.resId,
            NoiseValueSort.THIS_MONTH.resId, NoiseValueSort.LAST_24.resId -> true
            else -> false
        }
    }

    private fun checkRb(resId: Int?, top: RadioGroup, bottom: RadioGroup) {
        resId?.let {
            val isTop = isTop(resId)
            if (isTop) {
                bottom.clearCheck()
                top.check(it)
            } else {
                top.clearCheck()
                bottom.check(it)
            }
        }
    }

    private fun clearFilter() {
        dateFilteredArray.clear()
        dateFilteredArray.addAll(noiseList)
        noiseAdapter.submitList(dateFilteredArray)
        visibleNoResult(noiseList.isEmpty())
    }

    private fun paredSort(resId: Int): Int {
        return when (resId) {
            R.id.radioToday -> NoiseValueSort.TODAY.index
            R.id.radio24Hours -> NoiseValueSort.LAST_24.index
            R.id.radioTWeek -> NoiseValueSort.THIS_WEEK.index
            R.id.radioEntire -> NoiseValueSort.ENTIRE.index
            R.id.radioTMonth -> NoiseValueSort.THIS_MONTH.index
            R.id.radioTYear -> NoiseValueSort.THIS_YEAR.index
            R.id.radioSelect -> NoiseValueSort.CUSTOM.index
            else -> -1
        }
    }

    private fun filterByNoiseValue(newList: ArrayList<AdapterModel.NoiseDetailItem>, bound: Int)
    : ArrayList<AdapterModel.NoiseDetailItem> {
        dateFilteredArray.forEach { oldItem ->
            oldItem.noise?.let { noise ->
                if (noise >= bound) {
                    newList.add(oldItem)
                }
            }
        }

        return newList
    }

    private fun filtering(sort: Int) {
        if (sort != currentSort) {
            currentSort = sort
            noiseViewModel.loadDataResult(intent?.extras?.getString("serial").toString(), sort,null,null)
        }
    }

    private fun visibleNoResult(b: Boolean) {
        binding.noiseDetailNoData.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun parsingLanguage(s: String): String {
        return when(s) {
            NoiseValueSort.TODAY.title -> { getString(R.string.today) }
            NoiseValueSort.LAST_24.title -> { getString(R.string.hour_24) }
            NoiseValueSort.ENTIRE.title -> { getString(R.string.entire) }
            NoiseValueSort.THIS_WEEK.title -> { getString(R.string.t_week) }
            NoiseValueSort.THIS_MONTH.title -> { getString(R.string.this_month) }
            NoiseValueSort.THIS_YEAR.title -> { getString(R.string.this_year) }
            NoiseValueSort.CUSTOM.title -> { getString(R.string.direct_input) }
            NoiseValueSort.NO_DECIBEL.title -> { getString(R.string.nothing) }
            else -> ""
        }
    }

    private fun applyNoiseViewModel() {
        try {
            fetch.observe(this) { result ->
                result?.let { noise ->
                    when (noise) {
                        // 통신 성공
                        is BaseRepository.ApiState.Success -> {
                            val body = noise.data
                            noiseList.clear()
                            TimberUtil().d("noisetest", body.toString())
                            body?.let { b ->
                                val parsed = b as ArrayList<AdapterModel.NoiseDetailItem>
                                val db = binding.noiseFilterByDbValue.text.toString().replace("dB","")
                                noiseAdapter.submitList(
                                    if (db == "없음") parsed
                                    else filterByNoiseValue(parsed,db.toInt()))
                            }

                            dateFilteredArray.clear()
                            dateFilteredArray.addAll(noiseList)
                        }

                        // 통신 실패
                        is BaseRepository.ApiState.Error -> {
                            TimberUtil().e("noisetest", noise.errorMessage)
                            Toast.makeText(
                                this@EyeNoiseDetailActivity,
                                "소음 불러오기에 실패했습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // 통신 중
                        is BaseRepository.ApiState.Loading -> {}
                    }
                }
            }
        } catch (e: Exception) {
            TimberUtil().e("noisetest", e.stackTraceToString())
            Toast.makeText(this@EyeNoiseDetailActivity, "소음 불러오기에 실패했습니다", Toast.LENGTH_SHORT)
                .show()
        }
    }
}