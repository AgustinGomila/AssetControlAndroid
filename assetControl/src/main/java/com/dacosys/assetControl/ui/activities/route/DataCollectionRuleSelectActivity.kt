package com.dacosys.assetControl.ui.activities.route

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.datacollection.DataCollectionRuleAdapter
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleDbHelper
import com.dacosys.assetControl.databinding.DataCollectionRuleSelectActivityBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.datacollection.DataCollectionRule
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.Preference
import org.parceler.Parcels


class DataCollectionRuleSelectActivity : AppCompatActivity() {

    private var tempTitle = ""

    private var currentDataCollectionRule: DataCollectionRule? = null
    private var asset: Asset? = null
    private var warehouseArea: WarehouseArea? = null
    private var itemCategory: ItemCategory? = null

    private var arrayAdapter: DataCollectionRuleAdapter? = null
    private var lastSelected: DataCollectionRule? = null
    private var firstVisiblePos: Int? = null

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        saveSharedPreferences()
        arrayAdapter?.refreshListeners(null)
    }

    private fun saveSharedPreferences() {
        Statics.prefsPutBoolean(
            Preference.selectDataCollectionRuleOnlyActive.key,
            binding.onlyActiveSwitch.isChecked
        )
    }

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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putParcelable("currentDataCollectionRule", currentDataCollectionRule)
        b.putBoolean("onlyActive", binding.onlyActiveSwitch.isChecked)

        if (asset != null) b.putParcelable("asset", Parcels.wrap(asset))
        if (warehouseArea != null) b.putParcelable("warehouseArea", Parcels.wrap(warehouseArea))
        if (itemCategory != null) b.putParcelable("itemCategory", Parcels.wrap(itemCategory))

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentDataCollectionRule())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle =
            if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.select_rule)
        // endregion

        binding.onlyActiveSwitch.isChecked = b.getBoolean("onlyActive")
        currentDataCollectionRule = b.getParcelable("currentDataCollectionRule")

        if (b.containsKey("asset")) asset = Parcels.unwrap<Asset>(b.getParcelable("asset"))
        if (b.containsKey("warehouseArea")) warehouseArea =
            Parcels.unwrap<WarehouseArea>(b.getParcelable("warehouseArea"))
        if (b.containsKey("itemCategory")) itemCategory =
            Parcels.unwrap<ItemCategory>(b.getParcelable("itemCategory"))

        // ADAPTER
        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_rule)
    }

    private lateinit var binding: DataCollectionRuleSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = DataCollectionRuleSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        binding.onlyActiveSwitch.setOnCheckedChangeListener(null)
        binding.onlyActiveSwitch.isChecked =
            Statics.prefsGetBoolean(Preference.selectDataCollectionRuleOnlyActive)

        when {
            warehouseArea != null -> makeText(
                binding.root,
                (warehouseArea ?: return).description,
                SnackBarType.INFO
            )
            itemCategory != null -> makeText(
                binding.root,
                (itemCategory ?: return).description,
                SnackBarType.INFO
            )
            asset != null -> makeText(
                binding.root,
                (asset ?: return).description,
                SnackBarType.INFO
            )
        }

        binding.etDescription.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                fillListView()
                true
            } else {
                false
            }
        }

        // Captura el toque (ya que no es igual al click) en el control y cambia el DataCollectionRuleReviewContent actual
        binding.dcRuleListView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val pos =
                        binding.dcRuleListView.pointToPosition(event.x.toInt(), event.y.toInt())
                    if (pos >= 0) {
                        selectRow(pos)
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        binding.dcRuleListView.setOnItemClickListener { _, _, pos, _ -> selectRow(pos) }

        binding.okButton.setOnClickListener { dataCollectionRuleSelect() }

        binding.onlyActiveSwitch.setOnCheckedChangeListener { _, _ -> fillListView() }

        fillListView()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun selectRow(dcr: DataCollectionRule?) {
        if (dcr == null) {
            selectRow(0)
            return
        }

        if (binding.dcRuleListView.adapter.count > 0) {
            val pos = (binding.dcRuleListView.adapter as DataCollectionRuleAdapter).getPosition(dcr)
            selectRow(pos)
        }
    }

    private fun selectRow(pos: Int) {
        binding.dcRuleListView.clearChoices()
        if (pos < 0) {
            return
        }

        if (binding.dcRuleListView.adapter.count > 0) {
            val tempObj = binding.dcRuleListView.adapter.getItem(pos)
            if (tempObj != null) {
                val typedObj = tempObj as DataCollectionRule

                currentDataCollectionRule = typedObj

                runOnUiThread {
                    binding.dcRuleListView.setItemChecked(pos, true)
                    binding.dcRuleListView.setSelection(pos)

                    (binding.dcRuleListView.adapter as DataCollectionRuleAdapter).notifyDataSetChanged()

                    binding.dcRuleListView.smoothScrollToPosition(pos)
                }
            }
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

    private fun dataCollectionRuleSelect() {
        Statics.closeKeyboard(this)

        if (currentDataCollectionRule != null) {
            val data = Intent()
            data.putExtra("dataCollectionRule", Parcels.wrap(currentDataCollectionRule))
            data.putExtra("asset", Parcels.wrap(asset))
            data.putExtra("warehouseArea", Parcels.wrap(warehouseArea))
            data.putExtra("itemCategory", Parcels.wrap(itemCategory))
            setResult(RESULT_OK, data)
            finish()
        } else {
            setResult(RESULT_CANCELED, null)
            finish()
        }
    }

    private fun fillListView() {
        try {
            val desc = binding.etDescription.text.toString()

            val dcrArray =
                when {
                    asset != null -> DataCollectionRuleDbHelper().selectByTargetAssetIdDescription(
                        (asset ?: return).assetId,
                        desc,
                        binding.onlyActiveSwitch.isChecked
                    )
                    warehouseArea != null -> DataCollectionRuleDbHelper().selectByTargetWarehouseAreaIdDescription(
                        (warehouseArea ?: return).warehouseAreaId,
                        desc,
                        binding.onlyActiveSwitch.isChecked
                    )
                    itemCategory != null -> DataCollectionRuleDbHelper().selectByTargetItemCategoryIdDescription(
                        (itemCategory ?: return).itemCategoryId,
                        desc,
                        binding.onlyActiveSwitch.isChecked
                    )
                    else -> DataCollectionRuleDbHelper().selectByDescription(desc)
                }

            fillAdapter(dcrArray)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun fillAdapter(dcrArray: ArrayList<DataCollectionRule>) {
        try {
            runOnUiThread {
                if (arrayAdapter != null) {
                    lastSelected =
                        arrayAdapter?.currentDataCollectionRule()
                    firstVisiblePos = arrayAdapter?.firstVisiblePos()
                }

                arrayAdapter = DataCollectionRuleAdapter(
                    activity = this,
                    resource = R.layout.data_collection_rule_row,
                    dcRules = dcrArray,
                    listView = binding.dcRuleListView,
                    multiSelect = false,
                    listener = null
                )

                while (binding.dcRuleListView.adapter == null) {
                    // Horrible wait for full load
                }

                if (arrayAdapter != null) {
                    arrayAdapter?.setSelectItemAndScrollPos(
                        lastSelected,
                        firstVisiblePos
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
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