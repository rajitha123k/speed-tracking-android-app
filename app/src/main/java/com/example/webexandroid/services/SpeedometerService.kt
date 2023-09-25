package com.example.webexandroid.services

import android.Manifest
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.webexandroid.SpeedHandler
import com.example.webexandroid.R
import com.example.webexandroid.search.SearchActivity3
import java.util.concurrent.TimeUnit

class SpeedometerService : Service(), LocationListener {

    companion object {
        private const val CHANNEL_ID = "SpeedometerServiceChannel"
    }

    lateinit var manager: LocationManager
    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val i = Intent(this, SearchActivity3::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Speedometer Service")
            .setContentText("Tracking speedometer data...")
            .setSmallIcon(R.drawable.speed_icon)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(23423, notification)
        requestLocationChange()
        return START_STICKY
    }

    private fun requestLocationChange() {
        Log.d("SpeedometerService", "checking permission")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("SpeedometerService", "Permission not granted")
            //  stopSelf()
        } else {
            Log.d("SpeedometerService", "permission granted, requesting location updates")
            manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                TimeUnit.SECONDS.toMillis(1),
                10f,
                this
            )
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Speedometer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

    }

    override fun onLocationChanged(location: Location) {
        val speed = convertSpeedMph(location.speed.toDouble())
        Log.d("SpeedometerService", "Speed: $speed mph")
        if (speed > 10) {
            val i = Intent(this, SearchActivity3::class.java)
            i.putExtra("speed", true)
            i.addFlags(FLAG_ACTIVITY_NEW_TASK)
            if (!SpeedHandler.isActivityRunning()) {
                startActivity(i)
            }
            SpeedHandler.updateSpeed(true)
            Log.d("SpeedometerService", "Speed is greater than 7 mph")
        } else {
            SpeedHandler.updateSpeed(false)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("Not yet implemented")
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("Not yet implemented")
    }

    private fun convertSpeedMph(speed: Double): Double {
        return speed * 2.23694
    }
}