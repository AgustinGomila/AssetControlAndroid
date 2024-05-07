package com.dacosys.assetControl.utils

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.webservice.common.SessionObject
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by Agustin on 24/01/2017.
 */

class Statics {

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {

        val newLine: String
            get() = System.lineSeparator() ?: "\r\n"

        // region DEBUG DEMO
        const val DEMO_MODE = false
        const val DEMO_AUTO_SEND = false
        const val SUPER_DEMO_MODE = false
        // endregion DEBUG DEMO

        const val AUTO_SEND_ON_STARTUP = !DEMO_MODE

        const val DEFAULT_DATE = "2001-01-01 00:00:00"

        fun getPercentage(completedTask: Int, totalTask: Int): String {
            if (completedTask == 0 && totalTask == 0) return getContext().getString(R.string.ellipsis)
            return "${completedTask * 100 / if (totalTask == 0) 1 else totalTask}%"
        }

        var currentSession: SessionObject? = null

        const val AC_ROOT_PATH = "/asset_control"
        const val ERROR_LOG_PATH = "/error_log"

        var OFFLINE_MODE = false

        private var IMAGE_CONTROL_DATABASE_NAME = "imagecontrol.sqlite"

        // Estos números se corresponden con package_id https://manager.dacosys.com/package/index
        const val APP_VERSION_ID: Int = 12 // AssetControl Milestone13
        const val APP_VERSION_ID_IMAGECONTROL = 13 // ImageControl Milestone13

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
            return 0 != getContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

        fun classImplementsInterface(clazz: KClass<*>, interfaceKClass: KClass<*>): Boolean {
            return interfaceKClass.java.isAssignableFrom(clazz.java)
        }

        fun appHasBluetoothPermission(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}