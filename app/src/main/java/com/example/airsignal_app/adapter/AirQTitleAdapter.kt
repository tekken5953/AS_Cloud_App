package com.example.airsignal_app.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-07-12 오전 11:12
 **/
class AirQTitleAdapter(private val context: Context, list: ArrayList<AdapterModel.AirQTitleItem>) :
    RecyclerView.Adapter<AirQTitleAdapter.ViewHolder>() {
    private val mList = list

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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = itemView.findViewById(R.id.listItemCpvTitle)

        fun bind(dao: AdapterModel.AirQTitleItem) {
            title.text = dao.title

            if (dao.isSelect) {
                title.setBackgroundResource(R.drawable.pm_rv_title_bg_s)
                title.setTextColor(context.getColor(R.color.white))
            } else {
                title.setBackgroundResource(R.drawable.pm_rv_title_bg_ns)
                title.setTextColor(context.getColor(R.color.airQ_unSelected_text_color))
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