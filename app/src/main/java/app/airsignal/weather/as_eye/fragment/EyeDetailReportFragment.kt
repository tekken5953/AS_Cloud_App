package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.adapter.ReportViewPagerAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.EyeDetailReportFragmentBinding
import app.airsignal.weather.util.TimberUtil
import app.chart.LineGraphClass
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.random.Random

class EyeDetailReportFragment : Fragment() {

    companion object {
        const val VIRUS_INDEX = "VIRUS"
        const val CAI_INDEX = "CAI"
    }

    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeDetailReportFragmentBinding
    private val autoJob = Job()
    private val reportViewPagerItem = ArrayList<EyeDataModel.EyeReportAdapter>()
    private val reportViewPagerAdapter by lazy {
        ReportViewPagerAdapter(
            requireActivity(),
            reportViewPagerItem,
            binding.reportVp
        )
    }

    private var caiValue = 0
    private var caiLvl = 0
    private var virusValue = 0
    private var virusLvl = 0
    private val reportArray = ArrayList<Pair<String,String>>()

    override fun onDetach() {
        super.onDetach()
        autoJob.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_report_fragment, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportCaiValue.text = caiValue.toString()
        binding.reportCaiGrade.text = parseLvlToGrade(caiLvl)
        binding.reportVirusValue.text = virusValue.toString()
        binding.reportVirusGrade.text = parseLvlToGrade(virusLvl)
        reportArray.forEach { reportItem ->
            addViewPagerItem(reportItem.first,reportItem.second)
        }

        binding.reportCaiPb.progress = setProgress(CAI_INDEX, caiValue,caiLvl)
        binding.reportVirusPb.progress = setProgress(VIRUS_INDEX, virusValue,virusLvl)
        binding.caiModerLow.text = getModerate(CAI_INDEX,caiLvl).first.toString()
        binding.caiModerHigh.text = getModerate(CAI_INDEX,caiLvl).second.toString()
        binding.virusModerLow.text = getModerate(VIRUS_INDEX,virusLvl).first.toString()
        binding.virusModerHigh.text = getModerate(VIRUS_INDEX,virusLvl).second.toString()
        binding.reportCaiContainer.setBackgroundResource(getBackground(caiLvl))
        binding.reportVirusContainer.setBackgroundResource(getBackground(virusLvl))

        binding.reportVp.apply{
            adapter = reportViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            scrollIndicators = View.SCROLL_INDICATOR_BOTTOM
        }
        reportViewPagerAdapter.notifyDataSetChanged()
        warningSlideAuto()

        createPMChart(binding.pmAvgLineChart,true)
    }

    fun onDataReceived(data: EyeDataModel.ReportFragment?) {
        TimberUtil().d("eyetest","report data received : $data")
        data?.let {
            addViewPagerItem("CO2(이산화탄소)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
            addViewPagerItem("PM2.5(초미세먼지)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
            reportArray.clear()
            it.report.forEach { r ->
                reportArray.add(Pair(r.title,r.content))
            }

            caiValue = it.caiValue
            caiLvl = it.caiLvl
            virusValue = it.virusValue
            virusLvl = it.virusLvl
        }
    }

    private fun parseLvlToGrade(lvl: Int): String {
        return when(lvl) {
            0 -> "좋음"
            1 -> "보통"
            2 -> "나쁨"
            3 -> "매우나쁨"
            else -> "에러"
        }
    }

    private fun setProgress(sort: String, value: Int, grade: Int): Int {
        val result = when(sort) {
            CAI_INDEX -> {
                when(grade) {
                    0 -> {((value - 0).toDouble() / (50 - 0).toDouble() * 100).roundToInt()}
                    1 -> {((value - 51).toDouble() / (100 - 51).toDouble() * 100).roundToInt()}
                    2 -> {((value - 101).toDouble() / (250 - 101).toDouble() * 100).roundToInt()}
                    3 -> {((value - 251).toDouble() / (500 - 251).toDouble() * 100).roundToInt()}
                    else -> {0}
                }
            }

            VIRUS_INDEX -> {
                when(grade) {
                    0 -> {((value - 0).toDouble() / (3 - 0).toDouble() * 100).roundToInt()}
                    1 -> {((value - 4).toDouble() / (6 - 4).toDouble() * 100).roundToInt()}
                    2 -> {((value - 7).toDouble() / (8 - 7).toDouble() * 100).roundToInt()}
                    3 -> {((value - 9).toDouble() / (10 - 9).toDouble() * 100).roundToInt()}
                    else -> { 0 }
                }
            }
            else -> {0}
        }

        return result
    }

    private fun getModerate(sort: String, grade: Int): Pair<Int, Int> {
        return when(sort) {
            CAI_INDEX -> {
                when (grade) {
                    0 -> { Pair(0, 50) }
                    1 -> { Pair(51, 100) }
                    2 -> { Pair(101, 250) }
                    3 -> { Pair(251, 500) }
                    else -> { Pair(0, 0) }
                }
            }
            VIRUS_INDEX -> {
                when (grade) {
                    0 -> { Pair(0, 3) }
                    1 -> { Pair(4, 6) }
                    2 -> { Pair(7, 8) }
                    3 -> { Pair(9, 10) }
                    else -> { Pair(0, 0) }
                }
            }
            else -> { Pair(0, 0) }
        }
    }

    private fun getBackground(grade: Int): Int {
        val result = when(grade) {
            0 -> {R.drawable.report_bg_good}
            1 -> {R.drawable.report_bg_normal}
            2 -> {R.drawable.report_bg_bad}
            3 -> {R.drawable.report_bg_verybad}
            else -> {R.drawable.report_bg_good}
        }

        return result
    }

    // 기상특보 자동 슬라이드 적용
    private fun warningSlideAuto() {
        if (reportViewPagerItem.size > 1) {
            CoroutineScope(autoJob + Dispatchers.Main).launch {
                delay(5000)
                val vp = binding.reportVp
                vp.currentItem =
                    if (vp.currentItem + 1 < reportViewPagerItem.size) vp.currentItem + 1 else 0
                warningSlideAuto()
            }

            autoJob.start()
        }
    }

    private fun addViewPagerItem(title: String, content: String) {
        val item = EyeDataModel.EyeReportAdapter(title,content)
        reportViewPagerItem.add(item)
    }

    private fun createPMChart(charView: LineChart, isGradient: Boolean) {
        try {
            val yValue1 = listOf(
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50,
                Random.nextFloat() * 50)
            val yValue2 = listOf(
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100,
                Random.nextFloat() * 100)

            val entry1 = ArrayList<Entry>()
            yValue1.forEachIndexed { i, d ->
                entry1.add(Entry(i.toFloat(), d))
            }
            val entry2 = ArrayList<Entry>()
            yValue2.forEachIndexed { i, d ->
                entry2.add(Entry(i.toFloat(), d))
            }

            LineGraphClass(requireContext(),isGradient)
                .getInstance(charView)
                .setChart()
                .addDataSet("미세먼지",entry1)
                .addDataSet("초미세먼지",entry2)
                .createGraph()
        } catch (e: Exception) {
            TimberUtil().e("graph_tag","graph error ${e.stackTraceToString()}")
            e.printStackTrace()
        }
    }
}