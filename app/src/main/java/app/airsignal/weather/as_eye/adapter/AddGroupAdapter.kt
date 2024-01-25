package app.airsignal.weather.as_eye.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.OnAdapterItemClick
import java.util.*

class AddGroupAdapter(
    private val context: Context,
    list: ArrayList<EyeDataModel.Group>
) :
    RecyclerView.Adapter<AddGroupAdapter.ViewHolder>() {
    private val mList = list

    private lateinit var onClickListener: OnAdapterItemClick.OnAdapterItemClick

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_add_group, parent, false)

        return ViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnAdapterItemClick.OnAdapterItemClick) {
        this.onClickListener = listener
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            changeSelected(position,isChecked)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val alias: TextView = itemView.findViewById(R.id.listItemAeAddGroupName)
        private val serial: TextView = itemView.findViewById(R.id.listItemAeAddGroupSerial)
        val checkBox: CheckBox = itemView.findViewById(R.id.listItemAeAddGroupCheck)
        private val master: TextView = itemView.findViewById(R.id.listItemAeAddGroupMaster)

        fun bind(dao: EyeDataModel.Group) {
            alias.text = dao.device.alias
            serial.text = dao.device.serial.serial

            if (dao.device.isMaster) {
                master.visibility = View.VISIBLE
            } else {
                master.visibility = View.GONE
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

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelected(p: Int, b: Boolean) {
        mList[p].isChecked = b
        notifyDataSetChanged()
    }

    fun getChecked(p: Int): Boolean {
        return mList[p].isChecked
    }

    fun getCheckedCount(): Int {
        return mList.count{it.isChecked}
    }
}