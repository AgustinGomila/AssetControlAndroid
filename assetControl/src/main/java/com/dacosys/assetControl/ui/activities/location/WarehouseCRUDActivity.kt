package com.dacosys.assetControl.ui.activities.location

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.WarehouseCrudActivityBinding
import com.dacosys.assetControl.model.common.CrudCompleted
import com.dacosys.assetControl.model.common.CrudResult
import com.dacosys.assetControl.model.common.CrudStatus.*
import com.dacosys.assetControl.model.location.Warehouse
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.location.WarehouseCRUDFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import org.parceler.Parcels


class WarehouseCRUDActivity : AppCompatActivity(), CrudCompleted,
    ImageControlButtonsFragment.DescriptionRequired {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    override fun <T> onCompleted(result: CrudResult<T?>) {
        if (isDestroyed || isFinishing) return

        val warehouse: Warehouse? = (result.itemResult as Warehouse?)
        when (result.status) {
            UPDATE_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.warehouse_modified_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && warehouse != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("warehouse", Parcels.wrap(warehouse))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            INSERT_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.warehouse_added_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && warehouse != null) {
                    imageControlFragment?.updateObjectId1(warehouse.warehouseId)
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("warehouse", Parcels.wrap(warehouse))
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
                binding.root, getString(R.string.error_updating_warehouse), SnackBarType.ERROR
            )

            ERROR_INSERT -> makeText(
                binding.root, getString(R.string.error_adding_warehouse), SnackBarType.ERROR
            )
        }
    }

    private var isNew = true
    private var warehouse: Warehouse? = null

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var warehouseCRUDFragment: WarehouseCRUDFragment? = null

    private var returnOnSuccess: Boolean = false
    private var rejectNewInstances = false


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("warehouse", warehouse)
        savedInstanceState.putBoolean("returnOnSuccess", returnOnSuccess)
        savedInstanceState.putBoolean("isNew", isNew)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )
        supportFragmentManager.putFragment(
            savedInstanceState, "warehouseCRUDFragment", warehouseCRUDFragment as Fragment
        )
    }

    private lateinit var binding: WarehouseCrudActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permitir escanear códigos dentro de la actividad
        // Si ya está cargado un área preguntar si descartar cambios

        title = getString(R.string.edit_warehouse)

        warehouseCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as WarehouseCRUDFragment
        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getParcelable<Warehouse>("warehouse")
            if (t1 != null) {
                warehouse = t1
            }

            returnOnSuccess = savedInstanceState.getBoolean("returnOnSuccess")
            isNew = savedInstanceState.getBoolean("isNew")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
            warehouseCRUDFragment = supportFragmentManager.getFragment(
                savedInstanceState, "warehouseCRUDFragment"
            ) as WarehouseCRUDFragment
        } else {
            val extras = intent.extras
            if (extras != null) {
                warehouse = extras.getParcelable("warehouse")
                returnOnSuccess = extras.getBoolean("return_on_success", false)
                isNew = extras.getBoolean("is_new", false)
            }
        }

        binding.selectButton.setOnClickListener { selectWarehouse() }
        binding.saveButton.setOnClickListener { warehouseCRUDFragment?.saveWarehouse(this) }

        setupUI(binding.root, this)
    }

    private fun selectWarehouse() {
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(baseContext, LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("title", getString(R.string.select_warehouse))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("onlyActive", true)
            intent.putExtra("warehouseAreaVisible", false)
            resultForWarehouseSelect.launch(intent)
        }
    }

    private val resultForWarehouseSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val w = Parcels.unwrap<Warehouse>(data.getParcelableExtra("warehouse"))
                        ?: return@registerForActivityResult

                    try {
                        changeWarehouse(w)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        makeText(
                            binding.root,
                            getString(R.string.error_trying_to_add_warehouse),
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
            }
        }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false

        fillControls()
    }

    override fun onBackPressed() {
        cancelWarehouseModify()
    }

    private fun fillControls() {
        if (warehouse == null) {
            setImageControlFragment()
            return
        }

        isNew = false

        runOnUiThread {
            warehouseCRUDFragment?.setWarehouse(warehouse)
            setImageControlFragment()
        }
    }

    private fun clearControl() {
        runOnUiThread {
            warehouse = null
            isNew = true
            warehouseCRUDFragment?.setWarehouse(warehouse)
        }
    }

    private fun setImageControlFragment() {
        var warehouseId = 0L
        var description = ""
        if (warehouse != null) {
            warehouseId = warehouse!!.warehouseId
            description = warehouse!!.description
        }

        val tableName = Table.warehouse.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                Table.warehouse.tableId.toLong(), warehouseId.toString(), null
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
            imageControlFragment?.setTableId(Table.warehouse.tableId)
            imageControlFragment?.setObjectId1(warehouseId)
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

    private fun changeWarehouse(tempWarehouse: Warehouse) {
        if (warehouse != null || (warehouseCRUDFragment?.getDescription() ?: "").isNotEmpty()) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_warehouse_registration_modification))
            alert.setMessage(getString(R.string.discard_changes_and_load_the_scanned_warehouse_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                warehouse = tempWarehouse
                fillControls()
            }

            alert.show()
        } else {
            warehouse = tempWarehouse
            fillControls()
        }
    }

    private fun cancelWarehouseModify() {
        if (warehouse != null || (warehouseCRUDFragment?.getDescription() ?: "").isNotEmpty()) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_warehouse_registration_modification))
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

    override fun onDescriptionRequired() {
        imageControlFragment?.setDescription(warehouseCRUDFragment?.getDescription() ?: "")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}