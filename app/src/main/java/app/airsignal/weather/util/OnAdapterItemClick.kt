package app.airsignal.weather.util

import android.view.View

interface OnAdapterItemClick {
    fun onItemClick(v: View, position: Int)
}