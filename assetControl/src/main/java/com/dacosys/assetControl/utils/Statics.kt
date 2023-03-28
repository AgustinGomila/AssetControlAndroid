package com.dacosys.assetControl.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.InsetDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputType
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.category.ItemCategoryDbHelper
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionDbHelper
import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.dataBase.location.WarehouseDbHelper
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementDbHelper
import com.dacosys.assetControl.dataBase.review.AssetReviewDbHelper
import com.dacosys.assetControl.dataBase.route.RouteProcessDbHelper
import com.dacosys.assetControl.dataBase.user.UserDbHelper
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.datacollection.DataCollection
import com.dacosys.assetControl.model.location.Warehouse
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.manteinance.AssetManteinance
import com.dacosys.assetControl.model.movement.WarehouseMovement
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.route.RouteProcess
import com.dacosys.assetControl.model.user.User
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.clientPackages.GetClientPackages
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Preferences.Companion.prefs
import com.dacosys.assetControl.utils.Preferences.Companion.prefsCleanKey
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetByKey
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetFloat
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetLong
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.Screen.Companion.getScreenHeight
import com.dacosys.assetControl.utils.Screen.Companion.getScreenWidth
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.utils.settings.QRConfigType
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigImageControl
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.utils.settings.collectorType.CollectorType
import com.dacosys.assetControl.webservice.common.SessionObject
import com.dacosys.assetControl.webservice.common.Webservice
import com.dacosys.imageControl.room.database.IcDatabase.Companion.cleanInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
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
    companion object : DialogInterface.OnMultiChoiceClickListener {

        // region DEBUG DEMO
        const val demoMode = false
        const val superDemoMode = false

        var demoQrConfigCode = """
{"config":{"client_email":"demo@dacosys.com","client_password":"1234"}}
                    """.trimIndent()
        // endregion DEBUG DEMO

        var appName: String = "${getApplicationName()}M13"

        // Este flag es para reinicializar el colector después de cambiar en Settings.
        var collectorTypeChanged = false

        fun closeImageControl() {
            com.dacosys.imageControl.Statics.cleanInstance()
        }

        const val defaultDate = "2001-01-01 00:00:00"

        fun setupImageControl() {
            // Setup ImageControl
            com.dacosys.imageControl.Statics.appAllowScreenRotation =
                prefsGetBoolean(Preference.allowScreenRotation)

            val currentUser = currentUser()
            if (currentUser != null) {
                com.dacosys.imageControl.Statics.currentUserId = currentUser.userId
                com.dacosys.imageControl.Statics.currentUserName = currentUser.name
                com.dacosys.imageControl.Statics.newInstance()
            }

            com.dacosys.imageControl.Statics.useImageControl = useImageControl
            com.dacosys.imageControl.Statics.wsIcUrl = wsIcUrl
            com.dacosys.imageControl.Statics.wsIcNamespace = wsIcNamespace
            com.dacosys.imageControl.Statics.wsIcProxy = wsIcProxy
            com.dacosys.imageControl.Statics.wsIcProxyPort = wsIcProxyPort
            com.dacosys.imageControl.Statics.wsIcUseProxy = wsIcUseProxy
            com.dacosys.imageControl.Statics.wsIcProxyUser = wsIcProxyUser
            com.dacosys.imageControl.Statics.wsIcProxyPass = wsIcProxyPass
            com.dacosys.imageControl.Statics.icUser = icUser
            com.dacosys.imageControl.Statics.icPass = icPass
            com.dacosys.imageControl.Statics.wsIcUser = wsIcUser
            com.dacosys.imageControl.Statics.wsIcPass = wsIcPass
            com.dacosys.imageControl.Statics.maxHeightOrWidth = maxHeightOrWidth
        }

        // region AssetControlManteinance WS
        private lateinit var wsMant: Webservice
        private var wsMantInitialized = false

        fun getMantWebservice(): Webservice {
            if (!wsMantInitialized) {
                wsMant = Webservice(Webservice.WebServiceType.AssetControlManteinance)
                wsMantInitialized = true
            }
            return wsMant
        }
        // endregion AssetControlManteinance WS

        fun getPercentage(completedTask: Int, totalTask: Int): String {
            if (completedTask == 0 && totalTask == 0) return getContext().getString(R.string.ellipsis)
            return "${completedTask * 100 / if (totalTask == 0) 1 else totalTask}%"
        }

        // region Get new DEFAULT AssetControl WS or Initialize one.
        private var wsInitialized = false
        lateinit var ws: Webservice

        fun getWebservice(): Webservice {
            if (!wsInitialized) {
                ws = Webservice(Webservice.WebServiceType.AssetControl)
                wsInitialized = true
            }
            return ws
        }
        // endregion

        // region Check Connections
        @SuppressLint("MissingPermission")
        fun isOnline(): Boolean {
            val connectivityManager =
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    //for other device how are able to connect with Ethernet
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                    //for check internet over Bluetooth
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_BLUETOOTH")
                        return true
                    }
                }
            }
            Log.w("Internet", "NO CONNECTION")
            return false
        }
        // endregion Check Connections

        var downloadDbRequired = false

        const val DATABASE_VERSION = 1
        var currentUserId: Long? = null
        var currentSession: SessionObject? = null

        const val AC_ROOT_PATH = "/asset_control"
        const val ERROR_LOG_PATH = "/error_log"

        var OFFLINE_MODE = false
        var DATABASE_NAME = "assetcontroldb.sqlite"
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

        // region COLLECTOR TYPE THINGS

        val collectorType: CollectorType
            get() {
                return try {
                    return when (val pref =
                        prefsGetByKey(Preference.collectorType.key) ?: CollectorType.none) {
                        is CollectorType -> pref
                        is String -> {
                            val id: Int = if (pref.isEmpty()) 0 else pref.toInt()
                            CollectorType.getById(id)
                        }
                        is Int, is Long -> {
                            CollectorType.getById(pref as Int)
                        }
                        else -> {
                            CollectorType.none
                        }
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e(this::class.java.simpleName, ex.message.toString())
                    prefsCleanKey(Preference.collectorType.key)
                    CollectorType.none
                }
            }

        // endregion COLLECTOR TYPE THINGS

        fun deleteTimeFile() {
            val timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
            if (timeFileLocation.exists()) {
                timeFileLocation.delete()
            }
        }

        fun autoSend(): Boolean {
            return isOnline() && prefsGetBoolean(Preference.autoSend)
        }

        fun currentUser(): User? {
            return if (currentUserId != null) {
                UserDbHelper().selectById(currentUserId ?: return null)
            } else null
        }

        // region BLUETOOTH PRINTER THINGS
        var printerBluetoothDevice: BluetoothDevice? = null
            get() {
                if (field == null) {
                    refreshBluetoothPrinter()
                }
                return field
            }

        @SuppressLint("MissingPermission")
        private fun refreshBluetoothPrinter() {
            if (prefsGetBoolean(Preference.useBtPrinter)) {
                val printerMacAddress = prefsGetString(Preference.printerBtAddress)
                if (printerMacAddress.isEmpty()) {
                    return
                }

                val bluetoothManager =
                    getContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val mBluetoothAdapter = bluetoothManager.adapter

                if (ActivityCompat.checkSelfPermission(
                        getContext(), Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                val mPairedDevices = mBluetoothAdapter!!.bondedDevices

                if (mPairedDevices.size > 0) {
                    for (mDevice in mPairedDevices) {
                        if (mDevice.address == printerMacAddress) {
                            printerBluetoothDevice = mDevice
                            return
                        }
                    }
                }
            }
        }
        // endregion BLUETOOTH PRINTER THINGS

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

        // region Selección automática de paquetes del cliente
        private var allProductsArray: ArrayList<JSONObject> = ArrayList()
        private var validProductsArray: ArrayList<JSONObject> = ArrayList()
        private var selected: BooleanArray? = null

        override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
            if (isChecked) {
                val tempProdVersionId = validProductsArray[which].getString("product_version_id")

                for (i in 0 until validProductsArray.size) {
                    if ((selected ?: return)[i]) {
                        val prodVerId = validProductsArray[i].getString("product_version_id")
                        if (prodVerId == tempProdVersionId) {
                            (selected ?: return)[i] = false
                            (dialog as AlertDialog).listView.setItemChecked(i, false)
                        }
                    }
                }
            }

            (selected ?: return)[which] = isChecked
        }

        private var ValidProducts = getValidProducts()
        private fun getValidProducts(): ArrayList<String> {
            val r: ArrayList<String> = ArrayList()
            r.add(APP_VERSION_ID.toString())
            r.add(APP_VERSION_ID_IMAGECONTROL.toString())
            return r
        }

        fun selectClientPackage(
            parentView: View,
            callback: TaskConfigPanelEnded,
            weakAct: WeakReference<Activity>,
            allPackage: ArrayList<JSONObject>,
            email: String,
            password: String,
        ) {
            val activity = weakAct.get() ?: return
            if (activity.isFinishing) return

            allProductsArray.clear()
            for (pack in allPackage) {
                val pvId = pack.getString("product_version_id")
                if (ValidProducts.contains(pvId) && !allProductsArray.contains(pack)) {
                    allProductsArray.add(pack)
                }
            }

            if (!allProductsArray.any()) {
                makeText(
                    parentView,
                    getContext().getString(R.string.there_are_no_valid_products_for_the_selected_client),
                    SnackBarType.ERROR
                )
                return
            }

            if (allProductsArray.size == 1) {
                val productVersionId = allProductsArray[0].getString("product_version_id")
                if (productVersionId == APP_VERSION_ID.toString() || productVersionId == APP_VERSION_ID_IMAGECONTROL.toString()) {
                    setConfigPanel(
                        parentView = parentView,
                        callback = callback,
                        packArray = arrayListOf(allProductsArray[0]),
                        email = email,
                        password = password
                    )
                    return
                } else {
                    makeText(
                        parentView,
                        getContext().getString(R.string.there_are_no_valid_products_for_the_selected_client),
                        SnackBarType.ERROR
                    )
                    return
                }
            }

            var validProducts = false
            validProductsArray.clear()
            val client = allProductsArray[0].getString("client")
            val listItems: ArrayList<String> = ArrayList()

            for (pack in allProductsArray) {
                val productVersionId = pack.getString("product_version_id")

                // AssetControl M13 o ImageControl M13
                if (productVersionId == APP_VERSION_ID.toString() || productVersionId == APP_VERSION_ID_IMAGECONTROL.toString()) {
                    validProducts = true
                    val clientPackage = pack.getString("client_package_content_description")

                    listItems.add(clientPackage)
                    validProductsArray.add(pack)
                }
            }

            if (!validProducts) {
                makeText(
                    parentView,
                    getContext().getString(R.string.there_are_no_valid_products_for_the_selected_client),
                    SnackBarType.ERROR
                )
                return
            }

            selected = BooleanArray(validProductsArray.size)

            val cw = ContextThemeWrapper(activity, R.style.AlertDialogTheme)
            val builder = AlertDialog.Builder(cw)

            val title = TextView(activity)
            title.text = String.format(
                "%s - %s", client, getContext().getString(R.string.select_package)
            )
            title.textSize = 16F
            title.gravity = Gravity.CENTER_HORIZONTAL
            builder.setCustomTitle(title)

            builder.setMultiChoiceItems(
                listItems.toTypedArray(), selected, this
            )

            builder.setPositiveButton(R.string.accept) { dialog, _ ->
                val selectedPacks: ArrayList<JSONObject> = ArrayList()
                for ((i, prod) in validProductsArray.withIndex()) {
                    if ((selected ?: return@setPositiveButton)[i]) {
                        selectedPacks.add(prod)
                    }
                }

                if (selectedPacks.size > 0) {
                    setConfigPanel(
                        parentView = parentView,
                        callback = callback,
                        packArray = selectedPacks,
                        email = email,
                        password = password
                    )
                }
                dialog.dismiss()
            }

            val layoutDefault = ResourcesCompat.getDrawable(
                getContext().resources, R.drawable.layout_thin_border, null
            )
            val inset = InsetDrawable(layoutDefault, 20)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(inset)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
        }

        interface TaskConfigPanelEnded {
            fun onTaskConfigPanelEnded(status: ProgressStatus)
        }

        private fun setConfigPanel(
            parentView: View,
            callback: TaskConfigPanelEnded,
            packArray: ArrayList<JSONObject>,
            email: String,
            password: String,
        ) {
            for (pack in packArray) {
                val active = pack.getInt("active")
                if (active == 0) {
                    makeText(
                        parentView,
                        getContext().getString(R.string.inactive_installation),
                        SnackBarType.ERROR
                    )
                    continue
                }

                // PANEL DE CONFIGURACIÓN
                val productId = pack.getString("product_version_id")
                val panelJsonObj = pack.getJSONObject("panel")
                val appUrl = when {
                    panelJsonObj.has("url") -> panelJsonObj.getString("url") ?: ""
                    else -> ""
                }

                if (appUrl.isEmpty()) {
                    makeText(
                        parentView,
                        getContext().getString(R.string.app_panel_url_can_not_be_obtained),
                        SnackBarType.ERROR
                    )
                    return
                }

                val clientPackage = when {
                    pack.has("client_package_content_description") -> pack.getString("client_package_content_description")
                        ?: ""
                    else -> ""
                }

                val installationCode = when {
                    pack.has("installation_code") -> pack.getString("installation_code") ?: ""
                    else -> ""
                }

                var url: String
                var namespace: String
                var user: String
                var pass: String
                var icUser: String
                var icPass: String

                val wsJsonObj = pack.getJSONObject("ws")
                url = if (wsJsonObj.has("url")) wsJsonObj.getString("url") else ""
                namespace = if (wsJsonObj.has("namespace")) wsJsonObj.getString("namespace") else ""
                user = if (wsJsonObj.has("ws_user")) wsJsonObj.getString("ws_user") else ""
                pass = if (wsJsonObj.has("ws_password")) wsJsonObj.getString("ws_password") else ""

                val customOptJsonObj = pack.getJSONObject("custom_options")
                icUser =
                    if (customOptJsonObj.has("ic_user")) customOptJsonObj.getString("ic_user") else ""
                icPass =
                    if (customOptJsonObj.has("ic_password")) customOptJsonObj.getString("ic_password") else ""

                if (prefs == null) {
                    return
                }
                val x = (prefs ?: return).edit()
                if (productId == APP_VERSION_ID.toString()) {
                    x.putString(Preference.urlPanel.key, appUrl)
                    x.putString(Preference.installationCode.key, installationCode)
                    x.putString(Preference.clientPackage.key, clientPackage)
                    x.putString(Preference.clientEmail.key, email)
                    x.putString(Preference.clientPassword.key, password)

                    x.putString(Preference.acWsServer.key, url)
                    x.putString(Preference.acWsNamespace.key, namespace)
                    x.putString(Preference.acWsUser.key, user)
                    x.putString(Preference.acWsPass.key, pass)

                    x.putString(Preference.icUser.key, icUser)
                    x.putString(Preference.icPass.key, icPass)
                } else if (productId == APP_VERSION_ID_IMAGECONTROL.toString()) {
                    x.putBoolean(Preference.useImageControl.key, true)

                    x.putString(Preference.icWsServer.key, url)
                    x.putString(Preference.icWsNamespace.key, namespace)
                    x.putString(Preference.icWsUser.key, user)
                    x.putString(Preference.icWsPass.key, pass)
                }
                run { x.apply() }
            }

            downloadDbRequired = true
            callback.onTaskConfigPanelEnded(ProgressStatus.finished)
        }
        // endregion

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

        // region PROXY THINGS
        private var avoidSetupProxyDialog = false

        interface TaskSetupProxyEnded {
            fun onTaskSetupProxyEnded(
                status: ProgressStatus,
                email: String,
                password: String,
                installationCode: String,
            )
        }

        fun setupProxy(
            callback: TaskSetupProxyEnded,
            weakAct: WeakReference<Activity>,
            email: String,
            password: String,
            installationCode: String = "",
        ) {
            val activity = weakAct.get() ?: return
            if (activity.isFinishing) return

            if (avoidSetupProxyDialog) {
                return
            }

            avoidSetupProxyDialog = true

            val alert: AlertDialog.Builder = AlertDialog.Builder(activity)
            alert.setTitle(
                getContext().getString(R.string.configure_proxy_question)
            )

            val proxyEditText = EditText(activity)
            proxyEditText.hint = getContext().getString(R.string.proxy)
            proxyEditText.isFocusable = true
            proxyEditText.isFocusableInTouchMode = true

            val proxyPortEditText = EditText(activity)
            proxyPortEditText.inputType = InputType.TYPE_CLASS_NUMBER
            proxyPortEditText.hint = getContext().getString(R.string.port)
            proxyPortEditText.isFocusable = true
            proxyPortEditText.isFocusableInTouchMode = true

            val proxyUserEditText = EditText(activity)
            proxyUserEditText.inputType = InputType.TYPE_CLASS_TEXT
            proxyUserEditText.hint = getContext().getString(R.string.user)
            proxyUserEditText.isFocusable = true
            proxyUserEditText.isFocusableInTouchMode = true

            val proxyPassEditText = TextInputEditText(activity)
            proxyPassEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            proxyPassEditText.hint = getContext().getString(R.string.password)
            proxyPassEditText.isFocusable = true
            proxyPassEditText.isFocusableInTouchMode = true
            proxyPassEditText.typeface = Typeface.DEFAULT
            proxyPassEditText.transformationMethod = PasswordTransformationMethod()

            val inputLayout = TextInputLayout(getContext())
            inputLayout.endIconMode = END_ICON_PASSWORD_TOGGLE
            inputLayout.addView(proxyPassEditText)

            val layout = LinearLayout(getContext())
            layout.orientation = LinearLayout.VERTICAL

            layout.addView(proxyEditText)
            layout.addView(proxyPortEditText)
            layout.addView(proxyUserEditText)
            layout.addView(inputLayout)

            alert.setView(layout)
            alert.setNegativeButton(R.string.no) { _, _ ->
                if (prefs == null) {
                    return@setNegativeButton
                }
                val p = (prefs ?: return@setNegativeButton).edit()
                p.putBoolean(Preference.acWsUseProxy.key, false)
                p.apply()
            }
            alert.setPositiveButton(R.string.yes) { _, _ ->
                if (prefs == null) {
                    return@setPositiveButton
                }
                val proxy = proxyEditText.text
                val port = proxyPortEditText.text
                val user = proxyUserEditText.text
                val pass = proxyPassEditText.text

                val p = (prefs ?: return@setPositiveButton).edit()
                if (proxy != null) {
                    p.putBoolean(Preference.acWsUseProxy.key, true)
                    p.putString(Preference.acWsProxy.key, proxy.toString())
                }

                if (port != null) {
                    p.putInt(Preference.acWsProxyPort.key, Integer.parseInt(port.toString()))
                }

                if (user.isNotEmpty()) {
                    p.putString(Preference.acWsProxyUser.key, user.toString())
                }

                if (pass != null && pass.isNotEmpty()) {
                    p.putString(Preference.acWsProxyPass.key, pass.toString())
                }

                p.apply()
            }
            alert.setOnDismissListener {
                callback.onTaskSetupProxyEnded(
                    status = ProgressStatus.finished,
                    email = email,
                    password = password,
                    installationCode = installationCode
                )
                avoidSetupProxyDialog = false
            }

            val dialog = alert.create()
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            dialog.show()
            proxyEditText.requestFocus()
        }
        // endregion PROXY THINGS

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

        fun pendingAssetReview(): ArrayList<AssetReview> {
            return AssetReviewDbHelper().selectByCompleted()
        }

        fun pendingWarehouseMovement(): ArrayList<WarehouseMovement> {
            return WarehouseMovementDbHelper().selectByNoTransferred()
        }

        fun pendingAsset(): ArrayList<Asset> {
            return AssetDbHelper().selectNoTransferred()
        }

        fun pendingWarehouseArea(): ArrayList<WarehouseArea> {
            return WarehouseAreaDbHelper().selectNoTransfered()
        }

        fun pendingWarehouse(): ArrayList<Warehouse> {
            return WarehouseDbHelper().selectNoTransfered()
        }

        fun pendingItemCategory(): ArrayList<ItemCategory> {
            return ItemCategoryDbHelper().selectNoTransfered()
        }

        fun pendingDataCollection(): ArrayList<DataCollection> {
            return DataCollectionDbHelper().selectByNoTransferred()
        }

        fun pendingRouteProcess(): ArrayList<RouteProcess> {
            return RouteProcessDbHelper().selectByNoTransferred()
        }

        fun pendingAssetManteinance(): ArrayList<AssetManteinance> {
            return AssetManteinanceDbHelper().selectNoTransfered()
        }

        fun pendingDelivery(): Boolean {
            return when {
                pendingAssetReview().any() -> true
                pendingWarehouseMovement().any() -> true
                pendingAsset().any() -> true
                pendingWarehouseArea().any() -> true
                pendingWarehouse().any() -> true
                pendingItemCategory().any() -> true
                pendingDataCollection().any() -> true
                pendingRouteProcess().any() -> true
                pendingAssetManteinance().any() -> true
                else -> false
            }
        }

        fun initRequired(): Boolean {
            return if (isRfidRequired()) {
                if (Rfid.rfidDevice == null) {
                    true
                } else {
                    if ((Rfid.rfidDevice is Vh75Bt)) {
                        (Rfid.rfidDevice as Vh75Bt).mState == Vh75Bt.STATE_NONE
                    } else false
                }
            } else false
        }

        fun isRfidRequired(): Boolean {
            if (!prefsGetBoolean(Preference.useBtRfid)) {
                return false
            }

            val btAddress = prefsGetString(Preference.rfidBtAddress)
            return btAddress.isNotEmpty()
        }

        fun isNfcRequired(): Boolean {
            return prefsGetBoolean(Preference.useNfc)
        }

        var RFID: Rfid? = null

        fun removeDataBases() {
            SyncDownload.resetSyncDates()

            // TODO: Eliminar cuando ya no se necesite
            // DbHelper().deleteDb()
            cleanInstance()
            DataBaseHelper().deleteDb()

            SQLiteDatabase.releaseMemory()
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