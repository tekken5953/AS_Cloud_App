package com.example.airsignal_app.util


/**
 * @author : Lee Jae Young
 * @since : 2023-06-05 오전 11:12
 **/
class WrapTextClass {
    // WordWrap 적용
    fun getFormedText(s: String, max: Int): String {
        val fs = getSplitCount(s)
        val sb = StringBuffer()
        var lineCount = 1
        for (i: Int in 0..fs.lastIndex) {
            if (i != 0) {
                if (i == lineCount) {
                    if (fs[i-1].length + fs[i].length > max) {
                        sb.append("\n").append(fs[i])
                        lineCount++
                    } else {
                        sb.append(" " + fs[i])
                    }
                } else {
                    sb.append("\n" + fs[i])
                    lineCount = i + 1
                }
            } else {
                sb.append(fs[0])
            }
        }

        return sb.toString()
    }

    private fun getSplitCount(s: String): List<String> {
        return s.split(" ")
    }
}