package com.example.airsignal_app.util

import com.example.airsignal_app.util.`object`.GetAppInfo


/**
 * @author : Lee Jae Young
 * @since : 2023-07-19 오전 10:30
 **/
class AddressFromRegex(private val address: String) {

    fun getAddress(): String {
        val s1: StringBuilder = StringBuilder()
        val s2: StringBuilder = StringBuilder()
        val s3: StringBuilder = StringBuilder()
        val s4: StringBuilder = StringBuilder()
        val sr: StringBuilder = StringBuilder()

        generatePatternFirst().forEach { first ->
            if (!first.findAll(address).none()) {
                val value = first.find(address)!!.value
                if (!s1.contains(value))
                    s1.append("$value ")
            }
        }
        generatePatternSecond().forEach { second ->
            if (!second.findAll(address).none()) {
                val value = second.find(address)!!.value
                if (!s1.contains(value))
                    s2.append("$value ")
            }
        }
        generatePatternThird().forEach { third ->
            if (!third.findAll(address).none()) {
                val value = third.find(address)!!.value
                if (!s1.contains(value) && !s2.contains(value))
                    s3.append("$value ")
            }
        }
        generatePatternFourth().forEach { fourth ->
            if (!fourth.findAll(address).none()) {
                val value = fourth.find(address)!!.value
                if (!s1.contains(value) && !s2.contains(value) && !s3.contains(value))
                    s4.append(value)
            }
        }
        generatePatternRoad().forEach { road ->
            if (!road.findAll(address).none()) {
                val value = road.find(address)!!.value
                if (!s1.contains(value) && !s2.contains(value))
                    sr.append(value)
            }
        }

        return if (isRoadAddress()) {
            val addr = "${s1}${s2}${sr}"
            if (countSpacesInStringBuilder(addr) < 2) {
                address.replace("대한민국","").replace("South Korea","")
            } else {
                addr
            }
        } else {
            val addr = "${s1}${s2}${s3}${s4}"
            if (countSpacesInStringBuilder(addr) < 2) {
                address.replace("대한민국","").replace("South Korea","")
            } else {
                addr
            }
        }
    }

    fun getNotificationAddress(): String {
        val sb = StringBuilder()
        return try {
            generatePatternThird().forEach { third ->
                if (!third.findAll(address).none()) {
                    sb.append(third.find(address)!!.value)
                }
            }
            if (sb.isEmpty()) {
                getAddress().split(" ").last()
            } else {
                sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            address.replace("대한민국","").replace("South Korea","")
        }
    }

    private fun countSpacesInStringBuilder(s: String): Int {
        var count = 0
        for (element in s) {
            if (element == ' ') {
                count++
            }
        }
        return count
    }

    private fun isRoadAddress(): Boolean {
        val roadList = generatePatternRoad()
        roadList.forEach { list ->
            return list.find(address) != null
        }
        return false
    }

    private fun generatePatternFirst(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+광역시\\b"),
            Regex("\\b\\S+특별시\\b"),
            Regex("\\b\\S+도\\b")
        )
    }

    private fun generatePatternSecond(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+시\\b"),
            Regex("\\b\\S+구\\b"),
            Regex("\\b\\S+군\\b")
        )
    }

    private fun generatePatternThird(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+읍\\b"),
            Regex("\\b\\S+면\\b"),
            Regex("\\b\\S+동\\b")
        )
    }

    private fun generatePatternFourth(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+리\\b")
        )
    }

//    private fun generatePatternOthers(): ArrayList<Regex> {
//        return arrayListOf(
//            Regex("\\b\\d+\\S*"),
//            Regex("\\b\\w+\\S*")
//        )
//    }

    private fun generatePatternRoad(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+로\\b"),
            Regex("\\b\\S+길\\b")
        )
    }
}