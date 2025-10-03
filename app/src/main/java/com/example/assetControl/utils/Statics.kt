package com.example.assetControl.utils

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.webservice.common.SessionObject
import io.github.cdimascio.dotenv.DotenvBuilder
import java.util.*
import kotlin.reflect.KClass

class Statics {

    @Suppress("unused")
    companion object {

        val newLine: String
            get() = System.lineSeparator() ?: "\r\n"

        // region Variables para DEBUG/DEMO

        val GOD_MODE: Boolean =
            try {
                val env = DotenvBuilder()
                    .directory("/assets")
                    .filename("env")
                    .load()

                env["ENV_GOD_MODE"] == "true"
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }

        val DEMO_MODE: Boolean =
            try {
                val env = DotenvBuilder()
                    .directory("/assets")
                    .filename("env")
                    .load()

                env["ENV_DEMO"] == "true"
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }

        var isCustomDbInUse = false
        val OFFLINE_MODE: Boolean =
            try {
                if (isCustomDbInUse) true
                else {
                    val env = DotenvBuilder()
                        .directory("/assets")
                        .filename("env")
                        .load()
                    env["ENV_OFFLINE"] == "true"
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }

        val AUTO_SEND: Boolean =
            try {
                val env = DotenvBuilder()
                    .directory("/assets")
                    .filename("env")
                    .load()

                env["ENV_AUTOSEND"] == "true"
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }

        // endregion Variables para DEBUG/DEMO

        const val DEFAULT_DATE = "2001-01-01 00:00:00"

        fun getPercentage(completedTask: Int, totalTask: Int): String {
            if (completedTask == 0 && totalTask == 0) return context.getString(R.string.ellipsis)
            return "${completedTask * 100 / if (totalTask == 0) 1 else totalTask}%"
        }

        var currentSession: SessionObject? = null

        const val AC_ROOT_PATH = "/asset_control"
        const val ERROR_LOG_PATH = "/error_log"

        var IMAGE_CONTROL_DATABASE_NAME = "imagecontrol.sqlite"

        // Estos números se corresponden con package_id https://manager.example.com/package/index
        const val APP_VERSION_ID: Int = 12 // AssetControl Milestone13
        const val APP_VERSION_ID_IMAGE_CONTROL = 13 // ImageControl Milestone13

        // Este es el valor de program_id (Ver archivo Program.cs en el proyecto Identification)
        // Lo utiliza internamente ImageControl para identificar la aplicación que lo está usando.
        // Ver: https://source.cloud.google.com/assetcontrol/libs_windows/+/master:Collector/Identification/Program.cs
        const val INTERNAL_IMAGE_CONTROL_APP_ID: Int = 1

        const val RESERVED_CHAR = "#"
        const val TIME_FILE_NAME = "android_time.txt"

        fun deviceDateIsValid(): Boolean {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val pandemicYear = 2020
            return when {
                year < pandemicYear -> false
                else -> true
            }
        }

        fun isDebuggable(): Boolean {
            return 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

        fun classImplementsInterface(clazz: KClass<*>, interfaceKClass: KClass<*>): Boolean {
            return interfaceKClass.java.isAssignableFrom(clazz.java)
        }

        fun appHasBluetoothPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}