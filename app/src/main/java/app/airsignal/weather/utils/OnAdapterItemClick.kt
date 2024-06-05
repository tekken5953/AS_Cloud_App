package app.airsignal.weather.utils

import android.view.View

interface OnAdapterItemClick {
    fun onItemClick(v: View, position: Int)
}