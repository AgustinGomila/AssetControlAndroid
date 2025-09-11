package com.example.assetControl.ui.activities.maintenance

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import com.example.assetControl.R
import com.example.assetControl.data.enums.maintenance.MaintenanceStatus
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenance
import com.example.assetControl.data.room.dto.maintenance.MaintenanceType
import com.example.assetControl.databinding.AssetManteinanceStatusActivityBinding
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.manteinance.MaintenanceStatusSpinnerFragment
import com.example.assetControl.ui.fragments.manteinance.MaintenanceTypeSpinnerFragment

class AssetMaintenanceStatusActivity : AppCompatActivity(),
    MaintenanceTypeSpinnerFragment.OnItemSelectedListener,
    MaintenanceStatusSpinnerFragment.OnItemSelectedListener {
    private var typeSpinnerFragment: MaintenanceTypeSpinnerFragment? = null
    private var statusSpinnerFragment: MaintenanceStatusSpinnerFragment? = null
    private var maintenance: AssetMaintenance? = null

    private lateinit var binding: AssetManteinanceStatusActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetManteinanceStatusActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.maintenance_status)

        val extras = intent.extras
        if (extras != null) {
            maintenance = extras.parcelable("assetManteinance")
        }

        binding.descriptionTextView.text = maintenance?.assetStr
        binding.codeTextView.text = maintenance?.assetCode

        statusSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceStatusFragment.id) as MaintenanceStatusSpinnerFragment
        typeSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceTypeFragment.id) as MaintenanceTypeSpinnerFragment

        binding.okButton.setOnClickListener { confirm() }

        fillControls()

        setupUI(binding.root, this)
    }

    private fun fillControls() {
        statusSpinnerFragment?.selectedMaintenanceStatus = maintenance?.maintenanceStatus
        typeSpinnerFragment?.selectedId = maintenance?.maintenanceTypeId
    }

    private fun confirm() {
        if (statusSpinnerFragment?.selectedMaintenanceStatus == null ||
            (statusSpinnerFragment?.selectedMaintenanceStatus?.id ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_state),
                SnackBarType.INFO
            )
            return
        }

        if (typeSpinnerFragment?.selectedType == null ||
            (typeSpinnerFragment?.selectedType?.id ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_maintenance_task),
                SnackBarType.INFO
            )
            return
        }

        maintenance?.observations = binding.obsEditText.text.toString()
        maintenance?.maintenanceStatusId = statusSpinnerFragment?.selectedMaintenanceStatus?.id ?: -1
        maintenance?.maintenanceTypeId = typeSpinnerFragment?.selectedType?.id ?: -1
        maintenance?.transferred = false

        maintenance?.saveChanges()
        makeText(
            binding.root,
            getString(R.string.maintenance_saved_correctly),
            SnackBarType.SUCCESS
        )
        finish()
    }

    override fun onItemSelected(maintenanceType: MaintenanceType?) {
    }

    override fun onItemSelected(maintenanceStatus: MaintenanceStatus?) {
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }
}