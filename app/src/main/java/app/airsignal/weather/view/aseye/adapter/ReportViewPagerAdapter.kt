package app.airsignal.weather.view.aseye.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.view.aseye.dao.EyeDataModel

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class ReportViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<EyeDataModel.EyeReportModel>,
    private val viewPager2: ViewPager2
) :
    RecyclerView.Adapter<ReportViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private var textColor: Int = Color.WHITE

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReportViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.view_pager_item_ae_report, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.vpAeTitle)
        private val content = view.findViewById<TextView>(R.id.vpAeContent)

        fun bind(dao: EyeDataModel.EyeReportModel) {
            title.text = dao.title
            content.text = dao.content

            viewPager2.visibility = if(mList.size == 0) View.GONE else View.VISIBLE

            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}