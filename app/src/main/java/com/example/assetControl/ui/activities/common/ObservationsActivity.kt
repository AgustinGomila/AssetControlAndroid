package com.example.assetControl.ui.activities.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.databinding.ObservationsActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.utils.errorLog.ErrorLog

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("obs", binding.obsEditText.text.trim().toString())
    }

    private lateinit var binding: ObservationsActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = ObservationsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

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

        val assetArray = AssetRepository().selectByCode(tempCode)
        var asset: Asset? = null
        if (assetArray.isNotEmpty()) {
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
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            ScannerManager.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return svm.showScannedCode
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, true)

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
            ScannerManager.lockScanner(this, false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }
}