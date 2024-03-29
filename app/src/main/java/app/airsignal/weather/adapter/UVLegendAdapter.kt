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
 * @since : 2023-06-01 오후 2:17
 **/
class UVLegendAdapter(private val context: Context, list: ArrayList<AdapterModel.UVLegendItem>) :
    RecyclerView.Adapter<UVLegendAdapter.ViewHolder>() {
    private val mList = list
    private var isWhite = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UVLegendAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_uv_legend, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun setIsWhite(b: Boolean) {
        isWhite = b
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val value: TextView = itemView.findViewById(R.id.listItemUvLegendValue)
        private val color: View = itemView.findViewById(R.id.listItemUvLegendColor)
        private val grade: TextView = itemView.findViewById(R.id.listitemUvLegendGrade)

        fun bind(dao: AdapterModel.UVLegendItem) {
            value.text = dao.value
            color.setBackgroundColor(dao.color)
            grade.text = dao.grade

            val applyColor = context.getColor(if(isWhite)R.color.white else R.color.main_black)
            grade.setTextColor(applyColor)
            value.setTextColor(applyColor)
        }
    }
}