package app.airsignal.weather.view.aseye.dao

import com.google.gson.annotations.SerializedName

class EyeDataModel {
    data class DeviceModel(
        @SerializedName("name")
        var name: String,
        @SerializedName("serial")
        val serial: String,
        @SerializedName("power")
        val power: Boolean?,
        @SerializedName("report")
        val report: Int?,
        @SerializedName("isAdd")
        val isAdd: Boolean
    )

    data class EyeReportModel(
        @SerializedName("title")
        val title: String,
        @SerializedName("content")
        val content: String
    )

    data class AirValueModel(
        @SerializedName("pmValid")
        val pmValid: Int,
        @SerializedName("pm2p5Value")
        val pm2p5Value: Float,
        @SerializedName("pm2p5Lvl")
        val pm2p5Lvl: Int,
        @SerializedName("pm2p5AQI")
        val pm2p5AQI: Float,
        @SerializedName("thValid")
        val thValid: Int,
        @SerializedName("tempValue")
        val tempValue: Float,
        @SerializedName("tempLvl")
        val tempLvl: Int,
        @SerializedName("humidValue")
        val humidValue: Float,
        @SerializedName("humidLvl")
        val humidLvl: Int,
        @SerializedName("coValid")
        val coValid: Int,
        @SerializedName("coValue")
        val coValue: Float,
        @SerializedName("coLvl")
        val coLvl: Int,
        @SerializedName("co2Valid")
        val co2Valid: Int,
        @SerializedName("co2Value")
        val co2Value: Float,
        @SerializedName("tvocValid")
        val tvocValid: Int,
        @SerializedName("co2Lvl")
        val co2Lvl: Int,
        @SerializedName("tvocValue")
        val tvocValue: Float,
        @SerializedName("tvocLvl")
        val tvocLvl: Int,
        @SerializedName("no2Valid")
        val no2Valid: Int,
        @SerializedName("no2Value")
        val no2Value: Float,
        @SerializedName("no2Lvl")
        val no2Lvl: Int,
        @SerializedName("lightValid")
        val lightValid: Int,
        @SerializedName("lightValue")
        val lightValue: Float,
        @SerializedName("lightLvl")
        val lightLvl: Int,
        @SerializedName("gyroValid")
        val gyroValid: Int,
        @SerializedName("gyroXValue")
        val gyroXValue: Float,
        @SerializedName("gyroYValue")
        val gyroYValue: Float,
        @SerializedName("gyroZValue")
        val gyroZValue: Float,
        @SerializedName("GYROLvl")
        val GYROLvl: Int,
        @SerializedName("CAIValue")
        val CAIValue: Int,
        @SerializedName("CAILvl")
        val CAILvl: Int
    )

    data class SettingModel(
        @SerializedName("fwVer")
        val fwVer: String,  // x.x.x.x
        @SerializedName("installTime")
        val installTime: Int,   // yyyyMMdd
        @SerializedName("MeasureChk")
        val measureChk: Long    // 0 or 1 or 2 or 4 or 8 or 16 or 32 or 64 or 128 or 256
    )

    data class LifecycleModel(
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
        val gyroInstallTime: Int,
    )

    data class DisplayModel(
        @SerializedName("displayMode")
        val displayMode: Int,   // 0 or 1
        @SerializedName("displayBrightness")
        val displayBrightness: Int  // percent
    )

    data class EarthquakeModel(
        @SerializedName("earthquakeEnabled")
        val earthquakeEnabled: Int  // 0 or 1
    )

    data class WifiModel(
        @SerializedName("ssid")
        val ssid: String,
        @SerializedName("password")
        val password: String
    )

    data class BleModel(
        @SerializedName("bleEnabled")
        val bleEnabled: Int,    // 0 or 1
        @SerializedName("bleDataReport")
        val bleDataReport: Int  // 10 ~ 3,600(sec)
    )

    data class LocationModel(
        @SerializedName("latitude")
        val latitude: Double,
        @SerializedName("longitude")
        val longitude: Double
    )

    data class AlarmModel(
        @SerializedName("alarmState")
        val alarmState: Int // 0 or 1
    )

    data class ReportModel(
        @SerializedName("reportTime")
        val reportTime: Int // 10 ~ 3,600(sec)
    )

    data class PowerModel(
        @SerializedName("power")
        val power: Int // 0 - off
    )

    data class RebootModel(
        @SerializedName("complete")
        val complete: Int // 0 or 1
    )

    data class StatusModel(
        @SerializedName("status")
        val status: Int // 0 or 1
    )
}