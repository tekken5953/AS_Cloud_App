package app.airsignal.weather.adapter

import android.content.Context
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
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val headerContainer: LinearLayout = itemView.findViewById(R.id.noiseDetailHeaderContainer)
        private val headerValue: TextView = itemView.findViewById(R.id.noiseDetailHeaderTitle)
        private val dataDate: TextView = itemView.findViewById(R.id.noiseDetailTime)
        private val dataValue: TextView = itemView.findViewById(R.id.noiseDetailValue)

        fun bind(dao: AdapterModel.NoiseDetailItem) {
            dao.date?.let { date ->
                dao.value?.let { value ->
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

                    dataDate.text = date.format(timeFormatter)
                    dataValue.text = "${value}dB의 소음을 감지하였습니다"

                    val current = mList[adapterPosition].date
                    val prev = if (adapterPosition > 0) mList[adapterPosition-1].date else null
                    val isHeader: Boolean? =
                        prev?.let { p ->
                            current?.let { c ->
                                c.format(dateFormatter) != p.format(dateFormatter)
                            } ?: null
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