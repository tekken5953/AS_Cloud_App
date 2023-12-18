package app.airsignal.weather.view.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import app.address.AddressFromRegex
import app.airsignal.weather.koin.BaseApplication
import app.location.GetLocation
import com.google.android.gms.location.*
import com.google.android.gms.location.GeofencingClient

class GeofenceManager(private val context: Context) {
    companion object {
        private const val TAG = "GeofenceJobService"
        private const val requestId = "request_id_geofence"
    }
    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(BaseApplication.appContext)
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(): Location? {
        val location = GetLocation(context).getForegroundLocation()
        location?.let {
            val simpleAddr = getSimpleAddress(location.latitude,location.longitude)
            val geofence = Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(
                    location.latitude,
                    location.longitude,
                    100f  // 예시로 반경을 100m로 설정
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(context, BaseWidgetProvider::class.java)
            val pendingIntent = PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            ContextCompat.startForegroundService(context, intent)

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    // Geofence 추가 성공
                    Log.d(TAG,"Success to add geofence : ${location.latitude}${location.longitude},$simpleAddr")
                }
                .addOnFailureListener {
                    // Geofence 추가 실패
                    Log.w(TAG,"Failed to add geofence")
                    removeGeofence(requestId)
                }

            return location
        }
        return null
    }

    private fun removeGeofence(requestId: String) {
        geofencingClient.removeGeofences(listOf(requestId))
            .addOnSuccessListener {
                // Geofence 제거 성공
                Log.d(TAG,"Success to remove geofence")
            }
            .addOnFailureListener {
                // Geofence 제거 실패
                Log.w(TAG,"Failed to remove geofence")
            }
    }

    fun getSimpleAddress(lat: Double, lng: Double): String {
        val addr = GetLocation(context).getAddress(lat, lng)
        return getWidgetAddress(addr)
    }

    fun getWidgetAddress(addr: String): String {
        val result = AddressFromRegex(addr).getNotificationAddress()
        return if (result == "") AddressFromRegex(addr).getSecondAddress() else result
    }
}