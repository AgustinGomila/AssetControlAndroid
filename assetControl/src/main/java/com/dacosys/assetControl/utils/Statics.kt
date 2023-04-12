package com.dacosys.assetControl.utils

import android.content.pm.ApplicationInfo
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.user.UserDbHelper
import com.dacosys.assetControl.model.user.User
import com.dacosys.assetControl.webservice.common.SessionObject
import java.util.*

/**
 * Created by Agustin on 24/01/2017.
 */

class Statics {

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {

        // region DEBUG DEMO
        const val demoMode = false
        const val superDemoMode = false

        var demoQrConfigCode = """
{"config":{"client_email":"demo@dacosys.com","client_password":"1234"}}
                    """.trimIndent()
        // endregion DEBUG DEMO

        val appName: String
            get() = "${getApplicationName()}M13"

        private fun getApplicationName(): String {
            val applicationInfo = getContext().applicationInfo
            return when (val stringId = applicationInfo.labelRes) {
                0 -> applicationInfo.nonLocalizedLabel.toString()
                else -> getContext().getString(stringId)
            }
        }

        const val defaultDate = "2001-01-01 00:00:00"

        fun getPercentage(completedTask: Int, totalTask: Int): String {
            if (completedTask == 0 && totalTask == 0) return getContext().getString(R.string.ellipsis)
            return "${completedTask * 100 / if (totalTask == 0) 1 else totalTask}%"
        }

        var currentUserId: Long? = null
        var currentSession: SessionObject? = null

        const val AC_ROOT_PATH = "/asset_control"
        const val ERROR_LOG_PATH = "/error_log"

        var OFFLINE_MODE = false

        private var IMAGE_CONTROL_DATABASE_NAME = "imagecontrol.sqlite"

        const val APP_VERSION_ID: Int = 12
        const val APP_VERSION_ID_IMAGECONTROL = 13
        const val INTERNAL_IMAGE_CONTROL_APP_ID: Int = 1

        const val reservedChar = "#"
        const val timeFilename = "android_time.txt"

        fun deviceDateIsValid(): Boolean {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val pandemicYear = 2020
            return when {
                year < pandemicYear -> false
                else -> true
            }
        }

        /**
         * Current user
         * Se utiliza generalmente para obtener el nombre del usuario actual
         * necesario en algunos procesos.
         * @return
         */
        fun currentUser(): User? {
            return if (currentUserId != null) {
                UserDbHelper().selectById(currentUserId ?: return null)
            } else null
        }

        fun isDebuggable(): Boolean {
            return 0 != getContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }
    }
}