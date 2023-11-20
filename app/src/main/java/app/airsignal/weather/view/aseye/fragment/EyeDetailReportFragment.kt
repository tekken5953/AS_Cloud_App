package app.airsignal.weather.view.aseye.fragment

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
import app.airsignal.weather.databinding.EyeDetailReportFragmentBinding
import app.airsignal.weather.view.activity.MainActivity
import app.airsignal.weather.view.aseye.adapter.ReportViewPagerAdapter
import app.airsignal.weather.view.aseye.dao.EyeDataModel
import kotlinx.coroutines.*
import timber.log.Timber

class EyeDetailReportFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    private lateinit var binding : EyeDetailReportFragmentBinding
    private val autoJob = Job()

    private val reportViewPagerItem = ArrayList<EyeDataModel.EyeReportModel>()
    private val reportViewPagerAdapter by lazy {
        ReportViewPagerAdapter(
            requireActivity(),
            reportViewPagerItem,
            binding.reportVp
        )
    }

    override fun onDetach() {
        super.onDetach()
        autoJob.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) mainActivity = context
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

        binding.reportVp.apply{
            adapter = reportViewPagerAdapter
            isClickable = true
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
        }

        addViewPagerItem("CO2(이산화탄소)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
        addViewPagerItem("PM2.5(초미세먼지)","수치가 높습니다. 창문을 열어 환기를 시켜주세요")
        reportViewPagerAdapter.notifyDataSetChanged()
        warningSlideAuto()
    }

    // 기상특보 자동 슬라이드 적용
    private fun warningSlideAuto() {
        val vp = binding.reportVp
        if (reportViewPagerItem.size > 1) {
            vp.currentItem = if (vp.currentItem + 1 < reportViewPagerItem.size) vp.currentItem + 1 else 0
            CoroutineScope(autoJob + Dispatchers.Main).launch {
                delay(3500)
                warningSlideAuto()
            }

            autoJob.start()
        }
    }

    private fun addViewPagerItem(title: String, content: String) {
        val item = EyeDataModel.EyeReportModel(title,content)
        reportViewPagerItem.add(item)
    }
}