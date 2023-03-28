package com.dacosys.assetControl.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.user.UserDbHelper
import com.dacosys.assetControl.model.user.User
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.clientPackages.GetClientPackages
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.utils.Preferences.Companion.prefs
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetFloat
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetLong
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.Screen.Companion.getScreenHeight
import com.dacosys.assetControl.utils.Screen.Companion.getScreenWidth
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.utils.settings.QRConfigType
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigImageControl
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.webservice.common.SessionObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.util.*
import kotlin.math.min

/**
 * Created by Agustin on 24/01/2017.
 */

class Statics {
    interface TaskConfigEnded {
        // Define data you like to return from AysncTask
        fun onTaskConfigEnded(result: Boolean, msg: String)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {

        // region DEBUG DEMO
        const val demoMode = false
        const val superDemoMode = false

        var demoQrConfigCode = """
{"config":{"client_email":"demo@dacosys.com","client_password":"1234"}}
                    """.trimIndent()
        // endregion DEBUG DEMO

        var appName: String = "${getApplicationName()}M13"

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

        val clientPackage: String
            get() {
                return prefsGetString(Preference.clientPackage)
            }

        val installationCode: String
            get() {
                return prefsGetString(Preference.installationCode)
            }

        fun deviceDateIsValid(): Boolean {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val pandemicYear = 2020
            return when {
                year < pandemicYear -> false
                else -> true
            }
        }

        fun deleteTimeFile() {
            val timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
            if (timeFileLocation.exists()) {
                timeFileLocation.delete()
            }
        }

        fun currentUser(): User? {
            return if (currentUserId != null) {
                UserDbHelper().selectById(currentUserId ?: return null)
            } else null
        }

        val wsUrl: String //"https://dev.dacosys.com/Milestone13/ac/s1/service.php"
            get() {
                return prefsGetString(Preference.acWsServer)
            }

        val wsNamespace: String //"https://dev.dacosys.com/Milestone13/ac/s1"
            get() {
                return prefsGetString(Preference.acWsNamespace)
            }

        val urlPanel: String //"http://client.dacosys.com:80/7RDRHHAH/panel"
            get() {
                return prefsGetString(Preference.urlPanel)
            }

        val wsProxy: String
            get() {
                return prefsGetString(Preference.acWsProxy)
            }

        val wsProxyPort: Int
            get() {
                return prefsGetInt(Preference.acWsProxyPort)
            }

        val wsUseProxy: Boolean
            get() {
                return prefsGetBoolean(Preference.acWsUseProxy)
            }

        val wsProxyUser: String
            get() {
                return prefsGetString(Preference.acWsProxyUser)
            }

        val wsProxyPass: String
            get() {
                return prefsGetString(Preference.acWsProxyPass)
            }

        val wsUrlCron: String //"https://dev.dacosys.com/Milestone13/ac"
            get() {
                return wsNamespace.replace("/s1", "")
            }

        val wsMantUrl: String //"https://dev.dacosys.com/Milestone13/ac/smant/service.php"
            get() {
                return prefsGetString(Preference.acMantWsServer)
            }

        val wsMantNamespace: String //"https://dev.dacosys.com/Milestone13/ac/smant"
            get() {
                return prefsGetString(Preference.acMantWsNamespace)
            }

        val wsMantProxy: String
            get() {
                return prefsGetString(Preference.acMantWsProxy)
            }

        val wsMantProxyPort: Int
            get() {
                return prefsGetInt(Preference.acMantWsProxyPort)
            }

        val wsMantUseProxy: Boolean
            get() {
                return prefsGetBoolean(Preference.acMantWsUseProxy)
            }

        val wsMantProxyUser: String
            get() {
                return prefsGetString(Preference.acMantWsProxyUser)
            }

        val wsMantProxyPass: String
            get() {
                return prefsGetString(Preference.acMantWsProxyPass)
            }

        val useImageControl: Boolean
            get() {
                return prefsGetBoolean(Preference.useImageControl)
            }

        val wsIcUrl: String //"https://dev.dacosys.com/Milestone13/ic/s1/service.php"
            get() {
                return prefsGetString(Preference.icWsServer)
            }

        val wsIcNamespace: String  //"https://dev.dacosys.com/Milestone13/ic/s1"
            get() {
                return prefsGetString(Preference.icWsNamespace)
            }

        val wsIcProxy: String
            get() {
                return prefsGetString(Preference.icWsProxy)
            }

        val wsIcProxyPort: Int
            get() {
                return prefsGetInt(Preference.icWsProxyPort)
            }

        val wsIcUseProxy: Boolean
            get() {
                return prefsGetBoolean(Preference.icWsUseProxy)
            }

        val wsIcProxyUser: String
            get() {
                return prefsGetString(Preference.icWsProxyUser)
            }

        val wsIcProxyPass: String
            get() {
                return prefsGetString(Preference.icWsProxyPass)
            }

        val icUser: String
            get() {
                return prefsGetString(Preference.icUser)
            }

        val icPass: String
            get() {
                return prefsGetString(Preference.icPass)
            }

        val wsIcUser: String
            get() {
                return prefsGetString(Preference.icWsUser)
            }

        val wsIcPass: String
            get() {
                return prefsGetString(Preference.icWsPass)
            }

        var wsTestUrl: String = ""
        var wsTestNamespace: String = ""
        var wsTestProxyUrl: String = ""
        var wsTestProxyPort: Int = 0
        var wsTestUseProxy: Boolean = false
        var wsTestProxyUser: String = ""
        var wsTestProxyPass: String = ""

        val maxHeightOrWidth: Int
            get() {
                return prefsGetInt(Preference.icPhotoMaxHeightOrWidth)
            }

        fun getConfig(
            email: String,
            password: String,
            installationCode: String,
            onRequestProgress: (ClientPackagesProgress) -> Unit = {},
        ) {
            if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
                GetClientPackages(
                    email = email,
                    password = password,
                    installationCode = installationCode,
                    onProgress = onRequestProgress
                )
            }
        }

