package app.airsignal.weather.util

import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.*

@Suppress("EnumEntryName")
object Term24Class {
    const val TERMS_TITLE = "title"
    const val TERMS_DATE = "date"
    const val TERMS_EXPLAIN = "explain"

    enum class Term24(val title: String, val explain: String) {
        입춘("입춘(立春)", "봄의 시작"),
        우수("우수(雨水)", "눈이 녹기 시작하는 날"),
        경칩("경칩(驚蟄)", "개구리가 겨울잠에서 깨는 날"),
        춘분("춘분(春分)", "낮의 길이가 밤보다 길어지기 시작하는 날"),
        청명("청명(淸明)", "봄의 날씨가 가장 좋은 날"),
        곡우("곡우(穀雨)", "봄비가 내리는 날"),
        입하("입하(立夏)", "여름의 시작"),
        소만("소만(小滿)", "볕이 잘 드는 날"),
        망종("망종(芒種)", "곡식의 씨앗을 뿌리는 날"),
        하지("하지(夏至)", "1년 중 낮이 가장 긴 날"),
        소서("소서(小暑)", "본격적으로 더위가 시작되는 날"),
        대서("대서(大暑)", "1년 중 가장 더운 날"),
        입추("입추(立秋)", "가을의 시작"),
        처서("처서(處暑)", "가을바람이 불기 시작 하는 날"),
        백로("백로(白露)", "이슬이 맺히기 시작하는 날"),
        추분("추분(秋分)", "밤의 길이가 낮보다 길어지기 시작하는 날"),
        한로("한로(寒露)", "찬 이슬이 맺히기 시작 하는 날"),
        상강("상강(霜降)", "서리가 내리기 시작하는 날"),
        입동("입동(立冬)", "겨울의 시작"),
        소설("소설(小雪)", "눈이 내리기 시작하는 날"),
        대설("대설(大雪)", "1년 중 눈이 가장 많이 내리는 날"),
        동지("동지(冬至)", "1년 중 낮이 가장 짧은 날"),
        소한("소한(小寒)", "1년 중 가장 추운 날"),
        대한("대한(大寒)", "1년 중 소한 다음으로 큰 추위")
    }

    /** 24절기 불러오기 **/
    fun getTerms24Bundle(term24: String?): Bundle? {
        return term24?.let {
            val term = Term24.values().find { it.name == term24 }
            term?.let { putBundle(it) }
        }
    }

    /** 24절기 번들 필드 추가 **/
    private fun putBundle(term: Term24): Bundle {
        return Bundle().apply {
            putString(TERMS_TITLE, term.title)
            putString(TERMS_DATE, getTermDate())
            putString(TERMS_EXPLAIN, term.explain)
        }
    }

    /** 24절기 날짜 반환 **/
    private fun getTermDate(): String {
        val mFormat = SimpleDateFormat("MM월 dd일", Locale.KOREA)
        val calendar = Calendar.getInstance()
        return mFormat.format(calendar.time)
    }
}