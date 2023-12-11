package app.core_as_eye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.core_as_eye.R
import app.core_as_eye.dao.EyeDataModel
import app.utils.OnAdapterItemClick
import java.util.*

class EyeCategoryAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Category>
) :
    RecyclerView.Adapter<EyeCategoryAdapter.ViewHolder>() {
    private val mList = list
    private var selectedPosition = 0

    private lateinit var onClickListener: OnAdapterItemClick.OnAdapterItemClick

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_category, parent, false)

        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnAdapterItemClick.OnAdapterItemClick) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var categoryName: TextView = itemView.findViewById(R.id.listItemAeCategoryText)

        @SuppressLint("InflateParams")
        fun bind(dao: EyeDataModel.Category) {
            categoryName.text = dao.name

            if (adapterPosition == selectedPosition)
                categoryName.setTextColor(context.getColor(R.color.theme_ae_category_color))
            else categoryName.setTextColor(context.getColor(R.color.ae_sub_color))

            itemView.setOnClickListener {
                val position = adapterPosition
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