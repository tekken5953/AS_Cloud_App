package app.airsignal.core_network.retrofit

import com.google.gson.annotations.SerializedName

class ApiModel {

    /**
     * 앱 버전 모델
     * @param serviceName 앱의 테스트 버전 네임
     * @param serviceCode 앱의 테스트 버전 코드
     * @param date 앱 배포 날짜
     * @param releaseName 앱의 배포 버전 네임
     * @param releaseCode 앱의 배포 버전 코드
     */
    data class AppVersion(
        @SerializedName("versionName")
        val serviceName: String,
        @SerializedName("versionCode")
        val serviceCode: String,
        @SerializedName("releaseDate")
        val date: String,
        @SerializedName("testName")
        val releaseName: String,
        @SerializedName("testCode")
        val releaseCode: String,
        @SerializedName("inAppMsg")
        val inAppMsg: List<String>?
    )

    /**
     * 메타 데이터 모델
     * @param address 앱의 테스트 버전 네임
     * @param address1 앱의 테스트 버전 코드
     * @param address2 앱 배포 날짜
     * @param address3 앱의 배포 버전 네임
     */
    data class MetaData(
        @SerializedName("address")
        val address: String?,
        @SerializedName("address1")
        val address1: String?,
        @SerializedName("address2")
        val address2: String?,
        @SerializedName("address3")
        val address3: String?
    )

    // 시간별 날씨 데이터 모델
    data class RealTimeData(
        @SerializedName("base")
        val base: String?,
        @SerializedName("forecast")
        val forecast: String?,
        @SerializedName("rainProbability")
        val rainP: Double?,
        @SerializedName("rainType")
        val rainType: String = "없음",
        @SerializedName("humidity")
        val humid: Double = 0.0,
        @SerializedName("snowHourly")
        val snow: Double = 0.0,
        @SerializedName("sky")
        val sky: String?,
        @SerializedName("temperature")
        val temp: Double = 0.0,
        @SerializedName("windSpeedEW")
        val windSpeedEW: Double?,
        @SerializedName("windSpeedSN")
        val windSpeedSN: Double?,
        @SerializedName("wave")
        val wave: Double?,
        @SerializedName("vector")
        val vector: String?,
        @SerializedName("windSpeed")
        val windSpeed: Double = 0.0
    )

    // 주간별 날씨 데이터 모델
    data class WeeklyData(
//        @SerializedName("today")
//        val today: LocalDateTime,
        @SerializedName("rainDate")
        val rainDate: String?,
        @SerializedName("tempDate")
        val tempDate: String?,
        @SerializedName("rnSt0Am")
        val rnSt0Am: Double?,
        @SerializedName("rnSt0Pm")
        val rnSt0Pm: Double?,
        @SerializedName("rnSt1Am")
        val rnSt1Am: Double?,
        @SerializedName("rnSt1Pm")
        val rnSt1Pm: Double?,
        @SerializedName("rnSt2Am")
        val rnSt2Am: Double?,
        @SerializedName("rnSt2Pm")
        val rnSt2Pm: Double?,
        @SerializedName("rnSt3Am")
        val rnSt3Am: Double?,
        @SerializedName("rnSt3Pm")
        val rnSt3Pm: Double?,
        @SerializedName("rnSt4Am")
        val rnSt4Am: Double?,
        @SerializedName("rnSt4Pm")
        val rnSt4Pm: Double?,
        @SerializedName("rnSt5Am")
        val rnSt5Am: Double?,
        @SerializedName("rnSt5Pm")
        val rnSt5Pm: Double?,
        @SerializedName("rnSt6Am")
        val rnSt6Am: Double?,
        @SerializedName("rnSt6Pm")
        val rnSt6Pm: Double?,
        @SerializedName("rnSt7Am")
        val rnSt7Am: Double?,
        @SerializedName("wf0Am")
        val wf0Am: String?,
        @SerializedName("wf0Pm")
        val wf0Pm: String?,
        @SerializedName("wf1Am")
        val wf1Am: String?,
        @SerializedName("wf1Pm")
        val wf1Pm: String?,
        @SerializedName("wf2Am")
        val wf2Am: String?,
        @SerializedName("wf2Pm")
        val wf2Pm: String?,
        @SerializedName("wf3Am")
        val wf3Am: String?,
        @SerializedName("wf3Pm")
        val wf3Pm: String?,
        @SerializedName("wf4Am")
        val wf4Am: String?,
        @SerializedName("wf4Pm")
        val wf4Pm: String?,
        @SerializedName("wf5Am")
        val wf5Am: String?,
        @SerializedName("wf5Pm")
        val wf5Pm: String?,
        @SerializedName("wf6Am")
        val wf6Am: String?,
        @SerializedName("wf6Pm")
        val wf6Pm: String?,
        @SerializedName("wf7Am")
        val wf7Am: String?,
        @SerializedName("wf7Pm")
        val wf7Pm: String?,
        @SerializedName("taMin0")
        val taMin0: Double?,
        @SerializedName("taMax0")
        val taMax0: Double?,
        @SerializedName("taMin1")
        val taMin1: Double?,
        @SerializedName("taMax1")
        val taMax1: Double?,
        @SerializedName("taMin2")
        val taMin2: Double?,
        @SerializedName("taMax2")
        val taMax2: Double?,
        @SerializedName("taMin3")
        val taMin3: Double?,
        @SerializedName("taMax3")
        val taMax3: Double?,
        @SerializedName("taMin4")
        val taMin4: Double?,
        @SerializedName("taMax4")
        val taMax4: Double?,
        @SerializedName("taMin5")
        val taMin5: Double?,
        @SerializedName("taMax5")
        val taMax5: Double?,
        @SerializedName("taMin6")
        val taMin6: Double?,
        @SerializedName("taMax6")
        val taMax6: Double?,
        @SerializedName("taMin7")
        val taMin7: Double?,
        @SerializedName("taMax7")
        val taMax7: Double?
    )

