package app.airsignal.weather.as_eye.adapter

import android.content.Context
import android.graphics.Typeface
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.adapter.ItemDiffCallback
import app.airsignal.weather.as_eye.activity.EyeNoiseDetailActivity
import app.airsignal.weather.dao.AdapterModel
import java.time.format.DateTimeFormatter


class NoiseDetailAdapter(private val context: Context, list: ArrayList<AdapterModel.NoiseDetailItem>) :
    RecyclerView.Adapter<NoiseDetailAdapter.ViewHolder>() {
    private var mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_noise_detail, parent, false)

        return ViewHolder(view)
    }

    fun submitList(newItems: ArrayList<AdapterModel.NoiseDetailItem>, callback: () -> Unit) {
        val diffCallback = ItemDiffCallback(mList, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)

        mList = newItems
        diffResult.dispatchUpdatesTo(this)
        callback.invoke()
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headerContainer: LinearLayout = itemView.findViewById(R.id.noiseDetailHeaderContainer)
        private val headerValue: TextView = itemView.findViewById(R.id.noiseDetailHeaderTitle)
        val dataDate: TextView = itemView.findViewById(R.id.noiseDetailTime)
        private val dataValue: TextView = itemView.findViewById(R.id.noiseDetailValue)

        fun bind(dao: AdapterModel.NoiseDetailItem) {
            dao.date?.let { date ->
                dao.noise?.let { value ->
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

                    dataDate.text = date.format(timeFormatter)
                    dataValue.text = "${value}dB의 소음을 감지하였습니다"

                    if (bindingAdapterPosition == itemCount - 1) {
                        headerValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
                        dataDate.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
                        dataValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
                    } else {
                        headerValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_medium.ttf")
                        dataDate.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_medium.ttf")
                        dataValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_medium.ttf")
                    }

                    val current = mList[bindingAdapterPosition].date
                    val prev = if (bindingAdapterPosition > 0) mList[bindingAdapterPosition-1].date else null
                    val isHeader: Boolean? =
                        prev?.let { p ->
                            current?.let { c ->
                                c.format(dateFormatter) != p.format(dateFormatter)
                            } ?: false
                        } ?: true
                    isHeader?.let {
                        if (it) {
                            changeHeaderVisibility(date.format(dateFormatter))
                        } else {
                            changeHeaderVisibility(null)
                        }
                    } ?: changeHeaderVisibility(null)
                }
            }
        }

        private fun changeHeaderVisibility(value: String?) {
            value?.let {
                headerContainer.visibility = View.VISIBLE
                headerValue.text = value
            } ?: run {
                headerContainer.visibility = View.GONE
            }
        }
    }
}