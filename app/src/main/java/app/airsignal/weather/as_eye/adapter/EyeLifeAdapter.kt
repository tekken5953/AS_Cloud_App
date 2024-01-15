package app.airsignal.weather.as_eye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import java.util.*

class EyeLifeAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Life>
) :
    RecyclerView.Adapter<EyeLifeAdapter.ViewHolder>() {
    private val mList = list


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_life_data, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameEn: TextView = itemView.findViewById(R.id.listItemAeLifeEn)
        private val nameKr: TextView = itemView.findViewById(R.id.listItemAeLifeKr)
        private val lifeValue: TextView = itemView.findViewById(R.id.listItemAeLifeValue)
        private val pb: ProgressBar = itemView.findViewById(R.id.listItemAeLifePb)

        fun bind(dao: EyeDataModel.Life) {
            nameEn.text = dao.nameEn
            nameKr.text = dao.nameKr
            lifeValue.text = dao.value.toString()
            lifeValue.setTextColor(context.getColor(dao.pbColor))
            pb.progress = dao.value
            pb.progressTintList = ColorStateList.valueOf(context.getColor(dao.pbColor))
            pb.progressBackgroundTintList = ColorStateList.valueOf(context.getColor(dao.backColor))
        }
    }
}