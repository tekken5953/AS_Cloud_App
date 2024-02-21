package app.airsignal.weather.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel
import java.time.format.DateTimeFormatter
import java.util.Date


class NoiseDetailAdapter(private val context: Context, list: ArrayList<AdapterModel.NoiseDetailItem>) :
    RecyclerView.Adapter<NoiseDetailAdapter.ViewHolder>() {
    private val mList = list
    private var isLast = mutableMapOf<Int,Boolean>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoiseDetailAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_noise_detail, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])

        if (isLast[position] == true) {
            holder.headerValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
            holder.dataDate.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
            holder.dataValue.typeface = Typeface.createFromAsset(context.assets, "spoqa_hansansneo_bold.ttf")
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headerContainer: LinearLayout = itemView.findViewById(R.id.noiseDetailHeaderContainer)
        val headerValue: TextView = itemView.findViewById(R.id.noiseDetailHeaderTitle)
        val dataDate: TextView = itemView.findViewById(R.id.noiseDetailTime)
        val dataValue: TextView = itemView.findViewById(R.id.noiseDetailValue)

        fun bind(dao: AdapterModel.NoiseDetailItem) {
            dao.date?.let { date ->
                dao.value?.let { value ->
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

                    dataDate.text = date.format(timeFormatter)
                    dataValue.text = "${value}dB의 소음을 감지하였습니다"

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

    fun applyBold(i: Int) {
        isLast[i] = true
        notifyItemChanged(i)
    }
}