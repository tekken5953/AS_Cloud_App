package app.airsignal.weather.as_eye.dao

import app.core_as_eye.dao.EyeDataModel

interface DataTransferImp {
    fun sendLifeData(data: EyeDataModel.Life)
    fun sendLiveData(data: EyeDataModel.Life)
    fun sendReportData(data: EyeDataModel.Interval)
}