package app.airsignal.weather.utils

import android.content.Context
import android.graphics.Typeface

object TypeFaceObject {
    private const val TYPEFACE_REGULAR = "spoqa_hansansneo_regular.ttf"
    private const val TYPEFACE_MEDIUM = "spoqa_hansansneo_medium.ttf"
    private const val TYPEFACE_BOLD = "spoqa_hansansneo_bold.ttf"

    fun getBold(context: Context): Typeface = Typeface.createFromAsset(context.assets, TYPEFACE_BOLD)
    fun getMedium(context: Context): Typeface = Typeface.createFromAsset(context.assets, TYPEFACE_MEDIUM)
    fun getRegular(context: Context): Typeface = Typeface.createFromAsset(context.assets, TYPEFACE_REGULAR)
}