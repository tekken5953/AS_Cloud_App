package app.airsignal.weather.as_eye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.adapter.ItemDiffCallback
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.util.OnAdapterItemClick
import java.util.*

class EyeCategoryAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Category>
) :
    RecyclerView.Adapter<EyeCategoryAdapter.ViewHolder>() {
    private var mList = list
    var selectedPosition = 0

    private lateinit var onClickListener: OnAdapterItemSingleClick

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_category, parent, false)

        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnAdapterItemSingleClick) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var categoryName: TextView = itemView.findViewById(R.id.listItemAeCategoryText)

        fun bind(dao: EyeDataModel.Category) {
            categoryName.text = dao.name

            if (bindingAdapterPosition == selectedPosition)
                categoryName.setTextColor(context.getColor(R.color.theme_ae_category_color))
            else categoryName.setTextColor(context.getColor(R.color.ae_sub_color))

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try { onClickListener.onItemClick(it, position) }
                    catch (e: UninitializedPropertyAccessException) { e.printStackTrace() }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}