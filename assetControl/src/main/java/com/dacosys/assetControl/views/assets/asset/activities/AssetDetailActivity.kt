package com.dacosys.assetControl.views.assets.asset.activities

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
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.AssetDetailActivityBinding
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.imageControl.fragments.ImageControlButtonsFragment

class AssetDetailActivity : AppCompatActivity() {
    private var asset: Asset? = null
    private var rejectNewInstances = false

    private var imageControlFragment: ImageControlButtonsFragment? = null

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    override fun onBackPressed() {
        ////////////// IMAGE CONTROL //////////////
        imageControlFragment?.saveImages(true)
        super.onBackPressed()
    }

    private fun destroyLocals() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    override fun onResume() {
        super.onResume()
        rejectNewInstances = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if (imageControlFragment is ImageControlButtonsFragment)
            supportFragmentManager.putFragment(
                savedInstanceState,
                "imageControlFragment",
                imageControlFragment as ImageControlButtonsFragment
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

    private lateinit var binding: AssetDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = AssetDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.asset_information)

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
        } else {
            val extras = intent.extras
            if (extras != null) {
                asset = extras.getParcelable("asset")
            }
        }

        if (asset != null) {
            binding.descriptionTextView.text = (asset ?: return).description
            binding.codeTextView.text = (asset ?: return).code
            binding.eanTextView.text = (asset ?: return).ean
            binding.itemCategoryTextView.text = (asset ?: return).itemCategoryStr
            binding.warehouseTextView.text = (asset ?: return).warehouseStr
            binding.warehouseAreaTextView.text = (asset ?: return).warehouseAreaStr
            binding.origWarehouseTextView.text = (asset ?: return).originalWarehouseStr
            binding.origWarehouseAreaTextView.text = (asset ?: return).originalWarehouseAreaStr
            binding.assetStatusTextView.text = ((asset ?: return).assetStatus ?: return).description
            binding.manufacturerTextView.text = (asset ?: return).manufacturer
            binding.modelTextView.text = (asset ?: return).model
            binding.serialNumberTextView.text = (asset ?: return).serialNumber
        }

        binding.assetDetail.setOnClickListener { onBackPressed() }

        setImageControlFragment()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun setImageControlFragment() {
        var description = asset?.description

        val tableName = Table.asset.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                Table.asset.tableId,
                asset?.assetId ?: 0,
                null
            )

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            val fm = supportFragmentManager

            if (!isFinishing)
                runOnUiThread {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(
                            binding.imageControlLayout.id,
                            imageControlFragment ?: return@runOnUiThread
                        )
                        .commit()

                    if (!Statics.prefsGetBoolean(Preference.useImageControl)) {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(imageControlFragment as Fragment)
                            .commitAllowingStateLoss()
                    } else {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                            .commitAllowingStateLoss()
                    }
                }
        } else {
            imageControlFragment?.setTableId(Table.asset.tableId)
            imageControlFragment?.setObjectId1(asset?.assetId ?: 0)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }
        }
    }
}