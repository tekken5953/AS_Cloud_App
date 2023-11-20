package app.airsignal.weather.view.aseye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.adapter.OnAdapterItemClick
import app.airsignal.weather.view.aseye.dao.EyeDataModel
import java.util.*

class EyeDeviceAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.DeviceModel>
) :
    RecyclerView.Adapter<EyeDeviceAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnAdapterItemClick.OnAdapterItemClick

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EyeDeviceAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_device_list, parent, false)

        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnAdapterItemClick.OnAdapterItemClick) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: EyeDeviceAdapter.ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var deviceName: TextView = itemView.findViewById(R.id.listItemAeDeviceName)
        val serial: TextView = itemView.findViewById(R.id.listItemAeDeviceSerial)
        val power: TextView = itemView.findViewById(R.id.listItemAeDevicePower)
        val report: ImageView = itemView.findViewById(R.id.listItemAeDeviceReport)
        val addDevice: ImageView = itemView.findViewById(R.id.listItemAeDeviceAdd)
        val container: RelativeLayout = itemView.findViewById(R.id.listItemAeDeviceContainer)

        @SuppressLint("InflateParams")
        fun bind(dao: EyeDataModel.DeviceModel) {
            deviceName.text = dao.name
            serial.text = dao.serial

            dao.power?.let {
                if (it) {
                    power.text = "켜짐"
                    power.setTextColor(context.getColor(R.color.ae_power_on_color))
                    container.background = getRs(R.drawable.ae_device_bg_e)
                } else {
                    power.text = "꺼짐"
                    power.setTextColor(context.getColor(R.color.ae_sub_color))
                    container.background = getRs(R.drawable.ae_device_bg_d)
                }
            } ?: apply {
                power.text = "에러"
                power.setTextColor(context.getColor(R.color.ae_sub_color))
                container.background = getRs(R.drawable.ae_device_bg_d)
            }

            if (dao.isAdd) {
                deviceName.visibility = View.GONE
                serial.visibility = View.GONE
                power.visibility = View.GONE
                report.visibility = View.GONE
                addDevice.visibility = View.VISIBLE
            } else {
                deviceName.visibility = View.VISIBLE
                serial.visibility = View.VISIBLE
                power.visibility = View.VISIBLE
                report.visibility = View.VISIBLE
                addDevice.visibility = View.GONE
            }

            if (dao.report == null) {
                report.visibility = View.GONE
            } else {
                report.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try { onClickListener.onItemClick(it, position) }
                    catch (e: UninitializedPropertyAccessException) { e.printStackTrace() }
                }
            }
        }
    }

    private fun getRs(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, id,null)
    }
}