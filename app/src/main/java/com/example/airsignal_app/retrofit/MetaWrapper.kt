package com.example.airsignal_app.retrofit

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * @author : Lee Jae Young
 * @since : 2023-04-05 오후 4:30
 **/
@Parcelize
data class MetaWrapper<T>(
    @SerializedName("_id")
    val id: String,
    @SerializedName("tempCode")
    val tempCode: String,
    @SerializedName("midCode")
    val midCode: String,
    @SerializedName("gridX")
    val gridX: Int,
    @SerializedName("gridY")
    val gridY: Int,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("sunName")
    val sunName: String,
    @SerializedName("location")
    val location: InnerObject,
    @SerializedName("data")
    val data: @RawValue T? = null
)

data class InnerObject(
    @SerializedName("type")
    val locationType: String,
    @SerializedName("coordinates")
    val locationCoordinates: Map<Double,Double>
)