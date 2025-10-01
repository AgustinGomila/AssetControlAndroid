package com.example.assetControl.ui.fragments.settings

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.R
import com.example.assetControl.devices.scanners.Collector
import com.example.assetControl.devices.scanners.collector.CollectorType
import com.example.assetControl.devices.scanners.collector.CollectorTypePreference
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.rfid.RfidType
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.ui.common.snackbar.MakeText
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.utils.Statics.Companion.appHasBluetoothPermission
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.devices.DevicePreference
import com.google.android.gms.common.api.CommonStatusCodes
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread
import com.example.assetControl.utils.settings.config.Preference.Companion as PreferenceConfig

class DevicePreferenceFragment : PreferenceFragmentCompat(), Rfid.RfidDeviceListener {

    private lateinit var printerPref: PreferenceScreen
    private lateinit var rfidPref: PreferenceScreen

    private val context = AssetControlApp.context

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        val prefFragment = DevicePreferenceFragment()

        val args = Bundle()
        args.putString("rootKey", preferenceScreen.key)
        prefFragment.arguments = args

        parentFragmentManager
            .beginTransaction()
            .replace(id, prefFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_devices, key)

        // Llenar solo el fragmento que se ve para evitar NullExceptions
        when (key) {
            "printer" -> setPrinterPref()
            "rfid" -> setRfidPref()
            "symbology" -> setSymbology()
            else -> setDevicePref()
        }
    }

    /**
     * Set device preferences
     * Llena todos los fragmentos de configuración de dispositivos.
     * Se usa cuando estamos en la pantalla principal de configuración de dispositivos.
     */
    private fun setDevicePref() {
        /* Para actualizar el sumario de la preferencia */
        /* PANTALLA DE CONFIGURACIÓN DE LA IMPRESORA */
        printerPref = findPreference<Preference>("printer") as PreferenceScreen
        printerPref.summaryProvider = Preference.SummaryProvider<PreferenceScreen> {
            getPrinterName()
        }

        /* RFID DEVICE */
        rfidPref = findPreference<Preference>("rfid") as PreferenceScreen
        rfidPref.summaryProvider = Preference.SummaryProvider<PreferenceScreen> {
            getRfidSummary()
        }

        setSymbology()
        setPrinterPref()
        setCollectorPref()
        setRfidPref()
    }

    private fun setCollectorPref() {
        val collectorPref: CollectorTypePreference? = findPreference(PreferenceConfig.collectorType.key)
        collectorPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val collectorListPref: CollectorTypePreference? = findPreference(PreferenceConfig.collectorType.key)
        if (collectorListPref?.value == null) {
            // to ensure we don't selectByItemId a null value
            // set first value by default
            collectorListPref?.setValueIndex(0)
        }

        collectorListPref?.summaryProvider = Preference.SummaryProvider<CollectorTypePreference> { preference ->
            val text = preference.entry
            if (text.isNullOrEmpty()) {
                getString(R.string.there_is_no_rfid_device_selected)
            } else {
                text
            }
        }
        collectorListPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            preference.summary = CollectorType.getById(newValue.toString().toInt()).description
            Collector.collectorTypeChanged = true
            true
        }
    }

    private fun setupRfidReader() {
        try {
            if (Rfid.isRfidRequired(this::class)) {
                Rfid.setListener(this, RfidType.vh75)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            showSnackBar(SnackBarEventData(getString(R.string.rfid_reader_not_initialized), SnackBarType.INFO))
            ErrorLog.writeLog(activity, this::class.java.simpleName, ex)
        }
    }

    private val ipv4Regex =
        "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"

    private val ipv4Pattern: Pattern = Pattern.compile(ipv4Regex)

    fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dStart: Int, dEnd: Int): CharSequence? {
        if (source == "") return null // Para el backspace
        val builder = java.lang.StringBuilder(dest.toString())
        builder.replace(dStart, dEnd, source.subSequence(start, end).toString())
        val matcher: Matcher = ipv4Pattern.matcher(builder)
        return if (!matcher.matches()) "" else null
    }

    private fun getPrinterName(): String {
        val useBtPrinter = sr.prefsGetBoolean(PreferenceConfig.useBtPrinter)
        val useNetPrinter = sr.prefsGetBoolean(PreferenceConfig.useNetPrinter)
        val ipNetPrinter = sr.prefsGetString(PreferenceConfig.ipNetPrinter)
        val printerBtAddress = sr.prefsGetString(PreferenceConfig.printerBtAddress)
        val portNetPrinter = sr.prefsGetString(PreferenceConfig.portNetPrinter)

        val r = if (!useBtPrinter && !useNetPrinter) {
            getString(R.string.disabled)
        } else if (useBtPrinter && (printerBtAddress.isEmpty() || printerBtAddress == "0")) {
            getString(R.string.there_is_no_selected_printer)
        } else if (useNetPrinter && ipNetPrinter.isEmpty()) {
            getString(R.string.there_is_no_selected_printer)
        } else {
            when {
                useBtPrinter -> printerBtAddress
                else -> "$ipNetPrinter ($portNetPrinter)"
            }
        }
        return r
    }

    private fun setSymbology() {
        val resetSymbologyPref: Preference? = findPreference("restore_to_default")
        resetSymbologyPref?.onPreferenceClickListener = OnPreferenceClickListener {
            val diaBox = askForResetSymbology()
            diaBox.show()
            true
        }
    }

    private fun askForResetSymbology(): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.default_values))
            .setMessage(getString(R.string.do_you_want_to_restore_the_default_system_symbology_settings))
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog, _ ->
                resetSymbology()
                dialog.dismiss()
            }.setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun resetSymbology() {
        setDefaultSymbology()
        updateSymbologySwitchPreferences()
    }

    private fun setDefaultSymbology() {
        val allSymbology = PreferenceConfig.getSymbology()
        for (s in allSymbology) {
            sr.prefsPutBoolean(s.key, s.defaultValue as Boolean)
        }
    }

    private fun updateSymbologySwitchPreferences() {
        val preferenceScreen = preferenceScreen
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference is SwitchPreference) {
                val allSymbology = PreferenceConfig.getSymbology()
                val pref = allSymbology.firstOrNull { it.key == preference.key } ?: continue
                val defaultValue = pref.defaultValue as Boolean
                preference.isChecked = defaultValue
            }
        }
    }

    private fun setPrinterPref() {
        resultForPrinterBtPermissions()
    }

    private fun resultForPrinterBtPermissions() {
        if (!appHasBluetoothPermission()) {
            requestPermissionsPrinter.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            setPrinter()
        }
    }

    private val requestPermissionsPrinter = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            setPrinter()
        }
    }

    private fun setPrinter() {
        val deviceListPref: DevicePreference? = findPreference(PreferenceConfig.printerBtAddress.key)
        if (deviceListPref?.value == null) {
            // to ensure we don't selectByItemId a null value
            // set first value by default
            deviceListPref?.setValueIndex(0)
        }

        deviceListPref?.summaryProvider = Preference.SummaryProvider<DevicePreference> { preference ->
            val text = preference.entry
            if (text.isNullOrEmpty()) {
                getString(R.string.there_is_no_selected_printer)
            } else {
                text
            }
        }
        deviceListPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
            Collector.collectorTypeChanged = true
            true
        }

        //region //// PRINTER IP / PORT
        val portNetPrinterPref: EditTextPreference? = findPreference(PreferenceConfig.portNetPrinter.key)
        portNetPrinterPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val ipNetPrinterPref: EditTextPreference? = findPreference(PreferenceConfig.ipNetPrinter.key)
        ipNetPrinterPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        ipNetPrinterPref?.setOnBindEditTextListener { editText ->
            val filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                val input = (dest.toString() + source.toString())
                if (input.matches(Regex("^([0-9]{1,3}\\.){0,3}[0-9]{0,3}\$"))) {
                    val segments = input.split('.')
                    if (segments.all { it.isEmpty() || (it.toIntOrNull() in 0..255) }) {
                        return@InputFilter null
                    }
                }
                ""
            })
            editText.filters = filters
        }

        portNetPrinterPref?.setOnPreferenceChangeListener { _, newValue ->
            if (sr.prefsGetBoolean(PreferenceConfig.useNetPrinter) && newValue != null) {
                portNetPrinterPref.summary = newValue.toString()
            }
            true
        }
        //endregion //// PRINTER IP / PORT

        //region //// USE BLUETOOTH / NET PRINTER
        val swPrefBtPrinter: SwitchPreference? = findPreference(PreferenceConfig.useBtPrinter.key)
        val swPrefNetPrinter: SwitchPreference? = findPreference(PreferenceConfig.useNetPrinter.key)

        swPrefBtPrinter?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) swPrefNetPrinter?.isChecked = false
            else deviceListPref?.entry?.toString().orEmpty()
            true
        }
        swPrefNetPrinter?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) swPrefBtPrinter?.isChecked = false
            true
        }
        //endregion //// USE BLUETOOTH / NET PRINTER

        //region //// POTENCIA Y VELOCIDAD
        val maxPower = 23
        val printerPowerPref: EditTextPreference? = findPreference(PreferenceConfig.printerPower.key)
        printerPowerPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        printerPowerPref?.setOnBindEditTextListener {
            val filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                try {
                    val input = (dest.toString() + source.toString()).toInt()
                    if (input in 1 until maxPower) return@InputFilter null
                } catch (_: NumberFormatException) {
                }
                ""
            })
            it.filters = filters
        }

        val maxSpeed = 10
        val printerSpeedPref: EditTextPreference? = findPreference(PreferenceConfig.printerSpeed.key)
        printerSpeedPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        printerSpeedPref?.setOnBindEditTextListener {
            val filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                try {
                    val input = (dest.toString() + source.toString()).toInt()
                    if (input in 1 until maxSpeed) return@InputFilter null
                } catch (_: NumberFormatException) {
                }
                ""
            })
            it.filters = filters
        }
        //endregion //// POTENCIA Y VELOCIDAD

        //region //// CARÁCTER DE SALTO DE LÍNEA
        val swPrefCharLF: SwitchPreference? = findPreference("conf_printer_new_line_char_lf")
        val swPrefCharCR: SwitchPreference? = findPreference("conf_printer_new_line_char_cr")

        val lineSeparator = sr.prefsGetString(PreferenceConfig.lineSeparator)
        if (lineSeparator == Char(10).toString()) swPrefCharLF?.isChecked
        else if (lineSeparator == Char(13).toString()) swPrefCharCR?.isChecked

        swPrefCharLF?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                sr.prefsPutString(
                    PreferenceConfig.lineSeparator.key, Char(10).toString()
                )
                swPrefCharCR?.isChecked = false
            }
            true
        }

        swPrefCharCR?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                sr.prefsPutString(
                    PreferenceConfig.lineSeparator.key, Char(13).toString()
                )
                swPrefCharLF?.isChecked = false
            }
            true
        }
        //endregion //// CARÁCTER DE SALTO DE LÍNEA
    }

    private val resultForRfidConnect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CommonStatusCodes.SUCCESS ||
                it.resultCode == CommonStatusCodes.SUCCESS_CACHE
            ) {
                setupRfidReader()
            }
        }

    private fun getRfidSummary(): String {
        var rfidSummary =
            if (sr.prefsGetBoolean(PreferenceConfig.useBtRfid))
                getString(R.string.enabled)
            else
                getString(R.string.disabled)

        val btAddress = sr.prefsGetString(PreferenceConfig.rfidBtAddress)
        if (btAddress.isNotEmpty() && btAddress != 0.toString()) {
            var description = btAddress
            val btName = sr.prefsGetString(PreferenceConfig.rfidBtName)
            if (btName.isNotEmpty() && btName != 0.toString()) description = btName
            rfidSummary = "$rfidSummary: $description"
        }

        return rfidSummary
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onReadCompleted(scanCode: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onStateChanged(state: Int) {
        if (sr.prefsGetBoolean(PreferenceConfig.rfidShowConnectedMessage)) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    showSnackBar(
                        SnackBarEventData(
                            getString(R.string.rfid_connected),
                            SnackBarType.SUCCESS
                        )
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    showSnackBar(
                        SnackBarEventData(
                            getString(R.string.searching_rfid_reader),
                            SnackBarType.RUNNING
                        )
                    )
                }

                else -> {
                    showSnackBar(
                        SnackBarEventData(
                            getString(R.string.there_is_no_rfid_device_connected),
                            SnackBarType.INFO
                        )
                    )
                }
            }
        }
    }

    private fun setRfidPref() {
        resultForRfidBtPermissions()
    }

    private val vh75: Vh75Bt?
        get() = Rfid.vh75.takeIf { it?.state == Vh75Bt.STATE_CONNECTED }

    private fun resultForRfidBtPermissions() {
        if (!appHasBluetoothPermission()) {
            requestPermissionsRfid.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            setRfid()
        }
    }

    private val requestPermissionsRfid = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            setRfid()
        }
    }

    private fun setRfid() {
        //region //// USE RFID
        val swBtRfidSwitchPref: SwitchPreference? = findPreference(PreferenceConfig.useBtRfid.key)

        swBtRfidSwitchPref?.setOnPreferenceChangeListener { _, _ ->
            thread { connectToRfidDevice() }
            true
        }
        //endregion //// USE RFID

        //region //// BLUETOOTH NAME
        val rfidNamePref: EditTextPreference? = findPreference(PreferenceConfig.rfidBtName.key)
        rfidNamePref?.setOnPreferenceClickListener {
            if (vh75?.state != Vh75Bt.STATE_CONNECTED) {
                showSnackBar(
                    SnackBarEventData(
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.ERROR
                    )
                )
            }
            true
        }
        rfidNamePref?.setOnPreferenceChangeListener { _, newValue ->
            if (vh75?.state == Vh75Bt.STATE_CONNECTED) {
                vh75?.setBluetoothName(newValue.toString())
                rfidNamePref.summary = newValue.toString()
            } else {
                showSnackBar(
                    SnackBarEventData(
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.ERROR
                    )
                )
            }
            true
        }
        rfidNamePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        //endregion //// BLUETOOTH NAME

        //region //// DEVICE LIST PREFERENCE
        val deviceListPref: DevicePreference? = findPreference(PreferenceConfig.rfidBtAddress.key)
        if (deviceListPref?.value == null) {
            // to ensure we don't selectByItemId a null value
            // set first value by default
            deviceListPref?.setValueIndex(0)
        }
        deviceListPref?.summaryProvider = Preference.SummaryProvider<DevicePreference> { preference ->
            val text = preference.entry
            if (text.isNullOrEmpty()) {
                getString(R.string.there_is_no_selected_rfid_scanner)
            } else {
                text
            }
        }
        deviceListPref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                val entry = deviceListPref.entry
                if (!entry.isNullOrEmpty()) {
                    sr.prefsPutString(PreferenceConfig.rfidBtName.key, entry.toString())
                }
                true
            }
        //endregion //// DEVICE LIST PREFERENCE

        //region //// RFID POWER
        val rfidReadPower: SeekBarPreference? = findPreference(PreferenceConfig.rfidReadPower.key)
        rfidReadPower?.setOnPreferenceChangeListener { _, newValue ->
            rfidReadPower.summary = "$newValue dB"
            true
        }
        rfidReadPower?.summaryProvider = Preference.SummaryProvider<SeekBarPreference> { preference ->
            "${preference.value} dB"
        }
        //endregion //// RFID POWER

        //region //// RESET TO FACTORY
        val resetPref: Preference? = findPreference("rfid_reset_to_factory")
        resetPref?.onPreferenceClickListener = OnPreferenceClickListener {
            if (vh75?.state == Vh75Bt.STATE_CONNECTED) {
                val diaBox = askForResetToFactory()
                diaBox.show()
            } else {
                showSnackBar(
                    SnackBarEventData(
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.ERROR
                    )
                )
            }
            true
        }
        //endregion //// RESET TO FACTORY

        thread { connectToRfidDevice() }
    }

    private fun connectToRfidDevice() {
        if (!sr.prefsGetBoolean(PreferenceConfig.useBtRfid)) return

        val bluetoothManager =
            context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            showSnackBar(
                SnackBarEventData(
                    getString(R.string.there_are_no_bluetooth_devices), SnackBarType.INFO
                )
            )
        } else {
            if (mBluetoothAdapter.isEnabled) {
                setupRfidReader()
                return
            }

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            resultForRfidConnect.launch(enableBtIntent)
        }
    }

    private fun askForResetToFactory(): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.reset_to_factory))
            .setMessage(getString(R.string.you_want_to_reset_the_rfid_device_to_its_factory_settings))
            .setPositiveButton(
                getString(R.string.reset)
            ) { dialog, _ ->
                vh75?.resetToFactory()
                dialog.dismiss()
            }.setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun showSnackBar(it: SnackBarEventData) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return

        MakeText.makeText(requireView(), it.text, it.snackBarType)
    }
}