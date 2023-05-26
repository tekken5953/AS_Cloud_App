package com.example.airsignal_app.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.AdapterModel

/**
 * @author : Lee Jae Young
 * @since : 2023-05-25 오후 5:24
 **/
class MainViewPagerAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items_air = mutableListOf<AdapterModel.AirViewPagerItem>()
    private val items_sun = mutableListOf<AdapterModel.SunViewPagerItem>()

    companion object {
        const val VIEW_TYPE_AIR = 0
        const val VIEW_TYPE_SUN = 1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItemAir(viewType: Int, title: String) {
        val item = AdapterModel.AirViewPagerItem(viewType,title)
        items_air.add(item)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItemSun(viewType: Int, title: String) {
        val item = AdapterModel.SunViewPagerItem(viewType,title)
        items_sun.add(item)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_AIR -> {
                val view = inflater.inflate(R.layout.viewpager_item_air, parent, false)
                ViewHolderAir(view)
            }
            VIEW_TYPE_SUN -> {
                val view = inflater.inflate(R.layout.viewpager_item_sun, parent, false)
                ViewHolderSun(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // onBindViewHolder 구현
        Log.d("viewPagerTest","position : $position, viewType : ${getItemViewType(position)}")
        try {
            if (holder.itemViewType == VIEW_TYPE_AIR) {
                (holder as ViewHolderAir).bind(items_air[position])
            } else if (holder.itemViewType == VIEW_TYPE_SUN) {
                (holder as ViewHolderSun).bind(items_sun[position])
            }
        } catch(e: java.lang.IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        // 페이지 수를 반환
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        // viewType을 구분하는 기준을 정의
        return if (position == 0) {
            VIEW_TYPE_AIR
        } else {
            VIEW_TYPE_SUN
        }
    }

    inner class ViewHolderAir(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.vp_item_air_1)
        // ViewHolderOne의 내용 구현
        fun bind(dao: AdapterModel.AirViewPagerItem) {
            itemView.apply {
                title.text = dao.title
            }
        }
    }

    inner class ViewHolderSun(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolderTwo의 내용 구현
        private val title: TextView = itemView.findViewById(R.id.vp_item_sun_1)
        // ViewHolderOne의 내용 구현
        fun bind(dao: AdapterModel.SunViewPagerItem) {
            itemView.apply {
                title.text = dao.title
            }
        }
    }
}


