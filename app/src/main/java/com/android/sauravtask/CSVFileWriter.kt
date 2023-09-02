package com.android.sauravtask

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object CSVFileWriter {
    private const val CSV_DIRECTORY_NAME = "LocationData"

    fun writeToCSV(context: Context, latitude: Double, longitude: Double) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "location_$timestamp.csv"

        try {
            val filePath = "${Environment.getExternalStorageDirectory()}/Download/$fileName"
            val file = File(filePath)
            val fileExists = file.exists()
            val fileWriter = FileWriter(file, fileExists)
           // val rootDirectory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), CSV_DIRECTORY_NAME)

           // if (!rootDirectory.exists()) {
           //     rootDirectory.mkdirs()
           // }

           // val file = File(rootDirectory, fileName)
            //val fileWriter = FileWriter(file, true) // Append to existing file

            // Write location data to CSV file
            fileWriter.append("$latitude, $longitude\n")

            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}