package app.airsignal.weather.location


/**
 * @author : Lee Jae Young
 * @since : 2023-07-19 오전 10:30
 **/
class AddressFromRegex(private val address: String) {

    /** 축약 된 주소 반환 **/
    fun getAddress(): String {
        val sbArray: Array<StringBuilder> = Array(5) { StringBuilder() }

        generatePatternFirst().forEach { first ->
            if (first.findAll(address).any()) {
                val value = first.find(address)?.value
                value?.let { if (!sbArray[0].contains(it)) sbArray[0].append("$it ") }
            }
        }
        generatePatternSecond().forEach { second ->
            if (second.findAll(address).any()) {
                second.find(address)?.value?.let { if (!sbArray[0].contains(it)) sbArray[1].append("$it ") }
            }
        }
        generatePatternThird().forEach { third ->
            if (third.findAll(address).any()) {
                third.find(address)?.value?.let {
                    if (!sbArray[0].contains(it) && !sbArray[1].contains(it)) sbArray[2].append("$it ")
                }
            }
        }
        generatePatternFourth().forEach { fourth ->
            if (fourth.findAll(address).any()) {
                fourth.find(address)?.value?.let {
                    if (!sbArray[0].contains(it) && !sbArray[1].contains(it) && !sbArray[2].contains(it))
                        sbArray[3].append(it)
                }
            }
        }
        generatePatternRoad().forEach { road ->
            if (road.findAll(address).any()) {
                road.find(address)?.value?.let {
                    if (!sbArray[0].contains(it) && !sbArray[1].contains(it)) sbArray[4].append("$it ")
                }
            }
        }

        val formatAddress =
            if (isRoadAddress()) "${sbArray[0]}${sbArray[1]}${sbArray[4]}".trim()
            else "${sbArray[0]}${sbArray[1]}${sbArray[2]}${sbArray[3]}".trim()
        return if (countSpacesInStringBuilder(formatAddress) < 2 || formatAddress == "") replaceKorea(address).trim()
        else formatAddress.trim()
    }

    /** 기상 특보 전용 주소 반환 **/
    fun getWarningAddress(): String {
        val sb = StringBuilder()
        return kotlin.runCatching {
            generatePatternFirst().forEach { first ->
                if (first.findAll(address).any()) sb.append(first.find(address)?.value)
            }
            if (sb.isEmpty()) getAddress().split(" ").last() else sb.toString()
        }.getOrElse { "Error" }
    }

    fun getSecondAddress(): String {
        val sb = StringBuilder()
        return kotlin.runCatching {
            generatePatternSecond().forEach { second ->
                if (second.findAll(address).any()) sb.append(second.find(address)?.value + " ")
            }
            if (sb.isEmpty()) getAddress().split(" ").last().trim()
            else sb.toString().trim()
        }.getOrElse { replaceKorea(address) }
    }

    /** 알림 전용 주소 반환 **/
    fun getNotificationAddress(): String {
        val sb = StringBuilder()
        return kotlin.runCatching {
            generatePatternThird().forEach { third ->
                if (third.findAll(address).any()) sb.append(third.find(address)?.value + " ")
            }
            if (sb.isEmpty())
               getAddress().split(" ").filter { it.isNotEmpty() }.takeLast(2).joinToString(" ").trim()
            else sb.toString().trim()
        }.getOrElse { replaceKorea(address) }
    }

    // 문자열에서 띄워쓰기 개수 반환
    private fun countSpacesInStringBuilder(s: String): Int = s.count { it == ' ' }

    private fun replaceKorea(address: String): String =
        address.replace("대한민국", "").replace("South Korea", "")

    // 도로명 주소인지 검사
    fun isRoadAddress(): Boolean {
        generatePatternRoad().forEach { list -> return list.find(address) != null }
        return false
    }

    // 광역시,특별시,도 추출
    private fun generatePatternFirst(): ArrayList<Regex> =
        arrayListOf(
            Regex("\\b\\S+광역시\\b"),
            Regex("\\b\\S+특별시\\b"),
            Regex("\\b\\S+도\\b")
        )

    // 시,군,구 추출
    private fun generatePatternSecond(): ArrayList<Regex> =
        arrayListOf(
            Regex("\\b\\S+시\\b"),
            Regex("\\b\\S+구\\b"),
            Regex("\\b\\S+군\\b")
        )

    // 읍,면,동 추출 - 구주소
    private fun generatePatternThird(): ArrayList<Regex> =
        arrayListOf(
            Regex("\\b\\S+읍\\b"),
            Regex("\\b\\S+면\\b"),
            Regex("\\b\\S+동\\b")
        )

    // 리 추출 - 구주소
    private fun generatePatternFourth(): ArrayList<Regex> =
        arrayListOf(Regex("\\b\\S+리\\b"))

    // 도로명 주소 추출 - 신주소
    private fun generatePatternRoad(): ArrayList<Regex> =
        arrayListOf(
            Regex("\\b\\S+로\\b"),
            Regex("\\b\\S+길\\b")
        )
}