        private fun isDebuggable(): Boolean {
            return 0 != getContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        }

        // region SOME CONFIG VALUES AND PREFERENCES FUNCTIONS
        fun getConfigFromScannedCode(
            scanCode: String,
            mode: QRConfigType,
            onRequestProgress: (ClientPackagesProgress) -> Unit = {},
        ) {
            if (prefs == null) {
                onRequestProgress.invoke(
                    ClientPackagesProgress(
                        status = ProgressStatus.crashed,
                        result = ArrayList(),
                        clientEmail = "",
                        clientPassword = "",
                        msg = getContext().getString(R.string.configuration_not_loaded)
                    )
                )
                return
            }

            val mainJson = JSONObject(scanCode)
            val mainTag = when {
                mainJson.has("config") && mode == QRConfigClientAccount -> "config"
                mainJson.has(appName) && mode != QRConfigClientAccount -> appName
                else -> ""
            }

            if (mainTag.isEmpty()) {
                onRequestProgress.invoke(
                    ClientPackagesProgress(
                        status = ProgressStatus.crashed,
                        result = ArrayList(),
                        clientEmail = "",
                        clientPassword = "",
                        msg = getContext().getString(R.string.invalid_code)
                    )
                )
                return
            }

            val confJson = mainJson.getJSONObject(mainTag)

            when (mode) {
                QRConfigClientAccount -> {
                    // Package Client Setup
                    val installationCode =
                        if (confJson.has(Preference.installationCode.key)) confJson.getString(
                            Preference.installationCode.key
                        ) else ""
                    val email =
                        if (confJson.has(Preference.clientEmail.key)) confJson.getString(Preference.clientEmail.key) else ""
                    val password =
                        if (confJson.has(Preference.clientPassword.key)) confJson.getString(
                            Preference.clientPassword.key
                        ) else ""

                    if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
                        getConfig(
                            email = email,
                            password = password,
                            installationCode = installationCode,
                            onRequestProgress = onRequestProgress
                        )
                    } else {
                        onRequestProgress.invoke(
                            ClientPackagesProgress(
                                status = ProgressStatus.crashed,
                                result = ArrayList(),
                                clientEmail = email,
                                clientPassword = password,
                                msg = getContext().getString(R.string.invalid_code)
                            )
                        )
                    }
                }
                QRConfigWebservice, QRConfigApp, QRConfigImageControl -> {
                    tryToLoadConfig(confJson)
                    onRequestProgress.invoke(
                        ClientPackagesProgress(
                            status = ProgressStatus.success,
                            result = ArrayList(),
                            clientEmail = "",
                            clientPassword = "",
                            msg = when (mode) {
                                QRConfigImageControl -> getContext().getString(R.string.imagecontrol_configured)
                                QRConfigWebservice -> getContext().getString(R.string.server_configured)
                                else -> getContext().getString(R.string.configuration_applied)
                            }
                        )
                    )
                }
                else -> {
                    onRequestProgress.invoke(
                        ClientPackagesProgress(
                            status = ProgressStatus.crashed,
                            result = ArrayList(),
                            clientEmail = "",
                            clientPassword = "",
                            msg = getContext().getString(R.string.invalid_code)
                        )
                    )
                }
            }
        }

