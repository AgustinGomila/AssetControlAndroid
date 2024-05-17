package com.dacosys.assetControl.ui.activities.asset

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.AssetControlApp.Companion.currentUser
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.databinding.AssetDetailActivityBinding
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Repository.Companion.useImageControl
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable

class AssetDetailActivity : AppCompatActivity() {
    private var asset: Asset? = null
    private var rejectNewInstances = false

    private var imageControlFragment: ImageControlButtonsFragment? = null

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
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
        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )
    }

    private fun isBackPressed() {
        imageControlFragment?.saveImages(true)
        finish()
    }

    private lateinit var binding: AssetDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.asset_information)

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
        } else {
            val extras = intent.extras
            if (extras != null) {
                asset = extras.parcelable("asset")
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
            binding.assetStatusTextView.text = (asset ?: return).assetStatus.description
            binding.manufacturerTextView.text = (asset ?: return).manufacturer
            binding.modelTextView.text = (asset ?: return).model
            binding.serialNumberTextView.text = (asset ?: return).serialNumber
        }

        binding.assetDetail.setOnClickListener { isBackPressed() }

        setImageControlFragment()

        setupUI(binding.root, this)
    }

    private fun setImageControlFragment() {
        if (!useImageControl) {
            runOnUiThread {
                binding.imageControlLayout.visibility = View.GONE
            }
            return
        }

        val tempAsset = asset ?: return
        var description = tempAsset.description
        val table = Table.routeProcess
        val id = tempAsset.id

        description = "${table.tableName}: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = table.id.toLong(),
                objectId1 = id.toString()
            )

            setFragmentValues(description, "", obs)

            val fm = supportFragmentManager

            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                            binding.imageControlLayout.id, imageControlFragment ?: return@runOnUiThread
                        ).commit()

                    if (!prefsGetBoolean(Preference.useImageControl)) {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(imageControlFragment as Fragment).commitAllowingStateLoss()
                    } else {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                            .commitAllowingStateLoss()
                    }
                }
            }
        } else {
            imageControlFragment?.setTableId(table.id)
            imageControlFragment?.setObjectId1(id)
            imageControlFragment?.setObjectId2(null)

            setFragmentValues(description, "", obs)
        }
    }

    private fun setFragmentValues(description: String, reference: String, obs: String) {
        if (description.isNotEmpty()) {
            imageControlFragment?.setDescription(description)
        }

        if (reference.isNotEmpty()) {
            imageControlFragment?.setReference(reference)
        }

        if (obs.isNotEmpty()) {
            imageControlFragment?.setObs(obs)
        }
    }
}