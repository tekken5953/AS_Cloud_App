package com.example.airsignal_app.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.telephony.CarrierConfigManager.Gps
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오후 14:01
 **/
class AddressListAdapter(private val context: Context, list: ArrayList<String>) :
    RecyclerView.Adapter<AddressListAdapter.ViewHolder>() {
    private val mList = list
    private var visible = false

    private lateinit var onClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddressListAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_address_list, parent, false)
        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        private val address: TextView = itemView.findViewById(R.id.listCurrentAddressText)
        private val checked: ImageView = itemView.findViewById(R.id.listCurrentAddressChecked)
        private val gpsImg: ImageView = itemView.findViewById(R.id.listCurrentAddressImg)
        private val delete: TextView = itemView.findViewById(R.id.listCurrentAddressDelete)

        fun bind(dao: String) {
            val db = GpsRepository(context)

            address.text = dao
            if (dao == SharedPreferenceManager(context).getString(lastAddress)) {
                checked.visibility = View.VISIBLE
            } else {
                checked.visibility = View.GONE
            }

            if (mList[adapterPosition] == db.findById(CURRENT_GPS_ID).addr) {
                gpsImg.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.fixed_gps, null))
            }

            if (adapterPosition != 0 && visible) {
                delete.visibility = View.VISIBLE
            } else {
                delete.visibility = View.GONE
            }

            delete.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setMessage("${address.text}를 삭제하시겠습니까?")
                    setPositiveButton("예") { _, _ ->
                        db.deleteFromAddress(address.text.toString())
                        mList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        updateCheckBoxVisible(false)
                    }

                    setNegativeButton("아니오"
                    ) { p0, _ -> p0!!.dismiss() }
                }.show()
            }

            itemView.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    try {
                        onClickListener.onItemClick(it, position)
                    } catch(e: UninitializedPropertyAccessException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCheckBoxVisible(b: Boolean) {
        visible = b
        notifyDataSetChanged()
    }

    fun getCheckBoxVisible(): Boolean {
        return visible
    }
}