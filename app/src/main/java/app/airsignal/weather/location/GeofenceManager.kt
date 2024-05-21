package app.airsignal.weather.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.content.ContextCompat
import app.airsignal.weather.view.widget.BaseWidgetProvider
import com.google.android.gms.location.*

class GeofenceManager(private val context: Context) {
    private val requestId = "request_id_geofence"

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(): Location? {
        val location = GetLocation(context).getForegroundLocation()
        location?.let {
            val geofence = Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(location.latitude, location.longitude, 100f)
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
                .addOnFailureListener { removeGeofence() }

            return location
        }
        return null
    }

    private fun removeGeofence() {
        geofencingClient.removeGeofences(listOf(requestId))
    }

    fun getSimpleAddress(lat: Double, lng: Double): String {
        val addr = GetLocation(context).getAddress(lat, lng)
        return getWidgetAddress(addr)
    }

    private fun getWidgetAddress(addr: String): String {
        val result = AddressFromRegex(addr).getNotificationAddress()
        return if (result == "") AddressFromRegex(addr).getSecondAddress() else result
    }
}