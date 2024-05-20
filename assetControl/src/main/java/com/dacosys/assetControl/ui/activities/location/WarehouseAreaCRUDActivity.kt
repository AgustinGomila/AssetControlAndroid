package com.dacosys.assetControl.ui.activities.location

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.AssetControlApp.Companion.currentUser
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.common.CrudResult
import com.dacosys.assetControl.data.enums.common.CrudStatus.*
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.databinding.WarehouseAreaCrudActivityBinding
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.location.WarehouseAreaCRUDFragment
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels


class WarehouseAreaCRUDActivity : AppCompatActivity(), Scanner.ScannerListener,
    CrudCompleted, Rfid.RfidDeviceListener,
    ImageControlButtonsFragment.DescriptionRequired {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    /**
     * Interface que recibe los resultados del alta/modificación
     * del área
     */
    override fun <T> onCompleted(result: CrudResult<T?>) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val warehouseArea: WarehouseArea? = (result.itemResult as WarehouseArea?)
        when (result.status) {
            UPDATE_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.warehouse_area_modified_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && warehouseArea != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            INSERT_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.warehouse_area_added_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && warehouseArea != null) {
                    imageControlFragment?.updateObjectId1(warehouseArea.id)
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            ERROR_OBJECT_NULL -> makeText(
                binding.root, getString(R.string.error_null_object), SnackBarType.ERROR
            )

            ERROR_UPDATE -> makeText(
                binding.root, getString(R.string.error_updating_warehouse_area), SnackBarType.ERROR
            )

            ERROR_INSERT -> makeText(
                binding.root, getString(R.string.error_adding_warehouse_area), SnackBarType.ERROR
            )
        }
    }

    private var isNew = true
    private var warehouseArea: WarehouseArea? = null

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var warehouseAreaCRUDFragment: WarehouseAreaCRUDFragment? = null

    private var returnOnSuccess: Boolean = false
    private var rejectNewInstances = false


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("warehouseArea", warehouseArea)
        savedInstanceState.putBoolean("returnOnSuccess", returnOnSuccess)
        savedInstanceState.putBoolean("isNew", isNew)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )
        supportFragmentManager.putFragment(
            savedInstanceState, "warehouseAreaCRUDFragment", warehouseAreaCRUDFragment as Fragment
        )
    }

    private lateinit var binding: WarehouseAreaCrudActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseAreaCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.edit_area)

        warehouseAreaCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as WarehouseAreaCRUDFragment

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.parcelable<WarehouseArea>("warehouseArea")
            if (t1 != null) warehouseArea = t1

            returnOnSuccess = savedInstanceState.getBoolean("returnOnSuccess")
            isNew = savedInstanceState.getBoolean("isNew")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
            warehouseAreaCRUDFragment = supportFragmentManager.getFragment(
                savedInstanceState, "warehouseAreaCRUDFragment"
            ) as WarehouseAreaCRUDFragment
        } else {
            val extras = intent.extras
            if (extras != null) {
                warehouseArea = extras.parcelable("warehouseArea")
                returnOnSuccess = extras.getBoolean("return_on_success", false)
                isNew = extras.getBoolean("is_new", false)
            }
        }

        showProgressBar(false)

        binding.selectButton.setOnClickListener { selectWarehouseArea() }
        binding.saveButton.setOnClickListener { warehouseAreaCRUDFragment?.saveWarehouseArea(this) }


        setupUI(binding.root, this)
    }

    private fun setImageControlFragment() {
        var warehouseAreaId = 0L
        var description = ""
        if (warehouseArea != null) {
            warehouseAreaId = (warehouseArea ?: return).id
            description = (warehouseArea ?: return).description
        }

        val tableName = Table.warehouseArea.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = Table.warehouseArea.id.toLong(),
                objectId1 = warehouseAreaId.toString()
            )

            setFragmentValues(description, "", obs)

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)

            val fm = supportFragmentManager

            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(binding.imageControlFragment.id, imageControlFragment ?: return@runOnUiThread)
                        .commit()

                    if (!prefsGetBoolean(Preference.useImageControl)) {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(imageControlFragment as Fragment)
                            .commitAllowingStateLoss()
                    } else {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                            .commitAllowingStateLoss()
                    }
                }
            }
        } else {
            imageControlFragment?.setTableId(Table.warehouseArea.id)
            imageControlFragment?.setObjectId1(warehouseAreaId)
            imageControlFragment?.setObjectId2(null)

            setFragmentValues(description, "", obs)

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)
        }

        // OCULTAR BOTÓN DE FIRMA
        imageControlFragment?.showSignButton = false
    }

    private fun setFragmentValues(description: String, reference: String, obs: String) {
        if (description.isNotEmpty()) {
            imageControlFragment?.setDescription(description)
        }

        if (reference.isNotEmpty()) {
            imageControlFragment?.setReference(reference)
        }

        if (obs.isNotEmpty()) {
            imageControlFragment?.setObs(obs)
        }
    }

    private fun selectWarehouseArea() {
        if (!rejectNewInstances) {
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

            val intent = Intent(baseContext, LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("title", getString(R.string.select_warehouse_area))
            intent.putExtra("onlyActive", true)
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", true)
            resultForAreaSelect.launch(intent)
        }
    }

    private val resultForAreaSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val wa = Parcels.unwrap<WarehouseArea>(data.parcelable("warehouseArea"))
                        ?: return@registerForActivityResult

                    try {
                        changeWarehouseArea(wa)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        makeText(
                            binding.root,
                            getString(R.string.error_trying_to_add_the_area),
                            SnackBarType.ERROR
                        )
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                JotterListener.lockScanner(this, false)
            }
        }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            if (show && binding.progressBarLayout.visibility != View.VISIBLE) {
                binding.progressBarLayout.bringToFront()
                binding.progressBarLayout.visibility = View.VISIBLE

                ViewCompat.setZ(binding.progressBarLayout, 0F)
            } else if (!show && binding.progressBarLayout.visibility != View.GONE) {
                binding.progressBarLayout.visibility = View.GONE
            }
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        fillControls()
    }

    private fun fillControls() {
        if (warehouseArea == null) {
            setImageControlFragment()
            return
        }

        isNew = false

        runOnUiThread {
            warehouseAreaCRUDFragment?.setWarehouseArea(warehouseArea)
            setImageControlFragment()
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        JotterListener.lockScanner(this, true)

        try {
            val sc = ScannedCode(this).getFromCode(
                code = scanCode,
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                searchAssetEan = false
            )

            if (sc.codeFound && sc.warehouseArea != null) {
                // No hay ningún área cargada
                if (warehouseArea == null) {
                    warehouseArea = sc.warehouseArea
                    fillControls()
                } else {
                    changeWarehouseArea(sc.warehouseArea ?: return)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    private fun clearControl() {
        runOnUiThread {
            warehouseArea = null
            isNew = true
            warehouseAreaCRUDFragment?.setWarehouseArea(warehouseArea)
        }
    }

    private fun changeWarehouseArea(tempWarehouseArea: WarehouseArea) {
        if (warehouseArea != null || (warehouseAreaCRUDFragment?.getDescription()
                ?: "").isNotEmpty()
        ) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_warehouse_area_registration_modification))
            alert.setMessage(getString(R.string.discard_changes_and_load_the_scanned_warehouse_area_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                warehouseArea = tempWarehouseArea
                fillControls()
            }

            alert.show()
        } else {
            warehouseArea = tempWarehouseArea
            fillControls()
        }
    }

    private fun cancelWarehouseAreaModify() {
        if (warehouseArea != null || (warehouseAreaCRUDFragment?.getDescription()
                ?: "").isNotEmpty()
        ) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_warehouse_area_registration_modification))
            alert.setMessage(getString(R.string.discard_changes_and_return_to_the_main_menu_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)
                setResult(RESULT_CANCELED)
                finish()
            }

            alert.show()
        } else {
            closeKeyboard(this)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
    }

    private fun isBackPressed() {
        cancelWarehouseAreaModify()
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

    override fun onDescriptionRequired() {
        imageControlFragment?.setDescription(warehouseAreaCRUDFragment?.getDescription() ?: "")
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