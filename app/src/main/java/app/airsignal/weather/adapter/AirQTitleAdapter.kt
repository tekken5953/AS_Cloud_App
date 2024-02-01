package app.airsignal.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-07-12 오전 11:12
 **/
class AirQTitleAdapter(private val context: Context, list: ArrayList<AdapterModel.AirQTitleItem>) :
    RecyclerView.Adapter<AirQTitleAdapter.ViewHolder>() {
    private val mList = list

    private var isWhite = false

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AirQTitleAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_pm_title, parent, false)
        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun setIsWhite(b: Boolean) {
        isWhite = b
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = itemView.findViewById(R.id.listItemCpvTitle)

        fun bind(dao: AdapterModel.AirQTitleItem) {
            title.text = dao.name


            if (dao.isSelect) {
                title.setTextColor(context.getColor(R.color.white))
                title.setBackgroundResource(R.drawable.pm_rv_title_bg_s)
            } else {
                title.setBackgroundResource(
                    if(isWhite)R.drawable.pm_rv_title_bg_ns_w else R.drawable.pm_rv_title_bg_ns_b)
                title.setTextColor(
                   context.getColor( if(isWhite)R.color.white else R.color.sub_black))
            }

            itemView.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    try {
                        onClickListener.onItemClick(it, position)
                    } catch (e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}