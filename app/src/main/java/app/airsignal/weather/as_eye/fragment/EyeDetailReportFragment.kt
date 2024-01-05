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
import kotlinx.coroutines.*

class EyeDetailReportFragment : Fragment() {

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

        binding.reportVp.apply{
            adapter = reportViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
            scrollIndicators = View.SCROLL_INDICATOR_BOTTOM
        }
        reportViewPagerAdapter.notifyDataSetChanged()
        warningSlideAuto()
    }

    fun onDataReceived(data: EyeDataModel.ReportFragment?) {
        TimberUtil().d("eyetest","report data received : $data")
        data?.let {
            addViewPagerItem("CO2(이산화탄소)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
            addViewPagerItem("PM2.5(초미세먼지)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
            it.report.forEach { reportItem ->
                addViewPagerItem(reportItem.title,reportItem.content)
            }

            caiValue = it.caiValue
            caiLvl = it.caiLvl
//            binding.reportCaiValue.text = it.caiValue.toString()
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
}