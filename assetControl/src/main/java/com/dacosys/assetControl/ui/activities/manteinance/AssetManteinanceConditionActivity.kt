package com.dacosys.assetControl.ui.activities.manteinance

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceDbHelper
import com.dacosys.assetControl.databinding.AssetManteinanceConditionActivityBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetCondition
import com.dacosys.assetControl.model.manteinance.AssetManteinance
import com.dacosys.assetControl.model.manteinance.ManteinanceStatus
import com.dacosys.assetControl.model.manteinance.ManteinanceType
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.asset.AssetConditionSpinnerFragment
import com.dacosys.assetControl.ui.fragments.manteinance.ManteinanceTypeSpinnerFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI


class AssetManteinanceConditionActivity : AppCompatActivity(),
    ManteinanceTypeSpinnerFragment.OnItemSelectedListener,
    AssetConditionSpinnerFragment.OnItemSelectedListener {
    private var manteinanceTypeSpinnerFragment: ManteinanceTypeSpinnerFragment? = null
    private var assetConditionSpinnerFragment: AssetConditionSpinnerFragment? = null
    private var currentAsset: Asset? = null
    private var assetManteinance: AssetManteinance? = null

    private lateinit var binding: AssetManteinanceConditionActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetManteinanceConditionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.new_maintenance_task)

        val extras = intent.extras
        if (extras != null) {
            val assetId = extras.getLong("assetId")
            currentAsset = Asset(assetId, false)
        }

        binding.descriptionTextView.text = currentAsset?.description

        binding.codeTextView.text = currentAsset?.code

        assetConditionSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.conditionFragment.id) as AssetConditionSpinnerFragment
        manteinanceTypeSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceTypeFragment.id) as ManteinanceTypeSpinnerFragment

        binding.okButton.setOnClickListener { confirm() }

        fillControls()

        setupUI(binding.root, this)
    }

    private fun fillControls() {
        if (currentAsset?.assetCondition != null) {
            assetConditionSpinnerFragment?.selectedAssetCondition = currentAsset?.assetCondition
        }

        val aM =
            AssetManteinanceDbHelper().selectByAssetIdNotTransferred(currentAsset?.assetId ?: -1)
        if (aM != null) {
            assetManteinance = aM
            manteinanceTypeSpinnerFragment?.selectedManteinanceType =
                assetManteinance?.manteinanceType
            binding.obsEditText.setText(
                assetManteinance?.observations,
                TextView.BufferType.EDITABLE
            )
        }
    }

    private fun confirm() {
        if (currentAsset != null) {
            val tempObsText = binding.obsEditText.text.toString()

            var error = false

            // Save Manteinance
            if (manteinanceTypeSpinnerFragment?.selectedManteinanceType != null &&
                (manteinanceTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId
                    ?: -1) > 0
            ) {
                if (assetManteinance != null) {
                    assetManteinance?.observations = tempObsText
                    assetManteinance?.manteinanceStatusId = ManteinanceStatus.repair.id
                    assetManteinance?.manteinanceTypeId =
                        manteinanceTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId
                            ?: -1
                    assetManteinance?.transferred = false
                    assetManteinance?.saveChanges()
                } else {
                    if (currentAsset != null) {
                        val aM = AssetManteinanceDbHelper().insert(
                            currentAsset!!,
                            tempObsText,
                            ManteinanceStatus.repair.id,
                            manteinanceTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId
                                ?: -1
                        )

                        if (aM == null) {
                            error = true
                        }
                    }
                }
            } else {
                makeText(
                    binding.root,
                    getString(R.string.you_must_select_a_type_of_maintenance_task),
                    SnackBarType.INFO
                )
                return

                /*
                if (assetManteinance != null) { // VER ESTO!!! ¿Qué es esto?
                    val amDbHelper = AssetManteinanceDbHelper()
                    amDbHelper.deleteByAssetId(assetManteinance!!.assetId)
                }
                */
            }

            if (!error) {
                // Save Asset Condition
                if (assetConditionSpinnerFragment?.selectedAssetCondition != null) {
                    if (currentAsset != null &&
                        currentAsset?.assetCondition !== assetConditionSpinnerFragment?.selectedAssetCondition
                    ) {
                        currentAsset?.assetConditionId =
                            assetConditionSpinnerFragment?.selectedAssetCondition?.id ?: -1
                        currentAsset?.transferred = false

                        if (currentAsset?.saveChanges() == false) {
                            error = true
                        }
                    }
                }
            }

            if (!error) {
                makeText(
                    binding.root,
                    getString(R.string.maintenance_saved_correctly),
                    SnackBarType.SUCCESS
                )
                finish()
            } else {
                makeText(
                    binding.root,
                    getString(R.string.failed_to_save_maintenance),
                    SnackBarType.ERROR
                )
            }
        }
    }

    override fun onItemSelected(manteinanceType: ManteinanceType?) {
    }

    override fun onItemSelected(assetCondition: AssetCondition?) {
    }

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        closeKeyboard(this)

        setResult(RESULT_CANCELED)
        finish()
    }
}