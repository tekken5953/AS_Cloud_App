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
 * @since : 2023-03-28 오전 11:52
 **/
class NoticeAdapter(private val context: Context, list: ArrayList<AdapterModel.NoticeItem>) :
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoticeAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_notice, parent, false)
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
        private val date: TextView = itemView.findViewById(R.id.itemNoticeDate)
        private val title: TextView = itemView.findViewById(R.id.itemNoticeHeader)
        private val category: TextView = itemView.findViewById(R.id.itemNoticeCategory)

        fun bind(dao: AdapterModel.NoticeItem) {
            date.text = dao.created
            title.text = dao.title
            category.text = dao.category

            if (bindingAdapterPosition == 0) {
                date.setTextColor(context.getColor(R.color.main_blue_color))
            }

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(it, position)
                }
            }
        }
    }
}