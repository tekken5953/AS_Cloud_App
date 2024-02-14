package app.airsignal.weather.as_eye.dao

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

class EyeDataModel {
    data class Device(
        @SerializedName("created_at")
        val created_at: LocalDateTime,
        @SerializedName("is_master")
        val isMaster: Boolean,
        @SerializedName("email")
        var email: String,
        @SerializedName("alias")
        val alias: String,
        @SerializedName("serial")
        val serial: Serial
    )

    data class Entire(
        @SerializedName("current")
        val current: Measured?,
        val average: List<Average>?
    )

    data class ReportFragment(
        val report: List<String>?,
        val caiValue: Int,
        val caiLvl: Int,
        val virusValue: Int,
        val virusLvl: Int,
        val pm10Value: Float,
        val pm10p0List: List<Average>?
    )

    data class Life(
        val nameEn: String,
        val nameKr: String,
        val value: Int,
        val pbColor: Int,
        val backColor: Int
    )

    data class Serial(
        @SerializedName("last_modify")
        val lastModify: LocalDateTime,
        @SerializedName("serial")
        val serial: String,
        @SerializedName("report")
        val report: Boolean,
        @SerializedName("power")
        val power: Boolean
    )

    data class EyeReportAdapter(
        val title: String,
        val content: String,
        val isCaution: Boolean
    )

    data class Measured(
        @SerializedName("date")
        val date: String?,
        @SerializedName("pm2p5Value")
        val pm2p5Value: Float,
        @SerializedName("pm2p5Lvl")
        val pm2p5Lvl: Int,
        @SerializedName("pm2p5AQI")
        val pm2p5AQI: Float,
        @SerializedName("pm10p0Value")
        val pm10p0Value: Float,
        @SerializedName("pm10p0Lvl")
        val pm10p0Lvl: Int,
        @SerializedName("tempValue")
        val tempValue: Float,
        @SerializedName("tempLvl")
        val tempLvl: Int,
        @SerializedName("humidValue")
        val humidValue: Float,
        @SerializedName("humidLvl")
        val humidLvl: Int,
        @SerializedName("coValue")
        val coValue: Float,
        @SerializedName("coLvl")
        val coLvl: Int,
        @SerializedName("co2Value")
        val co2Value: Float,
        @SerializedName("co2Lvl")
        val co2Lvl: Int,
        @SerializedName("tvocValue")
        val tvocValue: Float,
        @SerializedName("tvocLvl")
        val tvocLvl: Int,
        @SerializedName("no2Value")
        val no2Value: Float,
        @SerializedName("no2Lvl")
        val no2Lvl: Int,
        @SerializedName("lightValue")
        val lightValue: Float,
        @SerializedName("lightLvl")
        val lightLvl: Int,
        @SerializedName("gyroXValue")
        val gyroXValue: Float,
        @SerializedName("gyroYValue")
        val gyroYValue: Float,
        @SerializedName("gyroZValue")
        val gyroZValue: Float,
        @SerializedName("gyroValid")
        val gyroValid: Int,
        @SerializedName("caiValue")
        val CAIValue: Int,
        @SerializedName("caiLvl")
        val CAILvl: Int,
        @SerializedName("noiseValid")
        val noiseValid: Int,
        @SerializedName("noiseValue")
        val noiseValue: Float,
        @SerializedName("virusValue")
        val virusValue: Int,
        @SerializedName("virusLvl")
        val virusLvl: Int,
        @SerializedName("anomalies")
        val flags: List<String>?
    )

    data class Interval(
        @SerializedName("reportTime")
        val interval: Int // 10 ~ 3,600(sec)
    )

    data class Status(
        @SerializedName("status")
        val status: Int // 0 or 1
    )

    data class Category(
        val name: String,
        val device: MutableList<Device>
    )

    data class Group(
        var isChecked: Boolean,
        val device: Device
    )

    data class Wifi(
        val ssid: String,
        val level: Int?,
        val capability: String?
    )

    data class Average (
        @SerializedName("device")
        val deviceSerial: String?,
        @SerializedName("date")
        val date: LocalDateTime?,
        @SerializedName("pm10p0Value")
        val pm10p0Value: Double?
    )
}