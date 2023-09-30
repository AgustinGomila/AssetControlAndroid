package com.dacosys.assetControl.ui.activities.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.databinding.ObservationsActivityBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.settings.Preference

class ObservationsActivity : AppCompatActivity(), Scanner.ScannerListener {
    private var obs = ""
    private var autoText = ""
    private val newLine = "\n"
    private val divisionChar = ";"

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("obs", binding.obsEditText.text.trim().toString())
    }

    private lateinit var binding: ObservationsActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = ObservationsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.observations)

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getString("obs")
            if (t1 != null) {
                obs = t1
            }
        }

        val extras = intent.extras
        if (extras != null) {
            val t1 = extras.getString("obs")
            if (t1 != null) {
                obs = t1
            }
        }

        binding.okButton.setOnClickListener { confirmObs() }
        binding.cancelButton.setOnClickListener { cancelObs() }

        fillControls()

        setupUI(binding.root, this)
    }

    private fun confirmObs() {
        closeKeyboard(this)

        val data = Intent()
        data.putExtra("obs", binding.obsEditText.text.trim().toString())
        setResult(RESULT_OK, data)
        finish()
    }

    private fun fillControls() {
        runOnUiThread {
            binding.obsEditText.setText(obs, TextView.BufferType.EDITABLE)
        }
    }

    private fun cancelObs() {
        closeKeyboard(this)

        setResult(RESULT_CANCELED, null)
        finish()
    }

    private fun addAutoText(code: String) {
        var tempCode = code
        var labelNumber: Int? = null
        val tempPos: Int = tempCode.indexOf(divisionChar)

        if (tempPos >= 0) {
            try {
                val tempLabelNumber: String = tempCode.substring(tempPos + 1)
                tempCode = tempCode.substring(0, tempCode.length - (tempLabelNumber.length + 1))
                labelNumber = tempLabelNumber.toInt()
            } catch (ex: Exception) {
                labelNumber = null
                tempCode = code
            }
        }

        autoText = ""

        val assetArray = AssetDbHelper().selectByCode(tempCode)
        var asset: Asset? = null
        if (assetArray.size > 0) {
            asset = assetArray.first()
        }

        var description = ""
        if (asset != null) {
            description = ", " + asset.description
        }

        if (binding.codePasteSwitch.isChecked) {
            autoText += tempCode + description + newLine
        }

        if (binding.autoTextSwitch.isChecked) {
            when {
                asset == null -> {
                    autoText += "|_ ${getString(R.string.does_not_exist_in_the_database)}$newLine"
                    return
                }

                labelNumber == 0 -> {
                    autoText += "|_ ${getString(R.string.it_has_a_label_valid_only_in_reports)}$newLine"
                    return
                }

                (labelNumber ?: return) < if (asset.labelNumber == null) {
                    0
                } else {
                    (asset.labelNumber ?: return).toInt()
                } -> autoText += "|_ ${getString(R.string.it_has_a_disabled_tag)}$newLine"
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT))
            JotterListener.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        JotterListener.lockScanner(this, true)

        try {
            addAutoText(scanCode)
            val obsText = binding.obsEditText.text.toString() + autoText

            runOnUiThread {
                binding.obsEditText.setText(obsText, TextView.BufferType.EDITABLE)
                binding.obsEditText.setSelection(binding.obsEditText.text.length)
                binding.obsEditText.scrollTo(0, binding.obsEditText.bottom)
            }

            makeText(binding.root, getString(R.string.ok), SnackBarType.SUCCESS)
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        closeKeyboard(this)

        setResult(RESULT_CANCELED)
        finish()
    }
}