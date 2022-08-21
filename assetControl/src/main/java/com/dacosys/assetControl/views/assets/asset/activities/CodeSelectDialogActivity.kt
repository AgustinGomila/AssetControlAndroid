package com.dacosys.assetControl.views.assets.asset.activities

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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.databinding.CodeSelectActivityBinding
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.commons.views.ContractsAutoCompleteTextView
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
        Statics.closeKeyboard(this)
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
    }

    private lateinit var binding: CodeSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = CodeSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val t3 = savedInstanceState.getParcelableArrayList<AssetStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (t1 != null && t1.isNotEmpty()) tempTitle = t1

                // Controles de filtrado visibles
                visibleStatusArray.clear()
                val t3 = extras.getParcelableArrayList<AssetStatus>("visibleStatusArray")
                if (t3 != null) visibleStatusArray = t3

                if (extras.containsKey("onlyActive")) onlyActive = extras.getBoolean("onlyActive")
                itemCode = extras.getString("itemCode") ?: ""
            }
        }

        title = tempTitle

        binding.codeSelect.setOnClickListener { onBackPressed() }

        binding.clearImageView.setOnClickListener {
            itemCode = ""
            refreshCodeText(cleanText = true, focus = true)
        }

        //Retrieve the values
        val set = Statics.prefsGetStringSet(
            Preference.assetSelectFragmentVisibleStatus.key,
            Preference.assetSelectFragmentVisibleStatus.defaultValue as ArrayList<String>
        )

        if (set != null) {
            for (i in set) {
                val status = AssetStatus.getById(i.toInt())
                if (status != null && !visibleStatusArray.contains(status)) {
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
                Statics.showKeyboard(this)
                adjustDropDownHeight()
                return@setOnTouchListener false
            } else if (motionEvent.action == MotionEvent.BUTTON_BACK) {
                Statics.closeKeyboard(this)

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
        Statics.closeKeyboard(this)
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
            itemArray = AssetDbHelper().select(onlyActive)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }

        val adapter = AssetAdapter(
            activity = this,
            resource = R.layout.asset_simple_row,
            assets = itemArray,
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
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT))
            JotterListener.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)

        try {
            //makeText(getString(R.string.ok), SnackbarType.SUCCESS)
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    override fun onBackPressed() {
        Statics.closeKeyboard(this)

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