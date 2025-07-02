package com.dacosys.assetControl.network.utils

import android.app.Activity
import android.graphics.Typeface
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.ref.WeakReference

class Proxy {
    companion object {

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
            if (activity.isDestroyed || activity.isFinishing) return

            if (avoidSetupProxyDialog) {
                return
            }

            avoidSetupProxyDialog = true

            val alert: AlertDialog.Builder = AlertDialog.Builder(activity)
            alert.setTitle(
                AssetControlApp.context.getString(R.string.configure_proxy_question)
            )

            val proxyEditText = EditText(activity)
            proxyEditText.hint = AssetControlApp.context.getString(R.string.proxy)
            proxyEditText.isFocusable = true
            proxyEditText.isFocusableInTouchMode = true

            val proxyPortEditText = EditText(activity)
            proxyPortEditText.inputType = InputType.TYPE_CLASS_NUMBER
            proxyPortEditText.hint = AssetControlApp.context.getString(R.string.port)
            proxyPortEditText.isFocusable = true
            proxyPortEditText.isFocusableInTouchMode = true

            val proxyUserEditText = EditText(activity)
            proxyUserEditText.inputType = InputType.TYPE_CLASS_TEXT
            proxyUserEditText.hint = AssetControlApp.context.getString(R.string.user)
            proxyUserEditText.isFocusable = true
            proxyUserEditText.isFocusableInTouchMode = true

            val proxyPassEditText = TextInputEditText(activity)
            proxyPassEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            proxyPassEditText.hint = AssetControlApp.context.getString(R.string.password)
            proxyPassEditText.isFocusable = true
            proxyPassEditText.isFocusableInTouchMode = true
            proxyPassEditText.typeface = Typeface.DEFAULT
            proxyPassEditText.transformationMethod = PasswordTransformationMethod()

            val inputLayout = TextInputLayout(activity)
            inputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            inputLayout.addView(proxyPassEditText)

            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL

            layout.addView(proxyEditText)
            layout.addView(proxyPortEditText)
            layout.addView(proxyUserEditText)
            layout.addView(inputLayout)

            alert.setView(layout)
            alert.setNegativeButton(R.string.no) { _, _ ->
                with(prefs.edit()) {
                    putBoolean(Preference.acWsUseProxy.key, false).apply()
                }
            }
            alert.setPositiveButton(R.string.yes) { _, _ ->
                val proxy = proxyEditText.text
                val port = proxyPortEditText.text
                val user = proxyUserEditText.text
                val pass = proxyPassEditText.text

                with(prefs.edit()) {
                    if (proxy != null) {
                        putBoolean(Preference.acWsUseProxy.key, true).apply()
                        putString(Preference.acWsProxy.key, proxy.toString()).apply()
                    }

                    if (port != null) {
                        val portNumber = try {
                            Integer.parseInt(port.toString())
                        } catch (_: java.lang.NumberFormatException) {
                            0
                        }
                        putInt(Preference.acWsProxyPort.key, portNumber).apply()
                    }

                    if (user.isNotEmpty()) {
                        putString(Preference.acWsProxyUser.key, user.toString()).apply()
                    }

                    if (!pass.isNullOrEmpty()) {
                        putString(Preference.acWsProxyPass.key, pass.toString()).apply()
                    }
                }
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
    }
}