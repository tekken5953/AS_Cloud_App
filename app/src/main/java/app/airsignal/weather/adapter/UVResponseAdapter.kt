package app.airsignal.weather.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-06-01 오후 3:42
 **/
class UVResponseAdapter(private val context: Context, list: ArrayList<AdapterModel.UVResponseItem>) :
    RecyclerView.Adapter<UVResponseAdapter.ViewHolder>() {
    private val mList = list
    private var isWhite = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UVResponseAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_uv_collapsed, parent, false)
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
        private val text: TextView = itemView.findViewById(R.id.listItemUvResponseText)
        private val dot: ImageView = itemView.findViewById(R.id.listItemUvResponseDot)

        fun bind(dao: AdapterModel.UVResponseItem) {
            text.text = dao.text

            text.setTextColor(if(isWhite)context.getColor(R.color.white) else context.getColor(R.color.main_black))
            dot.imageTintList = ColorStateList.valueOf(
                context.getColor(if(isWhite)R.color.white else R.color.main_black)
            )
        }
    }
}