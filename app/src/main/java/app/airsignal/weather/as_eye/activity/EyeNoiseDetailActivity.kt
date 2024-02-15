package app.airsignal.weather.as_eye.activity

import android.os.Bundle
import app.airsignal.weather.R
import app.airsignal.weather.adapter.NoiseDetailAdapter
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.databinding.ActivityEyeNoiseDetailBinding
import java.time.LocalDateTime
import kotlin.random.Random

class EyeNoiseDetailActivity : BaseEyeActivity<ActivityEyeNoiseDetailBinding>() {
    override val resID: Int get() = R.layout.activity_eye_noise_detail

    private val noiseList = ArrayList<AdapterModel.NoiseDetailItem>()
    private val noiseAdapter by lazy { NoiseDetailAdapter(this, noiseList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.apply {
            noiseDetailRv.adapter = noiseAdapter
            noiseDetailBack.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_top_to_bottom)
            }
        }

        val testItemArray = listOf<Pair<LocalDateTime,Int>>(
            Pair(LocalDateTime.now(), Random.nextInt(60,120)),
            Pair(LocalDateTime.now().minusHours(1), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusHours(2), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusHours(3), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(1).minusHours(5), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(1).minusHours(6), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(2).minusHours(1), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(2).minusHours(3), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(2).minusHours(4), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(3).minusHours(5), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(4).minusHours(7), Random.nextInt(80,120)),
            Pair(LocalDateTime.now().minusDays(4).minusHours(9), Random.nextInt(80,120))
        ).reversed()

        repeat(testItemArray.size) {
            val item = testItemArray[it]
            addNoiseItem(date = item.first, value = item.second)
            noiseAdapter.notifyItemInserted(it)

            if (it == testItemArray.lastIndex) {
                noiseAdapter.applyBold(it)
                binding.noiseDetailRv.scrollToPosition(it)
            }
        }
    }

    private fun addNoiseItem(date: LocalDateTime, value: Int) {
        val item = AdapterModel.NoiseDetailItem(date,value)
        noiseList.add(item)
    }
}