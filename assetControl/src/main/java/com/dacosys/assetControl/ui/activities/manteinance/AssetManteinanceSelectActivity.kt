package com.dacosys.assetControl.ui.activities.manteinance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceDbHelper
import com.dacosys.assetControl.databinding.AssetManteinanceSelectActivityBinding
import com.dacosys.assetControl.model.manteinance.AssetManteinance
import com.dacosys.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.dacosys.assetControl.ui.adapters.manteinance.AssetManteinanceAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutBoolean
import com.dacosys.assetControl.utils.settings.Preference

class AssetManteinanceSelectActivity : AppCompatActivity(),
    AssetManteinanceAdapter.CustomCheckedChangeListener {
    override fun onCustomCheckedChangeListener(isChecked: Boolean, pos: Int) {
        val tempObj = binding.assetManteinanceListView.adapter.getItem(pos)
        if (tempObj != null) {
            val w = tempObj as AssetManteinance
            if (isChecked) amantChecked.add(w)
            else amantChecked.remove(w)
        }
        selectRow(pos)
    }

    private var tempTitle = ""

    private var rejectNewInstances = false
    private var currentAssetManteinance: AssetManteinance? = null
    private var amantChecked: ArrayList<AssetManteinance> = ArrayList()
    private var arrayAdapter: AssetManteinanceAdapter? = null
    private var lastSelected: AssetManteinance? = null
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
        b.putParcelable("currentAssetManteinance", currentAssetManteinance)
        b.putBoolean("onlyActive", binding.onlyActiveSwitch.isChecked)
        b.putParcelableArrayList("amantChecked", amantChecked)
        b.putString("description", binding.etDescription.text.toString())

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentAssetManteinance())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t0 = b.getString("title")
        tempTitle = if (t0 != null && t0.isNotEmpty()) t0 else getString(R.string.route_process)
        // endregion

        binding.etDescription.setText(b.getString("description"))
        binding.onlyActiveSwitch.isChecked = b.getBoolean("onlyActive")

        currentAssetManteinance = b.getParcelable("currentAssetManteinance")
        amantChecked = (b.getParcelableArrayList("amantChecked") ?: return)

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
        binding.assetManteinanceListView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val pos = binding.assetManteinanceListView.pointToPosition(
                        event.x.toInt(), event.y.toInt()
                    )
                    if (pos >= 0) {
                        selectRow(pos)
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        binding.assetManteinanceListView.setOnItemClickListener { _, _, pos, _ -> selectRow(pos) }
        binding.statusButton.setOnClickListener { changeStatus() }
        binding.newButton.setOnClickListener { newManteinance() }
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
        binding.assetManteinanceListView.clearChoices()
        if (pos < 0) {
            return
        }

        if (binding.assetManteinanceListView.adapter.count > 0) {
            val tempObj = binding.assetManteinanceListView.adapter.getItem(pos)
            if (tempObj != null) {
                val typedObj = tempObj as AssetManteinance
                currentAssetManteinance = typedObj

                runOnUiThread {
                    binding.assetManteinanceListView.setItemChecked(pos, true)
                    binding.assetManteinanceListView.setSelection(pos)

                    (binding.assetManteinanceListView.adapter as AssetManteinanceAdapter).notifyDataSetChanged()

                    binding.assetManteinanceListView.smoothScrollToPosition(pos)
                }
            }
        }
    }

    private fun changeStatus() {
        if (currentAssetManteinance == null) return

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, AssetManteinanceStatusActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("assetManteinance", currentAssetManteinance)
            startActivity(intent)
        }
    }

    private fun newManteinance() {
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
                    val idParcel = data.getParcelableArrayListExtra<ParcelLong>(
                        "ids"
                    ) ?: return@registerForActivityResult

                    val ids: ArrayList<Long?> = ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val a = AssetDbHelper().selectById(ids[0]) ?: return@registerForActivityResult
                    try {
                        if (!rejectNewInstances) {
                            rejectNewInstances = true

                            val intent = Intent(this, AssetManteinanceConditionActivity::class.java)
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
            arrayAdapter?.listView = binding.assetManteinanceListView

            binding.assetManteinanceListView.adapter = arrayAdapter
            (binding.assetManteinanceListView.adapter as AssetManteinanceAdapter).notifyDataSetChanged()

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

        var assetMantList: ArrayList<AssetManteinance> = ArrayList()
        try {
            assetMantList = when {
                description.isNotEmpty() -> AssetManteinanceDbHelper().selectByDescriptionCodeEan(
                    description,
                    onlyActive
                )

                else -> AssetManteinanceDbHelper().select(onlyActive)
            }

            if (arrayAdapter == null) {
                runOnUiThread {
                    if (arrayAdapter != null) {
                        lastSelected = arrayAdapter?.currentAssetManteinance()
                        firstVisiblePos = arrayAdapter?.firstVisiblePos()
                    }

                    arrayAdapter = AssetManteinanceAdapter(
                        activity = this,
                        resource = R.layout.asset_manteinance_row,
                        assets = assetMantList,
                        listView = binding.assetManteinanceListView,
                        multiSelect = false,
                        listener = this
                    )

                    while (binding.assetManteinanceListView.adapter == null) {
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

        if (!assetMantList.any()) {
            makeText(
                binding.root, getString(R.string.no_maintenance_to_show), SnackBarType.INFO
            )
        }
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false
    }

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        closeKeyboard(this)

        setResult(RESULT_CANCELED)
        finish()
    }
}