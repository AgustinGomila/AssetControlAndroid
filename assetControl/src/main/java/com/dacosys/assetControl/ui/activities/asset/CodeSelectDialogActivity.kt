package com.dacosys.assetControl.ui.activities.asset

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.databinding.CodeSelectActivityBinding
import com.dacosys.assetControl.ui.adapters.asset.AssetAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.showKeyboard
import com.dacosys.assetControl.ui.common.views.custom.ContractsAutoCompleteTextView
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetStringSet
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import kotlin.concurrent.thread


@Suppress("UNCHECKED_CAST")
class CodeSelectDialogActivity : AppCompatActivity(),
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener,
    ContractsAutoCompleteTextView.OnContractsAvailability,
    KeyboardVisibilityEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        closeKeyboard(this)
        binding.autoCompleteTextView.setAdapter(null)
    }

    private var onlyActive: Boolean = true
    private var itemCode: String = ""

    // Configuración guardada de los controles que se ven o no se ven
    private var visibleStatusArray: ArrayList<AssetStatus> = ArrayList()

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        refreshFilterData()
        savedInstanceState.putString("itemCode", itemCode)
        savedInstanceState.putBoolean("onlyActive", onlyActive)
    }

    private lateinit var binding: CodeSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = CodeSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var tempTitle = getString(R.string.search_by_code)

        if (savedInstanceState != null) {
            // Dejo de escuchar estos eventos hasta pasar los valores guardados
            // más adelante se reconectan
            binding.autoCompleteTextView.setOnEditorActionListener(null)
            binding.autoCompleteTextView.onItemClickListener = null
            binding.autoCompleteTextView.onFocusChangeListener = null
            binding.autoCompleteTextView.setOnDismissListener(null)

            onlyActive = savedInstanceState.getBoolean("onlyActive")
            itemCode = savedInstanceState.getString("itemCode") ?: ""

            // Controles de filtrado visibles
            visibleStatusArray.clear()
            val t3 = savedInstanceState.parcelableArrayList<AssetStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (!t1.isNullOrEmpty()) tempTitle = t1

                // Controles de filtrado visibles
                visibleStatusArray.clear()
                val t3 = extras.parcelableArrayList<AssetStatus>("visibleStatusArray")
                if (t3 != null) visibleStatusArray = t3

                if (extras.containsKey("onlyActive")) onlyActive = extras.getBoolean("onlyActive")
                itemCode = extras.getString("itemCode") ?: ""
            }
        }

        title = tempTitle

        binding.codeSelect.setOnClickListener { isBackPressed() }

        binding.clearImageView.setOnClickListener {
            itemCode = ""
            refreshCodeText(cleanText = true, focus = true)
        }

        //Retrieve the values
        val set = prefsGetStringSet(
            Preference.assetSelectFragmentVisibleStatus.key,
            Preference.assetSelectFragmentVisibleStatus.defaultValue as ArrayList<String>
        )

        if (set != null) {
            for (i in set) {
                val status = AssetStatus.getById(i.toInt())
                if (!visibleStatusArray.contains(status)) {
                    visibleStatusArray.add(status)
                }
            }
        }

        // region Setup ITEM_CATEGORY ID AUTOCOMPLETE
        // Set an item click checkedChangedListener for auto complete text view
        binding.autoCompleteTextView.threshold = 1
        binding.autoCompleteTextView.hint = tempTitle
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                itemSelected()
            }
        binding.autoCompleteTextView.setOnContractsAvailability(this)
        binding.autoCompleteTextView.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.autoCompleteTextView.text.length >= binding.autoCompleteTextView.threshold &&
                    binding.autoCompleteTextView.adapter != null &&
                    (binding.autoCompleteTextView.adapter as AssetAdapter).count > 0 &&
                    !binding.autoCompleteTextView.isPopupShowing
                ) {
                    // Display the suggestion dropdown on focus
                    Handler(Looper.getMainLooper()).post {
                        run {
                            adjustAndShowDropDown()
                        }
                    }
                }
            }
        binding.autoCompleteTextView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                showKeyboard(this)
                adjustDropDownHeight()
                return@setOnTouchListener false
            } else if (motionEvent.action == MotionEvent.BUTTON_BACK) {
                closeKeyboard(this)

                setResult(RESULT_CANCELED, null)
                finish()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        binding.autoCompleteTextView.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (binding.autoCompleteTextView.text.length >= binding.autoCompleteTextView.threshold)
                    itemSelected()
                true
            } else {
                false
            }
        }
        // endregion

        KeyboardVisibilityEvent.registerEventListener(this, this)
        refreshCodeText(cleanText = false, focus = true)
        thread { fillAdapter() }
    }

    private fun refreshCodeText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.autoCompleteTextView.setText(
                if (itemCode == "") {
                    if (cleanText) {
                        ""
                    } else {
                        binding.autoCompleteTextView.text.toString()
                    }
                } else {
                    itemCode
                }
            )

            binding.autoCompleteTextView.post {
                binding.autoCompleteTextView.setSelection(
                    binding.autoCompleteTextView.length()
                )
            }

            if (focus) {
                binding.autoCompleteTextView.requestFocus()
            }
        }
    }

    private fun itemSelected() {
        closeKeyboard(this)
        refreshFilterData()

        val data = Intent()
        data.putExtra("itemCode", itemCode)
        setResult(RESULT_OK, data)
        finish()
    }

    // region FILL ADAPTERS
    private var isFilling = false
    private fun fillAdapter() {
        if (isFilling) {
            return
        }

        isFilling = true
        var itemArray: ArrayList<Asset> = ArrayList()
        try {
            Log.d(this::class.java.simpleName, "Selecting assets...")
            itemArray = ArrayList(AssetRepository().select(onlyActive))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }

        val adapter = AssetAdapter(
            activity = this,
            resource = R.layout.asset_simple_row,
            fullList = itemArray,
            suggestedList = ArrayList(),
            visibleStatus = visibleStatusArray
        )

        runOnUiThread {
            showProgressBar(VISIBLE)

            binding.autoCompleteTextView.setAdapter(adapter)
            (binding.autoCompleteTextView.adapter as AssetAdapter).notifyDataSetChanged()

            while (binding.autoCompleteTextView.adapter == null) {
                // Wait for complete loaded
            }
            showProgressBar(GONE)
        }

        refreshCodeText(cleanText = false, focus = false)
        isFilling = false
    }

    private fun showProgressBar(visibility: Int) {
        runOnUiThread {
            binding.progressBar.visibility = visibility
        }
    }

    private fun refreshFilterData() {
        itemCode = binding.autoCompleteTextView.text.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            JotterListener.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        JotterListener.lockScanner(this, false)
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }

    // region SOFT KEYBOARD AND DROPDOWN ISSUES
    override fun onVisibilityChanged(isOpen: Boolean) {
        adjustDropDownHeight()
    }

    private fun calculateDropDownHeight(): Int {
        val r = Rect()
        val mRootWindow = window
        val view: View = mRootWindow.decorView
        view.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    private var isAdjusting = false
    private fun adjustAndShowDropDown() {
        if (isAdjusting) return
        isAdjusting = true
        // TOP LAYOUT
        topLayout()

        val height = calculateDropDownHeight()
        val offsetY = binding.autoCompleteTextView.y + binding.autoCompleteTextView.height
        var newHeight = (height - offsetY).toInt()
        val maxItems = newHeight / AssetAdapter.defaultDropDownItemHeight()
        newHeight = maxItems * AssetAdapter.defaultDropDownItemHeight()
        val maxNeeded =
            ((binding.autoCompleteTextView.adapter
                ?: return) as AssetAdapter).maxHeightNeeded()
        if (maxNeeded < newHeight) {
            newHeight = maxNeeded
        }
        binding.autoCompleteTextView.dropDownHeight = newHeight
        binding.autoCompleteTextView.showDropDown()
        isAdjusting = false
    }

    private var isTopping = false
    private fun topLayout() {
        if (isTopping) return
        isTopping = true
        val set = ConstraintSet()

        set.clone(binding.codeSelect)
        set.clear(binding.gralLayout.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.codeSelect)

        isTopping = false
    }

    private var isCentring = false
    private fun centerLayout() {
        if (isCentring) return
        isCentring = true
        val set = ConstraintSet()

        set.clone(binding.codeSelect)
        set.connect(
            binding.gralLayout.id,
            ConstraintSet.BOTTOM,
            binding.codeSelect.id,
            ConstraintSet.BOTTOM,
            0
        )
        set.applyTo(binding.codeSelect)

        isCentring = false
    }

    private fun adjustDropDownHeight() {
        runOnUiThread {
            if (binding.autoCompleteTextView.isPopupShowing) adjustAndShowDropDown()
            else centerLayout()
        }
    }

    override fun contractsRetrieved(count: Int) {
        // CENTER LAYOUT
        if (count > 0) adjustDropDownHeight() else centerLayout()
    }
    // endregion SOFT KEYBOARD AND DROPDOWN ISSUES

    // region READERS Reception

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onStateChanged(state: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (prefsGetBoolean(Preference.rfidShowConnectedMessage)) {
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