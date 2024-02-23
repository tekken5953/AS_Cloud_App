package app.airsignal.weather.as_eye.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.HandlerCompat
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.NoiseDetailAdapter
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.databinding.ActivityEyeNoiseDetailBinding
import java.time.LocalDateTime
import kotlin.random.Random

class EyeNoiseDetailActivity : BaseEyeActivity<ActivityEyeNoiseDetailBinding>() {
    override val resID: Int get() = R.layout.activity_eye_noise_detail

    private enum class NoiseValueSort(val title: String)
    {
        NO_DECIBEL("없음"),
        TODAY("오늘"),
        LAST_24("24시간"),
        THIS_WEEK("이번주"),
        THIS_MOTH("이번달"),
        THIS_YEAR("올해"),
        ENTIRE("전체"),
        CUSTOM("선택")
    }

    private val noiseList = ArrayList<AdapterModel.NoiseDetailItem>()
    private val noiseAdapter by lazy { NoiseDetailAdapter(this, noiseList) }

    private val dateFilteredArray = ArrayList<AdapterModel.NoiseDetailItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.apply {
            noiseDetailRv.adapter = noiseAdapter
            noiseDetailBack.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_bottom_to_top, R.anim.slide_top_to_bottom)
            }

            noiseFilterByDbValue.setOnClickListener {
                val noiseDbFilterBuilder = AlertDialog.Builder(this@EyeNoiseDetailActivity)
                val noiseDbFilterView = LayoutInflater.from(this@EyeNoiseDetailActivity)
                    .inflate(R.layout.dialog_noise_filter_db,binding.noiseDetailRoot,false)
                noiseDbFilterBuilder.setView(noiseDbFilterView)
                val dialog = noiseDbFilterBuilder.create()

                val filterEt = noiseDbFilterView.findViewById<EditText>(R.id.dialogNoiseDbEt)
                val filterBtn = noiseDbFilterView.findViewById<AppCompatButton>(R.id.dialogNoiseDbBtn)

                filterBtn.setOnClickListener {
                    dialog.dismiss()
                    HandlerCompat.createAsync(Looper.getMainLooper()).postDelayed({
                        noiseFilterByDbValue.text = "${filterEt.text}dB"
                        applyFilterByDb(filterEt.text.toString().toInt())
                    },500)
                }

                dialog.show()
            }

            noiseFilterClear.setOnClickListener {
                binding.noiseFilterByDateValue.text = NoiseValueSort.THIS_WEEK.title
                binding.noiseFilterByDbValue.text = NoiseValueSort.NO_DECIBEL.title
                clearFilter()
            }

            noiseFilterByDateValue.setOnClickListener {
                applyFilterByDate(NoiseValueSort.TODAY.title)
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
            dateFilteredArray.add(AdapterModel.NoiseDetailItem(item.first,item.second))
            addNoiseItem(date = item.first, value = item.second)
            noiseAdapter.notifyItemInserted(it)

            if (it == testItemArray.lastIndex) {
                noiseAdapter.applyBold(noiseList)
                binding.noiseDetailRv.scrollToPosition(it)
            }
        }
    }

    private fun addNoiseItem(date: LocalDateTime, value: Int) {
        val item = AdapterModel.NoiseDetailItem(date,value)
        noiseList.add(item)
    }

    private fun clearFilter() {
        dateFilteredArray.clear()
        dateFilteredArray.addAll(noiseList)
        noiseAdapter.submitList(dateFilteredArray)
    }

    private fun applyFilterByDb(bound: Int) {
        val newArray = filterByNoiseValue(bound)
        noiseAdapter.submitList(newArray)
    }

    private fun applyFilterByDate(sort: String) {
        val newArray = filterByDate(sort)
        changeFilteredArray(newArray)
        noiseAdapter.submitList(newArray)
    }

    private fun filterByNoiseValue(bound: Int): ArrayList<AdapterModel.NoiseDetailItem> {
        val newList = ArrayList<AdapterModel.NoiseDetailItem>()
        dateFilteredArray.forEach { oldItem ->
            oldItem.value?.let { noise ->
                if (noise >= bound) {
                    newList.add(oldItem)
                }
            }
        }

        return newList
    }

    private fun changeFilteredArray(newArray: ArrayList<AdapterModel.NoiseDetailItem>) {
        dateFilteredArray.clear()
        dateFilteredArray.addAll(newArray)
    }

    private fun filterByDate(sort: String): ArrayList<AdapterModel.NoiseDetailItem> {
        val newList = ArrayList<AdapterModel.NoiseDetailItem>()
        noiseList.forEach { oldItem ->
            oldItem.date?.let { oldItemDate ->
                when(sort) {
                    NoiseValueSort.TODAY.title -> {
                        if (oldItemDate.year == LocalDateTime.now().year
                            && oldItemDate.dayOfMonth == LocalDateTime.now().dayOfMonth
                            && oldItemDate.monthValue == LocalDateTime.now().monthValue) {
                            oldItem.value?.let { oldItemValue ->
                                val valueFilter =
                                    try {binding.noiseFilterByDbValue.text.toString().replace("dB","").toInt()}
                                    catch (e: java.lang.NumberFormatException) { 0 }
                                valueFilter.let { filterValue ->
                                    if (oldItemValue >= filterValue) {
                                        newList.add(AdapterModel.NoiseDetailItem(oldItemDate,oldItemValue))
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        return newList
    }
}