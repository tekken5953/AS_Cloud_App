package app.airsignal.weather.view.aseye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.getColumnIndex
import app.airsignal.weather.R
import app.airsignal.weather.view.aseye.dao.EyeDataModel
import java.util.*

class EyeLifeAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.LifeModel>
) :
    RecyclerView.Adapter<EyeLifeAdapter.ViewHolder>() {
    private val mList = list


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EyeLifeAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_life_data, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: EyeLifeAdapter.ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameEn: TextView = itemView.findViewById(R.id.listItemAeLifeEn)
        private val nameKr: TextView = itemView.findViewById(R.id.listItemAeLifeKr)
        private val lifeValue: TextView = itemView.findViewById(R.id.listItemAeLifeValue)
        private val pb: ProgressBar = itemView.findViewById(R.id.listItemAeLifePb)

        @SuppressLint("InflateParams")
        fun bind(dao: EyeDataModel.LifeModel) {
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