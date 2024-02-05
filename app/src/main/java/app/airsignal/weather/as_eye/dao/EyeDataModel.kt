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

    data class EntireData(
        @SerializedName("last_modify")
        val lastModify: LocalDateTime,
        @SerializedName("measured")
        val measured: Measured,
        @SerializedName("report")
        val report: List<String>
    )

    data class ReportFragment(
        val report: List<String>?,
        val caiValue: Int,
        val caiLvl: Int,
        val virusValue: Int,
        val virusLvl: Int,
        val pm10Value: Float
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

    data class Setting(
        @SerializedName("fwVer")
        val fwVer: String,  // x.x.x.x
        @SerializedName("installTime")
        val installTime: Int,   // yyyyMMdd
        @SerializedName("MeasureChk")
        val measureChk: Long    // 0 or 1 or 2 or 4 or 8 or 16 or 32 or 64 or 128 or 256
    )

    data class Lifecycle(
        @SerializedName("pm2p5LifeRemain")
        val pm2p5LifeRemain: Int,
        @SerializedName("pm2p5LifePercent")
        val pm2p5LifePercent: Int,
        @SerializedName("pm2p5InstallTime")
        val pm2p5InstallTime: Int,
        @SerializedName("thLifeRemain")
        val thLifeRemain: Int,
        @SerializedName("thLifePercent")
        val thLifePercent: Int,
        @SerializedName("thInstallTime")
        val thInstallTime: Int,
        @SerializedName("co2LifeRemain")
        val co2LifeRemain: Int,
        @SerializedName("co2LifePercent")
        val co2LifePercent: Int,
        @SerializedName("co2InstallTime")
        val co2InstallTime: Int,
        @SerializedName("coLifeRemain")
        val coLifeRemain: Int,
        @SerializedName("coLifePercent")
        val coLifePercent: Int,
        @SerializedName("coInstallTime")
        val coInstallTime: Int,
        @SerializedName("tvocLifeRemain")
        val tvocLifeRemain: Int,
        @SerializedName("tvocLifePercent")
        val tvocLifePercent: Int,
        @SerializedName("tvocInstallTime")
        val tvocInstallTime: Int,
        @SerializedName("no2LifeRemain")
        val no2LifeRemain: Int,
        @SerializedName("no2LifePercent")
        val no2LifePercent: Int,
        @SerializedName("no2InstallTime")
        val no2InstallTime: Int,
        @SerializedName("lightLifeRemain")
        val lightLifeRemain: Int,
        @SerializedName("lightLifePercent")
        val lightLifePercent: Int,
        @SerializedName("lightInstallTime")
        val lightInstallTime: Int,
        @SerializedName("noiseLifeRemain")
        val noiseLifeRemain: Int,
        @SerializedName("noiseLifePercent")
        val noiseLifePercent: Int,
        @SerializedName("noiseInstallTime")
        val noiseInstallTime: Int,
        @SerializedName("gyroLifeRemain")
        val gyroLifeRemain: Int,
        @SerializedName("gyroLifePercent")
        val gyroLifePercent: Int,
        @SerializedName("gyroInstallTime")
        val gyroInstallTime: Int
    )

    data class Display(
        @SerializedName("displayMode")
        val displayMode: Int,   // 0 or 1
        @SerializedName("displayBrightness")
        val displayBrightness: Int  // percent
    )

    data class Earthquake(
        @SerializedName("earthquakeEnabled")
        val earthquakeEnabled: Int  // 0 or 1
    )

    data class Ble(
        @SerializedName("bleEnabled")
        val bleEnabled: Int,    // 0 or 1
        @SerializedName("bleDataReport")
        val bleDataReport: Int  // 10 ~ 3,600(sec)
    )

    data class Location(
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double
    )

    data class Alarm(
        @SerializedName("alarmState")
        val alarmState: Int // 0 or 1
    )

    data class Interval(
        @SerializedName("reportTime")
        val interval: Int // 10 ~ 3,600(sec)
    )

    data class Power(
        @SerializedName("power")
        val power: Int // 0 - off
    )

    data class Reboot(
        @SerializedName("complete")
        val complete: Int // 0 or 1
    )

    data class Status(
        @SerializedName("status")
        val status: Int // 0 or 1
    )

    data class Category(
        val name: String,
        val device: List<Device>?
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
}