package com.floodsense.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

object DeviceLocation {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasPermission(context: Context): Boolean =
        permissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    fun isEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun lastKnown(context: Context): Location? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        return providers.mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    fun requestCurrent(
        context: Context,
        timeoutMs: Long = 12_000L,
        onResult: (Location?) -> Unit
    ) {
        if (!hasPermission(context)) {
            onResult(null)
            return
        }
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            onResult(null)
            return
        }
        val recent = lastKnown(context)?.takeIf { System.currentTimeMillis() - it.time < 60_000L }
        if (recent != null) {
            onResult(recent)
            return
        }
        val finished = AtomicBoolean(false)
        val handler = Handler(Looper.getMainLooper())
        lateinit var listener: LocationListener
        lateinit var timeout: Runnable
        fun finish(location: Location?) {
            if (!finished.compareAndSet(false, true)) return
            handler.removeCallbacks(timeout)
            runCatching { manager.removeUpdates(listener) }
            onResult(location)
        }
        listener = LocationListener { location -> finish(location) }
        timeout = Runnable { finish(lastKnown(context)) }
        handler.postDelayed(timeout, timeoutMs)
        val started = runCatching {
            var any = false
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
                any = true
            }
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
                any = true
            }
            any
        }.getOrDefault(false)
        if (!started) {
            handler.removeCallbacks(timeout)
            finish(lastKnown(context))
        }
    }
}