        fun generateQrCode(weakAct: WeakReference<Activity>, data: String) {
            val activity = weakAct.get() ?: return
            if (activity.isFinishing) return

            val writer = QRCodeWriter()
            try {
                var w: Int = getScreenWidth(activity)
                val h: Int = getScreenHeight(activity)
                if (h < w) {
                    w = h
                }

                // CREAR LA IMAGEN
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, w, w)
                val width = bitMatrix.width
                val height = bitMatrix.height

                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    val offset = y * width
                    for (x in 0 until width) {
                        val color: Int = if (bitMatrix.get(x, y)) {
                            Color.BLACK
                        } else {
                            Color.WHITE
                        }

                        pixels[offset + x] = color
                    }
                }

                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bmp.setPixels(pixels, 0, width, 0, 0, width, height)

                val imageView = ImageView(activity)
                imageView.setImageBitmap(bmp)
                val builder = AlertDialog.Builder(activity).setTitle(R.string.configuration_qr_code)
                    .setMessage(R.string.scan_the_code_below_with_another_device_to_copy_the_configuration)
                    .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .setView(imageView)

                builder.create().show()
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        fun getBarcodeForConfig(ps: ArrayList<Preference>, mainTag: String): String {
            val jsonObject = JSONObject()

            for (p in ps) {
                if (p.defaultValue is Int) {
                    val value = prefsGetInt(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Boolean) {
                    val value = prefsGetBoolean(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is String) {
                    val value = prefsGetString(p)
                    if (value != p.defaultValue && value.isNotEmpty()) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Long) {
                    val value = prefsGetLong(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                } else if (p.defaultValue is Float) {
                    val value = prefsGetFloat(p)
                    if (value != p.defaultValue) {
                        jsonObject.put(p.key, value)
                    }
                }
            }

            val jsonRes = JSONObject()
            jsonRes.put(mainTag, jsonObject)

            Log.d(this::class.java.simpleName, jsonRes.toString())
            return jsonRes.toString()
        }

        private fun getApplicationName(): String {
            val applicationInfo = getContext().applicationInfo
            return when (val stringId = applicationInfo.labelRes) {
                0 -> applicationInfo.nonLocalizedLabel.toString()
                else -> getContext().getString(stringId)
            }
        }

        private fun tryToLoadConfig(conf: JSONObject) {
            val availablePref = Preference.getConfigPreferences()
            for (prefName in conf.keys()) {

                // No está permitido cargar configuraciones de cliente por esta vía.
                if (!availablePref.any { it.key == prefName }) {
                    continue
                }

                val p = (prefs ?: return).edit()
                val tempPref = (prefs ?: return).all[prefName]
                if (tempPref != null) {
                    try {
                        when (tempPref) {
                            is String -> p.putString(prefName, conf.getString(prefName))
                            is Boolean -> p.putBoolean(prefName, conf.getBoolean(prefName))
                            is Int -> p.putInt(prefName, conf.getInt(prefName))
                            is Float -> p.putFloat(prefName, conf.getDouble(prefName).toFloat())
                            is Long -> p.putLong(prefName, conf.getLong(prefName))
                            else -> p.putString(prefName, conf.getString(prefName))
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e(
                            this::class.java.simpleName,
                            "Imposible convertir valor de configuración: $prefName"
                        )
                        ErrorLog.writeLog(null, "tryToLoadConfig", ex)
                    }
                } else {
                    val pref = Preference.getByKey(prefName)
                    if (pref != null) {
                        try {
                            when (pref.defaultValue) {
                                is String -> p.putString(prefName, conf.getString(prefName))
                                is Boolean -> p.putBoolean(prefName, conf.getBoolean(prefName))
                                is Int -> p.putInt(prefName, conf.getInt(prefName))
                                is Float -> p.putFloat(prefName, conf.getDouble(prefName).toFloat())
                                is Long -> p.putLong(prefName, conf.getLong(prefName))
                                else -> p.putString(prefName, conf.getString(prefName))
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Log.e(
                                this::class.java.simpleName,
                                "Imposible convertir valor de configuración: $prefName"
                            )
                            ErrorLog.writeLog(null, "tryToLoadConfig", ex)
                        }
                    }
                }
                p.apply()
            }
        }

        fun setDebugConfigValues() {
            // VALORES POR DEFECTO, SÓLO PARA DEBUG
            if (prefs != null) {
                if (isDebuggable() || BuildConfig.DEBUG) {
                    val x = (prefs ?: return).edit()

                    // region ASSET CONTROL WEBSERVICE
                    if (wsUrl.isEmpty()) {
                        x.putString(
                            Preference.acWsServer.key, Preference.acWsServer.debugValue as String?
                        )
                    }

                    if (wsNamespace.isEmpty()) {
                        x.putString(
                            Preference.acWsNamespace.key,
                            Preference.acWsNamespace.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acWsUser).isEmpty()) {
                        x.putString(
                            Preference.acWsUser.key, Preference.acWsUser.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acWsPass).isEmpty()) {
                        x.putString(
                            Preference.acWsPass.key, Preference.acWsPass.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acUser).isEmpty()) {
                        x.putString(
                            Preference.acUser.key, Preference.acUser.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acPass).isEmpty()) {
                        x.putString(
                            Preference.acPass.key, Preference.acPass.debugValue as String?
                        )
                    }
                    // endregion

                    // region ASSET CONTROL MANTEINANCE WEBSERVICE
                    if (wsMantUrl.isEmpty()) {
                        x.putString(
                            Preference.acMantWsServer.key,
                            Preference.acMantWsServer.debugValue as String?
                        )
                    }

                    if (wsMantNamespace.isEmpty()) {
                        x.putString(
                            Preference.acMantWsNamespace.key,
                            Preference.acMantWsNamespace.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acMantWsUser).isEmpty()) {
                        x.putString(
                            Preference.acMantWsUser.key,
                            Preference.acMantWsUser.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acMantWsPass).isEmpty()) {
                        x.putString(
                            Preference.acMantWsPass.key,
                            Preference.acMantWsPass.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acMantUser).isEmpty()) {
                        x.putString(
                            Preference.acMantUser.key, Preference.acMantUser.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.acMantPass).isEmpty()) {
                        x.putString(
                            Preference.acMantPass.key, Preference.acMantPass.debugValue as String?
                        )
                    }
                    // endregion

                    // region IMAGE CONTROL WEBSERVICE
                    if (wsIcUrl.isEmpty()) {
                        x.putString(
                            Preference.icWsServer.key, Preference.icWsServer.debugValue as String?
                        )
                    }

                    if (wsIcNamespace.isEmpty()) {
                        x.putString(
                            Preference.icWsNamespace.key,
                            Preference.icWsNamespace.debugValue as String?
                        )
                    }

                    if (wsIcUser.isEmpty()) {
                        x.putString(
                            Preference.icWsUser.key, Preference.icWsUser.debugValue as String?
                        )
                    }

                    if (wsIcPass.isEmpty()) {
                        x.putString(
                            Preference.icWsPass.key, Preference.icWsPass.debugValue as String?
                        )
                    }

                    if (icUser.isEmpty()) {
                        x.putString(
                            Preference.icUser.key, Preference.icUser.debugValue as String?
                        )
                    }

                    if (icPass.isEmpty()) {
                        x.putString(
                            Preference.icPass.key, Preference.icPass.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.icUser).isEmpty()) {
                        x.putString(
                            Preference.icUser.key, Preference.icUser.debugValue as String?
                        )
                    }

                    if (prefsGetString(Preference.icPass).isEmpty()) {
                        x.putString(
                            Preference.icPass.key, Preference.icPass.debugValue as String?
                        )
                    }
                    // endregion

                    run {
                        x.apply()
                    }
                }
            }
        }

        fun roundToString(d: Double, decimalPlaces: Int): String {
            val r = round(d, decimalPlaces).toString()
            return if (decimalPlaces == 0 || d % 1 == 0.0) {
                r.substring(0, r.indexOf('.'))
            } else {
                r
            }
        }

        fun roundToString(d: Float, decimalPlaces: Int): String {
            return roundToString(d.toDouble(), decimalPlaces)
        }

        fun round(d: Float, decimalPlaces: Int): Double {
            return round(d.toDouble(), decimalPlaces)
        }

        fun round(d: Double, decimalPlaces: Int): Double {
            var bd = BigDecimal(d.toString())
            bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)
            return bd.toDouble()
        }

        //region FileHelper
        private var contentUri: Uri? = null

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.<br>
         * <br>
         * Callers should check whether the path is local before assuming it
         * represents a local file.
         *
         * @param uri     The Uri to query.
         */

        @Suppress("UNUSED_VARIABLE")
        @SuppressLint("NewApi")
        fun getPath(uri: Uri): String? {
            val selection: String?
            val selectionArgs: Array<String>?

            // DocumentProvider
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                val fullPath = getPathFromExtSD(split)
                return if (fullPath !== "") {
                    fullPath
                } else {
                    null
                }
            }

            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                var cursor: Cursor? = null
                try {
                    cursor = getContext().contentResolver.query(
                        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName: String = cursor.getString(0)
                        val path: String =
                            getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                                .toString() + "/Download/" + fileName
                        if (!TextUtils.isEmpty(path)) {
                            return path
                        }
                    }
                } finally {
                    cursor?.close()
                }
                val id: String = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads", "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        return try {
                            val contentUri: Uri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id)
                            )
                            getDataColumn(contentUri, "", null)
                        } catch (e: NumberFormatException) {
                            //In Android 8 and Android P the id is not a number
                            (uri.path ?: return null).replaceFirst("^/document/raw:", "")
                                .replaceFirst("^raw:", "")
                        }
                    }
                }
            }

            // MediaProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
                return getDataColumn(contentUri ?: return null, selection, selectionArgs)
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri)
            }
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(uri)
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri)
                }
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // return getFilePathFromURI(context,uri);
                    copyFileToInternalStorage(uri, "userfiles")
                    // return getRealPathFromURI(context,uri);
                } else {
                    getDataColumn(uri, "", null)
                }
            }
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/" + pathData[1]
            var fullPath: String

            // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage
            // don't know any API that can get the root path of that storage based on its id.
            //
            // so no "primary" type, but let the check here for other devices
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            // Environment.isExternalStorageRemovable() is `true` for external and internal storage
            // so we cannot relay on it.
            //
            // instead, for each possible path, check if file exists
            // we'll start with secondary storage as this could be our (physically) removable sd card
            fullPath = System.getenv("SECONDARY_STORAGE") ?: ("" + relativePath)
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE") ?: ("" + relativePath)
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        @Suppress("UNUSED_VARIABLE")
        private fun getDriveFilePath(uri: Uri): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = getContext().contentResolver.query(
                returnUri, null, null, null, null
            ) ?: return null
            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()
            val file = File(getContext().cacheDir, name)

            try {
                val inputStream: InputStream =
                    getContext().contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable: Int = inputStream.available()

                //int bufferSize = 1024;
                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Log.e("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
            } catch (e: java.lang.Exception) {
                Log.e("Exception", e.message ?: "")
            }
            returnCursor.close()
            return file.path
        }

        /***
         * Used for Android Q+
         * @param uri
         * @param newDirName if you want to create a directory, you can set this variable
         * @return
         */
        @Suppress("UNUSED_VARIABLE")
        private fun copyFileToInternalStorage(
            uri: Uri,
            newDirName: String,
        ): String? {
            val returnUri: Uri = uri
            val returnCursor: Cursor = getContext().contentResolver.query(
                returnUri, arrayOf(
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                ), null, null, null
            ) ?: return null

            /*
             * Get the column indexes of the data in the Cursor,
             *     * move to the first row in the Cursor, get the data,
             *     * and display it.
             * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()

            val output = if (newDirName != "") {
                val dir = File(getContext().filesDir.toString() + "/" + newDirName)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                File(getContext().filesDir.toString() + "/" + newDirName + "/" + name)
            } else {
                File(getContext().filesDir.toString() + "/" + name)
            }

            try {
                val inputStream: InputStream =
                    getContext().contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(output)
                var read: Int
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
            } catch (e: java.lang.Exception) {
                Log.e("Exception", e.message ?: "")
            }
            returnCursor.close()
            return output.path
        }

        private fun getFilePathForWhatsApp(uri: Uri): String? {
            return copyFileToInternalStorage(uri, "whatsapp")
        }

        private fun getDataColumn(
            uri: Uri,
            selection: String,
            selectionArgs: Array<String>?,
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = getContext().contentResolver.query(
                    uri, projection, selection, selectionArgs, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        private fun isWhatsAppFile(uri: Uri): Boolean {
            return "com.whatsapp.provider.media" == uri.authority
        }

        private fun isGoogleDriveUri(uri: Uri): Boolean {
            return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
        }
        //endregion
    }
}