    // 외부 공기질 데이터 모델
    data class AirQualityData(
        @SerializedName("pm25Grade1h")
        val pm25Grade1h: Int?,
        @SerializedName("pm10Value24")
        val pm10Value24: Double?,
        @SerializedName("pm10Grade1h")
        val pm10Grade1h: Int?,
        @SerializedName("pm25Value24")
        val pm25Value24: Int?,
        @SerializedName("dataTime")
        val dateTime: String?,
        @SerializedName("so2Grade")
        val so2Grade: Int?,
        @SerializedName("so2Value")
        val so2Value: Double?,
        @SerializedName("coGrade")
        val coGrade: Int?,
        @SerializedName("coValue")
        val coValue: Double?,
        @SerializedName("khaiGrade")
        val khaiGrade: Int?,
        @SerializedName("khaiValue")
        val khaiValue: Int?,
        @SerializedName("mangName")
        val mangName: String?,
        @SerializedName("no2Grade")
        val no2Grade: Int?,
        @SerializedName("no2Value")
        val no2Value: Double?,
        @SerializedName("o3Grade")
        val o3Grade: Int?,
        @SerializedName("o3Value")
        val o3Value: Double?,
        @SerializedName("pm10Grade")
        val pm10Grade: Int?,
        @SerializedName("pm10Value")
        val pm10Value: Double?,
        @SerializedName("pm25Grade")
        val pm25Grade: Int?,
        @SerializedName("pm25Value")
        val pm25Value: Int?,
        @SerializedName("sidoName")
        val sidoName: String?,
        @SerializedName("stationName")
        val stationName: String?,
        @SerializedName("stationDetail")
        val stationDetail: StationDetailInner
    )

    // 스테이션 정보 모델
    data class StationDetailInner(
        @SerializedName("item")
        val stationDetailItem: String?,
        @SerializedName("year")
        val stationDetailYear: String?,
        @SerializedName("addr")
        val stationDetailAddr: String?
    )

    // 일출/일몰 데이터 모델
    data class SunData(
        @SerializedName("locdate")
        val locdate: String?,
        @SerializedName("moonrise")
        val moonrise: String?,
        @SerializedName("moonset")
        val moonset: String?,
        @SerializedName("sunrise")
        val sunrise: String?,
        @SerializedName("sunset")
        val sunset: String?,
    )

