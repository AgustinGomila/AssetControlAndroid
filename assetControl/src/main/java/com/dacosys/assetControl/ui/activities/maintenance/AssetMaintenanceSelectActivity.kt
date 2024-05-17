package com.dacosys.assetControl.ui.activities.maintenance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenance
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.maintenance.AssetMaintenanceRepository
import com.dacosys.assetControl.databinding.AssetManteinanceSelectActivityBinding
import com.dacosys.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.dacosys.assetControl.ui.adapters.manteinance.AssetMaintenanceAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutBoolean
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList

class AssetMaintenanceSelectActivity : AppCompatActivity(),
    AssetMaintenanceAdapter.CustomCheckedChangeListener {
    override fun onCustomCheckedChangeListener(isChecked: Boolean, pos: Int) {
        val tempObj = binding.assetMaintenanceListView.adapter.getItem(pos)
        if (tempObj != null) {
            val w = tempObj as AssetMaintenance
            if (isChecked) amantChecked.add(w)
            else amantChecked.remove(w)
        }
        selectRow(pos)
    }

    private var tempTitle = ""

    private var rejectNewInstances = false
    private var currentAssetMaintenance: AssetMaintenance? = null
    private var amantChecked: ArrayList<AssetMaintenance> = ArrayList()
    private var arrayAdapter: AssetMaintenanceAdapter? = null
    private var lastSelected: AssetMaintenance? = null
    private var firstVisiblePos: Int? = null

    override fun onDestroy() {
        saveSharedPreferences()
        destroyLocals()
        super.onDestroy()
    }

    private fun saveSharedPreferences() {
        prefsPutBoolean(
            Preference.selectAssetMaintenanceOnlyActive.key, binding.onlyActiveSwitch.isChecked
        )
    }

    private fun destroyLocals() {
        arrayAdapter?.refreshListeners(null)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putParcelable("currentAssetMaintenance", currentAssetMaintenance)
        b.putBoolean("onlyActive", binding.onlyActiveSwitch.isChecked)
        b.putParcelableArrayList("amantChecked", amantChecked)
        b.putString("description", binding.etDescription.text.toString())

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentAssetMaintenance())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t0 = b.getString("title") ?: ""
        tempTitle = t0.ifEmpty { getString(R.string.route_process) }
        // endregion

        binding.etDescription.setText(b.getString("description"))
        binding.onlyActiveSwitch.isChecked = b.getBoolean("onlyActive")

        currentAssetMaintenance = b.parcelable("currentAssetMaintenance")
        amantChecked = (b.parcelableArrayList("amantChecked") ?: return)

        val t1 = b.getString("title")
        if (!t1.isNullOrEmpty()) tempTitle = t1
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_asset_maintenance)
    }

    private lateinit var binding: AssetManteinanceSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetManteinanceSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

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
        binding.onlyActiveSwitch.isChecked = prefsGetBoolean(Preference.selectWarehouseOnlyActive)

        binding.etDescription.setOnEditorActionListener(null)
        binding.etDescription.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_UNKNOWN || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                fillListView(false)
                true
            } else {
                false
            }
        }

        // Captura el toque (ya que no es igual al click) en el control y cambia el AssetReviewContent actual
        binding.assetMaintenanceListView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val pos = binding.assetMaintenanceListView.pointToPosition(
                        event.x.toInt(), event.y.toInt()
                    )
                    if (pos >= 0) {
                        selectRow(pos)
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        binding.assetMaintenanceListView.setOnItemClickListener { _, _, pos, _ -> selectRow(pos) }
        binding.statusButton.setOnClickListener { changeStatus() }
        binding.newButton.setOnClickListener { newMaintenance() }
        binding.onlyActiveSwitch.setOnCheckedChangeListener { _, _ -> fillListView(false) }

        fillListView(savedInstanceState != null)

        setupUI(binding.root, this)
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

    private fun selectRow(pos: Int) {
        binding.assetMaintenanceListView.clearChoices()
        if (pos < 0) {
            return
        }

        if (binding.assetMaintenanceListView.adapter.count > 0) {
            val tempObj = binding.assetMaintenanceListView.adapter.getItem(pos)
            if (tempObj != null) {
                val typedObj = tempObj as AssetMaintenance
                currentAssetMaintenance = typedObj

                runOnUiThread {
                    binding.assetMaintenanceListView.setItemChecked(pos, true)
                    binding.assetMaintenanceListView.setSelection(pos)

                    (binding.assetMaintenanceListView.adapter as AssetMaintenanceAdapter).notifyDataSetChanged()

                    binding.assetMaintenanceListView.smoothScrollToPosition(pos)
                }
            }
        }
    }

    private fun changeStatus() {
        if (currentAssetMaintenance == null) return

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, AssetMaintenanceStatusActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("assetManteinance", currentAssetMaintenance)
            startActivity(intent)
        }
    }

    private fun newMaintenance() {
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("multiSelect", false)
            resultForAssetSelect.launch(intent)
        }
    }

    private val resultForAssetSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val idParcel = data.parcelableArrayList<ParcelLong>(
                        "ids"
                    ) ?: return@registerForActivityResult

                    val ids: ArrayList<Long?> = ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val id = ids.first() ?: return@registerForActivityResult
                    val a = AssetRepository().selectById(id) ?: return@registerForActivityResult

                    try {
                        if (!rejectNewInstances) {
                            rejectNewInstances = true

                            val intent = Intent(this, AssetMaintenanceConditionActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra("asset", a)
                            startActivity(intent)
                        }
                    } catch (ex: Exception) {
                        val res = getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                        makeText(
                            binding.root, res, SnackBarType.ERROR
                        )
                        android.util.Log.d(this::class.java.simpleName, res)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun fillListView(refresh: Boolean) {
        if (refresh && arrayAdapter != null) {
            arrayAdapter?.listView = binding.assetMaintenanceListView

            binding.assetMaintenanceListView.adapter = arrayAdapter
            (binding.assetMaintenanceListView.adapter as AssetMaintenanceAdapter).notifyDataSetChanged()

            showProgressBar(false)
            closeKeyboard(this)
            return
        }

        amantChecked.clear()
        arrayAdapter = null

        val description = binding.etDescription.text.toString()
        val onlyActive = binding.onlyActiveSwitch.isChecked

        if (description.isEmpty()) {
            makeText(
                binding.root,
                getString(R.string.you_must_enter_at_least_one_letter_in_the_description),
                SnackBarType.INFO
            )

            showProgressBar(false)
            closeKeyboard(this)
            return
        }

        showProgressBar(true)

        var assetMainList: ArrayList<AssetMaintenance> = ArrayList()
        try {
            assetMainList = when {
                description.isNotEmpty() -> ArrayList(
                    AssetMaintenanceRepository().getBy(
                        description = description,
                        code = description,
                        ean = description,
                        onlyActive = onlyActive
                    )
                )

                else -> ArrayList(AssetMaintenanceRepository().select(onlyActive))
            }

            if (arrayAdapter == null) {
                runOnUiThread {
                    if (arrayAdapter != null) {
                        lastSelected = arrayAdapter?.currentAssetMaintenance()
                        firstVisiblePos = arrayAdapter?.firstVisiblePos()
                    }

                    arrayAdapter = AssetMaintenanceAdapter(
                        activity = this,
                        resource = R.layout.asset_manteinance_row,
                        assets = assetMainList,
                        listView = binding.assetMaintenanceListView,
                        multiSelect = false,
                        listener = this
                    )

                    while (binding.assetMaintenanceListView.adapter == null) {
                        // Horrible wait for full load
                    }

                    if (arrayAdapter != null) {
                        arrayAdapter?.setSelectItemAndScrollPos(
                            lastSelected, firstVisiblePos
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
            closeKeyboard(this)
        }

        if (!assetMainList.any()) {
            makeText(
                binding.root, getString(R.string.no_maintenance_to_show), SnackBarType.INFO
            )
        }
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }
}