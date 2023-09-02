package com.android.sauravtask

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CSVManager {
    private val fileName = "location_data.csv"

    fun appendLocationData(latitude: Double, longitude: Double) {
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            val fileWriter = FileWriter(file, true)
            fileWriter.append("$latitude,$longitude\n")
            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}