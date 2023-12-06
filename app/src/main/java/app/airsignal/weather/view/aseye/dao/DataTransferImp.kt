package app.airsignal.weather.view.aseye.dao

interface DataTransferImp {
    fun sendLifeData(data: EyeDataModel.Life)
    fun sendLiveData(data: EyeDataModel.Life)
    fun sendReportData(data: EyeDataModel.Interval)
}