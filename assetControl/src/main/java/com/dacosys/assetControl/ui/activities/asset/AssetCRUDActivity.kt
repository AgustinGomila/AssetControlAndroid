package com.dacosys.assetControl.ui.activities.asset

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.databinding.AssetCrudActivityBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetCRUD
import com.dacosys.assetControl.model.asset.AssetCRUD.Companion.RC_ERROR_INSERT
import com.dacosys.assetControl.model.asset.AssetCRUD.Companion.RC_ERROR_OBJECT_NULL
import com.dacosys.assetControl.model.asset.AssetCRUD.Companion.RC_ERROR_UPDATE
import com.dacosys.assetControl.model.asset.AssetCRUD.Companion.RC_INSERT_OK
import com.dacosys.assetControl.model.asset.AssetCRUD.Companion.RC_UPDATE_OK
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.asset.AssetCRUDFragment
import com.dacosys.assetControl.ui.fragments.asset.AssetStatusSpinnerFragment
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import org.parceler.Parcels

class AssetCRUDActivity : AppCompatActivity(), Scanner.ScannerListener,
    AssetStatusSpinnerFragment.OnItemSelectedListener, AssetCRUD.Companion.TaskCompleted,
    Rfid.RfidDeviceListener, ImageControlButtonsFragment.DescriptionRequired {
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
     * del activo
     */
    override fun onTaskCompleted(result: AssetCRUD.Companion.AssetCRUDResult) {
        if (isDestroyed || isFinishing) return

        when (result.resultCode) {
            RC_UPDATE_OK -> {
                makeText(
                    binding.root, getString(R.string.asset_modified_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && result.asset != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("asset", Parcels.wrap(result.asset))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }
            RC_INSERT_OK -> {
                makeText(
                    binding.root, getString(R.string.asset_added_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && result.asset != null) {
                    imageControlFragment?.updateObjectId1(
                        (result.asset ?: return).assetId
                    )
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("asset", Parcels.wrap(result.asset))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }
            RC_ERROR_OBJECT_NULL -> makeText(
                binding.root, getString(R.string.error_null_object), SnackBarType.ERROR
            )
            RC_ERROR_UPDATE -> makeText(
                binding.root, getString(R.string.error_updating_asset), SnackBarType.ERROR
            )
            RC_ERROR_INSERT -> makeText(
                binding.root, getString(R.string.error_adding_asset), SnackBarType.ERROR
            )
        }
    }

    private var isNew = true
    private var asset: Asset? = null

    private var assetCRUDFragment: AssetCRUDFragment? = null
    private var imageControlFragment: ImageControlButtonsFragment? = null

    private var panelBottomIsExpanded = true

    private var returnOnSuccess: Boolean = false
    private var rejectNewInstances = false


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("asset", asset)
        savedInstanceState.putBoolean("returnOnSuccess", returnOnSuccess)
        savedInstanceState.putBoolean("isNew", isNew)

        savedInstanceState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )

        supportFragmentManager.putFragment(
            savedInstanceState, "assetCRUDFragment", assetCRUDFragment as Fragment
        )
    }

    private lateinit var binding: AssetCrudActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permitir escanear códigos dentro de la actividad
        // Si ya está cargado un activo preguntar si descartar cambios

        title = getString(R.string.edit_asset)

        assetCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as AssetCRUDFragment

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getParcelable<Asset>("asset")
            if (t1 != null) {
                asset = t1
            }

            returnOnSuccess = savedInstanceState.getBoolean("returnOnSuccess")
            isNew = savedInstanceState.getBoolean("isNew")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
            assetCRUDFragment = supportFragmentManager.getFragment(
                savedInstanceState, "assetCRUDFragment"
            ) as AssetCRUDFragment

            panelBottomIsExpanded = savedInstanceState.getBoolean("panelBottomIsExpanded")
        } else {
            val extras = intent.extras
            if (extras != null) {
                asset = extras.getParcelable("asset")
                returnOnSuccess = extras.getBoolean("return_on_success", false)
                isNew = extras.getBoolean("is_new", false)
            }
        }

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()

        binding.selectButton.setOnClickListener { selectAsset() }
        binding.printButton.setOnClickListener { printLabel() }
        binding.rfidButton.setOnClickListener { rfidLink() }
        binding.saveButton.setOnClickListener { assetCRUDFragment?.saveAsset(this) }

        // Llenar la grilla
        setPanels()

        setupUI(binding.root, this)
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) currentLayout.load(this, R.layout.asset_crud_activity)
        else currentLayout.load(this, R.layout.asset_crud_activity_bottom_panel_collapsed)

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(binding.activityAssetCrud, transition)
        currentLayout.applyTo(binding.activityAssetCrud)

        if (panelBottomIsExpanded) binding.expandButton?.text = getString(R.string.collapse_panel)
        else binding.expandButton?.text = getString(R.string.more_options)
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) nextLayout.load(
                this, R.layout.asset_crud_activity_bottom_panel_collapsed
            )
            else nextLayout.load(this, R.layout.asset_crud_activity)

            panelBottomIsExpanded = !panelBottomIsExpanded
            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(binding.activityAssetCrud, transition)
            nextLayout.applyTo(binding.activityAssetCrud)

            if (panelBottomIsExpanded) binding.expandButton?.text =
                getString(R.string.collapse_panel)
            else binding.expandButton?.text = getString(R.string.more_options)
        }
    }

    private fun setImageControlFragment() {
        var assetId = 0L
        var description = ""

        if (asset != null) {
            assetId = (asset ?: return).assetId
            description = (asset ?: return).description
        }

        val tableName = Table.asset.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                Table.asset.tableId.toLong(), assetId.toString(), null
            )

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)

            val fm = supportFragmentManager

            if (!isFinishing) runOnUiThread {
                fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                        binding.imageControlFragment.id,
                        imageControlFragment ?: return@runOnUiThread
                    ).commit()

                if (!prefsGetBoolean(Preference.useImageControl)) {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(imageControlFragment as Fragment).commitAllowingStateLoss()
                } else {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                        .commitAllowingStateLoss()
                }
            }
        } else {
            imageControlFragment?.setTableId(Table.asset.tableId)
            imageControlFragment?.setObjectId1(assetId)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)
        }

        // OCULTAR BOTÓN DE FIRMA
        imageControlFragment?.showSignButton = false
    }

    private fun selectAsset() {
        if (!rejectNewInstances) {
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("multiSelect", false)
            intent.putExtra("onlyActive", true)
            resultForAssetSelect.launch(intent)
        }
    }

    private val resultForAssetSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val idParcel = data.getParcelableArrayListExtra<ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: java.util.ArrayList<Long?> = java.util.ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val a = AssetDbHelper().selectById(ids[0]) ?: return@registerForActivityResult

                    try {
                        changeAsset(a)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        makeText(
                            binding.root,
                            getString(R.string.an_error_occurred_while_trying_to_add_the_item),
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

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        fillControls()
    }

    override fun onBackPressed() {
        cancelAssetModify()
    }

    private fun fillControls() {
        isNew = asset == null
        runOnUiThread {
            assetCRUDFragment?.setAsset(asset)
            setImageControlFragment()
        }
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.pauseReaderDevices(this)

        try {
            val sc = ScannedCode(this).getFromCode(
                scanCode,
                searchWarehouseAreaId = false,
                searchAssetCode = true,
                searchAssetSerial = true,
                validateId = true
            )

            if (sc.codeFound && sc.asset != null) {
                // No hay ningún activo cargado
                if (asset == null) {
                    asset = sc.asset
                    fillControls()
                } else {
                    changeAsset(sc.asset ?: return)
                }
            } else {
                // No hay ningún activo cargado
                if (asset == null) {
                    runOnUiThread {
                        assetCRUDFragment?.setCode(scanCode)
                    }
                } else {
                    changeCode(scanCode)
                }
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.resumeReaderDevices(this)
        }
    }

    private fun clearControl() {
        runOnUiThread {
            asset = null
            isNew = true
            assetCRUDFragment?.setAsset(null)
        }
    }

    private fun changeCode(tempCode: String) {
        if (asset != null || (assetCRUDFragment?.getDescription()
                ?: "").isNotEmpty() || (assetCRUDFragment?.getCode() ?: "").isNotEmpty()
        ) {
            JotterListener.pauseReaderDevices(this)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.cancel_asset_registration_modification))
                alert.setMessage(getString(R.string.discard_changes_and_load_the_scanned_code_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    clearControl()
                    runOnUiThread {
                        assetCRUDFragment?.setCode(tempCode)
                    }
                }

                runOnUiThread {
                    alert.show()
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.resumeReaderDevices(this)
            }
        } else {
            clearControl()
            runOnUiThread {
                assetCRUDFragment?.setCode(tempCode)
            }
        }
    }

    private fun changeAsset(tempAsset: Asset) {
        if (asset != null || (assetCRUDFragment?.getDescription()
                ?: "").isNotEmpty() || (assetCRUDFragment?.getCode() ?: "").isNotEmpty()
        ) {
            JotterListener.pauseReaderDevices(this)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.cancel_asset_registration_modification))
                alert.setMessage(getString(R.string.discard_changes_and_load_the_scanned_asset_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    asset = tempAsset
                    fillControls()
                }

                runOnUiThread {
                    alert.show()
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.resumeReaderDevices(this)
            }
        } else {
            asset = tempAsset
            fillControls()
        }
    }

    private fun cancelAssetModify() {
        if (asset != null || (assetCRUDFragment?.getDescription()
                ?: "").isNotEmpty() || (assetCRUDFragment?.getCode() ?: "").isNotEmpty()
        ) {
            JotterListener.pauseReaderDevices(this)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.cancel_asset_registration_modification))
                alert.setMessage(getString(R.string.discard_changes_and_return_to_the_main_menu_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    closeKeyboard(this)

                    setResult(RESULT_CANCELED)
                    finish()
                }

                runOnUiThread {
                    alert.show()
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.resumeReaderDevices(this)
            }
        } else {
            closeKeyboard(this)

            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun printLabel() {
        if (asset == null) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val assetArray: ArrayList<Asset> = ArrayList()
            assetArray.add(asset ?: return)

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putParcelableArrayListExtra("assetArray", assetArray)
            startActivity(intent)
        }
    }

    private fun rfidLink() {
        if (Rfid.rfidDevice == null) {
            makeText(
                binding.root,
                getString(R.string.there_is_no_rfid_device_connected),
                SnackBarType.ERROR
            )
            return
        }

        if (asset != null) {
            if (!(Rfid.rfidDevice as Vh75Bt).writeTag((asset ?: return).code)) {
                makeText(
                    binding.root, getString(R.string.failed_rfid_writing), SnackBarType.ERROR
                )
            }
        }
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

    override fun onDescriptionRequired() {
        imageControlFragment?.setDescription(assetCRUDFragment?.getDescription() ?: "")
    }

    override fun onItemSelected(assetStatus: AssetStatus?) {}


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

    override fun onWriteCompleted(isOk: Boolean) {
        if (isOk) {
            makeText(
                binding.root, getString(R.string.rfid_writing_ok), SnackBarType.SUCCESS
            )
        } else {
            makeText(
                binding.root, getString(R.string.failed_rfid_writing), SnackBarType.ERROR
            )
        }
    }

    override fun onReadCompleted(scanCode: String) {
        scannerCompleted(scanCode)
    }

    //endregion READERS Reception
}