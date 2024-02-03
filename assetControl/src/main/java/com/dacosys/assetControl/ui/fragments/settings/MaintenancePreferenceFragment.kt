package com.dacosys.assetControl.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.activities.main.SettingsActivity.Companion.bindPreferenceSummaryToValue
import com.dacosys.assetControl.utils.settings.preferences.Preferences

/**
 * This fragment shows notification preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
class MaintenancePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_webservice_maintenance, key)
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        val prefFragment = MaintenancePreferenceFragment()
        val args = Bundle()
        args.putString("rootKey", preferenceScreen.key)
        prefFragment.arguments = args
        parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null).commit()
    }

    val p = com.dacosys.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindPreferenceSummaryToValue(this, p.acMantWsServer)
        bindPreferenceSummaryToValue(this, p.acMantWsNamespace)

        if (BuildConfig.DEBUG) {
            bindPreferenceSummaryToValue(this, p.acMantWsUser)
            bindPreferenceSummaryToValue(this, p.acMantWsPass)
            bindPreferenceSummaryToValue(this, p.acMantUser)
            bindPreferenceSummaryToValue(this, p.acMantPass)
        }

        findPreference<Preference>(p.acMantWsUseProxy.key)
        bindPreferenceSummaryToValue(this, p.acMantWsProxy)
        bindPreferenceSummaryToValue(this, p.acMantWsProxyPort)

        val button = findPreference<Preference>("ac_mant_test")
        button?.onPreferenceClickListener = OnPreferenceClickListener {
            val urlEditText = findPreference<Preference>(p.acMantWsServer.key)
            val namespaceEditText = findPreference<Preference>(p.acMantWsNamespace.key)

            if (urlEditText != null && namespaceEditText != null) {
                val url = Preferences.prefsGetString(p.acMantWsServer)
                val namespace = Preferences.prefsGetString(p.acMantWsNamespace)
                val urlProxy = Preferences.prefsGetString(p.acMantWsProxy)
                val proxyPort = Preferences.prefsGetInt(p.acMantWsProxyPort)
                val useProxy = Preferences.prefsGetBoolean(p.acMantWsUseProxy)
                val proxyUser = Preferences.prefsGetString(p.acMantWsProxyUser)
                val proxyPass = Preferences.prefsGetString(p.acMantWsProxyPass)

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