    // 내일 일출/일몰 데이터 모델
    data class SunTomorrow(
        @SerializedName("locdate")
        val locdate: String?,
        @SerializedName("moonrise")
        val moonrise: String?,
        @SerializedName("moonset")
        val moonset: String?,
        @SerializedName("sunrise")
        val sunrise: String?,
        @SerializedName("sunset")
        val sunset: String?,
    )

    // 오늘 최저/최고 기온 데이터 모델
    data class TodayTemp(
        @SerializedName("min")
        val min: Double?,
        @SerializedName("max")
        val max: Double?
    )

    // 어제 기온 데이터 모델
    data class YesterdayTemp(
        @SerializedName("temperature")
        val temp: Double?
    )

    // 자외선 데이터 모델
    data class UV(
        @SerializedName("value")
        val value: Int?,
        @SerializedName("flag")
        val flag: String?
    )

    // 실시간 정보 데이터 모델
    data class Current(
        @SerializedName("forecast")
        val currentTime: String,
        @SerializedName("rainType")
        var rainType: String,
        @SerializedName("rainHourly")
        val rainHourly: Double,
        @SerializedName("humidity")
        var humidity: Double,
        @SerializedName("temperature")
        var temperature: Double,
        @SerializedName("vector")
        val vector: String?,
        @SerializedName("windSpeed")
        var windSpeed: Double,
    )

    // 전체 데이터 모델
    data class GetEntireData(
        @SerializedName("metadata")
        val meta: MetaData,
        @SerializedName("realtime")
        val realtime: List<RealTimeData>,
        @SerializedName("week")
        val week: WeeklyData,
        @SerializedName("quality")
        val quality: AirQualityData,
        @SerializedName("sun")
        val sun: SunData,
        @SerializedName("sun2")
        val sun_tomorrow: SunTomorrow?,
        @SerializedName("today")
        val today: TodayTemp?,
        @SerializedName("yesterday")
        val yesterday: YesterdayTemp,
        @SerializedName("uv")
        val uv: UV?,
        @SerializedName("current")
        val current: Current,
        @SerializedName("thunder")
        val thunder: Double?,
        @SerializedName("summary")
        val summary: List<String>?,
        @SerializedName("term24")
        val term24: String?,
        @SerializedName("lunar")
        val lunar: LunarDate?
    )

    // 4x2 위젯 - 실시간 정보
    data class Widget4x2Current(
        @SerializedName("forecast")
        val currentTime: String,
        @SerializedName("rainType")
        val rainType: String?,
        @SerializedName("temperature")
        val temperature: Double?,
        @SerializedName("humidity")
        var humidity: Double
    )

    // 4x2 위젯 - 일출/일몰
    data class Widget4x2Sun(
        @SerializedName("sunrise")
        val sunrise: String?,
        @SerializedName("sunset")
        val sunset: String?
    )

    // 4x2 위젯 - 시간별 데이터
    data class Widget4x2Realtime(
        @SerializedName("forecast")
        val forecast: String?,
        @SerializedName("sky")
        val sky: String?,
        @SerializedName("rainProbability")
        val rainP: Double?,
        @SerializedName("rainType")
        val rainType: String?,
        @SerializedName("temperature")
        val temp: Double?,

        )

    // 4x2 위젯 - 전체 데이터
    data class WidgetData(
        @SerializedName("quality")
        val quality: AirQualityData,
        @SerializedName("current")
        val current: Widget4x2Current,
        @SerializedName("sun")
        val sun: Widget4x2Sun,
        @SerializedName("thunder")
        val thunder: Double?,
        @SerializedName("realtime")
        val realtime: List<Widget4x2Realtime>,
        @SerializedName("today")
        val today: TodayTemp,
        @SerializedName("lunar")
        val lunar: LunarDate?
    )

    // 기상 특보 검색
    data class BroadCastWeather(
        @SerializedName("region")
        val region: String,
        @SerializedName("content")
        val content: List<String>?
    )

    data class LunarDate(
        @SerializedName("lunYear")
        val year: Int,
        @SerializedName("lunMonth")
        val month: Int,
        @SerializedName("lunDay")
        val date: Int
    )

    // 공지사항 리스트
    data class NoticeItem(
        @SerializedName("category")
        val category: String?,
        @SerializedName("created")
        val created: String,
        @SerializedName("modified")
        val modified: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("content")
        val content: String
    )
}