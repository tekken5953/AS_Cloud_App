package app.core_as_eye.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.core_as_eye.R
import app.core_as_eye.activity.EyeDetailActivity
import app.core_as_eye.dao.EyeDataModel
import app.core_as_eye.databinding.EyeDetailLiveFragmentBinding
import app.utils.TypeParser
import java.util.*

class EyeDetailLiveFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding : EyeDetailLiveFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_detail_live_fragment, container, false)

        binding.aeLiveRefreshIcon.setOnClickListener {
            Thread.sleep(2000)
            fetchData()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchData()
    }
    fun onDataReceived(data: EyeDataModel.EyeReportAdapter?) {
    }

    private fun fetchData() {
        binding.aeLiveRefreshTime.text = TypeParser.currentDateTimeString("hh시 mm분 ss초")
        binding.aeLiveTemp.fetchData(Random().nextInt(50).toString())
        binding.aeLiveHumid.fetchData(Random().nextInt(50).toString())
        binding.aeLiveLight.fetchData(Random().nextInt(50).toString())
        binding.aeLiveNoise.fetchData(Random().nextInt(50).toString())

        binding.aeLivePM25.fetchData(Random().nextInt(5000).toString())
        binding.aeLivePM10.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveCO2.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveCO.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveTVOC.fetchData(Random().nextInt(5000).toString())
        binding.aeLiveNO2.fetchData(Random().nextInt(5000).toString())
    }
}