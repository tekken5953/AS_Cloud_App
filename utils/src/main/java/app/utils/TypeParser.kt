package app.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TypeParser {
    /** 위젯용 현재시간 타임포멧 **/
    fun currentDateTimeString(format: String): String {
        @SuppressLint("SimpleDateFormat") val mFormat = SimpleDateFormat(format)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        return mFormat.format(calendar.time)
    }
}