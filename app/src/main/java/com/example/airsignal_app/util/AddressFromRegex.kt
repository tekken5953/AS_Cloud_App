package com.example.airsignal_app.util

import com.example.airsignal_app.dao.StaticDataObject.IN_COMPLETE_ADDRESS


/**
 * @author : Lee Jae Young
 * @since : 2023-07-19 오전 10:30
 **/
class AddressFromRegex(private val address: String) {

    fun getAddress(): String? {
        val result: StringBuilder = StringBuilder()

        generatePatternFirst().forEachIndexed { indexF, first ->
//            Timber.tag("regexTest").d("First is $first")
            if (!first.findAll(address).none()) {
//                Timber.tag("regexTest").d("append : ${first.find(address)!!.value}")
                result.append(first.find(address)!!.value).append(" ")
                if (indexF == generatePatternFirst().lastIndex) {
                    if (result.split(" ").isEmpty()) {
                        return address
                    } else {
                        generatePatternSecond().forEachIndexed { indexS, second ->
                            if (!second.findAll(address).none()) {
//                            Timber.tag("regexTest").d("append : ${second.find(address)!!.value}")
                                result.append(second.find(address)!!.value).append(" ")
                                if (indexS == generatePatternSecond().lastIndex) {
                                    if (result.split(" ").size < 2) {
                                        return IN_COMPLETE_ADDRESS
                                    } else {
                                        if (isRoadAddress()) {
//                                    Timber.tag("regexTest").d("Is road address")
                                            generatePatternRoad().forEachIndexed { _, road ->
                                                if (!road.findAll(address).none()) {
//                                            Timber.tag("regexTest").d("append : ${road.find(address)!!.value}")
                                                    result.append(road.find(address)!!.value).append(" ")
                                                }
                                            }
                                        } else {
//                                    Timber.tag("regexTest").d("Is Not road address")
                                            generatePatternThird().forEachIndexed { indexT, third ->
                                                if (!third.findAll(address).none()) {
//                                            Timber.tag("regexTest").d("append : ${third.find(address)!!.value}")
                                                    result.append(third.find(address)!!.value).append(" ")
                                                    if (indexT == generatePatternRoad().lastIndex) {
                                                        generatePatternFourth().forEachIndexed { _, fourth ->
                                                            if (!fourth.findAll(address).none()) {
//                                                        Timber.tag("regexTest")
//                                                            .d("Result : ${fourth.find(address)!!.value}")
                                                                result.append(fourth.find(address)!!.value)
                                                                    .append(" ")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return if (result.isEmpty()) {
            null
        } else {
            result.toString()
        }
    }

    fun getNotificationAddress(): String {
        val result: StringBuilder = StringBuilder()
        try {
            generatePatternThird().forEach { third ->
                return if (!third.findAll(address).none()) {
                    result.append(third.find(address)!!.value)
                    result.toString()
                } else {
                    getAddress()!!
                }
            }
        } catch (e: java.lang.NullPointerException) {
            return address
        }

        return address
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
            Regex("\\b\\S+특별시\\b"),
            Regex("\\b\\S+광역시\\b"),
            Regex("\\b\\S+도\\b")
        )
    }

    private fun generatePatternSecond(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+시\\b"),
            Regex("\\b\\S+군\\b"),
            Regex("\\b\\S+구\\b")
        )
    }

    private fun generatePatternThird(): ArrayList<Regex> {
        return arrayListOf(
            Regex( "\\b\\S+읍\\b"),
            Regex( "\\b\\S+면\\b"),
            Regex( "\\b\\S+동\\b")
        )
    }

    private fun generatePatternFourth(): ArrayList<Regex> {
        return arrayListOf(
            Regex( "\\b\\S+리\\b")
        )
    }

    private fun generatePatternOthers(): ArrayList<Regex> {
        return arrayListOf(
            Regex( "\\b\\d+\\S*"),
            Regex("\\b\\w+\\S*")
        )
    }

    private fun generatePatternRoad(): ArrayList<Regex> {
        return arrayListOf(
            Regex("\\b\\S+로\\b"),
            Regex("\\b\\S+길\\b")
        )
    }
}