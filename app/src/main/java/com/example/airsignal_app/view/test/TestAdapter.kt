package com.example.airsignal_app.view.test

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel


/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:52
 **/
class TestAdapter(mContext: Context, list: ArrayList<AdapterModel.TestAdapter>) :
    RecyclerView.Adapter<TestAdapter.ViewHolder>() {
    private val mList = list
    private val context = mContext

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_test, parent, false)
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
        val textView: TextView = itemView.findViewById(R.id.itemTestText)

        fun bind(dao: AdapterModel.TestAdapter) {
            textView.text = dao.value
            textView.typeface = Typeface.createFromAsset(context.assets, "${dao.font}.ttf")
            textView.setTextColor(Color.parseColor(dao.color))
            textView.textSize = dao.size.toFloat()

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(it, position)
                }
            }
        }
    }
}