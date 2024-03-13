package app.airsignal.weather.as_eye.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.SnackBarUtils


class EyeMembersAdapter(private val context: Context, list: ArrayList<EyeDataModel.Members>) :
    RecyclerView.Adapter<EyeMembersAdapter.ViewHolder>() {
    private var mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_members, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val id = itemView.findViewById<TextView>(R.id.listItemMembersId)
        private val isMaster = itemView.findViewById<TextView>(R.id.listItemMembersMaster)
        private val delete = itemView.findViewById<ImageView>(R.id.listItemMembersDelete)
        private val changeMaster = itemView.findViewById<ImageView>(R.id.listItemMembersChangeMaster)

        fun bind(dao: EyeDataModel.Members) {
            id.text = dao.id
            isMaster.visibility = if (dao.isMaster) View.VISIBLE else View.GONE

            delete.visibility = if (dao.isMaster) View.GONE else View.VISIBLE

            changeMaster.visibility = if(dao.isMaster) View.GONE else View.VISIBLE

            delete.setOnClickListener {
                val dialog = MakeDoubleDialog(context)
                val make = dialog.make("멤버를 삭제하시겠습니까?","삭제","취소",R.color.red)
                make.first.setOnClickListener {
                    dialog.dismiss()
                    ToastUtils(context).showMessage("삭제가 완료되었습니다")
                }
                make.second.setOnClickListener {
                    dialog.dismiss()
                }
            }

            changeMaster.setOnClickListener {

            }
        }
    }
}