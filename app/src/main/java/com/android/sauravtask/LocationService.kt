package com.android.sauravtask

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.TimeUnit


class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    var FOREGROUND_SERVICE_ID = 1
    private val NOTIFICATION_ID = 1
    private val notificationId = 1
    private lateinit var notificationManager: NotificationManager
    private lateinit var csvManager: CSVManager
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var csvWriter: BufferedWriter? = null
    private lateinit var csvFile: File
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        csvManager = CSVManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationRequest = LocationRequest().apply {
            // Sets the desired interval for
            // active location updates.
            // This interval is inexact.
            interval = TimeUnit.SECONDS.toMillis(60)

            // Sets the fastest rate for active location updates.
            // This interval is exact, and your application will never
            // receive updates more frequently than this value
            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            // Sets the maximum time when batched location
            // updates are delivered. Updates may be
            // delivered sooner than this interval
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val currentTimeMillis = System.currentTimeMillis()
        val csvFileName = "location_data_$currentTimeMillis.csv"

        // Create a new CSV file in the app's internal storage directory
        val directory = File(filesDir, "location_data")
        directory.mkdirs()
        csvFile = File(directory, csvFileName)

        try {
            // Open the CSV file for writing
            csvWriter = BufferedWriter(FileWriter(csvFile, true))

            // Write a header row if the file is empty
            if (csvFile.length() == 0L) {
                csvWriter?.write("Latitude,Longitude,Time\n")
            }
        // Start fetching location updates here.
        // Use a handler to fetch location updates every 2 seconds and update the notification.
        startForeground(notificationId, createNotification())
        startLocationUpdates()
    } catch (e: IOException) {
        // Handle file I/O errors here
        e.printStackTrace()
    }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("RemoteViewLayout")
    private fun createNotification(): Notification {
        // Create a RemoteViews for the custom notification layout
        val contentView = RemoteViews(packageName, R.layout.custom_notification_layout)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContent(contentView)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Create a notification channel (required for Android Oreo and later)
        createNotificationChannel()

        return notification
    }

    private fun createNotificationChannel() {
        // Create a notification channel (required for Android Oreo and later)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest().apply {
            interval = 2000 // Location update interval in milliseconds (2 seconds)
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.lastLocation?.let {

                   var latitude = currentLocation?.latitude
                    var longitude = currentLocation?.longitude
                    // Update the notification with the latest location
                    latitude?.let { it1 -> longitude?.let { it2 -> updateNotification(it1, it2) } }
                    //CSVFileWriter.writeToCSV(context , it.latitude, it.longitude)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateNotification(latitude: Double, longitude: Double) {
        // Update the custom notification layout with the current location
        val contentView = RemoteViews(packageName, R.layout.custom_notification_layout)
        contentView.setTextViewText(R.id.textLocation, "Lat: $latitude, Long: $longitude")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, createNotification())

        try {
            csvWriter?.write("$latitude,$longitude,${System.currentTimeMillis()}\n")
            csvWriter?.flush()
        } catch (e: IOException) {
            // Handle file I/O errors here
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        // Close the CSV file when the service is destroyed
        try {
            csvWriter?.close()
        } catch (e: IOException) {
            // Handle file I/O errors here
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID = "LocationForegroundServiceChannel"
    }

    /*// Inside your location update code (within onStartCommand or wherever you fetch location updates)
    private fun handleLocationUpdate(latitude: Double, longitude: Double) {
        // Update the notification
        updateNotification(latitude, longitude)

        // Store location data in a CSV file
        CSVFileWriter.writeToCSV(this, latitude, longitude)
    }

    private fun updateNotification(latitude: Double, longitude: Double) {
        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Live Location")
            .setContentText("Latitude: $latitude, Longitude: $longitude")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()


        startForeground(FOREGROUND_SERVICE_ID, notification)
    }*/
}