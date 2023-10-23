package app.airsignal.weather.util

import android.os.Bundle
import app.airsignal.weather.util.`object`.DataTypeParser

class Term24Class {

    /** 24절기 번들 반환 **/
    fun getTerms24Bundle(term24: String?): Bundle? {
        term24?.let {
            if (it != "")
                return when(it) {
                    "입춘" -> put("입춘(立春)","봄의 시작")
                    "우수" -> put("우수(雨水)","눈이 녹기 시작하는 날")
                    "경칩" -> put("경칩(驚蟄)","개구리가 겨울잠에서 깨는 날")
                    "춘분" -> put("춘분(春分)","낮의 길이가 밤보다 길어지기 시작하는 날")
                    "청명" -> put("청명(淸明)","봄의 날씨가 가장 좋은 날")
                    "곡우" -> put("곡우(穀雨)","봄비가 내리는 날")
                    "입하" -> put("입하(立夏)","여름의 시작")
                    "소만" -> put("소만(小滿)","볕이 잘 드는 날")
                    "망종" -> put("망종(芒種)","곡식의 씨앗을 뿌리는 날")
                    "하지" -> put("하지(夏至)","1년 중 낮이 가장 긴 날")
                    "소서" -> put("소서(小暑)","본격적으로 더위가 시작되는 날")
                    "대서" -> put("대서(大暑)","1년 중 가장 더운 날")
                    "입추" -> put("입추(立秋)","가을의 시작")
                    "처서" -> put("처서(處暑)","가을바람이 불기 시작 하는 날")
                    "백로" -> put("백로(白露)","이슬이 맺히기 시작하는 날")
                    "추분" -> put("추분(秋分)","밤의 길이가 낮보다 길어지기 시작하는 날")
                    "한로" -> put("한로(寒露)","찬 이슬이 맺히기 시작 하는 날")
                    "상강" -> put("상강(霜降)","서리가 내리기 시작하는 날")
                    "입동" -> put("입동(立冬)","겨울의 시작")
                    "소설" -> put("소설(小雪)","눈이 내리기 시작하는 날")
                    "대설" -> put("대설(大雪)","1년 중 눈이 가장 많이 내리는 날")
                    "동지" -> put("동지(冬至)","1년 중 낮이 가장 짧은 날")
                    "소한" -> put("소한(小寒)","1년 중 가장 추운 날")
                    "대한" -> put("대한(大寒)","1년 중 소한 다음으로 큰 추위")
                    else -> null
                }
            else return null
        }?: return null
    }

    /** 24절기 날짜 반환 **/
    private fun getTermDate(): String {
        val date = DataTypeParser.currentDateTimeString("MM월 dd일").split(" ")
        val result = StringBuilder()
        date.forEach {
            if (it.first() == '0') result.append(it.replaceFirst("0","") + " ")
            else result.append("$it ")
        }
        return result.trim().toString()
    }

    /** 24절기 번들 필드 추가 **/
    private fun put(title: String, explain: String): Bundle {
        return Bundle().apply {
            putString("title",title)
            putString("date",getTermDate())
            putString("explain",explain)
        }
    }
}