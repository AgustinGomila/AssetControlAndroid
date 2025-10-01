package com.example.assetControl.ui.fragments.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.ui.activities.main.SettingsActivity

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
                val url = sr.prefsGetString(p.acMantWsServer)
                val namespace = sr.prefsGetString(p.acMantWsNamespace)
                val urlProxy = sr.prefsGetString(p.acMantWsProxy)
                val proxyPort = sr.prefsGetInt(p.acMantWsProxyPort)
                val useProxy = sr.prefsGetBoolean(p.acMantWsUseProxy)
                val proxyUser = sr.prefsGetString(p.acMantWsProxyUser)
                val proxyPass = sr.prefsGetString(p.acMantWsProxyPass)

                SettingsActivity.testWsConnection(
                    parentView = requireView(),
                    url = url,
                    namespace = namespace,
                    useProxy = useProxy,
                    proxyUrl = urlProxy,
                    proxyPort = proxyPort,
                    proxyUser = proxyUser,
                    proxyPass = proxyPass
                )
            }
            true
        }
    }
}