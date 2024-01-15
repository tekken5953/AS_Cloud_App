package app.airsignal.weather.as_eye.adapter

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
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.OnAdapterItemClick
import java.util.*

class EyeDeviceAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Device>
) :
    RecyclerView.Adapter<EyeDeviceAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnAdapterItemClick.OnAdapterItemClick

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_ae_device_list, parent, false)

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
        private var deviceName: TextView = itemView.findViewById(R.id.listItemAeDeviceName)
        private val serial: TextView = itemView.findViewById(R.id.listItemAeDeviceSerial)
        private val power: TextView = itemView.findViewById(R.id.listItemAeDevicePower)
        val report: ImageView = itemView.findViewById(R.id.listItemAeDeviceReport)
        private val addDevice: ImageView = itemView.findViewById(R.id.listItemAeDeviceAdd)
        private val container: RelativeLayout = itemView.findViewById(R.id.listItemAeDeviceContainer)
        private val master: TextView = itemView.findViewById(R.id.listItemAeDeviceMaster)

        fun bind(dao: EyeDataModel.Device) {
            deviceName.text = dao.alias
            serial.text = dao.serial.serial

            dao.serial.power.let {
                if (it) {
                    power.visibility = View.VISIBLE
                    container.background = getRs(R.drawable.ae_device_bg_e)
                } else {
                    power.visibility = View.GONE
                    container.background = getRs(R.drawable.ae_device_bg_d)
                }
            }

            if (dao.alias == "") {
                deviceName.visibility = View.GONE
                serial.visibility = View.GONE
                addDevice.visibility = View.VISIBLE
            } else {
                deviceName.visibility = View.VISIBLE
                serial.visibility = View.VISIBLE
                addDevice.visibility = View.GONE
            }

            if (dao.serial.report) {
                report.visibility = View.VISIBLE
            } else {
                report.visibility = View.GONE
            }

            if (dao.isMaster) {
                master.visibility = View.VISIBLE
            } else {
                master.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        if (dao.serial.power) {
                            onClickListener.onItemClick(it, position)
                        }
                    }
                    catch (e: UninitializedPropertyAccessException) { e.printStackTrace() }
                }
            }
        }
    }

    private fun getRs(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, id,null)
    }
}