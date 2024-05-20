package com.dacosys.assetControl.ui.activities.maintenance

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.asset.AssetCondition
import com.dacosys.assetControl.data.enums.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenance
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.maintenance.AssetMaintenanceRepository
import com.dacosys.assetControl.databinding.AssetManteinanceConditionActivityBinding
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.asset.AssetConditionSpinnerFragment
import com.dacosys.assetControl.ui.fragments.manteinance.MaintenanceTypeSpinnerFragment


class AssetMaintenanceConditionActivity : AppCompatActivity(),
    MaintenanceTypeSpinnerFragment.OnItemSelectedListener,
    AssetConditionSpinnerFragment.OnItemSelectedListener {
    private var typeSpinnerFragment: MaintenanceTypeSpinnerFragment? = null
    private var conditionSpinnerFragment: AssetConditionSpinnerFragment? = null
    private var currentAsset: Asset? = null
    private var assetMaintenance: AssetMaintenance? = null

    private lateinit var binding: AssetManteinanceConditionActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetManteinanceConditionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.new_maintenance_task)

        val extras = intent.extras
        if (extras != null) {
            val assetId = extras.getLong("assetId")
            currentAsset = AssetRepository().selectById(assetId)
        }

        binding.descriptionTextView.text = currentAsset?.description

        binding.codeTextView.text = currentAsset?.code

        conditionSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.conditionFragment.id) as AssetConditionSpinnerFragment
        typeSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceTypeFragment.id) as MaintenanceTypeSpinnerFragment

        binding.okButton.setOnClickListener { confirm() }

        fillControls()

        setupUI(binding.root, this)
    }

    private fun fillControls() {
        if (currentAsset?.assetCondition != null) {
            conditionSpinnerFragment?.selectedAssetCondition = currentAsset?.assetCondition
        }

        val assetId = currentAsset?.id ?: -1
        val aM = AssetMaintenanceRepository().selectByAssetIdNotTransferred(assetId)
        if (aM != null) {
            assetMaintenance = aM
            typeSpinnerFragment?.selectedId = assetMaintenance?.maintenanceTypeId
            binding.obsEditText.setText(assetMaintenance?.observations, TextView.BufferType.EDITABLE)
        }
    }

    private fun confirm() {
        if (currentAsset != null) {
            val tempObsText = binding.obsEditText.text.toString()

            // Save Maintenance
            if (typeSpinnerFragment?.selectedType != null &&
                (typeSpinnerFragment?.selectedType?.id
                    ?: -1) > 0
            ) {
                if (assetMaintenance != null) {
                    assetMaintenance?.observations = tempObsText
                    assetMaintenance?.statusId = MaintenanceStatus.repair.id
                    assetMaintenance?.maintenanceTypeId = typeSpinnerFragment?.selectedType?.id ?: -1
                    assetMaintenance?.transferred = false
                    assetMaintenance?.saveChanges()
                } else {
                    val maintenanceTypeId = typeSpinnerFragment?.selectedType?.id ?: -1
                    val asset = currentAsset

                    if (asset != null) {
                        val maintenance = AssetMaintenance(
                            asset = asset,
                            obs = tempObsText,
                            statusId = MaintenanceStatus.repair.id,
                            maintenanceTypeId = maintenanceTypeId
                        )
                        AssetMaintenanceRepository().insert(maintenance)
                    }
                }
            } else {
                makeText(
                    binding.root,
                    getString(R.string.you_must_select_a_type_of_maintenance_task),
                    SnackBarType.INFO
                )
                return
            }

            makeText(
                binding.root,
                getString(R.string.maintenance_saved_correctly),
                SnackBarType.SUCCESS
            )
            finish()
        }
    }

    override fun onItemSelected(maintenanceType: MaintenanceType?) {
    }

    override fun onItemSelected(assetCondition: AssetCondition?) {
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }
}