package com.dacosys.assetControl.views.assets.assetManteinance.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.databinding.AssetManteinanceStatusActivityBinding
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.`object`.AssetManteinance
import com.dacosys.assetControl.model.assets.manteinances.manteinanceStatus.`object`.ManteinanceStatus
import com.dacosys.assetControl.model.assets.manteinances.manteinanceType.`object`.ManteinanceType
import com.dacosys.assetControl.views.assets.assetManteinance.fragment.ManteinanceStatusSpinnerFragment
import com.dacosys.assetControl.views.assets.assetManteinance.fragment.ManteinanceTypeSpinnerFragment
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType

class AssetManteinanceStatusActivity : AppCompatActivity(),
    ManteinanceTypeSpinnerFragment.OnItemSelectedListener,
    ManteinanceStatusSpinnerFragment.OnItemSelectedListener {
    private var mantTypeSpinnerFragment: ManteinanceTypeSpinnerFragment? = null
    private var mantStatusSpinnerFragment: ManteinanceStatusSpinnerFragment? = null
    private var currentAssetMant: AssetManteinance? = null

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

    private lateinit var binding: AssetManteinanceStatusActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = AssetManteinanceStatusActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.maintenance_status)

        val extras = intent.extras
        if (extras != null) {
            currentAssetMant = extras.getParcelable("assetManteinance")
        }

        binding.descriptionTextView.text = currentAssetMant?.assetStr
        binding.codeTextView.text = currentAssetMant?.assetCode

        mantStatusSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceStatusFragment.id) as ManteinanceStatusSpinnerFragment
        mantTypeSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.manteinanceTypeFragment.id) as ManteinanceTypeSpinnerFragment

        binding.okButton.setOnClickListener { confirm() }

        fillControls()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun fillControls() {
        mantStatusSpinnerFragment?.selectedManteinanceStatus = currentAssetMant?.manteinanceStatus
        mantTypeSpinnerFragment?.selectedManteinanceType = currentAssetMant?.manteinanceType
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

    private fun confirm() {
        if (mantStatusSpinnerFragment?.selectedManteinanceStatus == null ||
            (mantStatusSpinnerFragment?.selectedManteinanceStatus?.id ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_state),
                SnackbarType.INFO
            )
            return
        }

        if (mantTypeSpinnerFragment?.selectedManteinanceType == null ||
            (mantTypeSpinnerFragment?.selectedManteinanceType?.manteinanceTypeId ?: -1) <= 0
        ) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_maintenance_task),
                SnackbarType.INFO
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
                SnackbarType.SUCCESS
            )
            finish()
        } else {
            makeText(
                binding.root,
                getString(R.string.failed_to_save_maintenance),
                SnackbarType.ERROR
            )
        }
    }

    override fun onItemSelected(manteinanceType: ManteinanceType?) {
    }

    override fun onItemSelected(manteinanceStatus: ManteinanceStatus?) {
    }

    override fun onBackPressed() {
        Statics.closeKeyboard(this)

        setResult(RESULT_CANCELED)
        finish()
    }
}