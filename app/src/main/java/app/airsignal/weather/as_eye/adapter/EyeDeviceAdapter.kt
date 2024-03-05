package app.airsignal.weather.as_eye.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.adapter.ItemDiffCallback
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class EyeDeviceAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Device>
) :
    RecyclerView.Adapter<EyeDeviceAdapter.ViewHolder>() {
    private var mList = list

    private val toast by lazy { ToastUtils(context) }

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
        private val container: RelativeLayout =
            itemView.findViewById(R.id.listItemAeDeviceContainer)
        private val master: TextView = itemView.findViewById(R.id.listItemAeDeviceMaster)

        fun bind(dao: EyeDataModel.Device) {
            deviceName.text = dao.alias
            serial.text = dao.serial

            if (dao.alias == "") {
                deviceName.visibility = View.GONE
                serial.visibility = View.GONE
                addDevice.visibility = View.VISIBLE
                report.visibility = View.GONE
                master.visibility = View.GONE
            } else {
                deviceName.visibility = View.VISIBLE
                serial.visibility = View.VISIBLE
                addDevice.visibility = View.GONE

                dao.detail?.let { pDetail ->
                    if (pDetail.report) {
                        report.visibility = View.VISIBLE
                    } else {
                        report.visibility = View.GONE
                    }

                    pDetail.power.let {
                        if (it) {
                            if (dao.serial != "") {
                                power.visibility = View.VISIBLE
                                container.background = getRs(R.drawable.ae_device_bg_e)
                            } else {
                                power.visibility = View.GONE
                                container.background = getRs(R.drawable.ae_device_bg_e)
                            }
                        } else {
                            power.visibility = View.GONE
                            container.background = getRs(R.drawable.ae_device_bg_d)
                        }
                    }
                }
            }

            if (dao.isMaster) {
                master.visibility = View.VISIBLE
                master.text = "소유자"
            } else {
                if (dao.serial != "") {
                    master.visibility = View.VISIBLE
                    master.text = "게스트"
                } else {
                    master.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        if (dao.detail?.power == true) {
                            onClickListener.onItemClick(it, position)
                        }
                    } catch (e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                    }
                }
            }

            itemView.setOnLongClickListener {
                val dialog = MakeDoubleDialog(context)
                val show = dialog.make(
                    "${dao.alias}(${dao.serial})를\n삭제하시겠습니까?",
                    "예", "아니오", android.R.color.holo_red_light
                )

                show.first.setOnClickListener {
                    dialog.dismiss()
                    dao.serial?.let {
                        deleteDevice(it, dao.email)
                    }
                }
                show.second.setOnClickListener {
                    dialog.dismiss()
                }

                true
            }
        }
    }

    private fun getRs(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, id, null)
    }

    private fun deleteDevice(sn: String, email: String) {
        HttpClient.getInstance(false).setClientBuilder().deleteDevice(
            sn, email
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                toast.showMessage(context.getString(R.string.success_to_delete))
                RefreshUtils(context).refreshActivity()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                toast.showMessage(context.getString(R.string.fail_to_delete))
            }
        })
    }
}