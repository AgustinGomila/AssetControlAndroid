package com.dacosys.assetControl.network.utils

import android.app.Activity
import android.content.DialogInterface
import android.graphics.drawable.InsetDrawable
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences
import org.json.JSONObject
import java.lang.ref.WeakReference

class ClientPackage {
    companion object : DialogInterface.OnMultiChoiceClickListener {

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
        private var finishByUser = false

        private fun getValidProducts(): ArrayList<String> {
            val r: ArrayList<String> = ArrayList()
            r.add(Statics.APP_VERSION_ID.toString())
            r.add(Statics.APP_VERSION_ID_IMAGE_CONTROL.toString())
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
            if (activity.isDestroyed || activity.isFinishing) return

            allProductsArray.clear()
            for (pack in allPackage) {
                val pvId = pack.getString("product_version_id")
                if (ValidProducts.contains(pvId) && !allProductsArray.contains(pack)) {
                    allProductsArray.add(pack)
                }
            }

            // Comprobamos que el usuario tenga opciones para elegir o podemos configurar el sistema de forma automática.
            val allMainApp =
                allProductsArray.mapNotNull { if (it.getString("product_version_id") == Statics.APP_VERSION_ID.toString()) it else null }
            val allSecondApp =
                allProductsArray.mapNotNull { if (it.getString("product_version_id") == Statics.APP_VERSION_ID_IMAGE_CONTROL.toString()) it else null }

            if (allMainApp.isEmpty() && allSecondApp.isEmpty()) {
                MakeText.makeText(
                    parentView,
                    AssetControlApp.context
                        .getString(R.string.there_are_no_valid_products_for_the_selected_client),
                    SnackBarType.ERROR
                )
                return
            }

            if (allMainApp.isEmpty() || allMainApp.size == 1) {
                // Disponible UNA o NINGUNA sola aplicación principal

                if (allSecondApp.isEmpty() || allSecondApp.size == 1) {
                    // Disponible UNA o NINGUNA aplicación secundaria (ImageControl)

                    val apps = when {
                        allMainApp.any() && allSecondApp.any() -> arrayListOf(allMainApp.first(), allSecondApp.first())
                        allMainApp.any() -> arrayListOf(allMainApp.first())
                        else -> arrayListOf(allSecondApp.first())
                    }

                    setConfigPanel(
                        parentView = parentView,
                        callback = callback,
                        packArray = apps,
                        email = email,
                        password = password
                    )
                    return
                }
            }

            // Unimos y ordenamos la lista para que los diferentes tipos de paquetes queden agrupados
            validProductsArray =
                ArrayList(allMainApp.union(allSecondApp).sortedBy { it.getString("product_version_id") })
            val listItems: ArrayList<String> = ArrayList()

            for (pack in validProductsArray) {
                val clientPackage = pack.getString("client_package_content_description")
                val installationCode = pack.getString("installation_code")
                val packageStr = "$clientPackage (${installationCode})"

                listItems.add(packageStr)
            }

            selected = BooleanArray(validProductsArray.size)

            finishByUser = false
            val cw = ContextThemeWrapper(activity, R.style.AlertDialogTheme)
            val builder = AlertDialog.Builder(cw)

            val title = TextView(activity)
            title.text = String.format(
                "%s - %s",
                allProductsArray.first().getString("client"),
                AssetControlApp.context.getString(R.string.select_package)
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

                if (selectedPacks.isNotEmpty()) {
                    setConfigPanel(
                        parentView = parentView,
                        callback = callback,
                        packArray = selectedPacks,
                        email = email,
                        password = password
                    )
                }
                finishByUser = true
                dialog.dismiss()
            }
            builder.setOnDismissListener {
                if (!finishByUser) callback.onTaskConfigPanelEnded(ProgressStatus.canceled)
            }

            val layoutDefault = ResourcesCompat.getDrawable(
                AssetControlApp.context.resources, R.drawable.layout_thin_border, null
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
                    MakeText.makeText(
                        parentView,
                        AssetControlApp.context.getString(R.string.inactive_installation),
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
                    MakeText.makeText(
                        parentView,
                        AssetControlApp.context
                            .getString(R.string.app_panel_url_can_not_be_obtained),
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

                val x = Preferences.prefs.edit()
                if (productId == Statics.APP_VERSION_ID.toString()) {
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
                } else if (productId == Statics.APP_VERSION_ID_IMAGE_CONTROL.toString()) {
                    x.putBoolean(Preference.useImageControl.key, true)

                    x.putString(Preference.icWsServer.key, url)
                    x.putString(Preference.icWsNamespace.key, namespace)
                    x.putString(Preference.icWsUser.key, user)
                    x.putString(Preference.icWsPass.key, pass)
                }
                run { x.apply() }
            }

            DownloadDb.downloadDbRequired = true
            callback.onTaskConfigPanelEnded(ProgressStatus.finished)
        }
        // endregion
    }
}