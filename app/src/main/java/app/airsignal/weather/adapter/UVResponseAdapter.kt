package app.airsignal.weather.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
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

        holder.text.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val lineCount = holder.text.lineCount
                val layoutParams = holder.dot.layoutParams as RelativeLayout.LayoutParams
                layoutParams.height = convertPixelsToDp(18 * lineCount)
                layoutParams.addRule(Gravity.CENTER_VERTICAL)
                holder.dot.layoutParams = layoutParams

                holder.text.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun setIsWhite(b: Boolean) { isWhite = b }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = itemView.findViewById(R.id.listItemUvResponseText)
        val dot: View = itemView.findViewById(R.id.listItemUvResponseDot)

        fun bind(dao: AdapterModel.UVResponseItem) {
            text.text = dao.text

            text.setTextColor(if(isWhite)context.getColor(R.color.white) else context.getColor(R.color.main_black))
            dot.setBackgroundColor(if(isWhite)context.getColor(R.color.white) else context.getColor(R.color.main_black))
        }
    }

    private fun convertPixelsToDp(px: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (px * scale).toInt()
    }
}