package app.airsignal.weather.as_eye.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide

class TutorialViewPagerAdapter(
    private val context: Activity,
    list: ArrayList<Int>
) :
    RecyclerView.Adapter<TutorialViewPagerAdapter.ViewHolder>() {
    private val mList = list
    private val lottieViews = mutableListOf<LottieAnimationView>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View = inflater.inflate(R.layout.tutorial_view_pager_item_eye, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val lottie = itemView.findViewById<LottieAnimationView>(R.id.listItemTutorial)

        fun bind(dao: Int) {
            lottieViews.add(lottie)
//            Glide.with(context).load(ResourcesCompat.getDrawable(context.resources, dao, null)).into(img)
            lottie.setAnimation(dao)
        }
    }

    fun pausePreviousLottie(position: Int) {
        for ((index, lottieView) in lottieViews.withIndex()) {
            if (index != position) lottieView.pauseAnimation()
        }
    }

    fun playCurrentLottie(position: Int) {
        lottieViews.getOrNull(position)?.playAnimation()
    }
}