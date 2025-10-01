package com.example.assetControl.ui.activities.asset

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.enums.asset.AssetStatus
import com.example.assetControl.data.enums.common.CrudCompleted
import com.example.assetControl.data.enums.common.CrudResult
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_INSERT
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_OBJECT_NULL
import com.example.assetControl.data.enums.common.CrudStatus.ERROR_UPDATE
import com.example.assetControl.data.enums.common.CrudStatus.INSERT_OK
import com.example.assetControl.data.enums.common.CrudStatus.UPDATE_OK
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.databinding.AssetCrudActivityBinding
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
import com.example.assetControl.ui.fragments.asset.AssetCRUDFragment
import com.example.assetControl.ui.fragments.asset.AssetStatusSpinnerFragment
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.ParcelLong
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.parcel.Parcelables.parcelableArrayList
import org.parceler.Parcels

class AssetCRUDActivity : AppCompatActivity(), Scanner.ScannerListener,
    AssetStatusSpinnerFragment.OnItemSelectedListener, CrudCompleted,
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
    override fun <T> onCompleted(result: CrudResult<T?>) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val asset: Asset? = (result.itemResult as Asset?)
        when (result.status) {
            UPDATE_OK -> {
                makeText(
                    binding.root, getString(R.string.asset_modified_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && asset != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("asset", Parcels.wrap(asset))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            INSERT_OK -> {
                makeText(
                    binding.root, getString(R.string.asset_added_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && asset != null) {
                    imageControlFragment?.updateObjectId1(asset.id)
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("asset", Parcels.wrap(asset))
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
                binding.root, getString(R.string.error_updating_asset), SnackBarType.ERROR
            )

            ERROR_INSERT -> makeText(
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("asset", asset)
        outState.putBoolean("returnOnSuccess", returnOnSuccess)
        outState.putBoolean("isNew", isNew)

        outState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            outState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )

        supportFragmentManager.putFragment(
            outState, "assetCRUDFragment", assetCRUDFragment as Fragment
        )
    }

    private lateinit var binding: AssetCrudActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permitir escanear códigos dentro de la actividad
        // Si ya está cargado un activo preguntar si descartar cambios

        title = getString(R.string.edit_asset)

        assetCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as AssetCRUDFragment

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.parcelable<Asset>("asset")
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
                asset = extras.parcelable("asset")
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
        val a = asset

        if (a != null) {
            assetId = a.id
            description = a.description
        }

        val tableDescription = Table.asset.description
        description = "$tableDescription: $description".take(255)

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = Table.asset.id.toLong(),
                objectId1 = assetId.toString()
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

                    if (!svm.useImageControl) {
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
            imageControlFragment?.setTableId(Table.asset.id)
            imageControlFragment?.setObjectId1(assetId)
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

    private fun selectAsset() {
        if (!rejectNewInstances) {
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

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
                    val idParcel = data.parcelableArrayList<ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: java.util.ArrayList<Long?> = java.util.ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val id = ids.first() ?: return@registerForActivityResult
                    val a = AssetRepository().selectById(id) ?: return@registerForActivityResult

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
                ScannerManager.lockScanner(this, false)
            }
        }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        fillControls()
    }

    private fun fillControls() {
        isNew = asset == null
        runOnUiThread {
            assetCRUDFragment?.setAsset(asset)
            setImageControlFragment()
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
            val sc = ScannedCode(this).getFromCode(
                scanCode,
                searchWarehouseAreaId = false,
                searchAssetCode = true,
                searchAssetSerial = true,
                searchAssetEan = true
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
            ScannerManager.lockScanner(this, false)
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
            ScannerManager.lockScanner(this, true)
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
                ScannerManager.lockScanner(this, false)
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
            ScannerManager.lockScanner(this, true)
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
                ScannerManager.lockScanner(this, false)
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
            ScannerManager.lockScanner(this, true)
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
                ScannerManager.lockScanner(this, false)
            }
        } else {
            closeKeyboard(this)

            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun printLabel() {
        val asset = asset ?: return

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val assetArray: ArrayList<Asset> = ArrayList()
            assetArray.add(asset)

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putParcelableArrayListExtra("assetArray", assetArray)
            startActivity(intent)
        }
    }

    private fun rfidLink() {
        if (Rfid.vh75 == null) {
            makeText(
                binding.root,
                getString(R.string.there_is_no_rfid_device_connected),
                SnackBarType.ERROR
            )
            return
        }

        val tempAsset = asset
        if (tempAsset != null) {
            if (Rfid.vh75?.writeTag(tempAsset.code) != true) {
                makeText(
                    binding.root, getString(R.string.failed_rfid_writing), SnackBarType.ERROR
                )
            }
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
        cancelAssetModify()
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
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
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

    override fun onStateChanged(state: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (svm.rfidShowConnectedMessage) {
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