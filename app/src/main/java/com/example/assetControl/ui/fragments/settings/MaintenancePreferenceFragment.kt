package com.example.assetControl.ui.fragments.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.ui.activities.main.SettingsActivity
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR

class MaintenancePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_webservice_maintenance, key)
    }

    val p = com.example.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wsServerPref: EditTextPreference? = findPreference(p.acMantWsServer.key)
        wsServerPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsNamespacePref: EditTextPreference? = findPreference(p.acMantWsNamespace.key)
        wsNamespacePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsUserPref: EditTextPreference? = findPreference(p.acMantWsUser.key)
        val wsPassPref: EditTextPreference? = findPreference(p.acMantWsPass.key)
        val userPref: EditTextPreference? = findPreference(p.acMantUser.key)
        val passPref: EditTextPreference? = findPreference(p.acMantPass.key)

        if (BuildConfig.DEBUG) {
            wsUserPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            wsPassPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            userPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            passPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        val wsProxyPref: EditTextPreference? = findPreference(p.acMantWsProxy.key)
        wsProxyPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsProxyPortPref: EditTextPreference? = findPreference(p.acMantWsProxyPort.key)
        wsProxyPortPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val testPref: Preference? = findPreference("ac_mant_test")
        testPref?.onPreferenceClickListener = OnPreferenceClickListener {
            if (wsServerPref != null && wsNamespacePref != null) {
                val url = svm.acMantWsServer
                val namespace = svm.acMantWsNamespace
                val urlProxy = svm.acMantWsProxy
                val proxyPort = svm.acMantWsProxyPort
                val useProxy = svm.acMantWsUseProxy
                val proxyUser = svm.acMantWsProxyUser
                val proxyPass = svm.acMantWsProxyPass

                SettingsActivity.testWsConnection(
                    url = url,
                    namespace = namespace,
                    useProxy = useProxy,
                    proxyUrl = urlProxy,
                    proxyPort = proxyPort,
                    proxyUser = proxyUser,
                    proxyPass = proxyPass,
                    onUiEvent = ::showMessage
                )
            }
            true
        }
    }

    private fun showMessage(msg: String, type: SnackBarType) {
        if (type == ERROR) logError(msg)
        makeText(requireView(), msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)
}