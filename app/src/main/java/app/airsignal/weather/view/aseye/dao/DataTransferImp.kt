package app.airsignal.weather.view.aseye.dao

interface DataTransferImp {
    fun sendLifeData(data: EyeDataModel.LifeModel)
    fun sendLiveData(data: EyeDataModel.LifeModel)
    fun sendReportData(data: EyeDataModel.ReportModel)
}