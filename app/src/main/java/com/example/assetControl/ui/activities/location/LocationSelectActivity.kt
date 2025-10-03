package com.example.assetControl.ui.activities.location

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
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.async.location.WarehouseAreaChangedObserver
import com.example.assetControl.data.async.location.WarehouseChangedObserver
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.room.repository.location.WarehouseRepository
import com.example.assetControl.databinding.LocationSelectActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.ui.adapters.location.WarehouseAdapter
import com.example.assetControl.ui.adapters.location.WarehouseAreaAdapter
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.common.utils.Screen.Companion.showKeyboard
import com.example.assetControl.ui.common.views.custom.ContractsAutoCompleteTextView
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.parcel.Parcelables.parcelableArrayList
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.registerEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.parceler.Parcels
import kotlin.concurrent.thread


class LocationSelectActivity : AppCompatActivity(),
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener,
    ContractsAutoCompleteTextView.OnContractsAvailability,
    WarehouseChangedObserver,
    WarehouseAreaChangedObserver,
    KeyboardVisibilityEventListener {
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
        if (showScannedCode) showMessage(scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, true)

        try {
            val sc = ScannedCode(this).getFromCode(
                code = scanCode,
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                searchAssetEan = false
            )

            var locationOk = false
            if (sc.codeFound && sc.warehouseArea != null) {
                // No hay ningún área cargada
                if (warehouseArea == null) {
                    warehouseArea = sc.warehouseArea
                    locationOk = true
                }
            } else {
                showMessage(
                    getString(R.string.code_read_does_not_correspond_to_a_location),
                    ERROR
                )
            }

            if (locationOk) {
                locationSelect()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            showMessage(
                ex.message.toString(),
                ERROR
            )
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    override fun onWarehouseChanged(w: Warehouse?) {
        warehouseArea = null
        fillWarehouseAreaAdapter()
    }

    override fun onWarehouseAreaChanged(w: WarehouseArea?) {
    }

    private var wChangedListener: WarehouseChangedObserver? = null
    private var waChangedListener: WarehouseAreaChangedObserver? = null

    private var onlyActive: Boolean = true
    private var warehouse: Warehouse? = null
    private var warehouseDescription: String = ""
    private var warehouseArea: WarehouseArea? = null
    private var warehouseAreaDescription: String = ""

    private var warehouseArray: ArrayList<Warehouse> = ArrayList()
    private var warehouseAreaArray: ArrayList<WarehouseArea> = ArrayList()

    private var warehouseVisible: Boolean = true
    private var warehouseAreaVisible: Boolean = true

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("title", title.toString())

        outState.putBoolean("onlyActive", onlyActive)
        outState.putParcelable("warehouse", warehouse)
        outState.putParcelable("warehouseArea", warehouseArea)

        outState.putBoolean("warehouseVisible", warehouseVisible)
        outState.putBoolean("warehouseAreaVisible", warehouseAreaVisible)

        outState.putParcelableArrayList("warehouseArray", warehouseArray)
        outState.putParcelableArrayList("warehouseAreaArray", warehouseAreaArray)

        refreshFilterData()
        outState.putString("warehouseDescription", warehouseDescription)
        outState.putString("warehouseAreaDescription", warehouseAreaDescription)
    }


    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        closeKeyboard(this)
        binding.warehouse.setOnContractsAvailability(null)
        binding.warehouse.setAdapter(null)
        binding.warehouseArea.setOnContractsAvailability(null)
        binding.warehouseArea.setAdapter(null)
    }

    private lateinit var binding: LocationSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = LocationSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var tempTitle = getString(R.string.select_warehouse_area)

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getString("title")
            if (!t1.isNullOrEmpty()) tempTitle = t1

            onlyActive = savedInstanceState.getBoolean("onlyActive")
            warehouseArray =
                savedInstanceState.parcelableArrayList("warehouseArray") ?: ArrayList()
            warehouseAreaArray =
                savedInstanceState.parcelableArrayList("warehouseAreaArray") ?: ArrayList()

            binding.warehouse.setOnEditorActionListener(null)
            binding.warehouse.onItemClickListener = null
            binding.warehouse.onFocusChangeListener = null
            binding.warehouse.setOnDismissListener(null)
            warehouse = savedInstanceState.parcelable("warehouse")
            warehouseDescription = savedInstanceState.getString("warehouseDescription") ?: ""

            binding.warehouseArea.setOnEditorActionListener(null)
            binding.warehouseArea.onItemClickListener = null
            binding.warehouseArea.onFocusChangeListener = null
            binding.warehouseArea.setOnDismissListener(null)
            warehouseArea = savedInstanceState.parcelable("warehouseArea")
            warehouseAreaDescription =
                savedInstanceState.getString("warehouseAreaDescription") ?: ""

            warehouseAreaVisible = savedInstanceState.getBoolean("warehouseAreaVisible")
            warehouseVisible = savedInstanceState.getBoolean("warehouseVisible")
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (!t1.isNullOrEmpty()) tempTitle = t1

                if (extras.containsKey("onlyActive")) onlyActive = extras.getBoolean("onlyActive")
                val t2 = extras.parcelable("warehouseArea") as WarehouseArea?
                if (t2 != null) warehouseArea = t2

                warehouse = extras.parcelable("warehouse")
                warehouseDescription = extras.getString("warehouseDescription") ?: ""
                warehouseVisible = extras.getBoolean("warehouseVisible")

                warehouseArea = extras.parcelable("warehouseArea")
                warehouseAreaDescription = extras.getString("warehouseAreaDescription") ?: ""
                warehouseAreaVisible = extras.getBoolean("warehouseAreaVisible")
            }
        }

        title = tempTitle

        binding.locationSelect.setOnClickListener { isBackPressed() }
        binding.selectButton.setOnClickListener { locationSelect() }
        binding.scanButton.setOnClickListener {
            ScannerManager.toggleCameraFloatingWindowVisibility(
                this
            )
        }

        binding.clearImageViewW.setOnClickListener {
            warehouse = null
            warehouseDescription = ""
            refreshWarehouseText(cleanText = true, focus = true)
        }

        binding.clearImageViewWa.setOnClickListener {
            warehouseArea = null
            warehouseAreaDescription = ""
            refreshWarehouseAreaText(cleanText = true, focus = true)
        }

        // region Setup WAREHOUSE ID AUTOCOMPLETE
        // Set an item click checkedChangedListener for auto complete text view

        wChangedListener = this
        binding.warehouse.threshold = 1
        binding.warehouse.hint = getString(R.string.select_warehouse)
        binding.warehouse.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                warehouse = (binding.warehouse.adapter as WarehouseAdapter).getItem(position)
                itemSelected()
            }
        binding.warehouse.setOnContractsAvailability(this)
        binding.warehouse.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.warehouse.text.length >= binding.warehouse.threshold &&
                    binding.warehouse.adapter != null &&
                    (binding.warehouse.adapter as WarehouseAdapter).count > 0 &&
                    !binding.warehouse.isPopupShowing
                ) {
                    // Display the suggestion dropdown on focus
                    Handler(Looper.getMainLooper()).post {
                        run {
                            adjustAndShowWarehouseDropDown()
                        }
                    }
                }
                if (!hasFocus && binding.warehouse.text.length <= binding.warehouse.threshold
                ) {
                    refreshFilterData()
                    setWarehouse(null)
                }
            }
        binding.warehouse.setOnTouchListener { _, motionEvent ->
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
        binding.warehouse.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (binding.warehouse.text.length >= binding.warehouse.threshold)
                    itemSelected()
                true
            } else {
                false
            }
        }
        // endregion

        // region Setup WAREHOUSE_AREA ID AUTOCOMPLETE
        // Set an item click checkedChangedListener for auto complete text view

        waChangedListener = this
        binding.warehouseArea.threshold = 1
        binding.warehouseArea.hint = getString(R.string.select_warehouse_area)
        binding.warehouseArea.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                warehouseArea =
                    (binding.warehouseArea.adapter as WarehouseAreaAdapter).getItem(
                        position
                    )
                itemSelected()
            }
        binding.warehouseArea.setOnContractsAvailability(this)
        binding.warehouseArea.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.warehouseArea.text.length >= binding.warehouseArea.threshold &&
                    binding.warehouseArea.adapter != null &&
                    (binding.warehouseArea.adapter as WarehouseAreaAdapter).count > 0 &&
                    !binding.warehouseArea.isPopupShowing
                ) {
                    // Display the suggestion dropdown on focus
                    Handler(Looper.getMainLooper()).post {
                        run {
                            adjustAndShowWarehouseAreaDropDown()
                        }
                    }
                }
                if (!hasFocus &&
                    binding.warehouseArea.text.length <= binding.warehouseArea.threshold
                ) {
                    setWarehouseArea(null)
                }
            }
        binding.warehouseArea.setOnTouchListener { _, motionEvent ->
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
        binding.warehouseArea.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (binding.warehouseArea.text.length >= binding.warehouseArea.threshold)
                    itemSelected()
                true
            } else {
                false
            }
        }
        // endregion

        registerEventListener(this, this)
        setControlVisibility()
        refreshWarehouseText(cleanText = false, focus = !warehouseAreaVisible)
        refreshWarehouseAreaText(cleanText = false, focus = warehouseAreaVisible)
        fillAdapters()

        setupUI(binding.root, this)
    }

    private fun itemSelected() {
        closeKeyboard(this)
        refreshFilterData()

        val data = Intent()
        if (warehouseAreaVisible) {
            data.putExtra("warehouseAreaDescription", warehouseAreaDescription)
            data.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
        }

        if (warehouseVisible) {
            data.putExtra("warehouseDescription", warehouseDescription)
            data.putExtra("warehouse", Parcels.wrap(warehouse))
        }

        setResult(RESULT_OK, data)
        finish()
    }

    private fun setControlVisibility() {
        runOnUiThread {
            binding.warehouse.visibility = if (warehouseVisible) VISIBLE else GONE
            binding.warehouseProgressBar.visibility = if (warehouseVisible) VISIBLE else GONE

            binding.warehouseArea.visibility = if (warehouseAreaVisible) VISIBLE else GONE
            binding.warehouseAreaProgressBar.visibility =
                if (warehouseAreaVisible) VISIBLE else GONE
        }
    }

    private var oldSelectedWarehouse: Warehouse? = null
    private fun setWarehouse(w: Warehouse?) {
        if (w == null) {
            if (oldSelectedWarehouse == null) {
                return
            }

            oldSelectedWarehouse = warehouse
            warehouse = null
            wChangedListener?.onWarehouseChanged(null)
        } else {
            val oldWarehouse = oldSelectedWarehouse
            if (oldWarehouse == w) {
                return
            }

            oldSelectedWarehouse = warehouse
            warehouse = w
            wChangedListener?.onWarehouseChanged(warehouse)
        }
    }

    private fun refreshWarehouseText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.warehouse.setText(
                if (warehouse == null) {
                    if (cleanText) ""
                    else binding.warehouse.text.toString()
                } else {
                    warehouse?.description ?: ""
                }
            )

            binding.warehouse.post {
                binding.warehouse.setSelection(
                    binding.warehouse.length()
                )
            }

            if (focus) {
                binding.warehouse.requestFocus()
            }
        }
    }

    private var oldSelectedWarehouseArea: WarehouseArea? = null
    private fun setWarehouseArea(wa: WarehouseArea?) {
        if (wa == null) {
            if (oldSelectedWarehouseArea == null) {
                return
            }

            oldSelectedWarehouseArea = warehouseArea
            warehouseArea = null
            waChangedListener?.onWarehouseAreaChanged(null)
        } else {
            val oldWarehouseArea = oldSelectedWarehouseArea
            if (oldWarehouseArea == wa) {
                return
            }

            oldSelectedWarehouseArea = warehouseArea
            warehouseArea = wa
            waChangedListener?.onWarehouseAreaChanged(warehouseArea)
        }
    }

    private fun refreshWarehouseAreaText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.warehouseArea.setText(
                if (warehouseArea == null) {
                    if (cleanText) {
                        ""
                    } else {
                        binding.warehouseArea.text.toString()
                    }
                } else {
                    warehouseArea?.description ?: ""
                }
            )

            binding.warehouseArea.post {
                binding.warehouseArea.setSelection(
                    binding.warehouseArea.length()
                )
            }

            if (focus) {
                binding.warehouseArea.requestFocus()
            }
        }
    }

    private fun fillAdapters() {
        try {
            getWarehouseAndFillAdapter()
            getWarehouseAreaAndFillAdapter()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun getWarehouseAndFillAdapter() {
        if (warehouseVisible) {
            thread {
                getWarehouse()
                fillWarehouseAdapter()
            }
        }
    }

    private fun getWarehouse() {
        if (warehouseArray.isNotEmpty()) {
            return
        }

        try {
            warehouseArray = ArrayList(WarehouseRepository().select(onlyActive))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private var wIsFilling = false
    private fun fillWarehouseAdapter() {
        if (wIsFilling) return
        wIsFilling = true

        val adapter = WarehouseAdapter(
            resource = R.layout.warehouse_row,
            wArray = warehouseArray,
            suggestedList = ArrayList()
        )

        runOnUiThread {
            binding.warehouseProgressBar.visibility = VISIBLE
            binding.warehouse.setAdapter(adapter)
            (binding.warehouse.adapter as WarehouseAdapter).notifyDataSetChanged()

            while (binding.warehouse.adapter == null) {
                // Wait for complete loaded
            }
            binding.warehouseProgressBar.visibility = GONE
        }

        refreshWarehouseText(cleanText = false, focus = !warehouseAreaVisible)
        wIsFilling = false
    }

    private fun getWarehouseAreaAndFillAdapter() {
        if (warehouseAreaVisible) {
            thread {
                getWarehouseArea()
                fillWarehouseAreaAdapter()
            }
        }
    }

    private fun getWarehouseArea() {
        if (warehouseAreaArray.isNotEmpty()) {
            return
        }

        // Si el área anterior no pertenece al depósito actual
        // limpiar el área.
        val wa = warehouseArea
        val warehouse = warehouse

        if (wa != null &&
            warehouse != null &&
            warehouse.id != wa.warehouseId
        ) {
            warehouseArea = null
        }

        try {
            warehouseAreaArray = ArrayList(WarehouseAreaRepository().select(onlyActive))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private var waIsFilling = false
    private fun fillWarehouseAreaAdapter() {
        if (waIsFilling) return
        waIsFilling = true

        val t: ArrayList<WarehouseArea> = ArrayList()
        val warehouse = warehouse
        for (wa in warehouseAreaArray) {
            if (warehouse != null) {
                if (wa.warehouseId == warehouse.id) {
                    t.add(wa)
                }
            } else {
                t.add(wa)
            }
        }

        val adapter = WarehouseAreaAdapter(
            activity = this,
            resource = R.layout.warehouse_area_row,
            warehouseAreas = t,
            suggestedList = ArrayList()
        )

        runOnUiThread {
            binding.warehouseAreaProgressBar.visibility = VISIBLE
            binding.warehouseArea.setAdapter(adapter)
            (binding.warehouseArea.adapter as WarehouseAreaAdapter).notifyDataSetChanged()

            while (binding.warehouseArea.adapter == null) {
                // Wait for complete loaded
            }
            binding.warehouseAreaProgressBar.visibility = GONE
        }

        refreshWarehouseAreaText(cleanText = false, focus = warehouseAreaVisible)
        waIsFilling = false
    }

    private fun locationSelect() {
        closeKeyboard(this)

        when {
            warehouseArea != null -> {
                val data = Intent()
                data.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
                setResult(RESULT_OK, data)
                finish()
            }

            else -> {
                if (warehouse != null) {
                    val data = Intent()
                    data.putExtra("warehouse", Parcels.wrap(warehouse))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    setResult(RESULT_CANCELED, null)
                    finish()
                }
            }
        }
    }

    private fun refreshFilterData() {
        if (warehouseVisible) {
            warehouseDescription = binding.warehouse.text.toString()
        }

        if (warehouseAreaVisible) {
            warehouseAreaDescription = binding.warehouseArea.text.toString()
        }
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

    private fun adjustAndShowWarehouseAreaDropDown() {
        // TOP LAYOUT
        topLayout()

        val height = calculateDropDownHeight()
        val offsetY =
            binding.warehouseArea.y + binding.warehouseArea.height
        var newHeight = (height - offsetY).toInt()
        val maxItems = newHeight / WarehouseAreaAdapter.defaultRowHeight()
        newHeight = maxItems * WarehouseAreaAdapter.defaultRowHeight()
        val maxNeeded =
            ((binding.warehouseArea.adapter
                ?: return) as WarehouseAreaAdapter).maxHeightNeeded()
        if (maxNeeded < newHeight) {
            newHeight = maxNeeded
        }
        binding.warehouseArea.dropDownHeight = newHeight
        binding.warehouseArea.showDropDown()
    }

    private fun adjustAndShowWarehouseDropDown() {
        // TOP LAYOUT
        topLayout()

        val height = calculateDropDownHeight()
        val offsetY = binding.warehouse.y + binding.warehouse.height
        var newHeight = (height - offsetY).toInt()
        val maxItems = newHeight / WarehouseAdapter.defaultRowHeight()
        newHeight = maxItems * WarehouseAdapter.defaultRowHeight()
        val maxNeeded = ((binding.warehouse.adapter
            ?: return) as WarehouseAdapter).maxHeightNeeded()
        if (maxNeeded < newHeight) {
            newHeight = maxNeeded
        }
        binding.warehouse.dropDownHeight = newHeight
        binding.warehouse.showDropDown()
    }

    private var isTopping = false
    private fun topLayout() {
        if (isTopping) return
        isTopping = true
        val set = ConstraintSet()

        set.clone(binding.locationSelect)
        set.clear(binding.gralLayout.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.locationSelect)

        isTopping = false
    }

    private var isCentring = false
    private fun centerLayout() {
        if (isCentring) return
        isCentring = true
        val set = ConstraintSet()

        set.clone(binding.locationSelect)
        set.connect(
            binding.gralLayout.id,
            ConstraintSet.BOTTOM,
            binding.locationSelect.id,
            ConstraintSet.BOTTOM,
            0
        )
        set.applyTo(binding.locationSelect)

        isCentring = false
    }

    private fun adjustDropDownHeight() {
        runOnUiThread {
            when {
                binding.warehouseArea.isPopupShowing -> adjustAndShowWarehouseAreaDropDown()
                binding.warehouse.isPopupShowing -> adjustAndShowWarehouseDropDown()
                else -> centerLayout()
            }
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
        if (svm.rfidShowConnectedMessage) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    showMessage(
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    showMessage(
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    showMessage(
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

    private fun showMessage(msg: String, type: SnackBarType) {
        if (isFinishing || isDestroyed) return
        if (type == ERROR) logError(msg)
        showMessage(msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)
}