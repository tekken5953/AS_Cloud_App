package app.airsignal.weather.as_eye.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class ReportViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<EyeDataModel.EyeReportAdapter>,
    private val viewPager2: ViewPager2
) :
    RecyclerView.Adapter<ReportViewPagerAdapter.ViewHolder>() {
    private val mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
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
        private val cautionImg = view.findViewById<ImageView>(R.id.vpAeAlert)

        fun bind(dao: EyeDataModel.EyeReportAdapter) {
            title.text = dao.title
            content.text = dao.content

            cautionImg.setImageDrawable(ResourcesCompat.getDrawable(context.resources,
                if (dao.isCaution) R.drawable.caution_test else R.drawable.caution_good,null))

//            viewPager2.visibility = if(mList.size == 0) View.GONE else View.VISIBLE
        }
    }
}