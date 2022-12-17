package com.dacosys.assetControl.views.routes.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.DataCollectionRuleTargetActivityBinding
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.`object`.DataCollectionRule
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.views.assets.asset.activities.AssetPrintLabelActivity
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.assetControl.views.locations.locationSelect.LocationSelectActivity
import org.parceler.Parcels

class DataCollectionRuleTargetActivity : AppCompatActivity() {
    private var rejectNewInstances = false

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
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

    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private lateinit var binding: DataCollectionRuleTargetActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = DataCollectionRuleTargetActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.data_collection_target)

        binding.assetButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, AssetPrintLabelActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("multiSelect", false)
                resultForAssetSelect.launch(intent)
            }
        }

        binding.warehouseAreaButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, LocationSelectActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("title", getString(R.string.select_warehouse_area))
                intent.putExtra("warehouseVisible", true)
                intent.putExtra("warehouseAreaVisible", true)
                resultForAreaSelect.launch(intent)
            }
        }

        binding.itemCategoryButton.setOnClickListener { }

        // VER ESTO!!! No está implementado la recolección para categorías
        binding.itemCategoryButton.visibility = INVISIBLE

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private val resultForAssetSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val idParcel = data.getParcelableArrayListExtra<ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: ArrayList<Long?> = ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val a = AssetDbHelper().selectById(ids[0]) ?: return@registerForActivityResult

                    try {
                        if (!rejectNewInstances) {
                            rejectNewInstances = true

                            val intent =
                                Intent(baseContext, DataCollectionRuleSelectActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra("asset", Parcels.wrap(a))
                            resultForDcrSelect.launch(intent)
                        }
                    } catch (ex: Exception) {
                        val res = getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                        makeText(
                            binding.root,
                            res,
                            SnackBarType.ERROR
                        )
                        Log.d(this::class.java.simpleName, res)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private val resultForAreaSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val warehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))
                            ?: return@registerForActivityResult

                    if (!rejectNewInstances) {
                        rejectNewInstances = true

                        val intent =
                            Intent(baseContext, DataCollectionRuleSelectActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        intent.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
                        resultForDcrSelect.launch(intent)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private val resultForDcrSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val dcr =
                        Parcels.unwrap<DataCollectionRule>(data.getParcelableExtra("dataCollectionRule"))
                            ?: return@registerForActivityResult

                    // Targets
                    val asset = Parcels.unwrap<Asset>(data.getParcelableExtra("asset"))
                    val warehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))
                    val itemCategory =
                        Parcels.unwrap<ItemCategory>(data.getParcelableExtra("itemCategory"))

                    if (asset == null && warehouseArea == null && itemCategory == null) {
                        return@registerForActivityResult
                    }

                    if (!rejectNewInstances) {
                        rejectNewInstances = true

                        val intent = Intent(baseContext, DccActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        intent.putExtra("dataCollectionRule", Parcels.wrap(dcr))
                        when {
                            asset != null -> intent.putExtra("asset", Parcels.wrap(asset))
                            warehouseArea != null -> intent.putExtra(
                                "warehouseArea",
                                Parcels.wrap(warehouseArea)
                            )
                            itemCategory != null -> intent.putExtra(
                                "itemCategory",
                                Parcels.wrap(itemCategory)
                            )
                        }
                        resultForFinishDcc.launch(intent)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private val resultForFinishDcc =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    Statics.closeKeyboard(this)
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

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

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
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