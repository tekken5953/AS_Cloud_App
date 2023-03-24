package com.example.airsignal_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-03-23 오후 4:00
 **/
class HomeViewPagerAdapter(mContext: Context, list: ArrayList<AdapterModel.ViewPagerItem>) :
    RecyclerView.Adapter<HomeViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private val context = mContext

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_main_address, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
//        private val address = itemView.findViewById<TextView>(R.id.mainGpsTitleTv)!!

        fun bind(dao: AdapterModel.ViewPagerItem) {
//            address.text = dao.item
        }
    }
}