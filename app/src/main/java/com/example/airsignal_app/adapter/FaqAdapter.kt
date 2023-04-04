package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R

/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:52
 **/
class FaqAdapter(private val context: Context, list: ArrayList<String>) :
    RecyclerView.Adapter<FaqAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FaqAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_faq, parent, false)
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
        private val headLine: TextView = itemView.findViewById(R.id.itemFaqText)

        fun bind(dao: String) {
            headLine.text = dao

            itemView.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(it, position)
                }
            }
        }
    }
}