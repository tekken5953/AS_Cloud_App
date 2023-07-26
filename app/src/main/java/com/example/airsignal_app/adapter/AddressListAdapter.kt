package com.example.airsignal_app.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLastAddress
import org.koin.core.component.getScopeId
import timber.log.Timber

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

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val address: TextView = itemView.findViewById(R.id.listCurrentAddressText)
        private val checked: ImageView = itemView.findViewById(R.id.listCurrentAddressChecked)
        private val gpsImg: ImageView = itemView.findViewById(R.id.listCurrentAddressImg)
        private val delete: TextView = itemView.findViewById(R.id.listCurrentAddressDelete)

        fun bind(dao: String) {
            val db = GpsRepository(context)

            address.text = dao

            if (dao == getUserLastAddress(context)) {
                checked.visibility = View.VISIBLE
            } else {
                checked.visibility = View.GONE
            }

            if (mList[adapterPosition] == db.findById(CURRENT_GPS_ID).addr) {
                gpsImg.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.gps_fix,
                        null
                    )
                )
            }

            if (adapterPosition != 0 && visible) {
                delete.animate().alpha(1f).duration = 500
                delete.visibility = View.VISIBLE
            } else {
                delete.animate().alpha(0f).duration = 500
                delete.visibility = View.GONE
            }

            delete.setOnClickListener {
                val builder = Dialog(context)
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_alert_double_btn, null)
                builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.setContentView(view)
                builder.create()

                val cancel = view.findViewById<AppCompatButton>(R.id.alertDoubleCancelBtn)
                val apply = view.findViewById<AppCompatButton>(R.id.alertDoubleApplyBtn)
                val title = view.findViewById<TextView>(R.id.alertDoubleTitle)

                val span = SpannableStringBuilder("${address.text}을(를)\n삭제하시겠습니까?")
                span.setSpan(ForegroundColorSpan(ResourcesCompat.getColor(context.resources,
                    R.color.theme_alert_double_apply_color, null)),0,
                    address.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                title.text = span
                apply.text = context.getString(R.string.delete)
                cancel.text = context.getString(R.string.cancel)
                apply.setOnClickListener {
                    db.deleteFromAddress(address.text.toString())
                    mList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    updateCheckBoxVisible(false)
                    builder.dismiss()
                }
                cancel.setOnClickListener {
                    builder.dismiss()
                }

                builder.show()
            }

            itemView.setOnClickListener {
                val position = adapterPosition

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

    // 삭제버튼 보이기/숨기기
    @SuppressLint("NotifyDataSetChanged")
    fun updateCheckBoxVisible(b: Boolean) {
        visible = b
        notifyDataSetChanged()
    }

    // 삭제버튼 현재 상태 불러오기
    fun getCheckBoxVisible(): Boolean {
        return visible
    }
}