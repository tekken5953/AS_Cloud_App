package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-04-12 오후 15:41
 **/
class AirQualityAdapter(private val context: Context, list: ArrayList<AdapterModel.AirQualityItem>) :
    RecyclerView.Adapter<AirQualityAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AirQualityAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_quality, parent, false)
        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val title: TextView = itemView.findViewById(R.id.airItemTitle)
        private val data: TextView = itemView.findViewById(R.id.airItemData)
        private val line: View = itemView.findViewById(R.id.airItemLine)

        fun bind(dao: AdapterModel.AirQualityItem) {
            title.text = dao.title
            data.text = dao.data

            if (adapterPosition == itemCount - 1) {
                line.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    try {
                        onClickListener.onItemClick(it, position)
                    } catch(e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}