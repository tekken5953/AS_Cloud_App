package app.airsignal.weather.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.utils.controller.ItemDiffCallback

/**
 * @author : Lee Jae Young
 * @since : 2023-06-013 오후 2:35
 **/
class WarningDetailAdapter(
    private val context: Activity,
    list: ArrayList<String>,
) :
    RecyclerView.Adapter<WarningDetailAdapter.ViewHolder>() {
    private var mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WarningDetailAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.list_item_warning_detail, parent, false)
        return ViewHolder(view)
    }

    fun submitList(newItems: ArrayList<String>) {
        val diffCallback = ItemDiffCallback(mList, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)

        mList = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.text12)

        fun bind(dao: String) {
            textView.text = dao
        }
    }
}