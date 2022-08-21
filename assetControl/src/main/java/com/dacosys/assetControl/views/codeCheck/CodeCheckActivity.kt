package com.dacosys.assetControl.views.codeCheck

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.isRfidRequired
import com.dacosys.assetControl.databinding.CodeCheckActivityBinding
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scannedCode.ScannedCode
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.views.assets.asset.fragments.AssetDetailFragment
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.locations.warehouseArea.fragments.WarehouseAreaDetailFragment
import java.util.*

class CodeCheckActivity : AppCompatActivity(),
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener {
    private var rejectNewInstances = false

    private var currentFragment: androidx.fragment.app.Fragment? = null

    private fun touchButton(motionEvent: MotionEvent, button: Button) {
        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> {
                button.isPressed = false
                button.performClick()
            }
            MotionEvent.ACTION_DOWN -> {
                button.isPressed = true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch setOnTouchListener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, motionEvent ->
                Statics.closeKeyboard(this)
                if (view is Button && view !is Switch && view !is CheckBox) {
                    touchButton(motionEvent, view)
                    true
                } else {
                    false
                }
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .forEach { setupUI(it) }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("code", binding.codeEditText.text.toString())
    }

    private lateinit var binding: CodeCheckActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = CodeCheckActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun fillPanel(fragment: androidx.fragment.app.Fragment?) {
        if (isFinishing || isDestroyed) return

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
                if (!isFinishing) fragmentTransaction.remove(oldFragment).commitAllowingStateLoss()
            } catch (ex: java.lang.Exception) {
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
                    if (!isFinishing) fragmentTransaction.replace(
                        binding.fragmentLayout.id,
                        newFragment
                    )
                        .commitAllowingStateLoss()
                } catch (ex: java.lang.Exception) {
                }
            }
        }

        currentFragment = fragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired()) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.home, android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_rfid_connect -> {
                JotterListener.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }
            R.id.action_trigger_scan -> {
                JotterListener.trigger(this)
                return super.onOptionsItemSelected(item)
            }
            R.id.action_read_barcode -> {
                JotterListener.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
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
        Statics.closeKeyboard(this)

        // Nada que hacer, volver
        if (scannedCode.isEmpty()) {
            val res = getString(R.string.invalid_code)
            makeText(binding.root, res, SnackbarType.ERROR)
            Log.d(this::class.java.simpleName, res)
            return
        }

        JotterListener.lockScanner(this, true)
        try {
            val sc = ScannedCode(this).getFromCode(
                code = scannedCode,
                searchWarehouseAreaId = true,
                searchAssetCode = true,
                searchAssetSerial = true,
                validateId = true
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
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    override fun scannerCompleted(scanCode: String) {
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
        /*
          This method gets called, when a new Intent gets associated with the current activity instance.
          Instead of creating a new activity, onNewIntent will be called. For more information have a look
          at the documentation.

          In our case this method gets called, when the user attaches a className to the device.
         */
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onReadCompleted(scanCode: String) {
        scannerCompleted(scanCode)
    }

    //endregion READERS Reception


}