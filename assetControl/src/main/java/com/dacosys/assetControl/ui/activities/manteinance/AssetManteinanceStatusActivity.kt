package com.dacosys.assetControl.ui.activities.manteinance

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.model.manteinance.AssetManteinance
import com.dacosys.assetControl.data.model.manteinance.ManteinanceStatus
import com.dacosys.assetControl.data.model.manteinance.ManteinanceType
import com.dacosys.assetControl.databinding.AssetManteinanceStatusActivityBinding
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.manteinance.ManteinanceStatusSpinnerFragment
import com.dacosys.assetControl.ui.fragments.manteinance.ManteinanceTypeSpinnerFragment
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable

class AssetManteinanceStatusActivity : AppCompatActivity(),
    ManteinanceTypeSpinnerFragment.OnItemSelectedListener,
    ManteinanceStatusSpinnerFragment.OnItemSelectedListener {
    private var mantTypeSpinnerFragment: ManteinanceTypeSpinnerFragment? = null
    private var mantStatusSpinnerFragment: ManteinanceStatusSpinnerFragment? = null
    private var currentAssetMant: AssetManteinance? = null

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
            currentAssetMant = extras.parcelable("assetManteinance")
        }

        binding.descriptionTextView.text = currentAssetMant?.assetStr
        binding.codeTextView.text = currentAssetMant?.assetCode

        mantStatusSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceStatusFragment.id) as ManteinanceStatusSpinnerFragment
        mantTypeSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceTypeFragment.id) as ManteinanceTypeSpinnerFragment

        binding.okButton.setOnClickListener { confirm() }

        fillControls()

        setupUI(binding.root, this)
    }

    private fun fillControls() {
        mantStatusSpinnerFragment?.selectedManteinanceStatus = currentAssetMant?.manteinanceStatus
        mantTypeSpinnerFragment?.selectedManteinanceType = currentAssetMant?.manteinanceType
    }

    private fun confirm() {
        if (mantStatusSpinnerFragment?.selectedManteinanceStatus == null ||
            (mantStatusSpinnerFragment?.selectedManteinanceStatus?.id ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_state),
                SnackBarType.INFO
            )
            return
        }

        if (mantTypeSpinnerFragment?.selectedManteinanceType == null ||
            (mantTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_maintenance_task),
                SnackBarType.INFO
            )
            return
        }

        currentAssetMant?.observations = binding.obsEditText.text.toString()
        currentAssetMant?.manteinanceStatusId =
            mantStatusSpinnerFragment?.selectedManteinanceStatus?.id ?: -1
        currentAssetMant?.manteinanceTypeId =
            mantTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId ?: -1
        currentAssetMant?.transferred = false

        if (currentAssetMant?.saveChanges() == true) {
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

    override fun onItemSelected(manteinanceType: ManteinanceType?) {
    }

    override fun onItemSelected(manteinanceStatus: ManteinanceStatus?) {
    }

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }
}