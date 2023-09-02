package com.android.sauravtask




import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.sauravtask.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var isLocationTrackingEnabled = false
    private lateinit var csvManager: CSVManager
    // Permission request contract
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, you can now access the location
                // Your location-related code here
            } else {
                // Permission denied, handle this case (e.g., show a message to the user)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        csvManager = CSVManager()
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(2)
            fastestInterval = TimeUnit.SECONDS.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // Check if location permissions are granted
        if (hasLocationPermission()) {
            // Permission already granted, you can access the location

        } else {
            // Request location permissions
            requestLocationPermission()
        }


        binding.Image.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            //val intent = Intent(this, ImagePreviewActivity::class.java)
            startActivity(intent)
        }
        binding.Switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Start the foreground service


                val serviceIntent = Intent(this@MainActivity, LocationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                }
                else{
                    val serviceIntent = Intent(this, LocationService::class.java)
                    stopService(serviceIntent)
                }
                someFunctionThatCallsHandleLocationUpdate()
            } else {
                // Stop the foreground service
                val serviceIntent = Intent(this, LocationService::class.java)
                stopService(serviceIntent)
            }
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startLocationTracking()
                }
            } else {
                stopLocationTracking()
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    private fun someFunctionThatCallsHandleLocationUpdate() {
        val latitude = 23.00 // obtain latitude
        val longitude = 25.00// obtain longitude

            // Check if the app has location permissions
            if (checkLocationPermission()) {
                // Call handleLocationUpdate and pass the context
                handleLocationUpdate(this, latitude, longitude)
            } else {
                // You don't have permission, request it
                requestLocationPermission()
            }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Explain to the user why you need location permissions if needed
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun handleLocationUpdate(context: Context, latitude: Double, longitude: Double) {
        // Update the notification
        updateNotification(latitude, longitude)

        // Store location data in a CSV file
        CSVFileWriter.writeToCSV(context, latitude, longitude)
    }

    private fun updateNotification(latitude: Double, longitude: Double) {
        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Live Location")
            .setContentText("Latitude: $latitude, Longitude: $longitude")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()


        //startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    // Check if the app has location permissions
    private fun checkLocationPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permissions
    /*private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(this, arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE)
    }*/

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can now use location
                    someFunctionThatCallsHandleLocationUpdate()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            // Handle other permission requests if needed
        }
    }

    //
    private fun startLocationFetching() {
        if (checkLocationPermission()) {
            // You have location permission, start location fetching
        } else {
            // You don't have permission, request it
           requestLocationPermission()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationTracking() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationTrackingEnabled = true
            startForegroundService(Intent(this, LocationService::class.java))
           // switchLocation.text = "Location Tracking (ON)"
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun stopLocationTracking() {
        isLocationTrackingEnabled = false
        stopService(Intent(this, LocationService::class.java))
        //switchLocation.text = "Location Tracking (OFF)"
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}