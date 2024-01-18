package com.dacosys.assetControl.ui.activities.route

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.INVISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.databinding.DataCollectionRuleTargetActivityBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.dataCollection.DataCollectionRule
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.dacosys.assetControl.ui.activities.location.LocationSelectActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import org.parceler.Parcels

class DataCollectionRuleTargetActivity : AppCompatActivity() {
    private var rejectNewInstances = false

    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private lateinit var binding: DataCollectionRuleTargetActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = DataCollectionRuleTargetActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

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

        setupUI(binding.root, this)
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
                    closeKeyboard(this)
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

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}