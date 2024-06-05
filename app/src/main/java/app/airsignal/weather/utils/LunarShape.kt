package app.airsignal.weather.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import app.airsignal.weather.R
import kotlin.math.roundToInt

class LunarShape(age: Float?) {
    private val moonAge = age ?: 0.0F

    fun shapeText(context: Context): String =
        when {
            moonAge <= 1.5 -> context.getString(R.string.lunar_sak)
            moonAge <= 6.5 -> context.getString(R.string.lunar_cho)
            moonAge <= 8.5 -> context.getString(R.string.lunar_sang_d)
            moonAge <= 13.5 -> context.getString(R.string.lunar_sang_m)
            moonAge <= 15.5 -> context.getString(R.string.lunar_bo)
            moonAge <= 20.5 -> context.getString(R.string.lunar_ha_m)
            moonAge <= 22.5 -> context.getString(R.string.lunar_ha_d)
            moonAge <= 29.5 -> context.getString(R.string.lunar_g)
            else -> context.getString(R.string.error)
        }

    fun shapeDrawable(context: Context): Drawable? =
        ResourcesCompat.getDrawable(context.resources,
            when {
                moonAge <= 1.5 -> R.drawable.moon_sak
                moonAge <= 6.5 -> R.drawable.moon_cho
                moonAge <= 8.5 -> R.drawable.moon_sang_d
                moonAge <= 13.5 -> R.drawable.moon_sang_m
                moonAge <= 15.5 -> R.drawable.moon_bo
                moonAge <= 20.5 -> R.drawable.moon_ha_m
                moonAge <= 22.5 -> R.drawable.moon_ha_d
                moonAge <= 29.5 -> R.drawable.moon_g
                else -> R.drawable.cancel
            }, null)

    fun progress(): Int =
        if (moonAge > 15.5) (200 - ((moonAge - 1.5) / 14 * 100).roundToInt())
        else ((moonAge - 1.5) / 14 * 100).roundToInt()
}