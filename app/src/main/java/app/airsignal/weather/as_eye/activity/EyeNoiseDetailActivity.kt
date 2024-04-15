package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
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
import app.airsignal.weather.viewmodel.NoiseDataViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

typealias TaItem = AdapterModel.NoiseDetailItem
typealias TaArray = ArrayList<TaItem>

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
        CUSTOM(7, "직접 입력", R.id.radioSelect),
        DECIBEL_UNIT(8, "dB", null)
    }

    private var startCalendar = LocalDate.now()
    private var endCalendar = LocalDate.now()

    private val noiseAdapter by lazy { NoiseDetailAdapter(this, allList) }
    private val allList = TaArray()
    private val filteredList = TaArray()

    private var currentSort = -1

    private val noiseViewModel by viewModel<NoiseDataViewModel>()

    private val fetch by lazy {noiseViewModel.fetchData()}
    private val serial by lazy {intent?.extras?.getString("serial").toString()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.apply {
            noiseDetailRv.adapter = noiseAdapter
            noiseDetailBack.setOnClickListener {
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_top_to_bottom)
            }

            noiseFilterDbContainer.setOnClickListener { createFilterDialog() }
            noiseFilterDateContainer.setOnClickListener { createFilterDialog() }

            noiseFilterClear.setOnClickListener { clearFilter() }
        }

        if (!fetch.hasActiveObservers()) applyNoiseViewModel()  // 옵저빙 중인 옵저버가 없으면 생성성

       loadThisWeekFilter()    // 디폴트로 이번 주 리스트 불러오기
    }

    // 이번 주 소음 필터 적용 (dB도 적용)
    private fun loadThisWeekFilter() {
        currentSort = NoiseValueSort.THIS_WEEK.index

        if (allList.isEmpty()) { // 필터를 처음 적용
            callApi(NoiseValueSort.THIS_WEEK.index)
        } else {    // 이미 Api 호출이 있었을 때
            if (currentSort == 3) { // 이미 이번주 필터일 때
                // dB만 필터 적용
                submit(filterByNoise(getCurrentNoiseValue()))
            } else {    // 이번 주 필터가 아닐 때
                callApi(NoiseValueSort.THIS_WEEK.index)
            }
        }
    }

    // 분류로 API 호출
    private fun callApi(sort: Int) {
        if (!fetch.hasActiveObservers()) applyNoiseViewModel()  // 현재 동작중인 옵저버가 없으면 생성
        noiseViewModel.loadDataResult(serial, sort,null,null)
    }

    // 변경 된 어레이로 리스트 변경
    private fun submit(newList: TaArray) {
        noiseAdapter.submitList(newList) {
            moveScrollToLastPosition(newList.lastIndex)
        }
    }

    // 스크롤을 가장 아래로
    private fun moveScrollToLastPosition(position: Int) {
        try {
            if (filteredList.isNotEmpty()) {
                HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                    binding.noiseDetailRv.smoothScrollToPosition(position)
                },300)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    // 날짜로 API 호출
    private fun callApi(start: Int, end: Int) {
        if (!fetch.hasActiveObservers()) applyNoiseViewModel()  // 현재 동작중인 옵저버가 없으면 생성
        noiseViewModel.loadDataResult(serial,null, start, end)
    }

    // dB 값으로 필터 적용된 리스트 반환
    private fun filterByNoise(bound: Int): TaArray {
        val newList = TaArray()
        allList.forEach { allItem ->    // 현재 전체 리스트
            allItem.noise?.let { allNoise ->    // dB 값이 있을 경우
                if (allNoise >= bound) {    // 기준 값 보다 dB값이 클 경우
                    newList.add(allItem)    // 리스트에 추가
                }
            }
        }

        return newList  // 필터 적용 된 리스트 반환
    }

    // 리스트를 파라미터로 받아 현재 dB을 적용한 리스트 반환
    private fun filterByNoise(dateFilteredArray: TaArray): TaArray {
        val newList = TaArray()
        dateFilteredArray.forEach { allItem ->    // 현재 전체 리스트
            allItem.noise?.let { allNoise ->    // dB 값이 있을 경우
                if (allNoise >= getCurrentNoiseValue()) {    // 기준 값 보다 dB값이 클 경우
                    newList.add(allItem)    // 리스트에 추가
                }
            }
        }

        return newList  // 필터 적용 된 리스트 반환
    }

    // 현재 적용되어 있는 dB 필터 값 반환
    private fun getCurrentNoiseValue(): Int {
        // 현재 적용 된 dB 필터 값
        val text = binding.noiseFilterByDbValue.text.toString().replace(NoiseValueSort.DECIBEL_UNIT.title,"")
        return if (text == parsingLanguage(NoiseValueSort.NO_DECIBEL.title)) 0 else text.toInt()    // 없으면 0 있으면 해당 값 반환
    }

    // 적용 된 필터를 해제
    private fun clearFilter() {
        filteredList.clear() // 적용 된 필터 리스트 클리어
        binding.noiseFilterByDateValue.text = parsingLanguage(NoiseValueSort.THIS_WEEK.title)
        binding.noiseFilterByDbValue.text = parsingLanguage(NoiseValueSort.NO_DECIBEL.title)

        if (currentSort == 3) { // 적용 된 전체 리스트가 이번 주 일 때
            filteredList.addAll(allList)
            submit(filteredList)
        } else { // 이번 주 가 아닐 때
            currentSort = 3
            callApi(NoiseValueSort.THIS_WEEK.index) // 이번 주 데이터 호출
        }

        visibleNoResult(allList.isEmpty()) // 빈 데이터 처리
    }

    // API 통신 옵저버 - 날짜로 데이터 불러옴 - UI 처리
    private fun applyNoiseViewModel() {
        try {
            fetch.observe(this) { result -> // 통신 결과 옵저빙
                result?.let { noise ->
                    when (noise) {
                        // 통신 성공
                        is BaseRepository.ApiState.Success -> {
                            val body = noise.data   // 통신 결과 데이터

                            allList.clear() // 새로 불러왔기 때문에 전체 리스트 클리어
                            filteredList.clear() // 전체 리스트가 클리어 됐기 때문에 필터 리스트도 클리어

                            body?.let { b -> // 통신 결과가 Null 이 아닌 경우
                                val bodyToArray = b as TaArray // List 형태를 ArrayList 형태로 변경
                                if (b.isNotEmpty()) { //통신 결과가 비어있지 않은 경우
                                    val dbFilteredArray = filterByNoise(bodyToArray) // 통신 결과에 dB 필터 적용한 리스트 생성
                                    filteredList.addAll(dbFilteredArray)

                                    if (allList.isEmpty()) { // 처음 불러오는 경우
                                        allList.addAll(bodyToArray) // 전체 리스트에 통신 결과 저장
                                        noiseAdapter.notifyItemRangeChanged(0, allList.size)
                                        binding.noiseDetailRv.scrollToPosition(bodyToArray.lastIndex)
                                    } else {    // 처음 불러오는게 아닌 경우
                                        allList.addAll(bodyToArray) // 전체 리스트에 통신 결과 저장
                                        submit(filteredList) // 모든 필터가 적용 된 리스트로 데이터 교체
                                    }
                                } else {
                                    @Suppress("NotifyDataSetChanged")
                                    noiseAdapter.notifyDataSetChanged()
                                    binding.noiseDetailRv.scrollToPosition(bodyToArray.lastIndex)
                                }
                            }

                            visibleNoResult(filteredList.isEmpty()) // 필터 리스트가 비어있는지에 따른 메시징
                        }

                        // 통신 실패
                        is BaseRepository.ApiState.Error -> {
                            Toast.makeText(
                                this@EyeNoiseDetailActivity,
                                getString(R.string.fail_to_get_noise),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {    // 익셉션 발생
            e.stackTraceToString()
            Toast.makeText(this@EyeNoiseDetailActivity, getString(R.string.fail_to_get_noise), Toast.LENGTH_SHORT)
                .show()
        }
    }

    // 필터 다이얼로그 생성
    @SuppressLint("SetTextI18n")
    private fun createFilterDialog() {
        val noiseDbFilterBuilder = AlertDialog.Builder(this@EyeNoiseDetailActivity) // 빌더 정의
        val noiseDbFilterView = LayoutInflater.from(this@EyeNoiseDetailActivity)    // 뷰 정의
            .inflate(R.layout.dialog_noise_filter_db, binding.noiseDetailRoot, false)
        noiseDbFilterBuilder.setView(noiseDbFilterView) // 빌더에 뷰 할당
        val dialog = noiseDbFilterBuilder.create()  // 빌더 생성

        val filterBtn = // 적용 버튼
            noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseApplyBtn)
        val rgTop = noiseDbFilterView.findViewById<RadioGroup>(R.id.dialogNoiseDbRgTop) // 라디오 그룹 상단
        val rgBottom = noiseDbFilterView.findViewById<RadioGroup>(R.id.dialogNoiseDbRgBottom)   // 라디오 그룹 하단
        val datePickerStart = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbDatePickerStart)   // 시작일
        val datePickerEnd = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbDatePickerEnd)   // 종료일
        val datePicker = noiseDbFilterView.findViewById<DatePicker>(R.id.dialogNoiseDbDatePicker)   // 데이트 피커
        val seekBar = noiseDbFilterView.findViewById<AppCompatSeekBar>(R.id.dialogNoiseDbSeek)  // dB 조정 시크바
        val seekBarValue = noiseDbFilterView.findViewById<TextView>(R.id.dialogNoiseDbValue)    // dB 조정 값
        val datePickerContainer= noiseDbFilterView.findViewById<LinearLayout>(R.id.datePickerContainer) // 데이트 피커 컨테이너
        val filterBack = noiseDbFilterView.findViewById<ImageView>(R.id.dialogNoiseBack)    // 뒤로 가기 버튼

        filterBack.setOnClickListener { dialog.dismiss() }  // 뒤로가기 클릭 시 다이얼로그 종료

        val oldDb = binding.noiseFilterByDbValue.text.toString().replace(NoiseValueSort.DECIBEL_UNIT.title,"") // 기존 적용 된 데시벨
        seekBar.progress = if (oldDb != parsingLanguage(NoiseValueSort.NO_DECIBEL.title)) oldDb.toInt() else 0  // 기존 적용 된 데시벨로 시크바 조정
        seekBarValue.text = if (oldDb != parsingLanguage(NoiseValueSort.NO_DECIBEL.title)) oldDb else "0" // 기존 적용 된 데시벨로 현재 조정 값 변경

        val currentTime = LocalDateTime.now() // 현재 시간 - LocalDateTime 타입
        datePickerStart.text = "${currentTime.year}-${insertDateZero(currentTime.monthValue)}-${insertDateZero(currentTime.dayOfMonth)}" // 시작 일 현재 날짜로 변경
        datePickerEnd.text = "${currentTime.year}-${insertDateZero(currentTime.monthValue)}-${insertDateZero(currentTime.dayOfMonth)}" // 종료 일 현재 날짜로 변경

        val dateSort = parseDateValueToResId(binding.noiseFilterByDateValue.text.toString()) // 현재 적용 된 날짜 필터 종류 적용
        dateSort?.let {
            checkRb(it, rgTop, rgBottom) // 현재 적용 된 날짜 필터로 라디오 버튼 체크

            datePickerContainer.visibility = if (it == R.id.radioSelect) View.VISIBLE else View.GONE // 선택 된 라디오 버튼이 선택이 아닌경우 데이트 피커 사라짐

            // 선택 된 라디오 버튼이 선택인 경우
            if (it == R.id.radioSelect) {
                // 현재 적용되어 있는 날짜를 불러옴
                val currentDate = binding.noiseFilterByDateValue.text.toString().split("\n~\n")
                // 시작 및 종료일에 적용
                datePickerStart.text = currentDate[0]
                datePickerEnd.text = currentDate[1]
            }
        } ?: checkRb(NoiseValueSort.THIS_WEEK.resId, rgTop, rgBottom) // 현재 필터링 된 날짜가 없는 경우 이번주로 체크

        rgTop.setOnCheckedChangeListener { _, checkedId -> // 상단 라디오 그룹을 체크
            if (checkedId != -1) { // 체크 된 라디오 버튼의 아이디가 유요한 경우
                rgBottom.clearCheck() // 하단 라디오 그룹 체크 해제
                rgTop.check(checkedId) // 선택 된 라디오 버튼 체크
            }
        }
        rgBottom.setOnCheckedChangeListener { _, checkedId -> // 하단 라디오 그룹을 체크
            if (checkedId != -1) { // 체크 된 라디오 버튼의 아이디가 유요한 경우
                rgTop.clearCheck() // 하단 라디오 그룹 체크 해제
                rgBottom.check(checkedId) // 선택 된 라디오 버튼 체크
            }

            // 하단 라디오 버튼 중 선택을 선택한 경우
            if (checkedId == R.id.radioSelect) {
                datePickerContainer.visibility = View.VISIBLE
                datePickerContainer.startAnimation(AnimationUtils.loadAnimation(this@EyeNoiseDetailActivity, R.anim.fade_in))
            } else {
                datePickerContainer.visibility = View.GONE
            }
        }

        // 시작 일 EditText 클릭 한 경우
        datePickerStart.setOnClickListener {
            // 이미 선택되어 있지 않은 경우
            if (!datePickerStart.isActivated) {
                datePickerStart.setTextColor(getColor(R.color.progress_color))
                datePickerEnd.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerEnd.isActivated = false
                datePickerStart.isActivated = true
            } else {    // 이미 선택 되어 있던 경우
                datePickerStart.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerStart.isActivated = false
            }
        }

        // 종료 일 EditText 클릭 한 경우
        datePickerEnd.setOnClickListener {
            // 이미 선택되어 있지 않은 경우
            if (!datePickerEnd.isActivated) {
                datePickerEnd.setTextColor(getColor(R.color.progress_color))
                datePickerStart.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerStart.isActivated = false
                datePickerEnd.isActivated = true
            } else {    // 이미 선택 되어 있던 경우
                datePickerEnd.setTextColor(getColor(R.color.eye_graph_gray))
                datePickerEnd.isActivated = false
            }
        }

        // 필터 적용 버튼 클릭
        filterBtn.setOnClickListener {
            dialog.dismiss()    // 다이얼로그 종료
            val checkedTopId = rgTop.checkedRadioButtonId   // 체크 된 상단 라디오 버튼 아이디
            val checkedBottomId = rgBottom.checkedRadioButtonId // 체크 된 하단 라디오 버튼 아이디
            val sort = if (checkedTopId == -1 && checkedBottomId != -1) {   // 상단 만 체크된 경우
                paredSort(checkedBottomId)
            } else if (checkedTopId != -1 && checkedBottomId == -1) {   // 하단 만 체크된 경우
                paredSort(checkedTopId)
            } else {    // 둘 다 체크되지 않은 경우
                NoiseValueSort.THIS_WEEK.index
            }

            // 시크바의 벨류가 유효하면 Unit 을 붙여서 적용
            binding.noiseFilterByDbValue.text =
                if (seekBarValue.text.toString().toInt() != 0) "${seekBarValue.text}dB" else parsingLanguage(NoiseValueSort.NO_DECIBEL.title)
            // 체크 된 라디오 버튼이 유효하면 해당 종류로 적용
            binding.noiseFilterByDateValue.text =
                if (sort != -1 && sort != 7) parsingLanguage(NoiseValueSort.values()[sort].title)
                else if (sort == 7) "${datePickerStart.text}\n~\n${datePickerEnd.text}"
                else ""

            try {
                // 선택한 날짜와 데시벨로 필터링 동작
                if (checkedBottomId == NoiseValueSort.CUSTOM.resId) {
                    filtering(sort,
                        startCalendar.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt(),
                        endCalendar.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt())
                } else {
                    filtering(sort)
                }
            } catch (e: NumberFormatException) {
                e.stackTraceToString()
                // 에러 발생 시 필터 클리어
                clearFilter()
            }
        }

        // 데이트 피커 날짜 변경 시
        datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            if (datePickerStart.isActivated && !datePickerEnd.isActivated) {    // 현재 선택 된 EditText 가 시작 일 일때
                startCalendar = LocalDate.of(year,monthOfYear + 1,dayOfMonth)
                datePickerStart.text = "${year}-${insertDateZero(monthOfYear + 1)}-${insertDateZero(dayOfMonth)}" // 시작 일 값을 변경
            } else if (datePickerEnd.isActivated && !datePickerStart.isActivated) { // 현재 선택 된 EditText 가 종료 일 일때
                endCalendar = LocalDate.of(year,monthOfYear + 1,dayOfMonth)
                datePickerEnd.text = "${year}-${insertDateZero(monthOfYear + 1)}-${insertDateZero(dayOfMonth)}" // 종료 일 값을 변경
            }
        }

        // 데시벨 시크바의 값 변경 시
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean
            ) { seekBarValue.text = progress.toString() }   // 데시벨 텍스트 값 변경

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.show()   // 다이얼로그 노출
    }

    // 날짜가 한자릿 수 일때 0을 붙여서 두자릿 수 로 만들어 줌
    private fun insertDateZero(i: Int): String {
        return if (i < 10) "0$i" else i.toString()
    }

    // 현재 날짜 필터링 값에 따라 라디오 버튼 아이디를 반환함
    private fun parseDateValueToResId(value: String): Int? {
        if (value.contains("\n~\n")) {
            return NoiseValueSort.CUSTOM.resId
        }

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

    // 현재 선택 된 라디오 버튼이 상단인지 아닌지 알려 줌
    private fun isTop(resId: Int?): Boolean {
        return when (resId) {
            NoiseValueSort.TODAY.resId, NoiseValueSort.THIS_WEEK.resId,
            NoiseValueSort.THIS_MONTH.resId, NoiseValueSort.LAST_24.resId -> true
            else -> false
        }
    }

    // 라디오 버튼의 체크를 변경 시킴(상단 + 하단 통합)
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

    // 선택 된 라디오 버튼의 아이디에 따라 언어 리소스가 적용 된 값으로 변경해 줌
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

    // 날짜 종류에 따라 필터링 함
    private fun filtering(sort: Int) {
        if (sort != currentSort) {
            currentSort = sort
            callApi(sort)
        } else { submit(filterByNoise(getCurrentNoiseValue())) }
    }

    // 날짜 종류에 따라 필터링 함
    private fun filtering(sort: Int, start: Int, end: Int) {
        if (sort != currentSort) {
            currentSort = sort
            callApi(start, end)
        } else { submit(filterByNoise(getCurrentNoiseValue())) }
    }

    // 결과 없음 메시지 노출
    private fun visibleNoResult(b: Boolean) {
        binding.noiseDetailNoData.visibility = if (b) View.VISIBLE else View.GONE
    }

    // 언어 리소스팩이 적용 된 타이틀로 변환해 줌
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

}