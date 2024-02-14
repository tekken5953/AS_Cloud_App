package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.activity.EyeNoiseDetailActivity
import app.airsignal.weather.as_eye.adapter.ReportViewPagerAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.chart.LineGraphClass
import app.airsignal.weather.databinding.EyeDetailReportFragmentBinding
import app.airsignal.weather.util.`object`.DataTypeParser
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class EyeDetailReportFragment : Fragment() {

    enum class ReportIndex {
        VIRUS_INDEX, CAI_INDEX
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
    private var pm10Value = 0f
    private val reportArray = ArrayList<Pair<String,String>>()
    private val pm10p0Array = ArrayList<EyeDataModel.Average>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reportViewPagerItem.clear()
        autoJob.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_report_fragment, container, false)
        binding.reportLogViewEntire.setOnClickListener {
            val intent = Intent(mActivity, EyeNoiseDetailActivity::class.java)
            mActivity.startActivity(intent)
            mActivity.overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_top_to_bottom)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportVp.apply {
            adapter = reportViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            scrollIndicators = View.SCROLL_INDICATOR_BOTTOM
        }

        applyData()

        binding.pmChartTitle.text = getString(R.string.today_pm_chart)
        binding.pmChartUnit.text = getString(R.string.pm_unit)

        warningSlideAuto()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun applyData() {
        binding.reportCaiValue.text = caiValue.toString()
        binding.reportCaiGrade.text = parseLvlToGrade(caiLvl)
        binding.reportVirusValue.text = virusValue.toString()
        binding.reportVirusGrade.text = parseLvlToGrade(virusLvl)

        reportViewPagerItem.clear()
        if (reportArray.isEmpty()) {
            addViewPagerItem(getString(R.string.all_data_good),getString(R.string.all_data_good_content), false)
            reportViewPagerAdapter.notifyDataSetChanged()
        } else {
            reportArray.forEachIndexed { index , reportItem ->
                addViewPagerItem("${reportItem.first} (${index+1}/${reportArray.size})",reportItem.second, true)
            }
            reportViewPagerAdapter.notifyDataSetChanged()
        }

        val entry2 = ArrayList<Entry>()
        if (pm10p0Array.isNotEmpty()) {
            binding.pmAvgLineChart.visibility = View.VISIBLE
            binding.pmAvgLineChartNoData.visibility = View.GONE
            repeat(pm10p0Array.size) {
                pm10p0Array[it].pm10p0Value?.let { pmValue ->
                    entry2.add(Entry(it.toFloat(), pmValue.toFloat()))
                }
            }
            createPMChart(entry2)
        } else {
            binding.pmAvgLineChart.visibility = View.GONE
            binding.pmAvgLineChartNoData.visibility = View.VISIBLE
        }

        binding.reportCaiPb.progress = setProgress(ReportIndex.CAI_INDEX, caiValue,caiLvl)
        binding.reportVirusPb.progress = setProgress(ReportIndex.VIRUS_INDEX, virusValue,virusLvl)
        binding.caiModerLow.text = getModerate(ReportIndex.CAI_INDEX,caiLvl).first.toString()
        binding.caiModerHigh.text = getModerate(ReportIndex.CAI_INDEX,caiLvl).second.toString()
        binding.virusModerLow.text = getModerate(ReportIndex.VIRUS_INDEX,virusLvl).first.toString()
        binding.virusModerHigh.text = getModerate(ReportIndex.VIRUS_INDEX,virusLvl).second.toString()
        binding.reportCaiContainer.setBackgroundResource(getBackground(caiLvl))
        binding.reportVirusContainer.setBackgroundResource(getBackground(virusLvl))
        binding.reportCaiSmile.setBackgroundResource(getSmile(caiLvl))
        binding.reportVirusSmile.setBackgroundResource(getSmile(virusLvl))
    }

    fun onDataTransfer(data: EyeDataModel.ReportFragment?) {
        data?.let {
            reportArray.clear()
            pm10p0Array.clear()
            it.report?.let { list ->
                list.forEach { r ->
                    reportArray.add(Pair(DataTypeParser.parseReportTitle(r), "위험단계입니다. 환기를 시켜주세요"))
                }
            }

            caiValue = it.caiValue
            caiLvl = it.caiLvl
            virusValue = it.virusValue
            virusLvl = it.virusLvl
            pm10Value = it.pm10Value
            it.pm10p0List?.let { pm10List ->
                pm10p0Array.addAll(pm10List)
            }

            if (this@EyeDetailReportFragment.isVisible) { applyData() }
        }
    }

    private fun parseLvlToGrade(lvl: Int): String {
        return when(lvl) {
            0 -> getString(R.string.good)
            1 -> getString(R.string.normal)
            2 -> getString(R.string.bad)
            3 -> getString(R.string.caution)
            else -> getString(R.string.error)
        }
    }

    private fun setProgress(sort: ReportIndex, value: Int, grade: Int): Int {
        val result = when(sort) {
            ReportIndex.CAI_INDEX -> {
                when(grade) {
                    0 -> {((value - 0).toDouble() / (50 - 0).toDouble() * 100).roundToInt()}
                    1 -> {((value - 51).toDouble() / (100 - 51).toDouble() * 100).roundToInt()}
                    2 -> {((value - 101).toDouble() / (250 - 101).toDouble() * 100).roundToInt()}
                    3 -> {((value - 251).toDouble() / (500 - 251).toDouble() * 100).roundToInt()}
                    else -> { 0 }
                }
            }

            ReportIndex.VIRUS_INDEX -> {
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

    private fun getModerate(sort: ReportIndex, grade: Int): Pair<Int, Int> {
        return when(sort) {
            ReportIndex.CAI_INDEX -> {
                when (grade) {
                    0 -> { Pair(0, 50) }
                    1 -> { Pair(51, 100) }
                    2 -> { Pair(101, 250) }
                    3 -> { Pair(251, 500) }
                    else -> { Pair(0, 0) }
                }
            }
            ReportIndex.VIRUS_INDEX -> {
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

    private fun getSmile(grade: Int): Int {
        val result = when(grade) {
            0 -> {R.drawable.smile_good}
            1 -> {R.drawable.smile_normal}
            2 -> {R.drawable.smile_bad}
            3 -> {R.drawable.smile_verybad}
            else -> {R.drawable.smile_normal}
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

    private fun addViewPagerItem(title: String, content: String, isCaution: Boolean) {
        val item = EyeDataModel.EyeReportAdapter(title,content,isCaution)
        reportViewPagerItem.add(item)
    }

    private fun createPMChart(pm10Entry: ArrayList<Entry>?) {
        try {
            val pmGraphInstance: LineGraphClass = LineGraphClass(requireContext()).getInstance(binding.pmAvgLineChart)
            pm10Entry?.let {
                pmGraphInstance
                    .clear()
                    .setChart()
                    .addDataSet("미세먼지", pm10Entry)
                    .createGraph()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}