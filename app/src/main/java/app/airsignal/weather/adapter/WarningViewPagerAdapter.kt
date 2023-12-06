package app.airsignal.weather.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.view.activity.WarningDetailActivity

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class WarningViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<String>,
    private val viewPager2: ViewPager2
) :
    RecyclerView.Adapter<WarningViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private var textColor: Int = Color.WHITE

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarningViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.view_pager_item_main_report, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun changeTextColor(color: Int) { textColor = color }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.vpWarningText)

        fun bind(dao: String) {
            textView.text = dao
            textView.setTextColor(textColor)

            viewPager2.visibility = if(mList.size == 0) View.GONE else View.VISIBLE

            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    try {
                        if (mList.size != 0) {
                            val intent = Intent(context, WarningDetailActivity::class.java)
                            intent.putExtra("warning", mList)
                            intent.putExtra("isMain", true)
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}