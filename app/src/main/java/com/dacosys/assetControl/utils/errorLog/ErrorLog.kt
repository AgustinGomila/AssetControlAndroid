package com.dacosys.assetControl.utils.errorLog

import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.misc.DateUtils.formatDateToString
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ErrorLog {
    companion object : ActivityCompat.OnRequestPermissionsResultCallback {
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) {
            when (requestCode) {
                REQUEST_EXTERNAL_STORAGE -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        reallyWriteLog()
                    }
                }
            }
        }

        var errorLogPath =
            "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}${Statics.ERROR_LOG_PATH}"

        private const val REQUEST_EXTERNAL_STORAGE = 1777

        private fun getFileName(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val date = sdf.parse(sdf.format(Date())) ?: return "2001-01-01"

            sdf.timeZone = TimeZone.getDefault()
            return sdf.format(date).toString().replace("/", "-") + ".log"
        }

        private var tClassName: String = ""
        private var tMsg: String = ""
        fun writeLog(activity: FragmentActivity? = null, className: String, ex: Exception) {
            if (activity == null) return

            val errors = StringWriter()
            ex.printStackTrace(PrintWriter(errors))
            tMsg = errors.toString()
            tClassName = className
            Log.e(tClassName, tMsg)

            if (!prefsGetBoolean(Preference.registryError)) return

            reallyWriteLog()
        }

        fun writeLog(activity: FragmentActivity? = null, className: String, msg: String) {
            if (activity == null) return

            tMsg = msg
            tClassName = className
            Log.e(tClassName, tMsg)

            if (!prefsGetBoolean(Preference.registryError)) return

            reallyWriteLog()
        }

        fun writeLog(activity: AppCompatActivity? = null, className: String, msg: String) {
            if (activity == null) return

            tMsg = msg
            tClassName = className
            Log.e(tClassName, tMsg)

            if (!prefsGetBoolean(Preference.registryError)) return

            reallyWriteLog()
        }

        fun writeLog(activity: AppCompatActivity? = null, className: String, ex: Exception) {
            if (activity == null) return

            val errors = StringWriter()
            ex.printStackTrace(PrintWriter(errors))
            tMsg = errors.toString()
            tClassName = className
            Log.e(tClassName, tMsg)

            if (!prefsGetBoolean(Preference.registryError)) return

            reallyWriteLog()
        }

        private fun reallyWriteLog() {
            val logFileName = getFileName()
            val logPath = errorLogPath

            val logFile = File("$logPath/$logFileName")
            val currentDate = formatDateToString(Date())

            val parent = logFile.parentFile
            parent?.mkdirs()

            try {
                val fOut = FileOutputStream(logFile, true)
                val outWriter = OutputStreamWriter(fOut)
                outWriter.append("\r$currentDate - $tClassName: $tMsg")

                outWriter.close()

                fOut.flush()
                fOut.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        fun getLastErrorLog(): File? {
            val directory = File(errorLogPath)
            val files = directory.listFiles()

            var newestFile: File? = null
            if (files != null && files.any()) {
                for (f in files) {
                    if (newestFile == null || f.lastModified() > (newestFile.lastModified())) {
                        newestFile = f
                    }
                }
            }

            return newestFile
        }
    }
}