package com.example.assetControl.ui.activities.code

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.databinding.CodeCheckActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.rfid.Rfid.Companion.isRfidRequired
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.asset.AssetDetailFragment
import com.example.assetControl.ui.fragments.location.WarehouseAreaDetailFragment
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.Preference
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class CodeCheckActivity : AppCompatActivity(),
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener {
    private var rejectNewInstances = false

    private var currentFragment: androidx.fragment.app.Fragment? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("code", binding.codeEditText.text.toString())
    }

    private lateinit var binding: CodeCheckActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = CodeCheckActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState != null) {
            binding.codeEditText.setText(
                savedInstanceState.getString("code"),
                TextView.BufferType.EDITABLE
            )
        }

        binding.codeEditText.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                fillHexTextView()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            clearControls()
        } else {
            fillHexTextView()
        }

        binding.codeEditText.requestFocus()

        setupUI(binding.root, this)
    }

    private fun fillPanel(fragment: androidx.fragment.app.Fragment?) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        var newFragment: androidx.fragment.app.Fragment? = null
        if (fragment != null) {
            newFragment = fragment
        }

        var oldFragment: androidx.fragment.app.Fragment? = null
        if (currentFragment != null) {
            oldFragment = currentFragment
        }

        var fragmentTransaction = supportFragmentManager.beginTransaction()
        if (oldFragment != null) {
            try {
                if (!isFinishing && !isDestroyed) {
                    fragmentTransaction
                        .remove(oldFragment)
                        .commitAllowingStateLoss()
                }
            } catch (ignored: java.lang.Exception) {
            }
        }

        // Close keyboard in transition
        if (currentFocus != null) {
            val inputManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                (currentFocus ?: return).windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        if (newFragment != null) {
            runOnUiThread {
                fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(
                    R.anim.animation_fade_in,
                    R.anim.animation_fade_out
                )
                try {
                    if (!isFinishing && !isDestroyed) {
                        fragmentTransaction
                            .replace(binding.fragmentLayout.id, newFragment)
                            .commitAllowingStateLoss()
                    }
                } catch (ignored: java.lang.Exception) {
                }
            }
        }

        currentFragment = fragment
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        if (BuildConfig.DEBUG || Statics.DEMO_MODE) {
            menu.add(Menu.NONE, menuItemManualCode, Menu.NONE, "Manual code")
            menu.add(Menu.NONE, menuItemRandomCode, Menu.NONE, "Random asset code")
            menu.add(Menu.NONE, menuItemRandomWa, Menu.NONE, "Random área")
            menu.add(Menu.NONE, menuItemRandomSerial, Menu.NONE, "Random asset serial")
        }

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    private val menuItemRandomCode = 999001
    private val menuItemManualCode = 999002
    private val menuItemRandomWa = 999004
    private val menuItemRandomSerial = 999005

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                return true
            }

            R.id.action_rfid_connect -> {
                ScannerManager.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_trigger_scan -> {
                ScannerManager.trigger(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_read_barcode -> {
                ScannerManager.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomCode -> {
                val allCodes = AssetRepository().selectAllCodes()
                if (allCodes.any()) scannerCompleted(allCodes[Random().nextInt(allCodes.count())])
                return super.onOptionsItemSelected(item)
            }

            menuItemManualCode -> {
                enterCode()
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomWa -> {
                val allWa = WarehouseAreaRepository().select(true)
                if (allWa.any()) {
                    val waId = allWa[Random().nextInt(allWa.count())].id
                    scannerCompleted("#WA#${String.format("%05d", waId)}#")
                }
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomSerial -> {
                val allSerials = AssetRepository().selectAllSerials()
                if (allSerials.any()) scannerCompleted(allSerials[Random().nextInt(allSerials.count())])
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun enterCode() {
        runOnUiThread {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.enter_code))

            val inputLayout = TextInputLayout(this)
            inputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

            val input = TextInputEditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
            input.isFocusable = true
            input.isFocusableInTouchMode = true
            input.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (alertDialog != null) {
                                alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
                                    .performClick()
                            }
                        }
                    }
                }
                false
            }

            inputLayout.addView(input)
            builder.setView(inputLayout)
            builder.setPositiveButton(R.string.accept) { _, _ ->
                scannerCompleted(input.text.toString())
            }
            builder.setNegativeButton(R.string.cancel, null)
            alertDialog = builder.create()

            alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            alertDialog.show()
            input.requestFocus()
        }
    }

    private fun clearControls() {
        fillPanel(null)
        binding.infoTextView.setText("", TextView.BufferType.EDITABLE)
    }

    @SuppressLint("DefaultLocale")
    private fun fillHexTextView() {
        clearControls()

        val scannedCode = binding.codeEditText.text.toString()

        if (scannedCode.trim().isEmpty()) {
            return
        }

        val buf = StringBuilder(200)
        for (ch in scannedCode.toCharArray()) {
            if (buf.isNotEmpty())
                buf.append(' ')
            buf.append(String.format("%04x", ch.code).uppercase(Locale.getDefault()))
        }

        binding.hexTextView.text = buf.toString()
        closeKeyboard(this)

        // Nada que hacer, volver
        if (scannedCode.isEmpty()) {
            val res = getString(R.string.invalid_code)
            makeText(binding.root, res, SnackBarType.ERROR)
            Log.d(this::class.java.simpleName, res)
            return
        }

        ScannerManager.lockScanner(this, true)
        try {
            val sc = ScannedCode(this).getFromCode(
                code = scannedCode,
                searchWarehouseAreaId = true,
                searchAssetCode = true,
                searchAssetSerial = true,
                searchAssetEan = true
            )

            if (sc.codeFound) {
                when {
                    sc.warehouseArea != null -> {
                        fillPanel(
                            WarehouseAreaDetailFragment.newInstance(
                                sc.warehouseArea ?: return
                            )
                        )
                        binding.infoTextView.setText(
                            R.string.the_code_belongs_to_an_area,
                            TextView.BufferType.EDITABLE
                        )
                    }

                    sc.asset != null -> {
                        fillPanel(AssetDetailFragment.newInstance(sc.asset ?: return))
                        binding.infoTextView.setText(
                            R.string.the_code_belongs_to_an_asset,
                            TextView.BufferType.EDITABLE
                        )
                    }

                    else -> {
                        fillPanel(null)
                        binding.infoTextView.setText(
                            R.string.the_code_does_not_belong_to_any_asset_or_area_in_the_database,
                            TextView.BufferType.EDITABLE
                        )
                    }
                }
            } else {
                binding.infoTextView.setText(
                    R.string.the_code_does_not_belong_to_any_asset_or_area_in_the_database,
                    TextView.BufferType.EDITABLE
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    private val showScannedCode: Boolean
        get() {
            return sr.prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        runOnUiThread {
            binding.codeEditText.setText(scanCode)
            binding.codeEditText.dispatchKeyEvent(
                KeyEvent(
                    0,
                    0,
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_ENTER,
                    0
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    // region READERS Reception

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onStateChanged(state: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (sr.prefsGetBoolean(Preference.rfidShowConnectedMessage)) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    makeText(
                        binding.root,
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    makeText(
                        binding.root,
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    makeText(
                        binding.root,
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.INFO
                    )
                }
            }
        }
    }

    override fun onReadCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        scannerCompleted(scanCode)
    }

    //endregion READERS Reception
}