package app.airsignal.weather.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.view.*
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.api.retrofit.ApiModel
import app.airsignal.weather.view.activity.WebURLActivity

class InAppViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<ApiModel.InAppMsgItem>,
) :
    RecyclerView.Adapter<InAppViewPagerAdapter.ViewHolder>() {
    private val mList = list
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InAppViewPagerAdapter.ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.view_pager_item_in_app, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val webView = view.findViewById<WebView>(R.id.viewPagerInAppWebView)
        private val linear = view.findViewById<LinearLayout>(R.id.viewPagerInAppLinear)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(dao: ApiModel.InAppMsgItem) {
            if (Build.VERSION.SDK_INT < 31) {
                linear.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        outline?.setRect(0, 0, view?.width ?: 0, view?.height ?: 0)
                    }
                }
            } else {
                linear.clipToOutline = true
            }

            webView.settings.apply {
                useWideViewPort = true // 화면 맞추기
            }

            webView.loadUrl(dao.img)

            webView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val intent = Intent(context, WebURLActivity::class.java)
                    intent.putExtra("appBar",false)
                    intent.putExtra("sort","inAppLink")
                    intent.putExtra("redirect", dao.redirect)
                    context.startActivity(intent)
                    true
                } else { false }
            }
        }
    }
}