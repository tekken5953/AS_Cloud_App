package app.airsignal.weather.as_eye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.OnAdapterItemClick
import java.util.*

class AddDeviceWifiAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Wifi>
) :
    RecyclerView.Adapter<AddDeviceWifiAdapter.ViewHolder>() {
    private val mList = list

    var isCapability = false

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_eye_wifi, parent, false)

        return ViewHolder(view)
    }

    private lateinit var onClickListener: OnAdapterItemClick.OnAdapterItemClick

    fun setOnItemClickListener(listener: OnAdapterItemClick.OnAdapterItemClick) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ssid: TextView = itemView.findViewById(R.id.listItemAddEyeWifiSsid)
        private val capability: ImageView = itemView.findViewById(R.id.listItemAddEyeWifiLock)
        private val level: ImageView = itemView.findViewById(R.id.listItemAddEyeWifiLevel)
        private val connect: TextView = itemView.findViewById(R.id.listItemAddEyeWifiConnect)

        fun bind(dao: EyeDataModel.Wifi) {
            ssid.text = dao.ssid
            level.setImageDrawable(parseLevel(dao.level))
            capability.visibility = if (isCapability(dao.capability)) View.VISIBLE else View.GONE

            isCapability = isCapability(dao.capability)

            connect.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        onClickListener.onItemClick(it, position)
                    } catch (e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // 비밀번호 유무
    private fun isCapability(capabilities: String): Boolean {
        return capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains(
            "WEP"
        )
    }

    private fun parseLevel(level: Int): Drawable? {
        return ResourcesCompat.getDrawable(
            context.resources,
            when {
                level >= -30 -> {
                    R.drawable.test_wifi_4
                }
                level in -30 downTo -60 -> {
                    R.drawable.test_wifi_4
                }
                level in -60 downTo -70 -> {
                    R.drawable.test_wifi_3
                }
                else -> {
                    R.drawable.test_wifi_1
                }
            }, null
        )
    }

//    // 대역폭 변환
//    private fun parseFrequency(frequency: Int): String {
//        return if (frequency in 2400..2484) {
//            "2.4"
//        } else if (frequency >= 5000) {
//            "5.0"
//        } else {
//            "Low"
//        }
//    }
}