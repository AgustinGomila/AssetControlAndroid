package com.example.assetControl.ui.activities.asset

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.databinding.AssetDetailActivityBinding
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.example.assetControl.utils.settings.preferences.Repository.Companion.useImageControl

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            outState,
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

        val asset = asset
        if (asset != null) {
            binding.descriptionTextView.text = asset.description
            binding.codeTextView.text = asset.code
            binding.eanTextView.text = asset.ean
            binding.itemCategoryTextView.text = asset.itemCategoryStr
            binding.warehouseTextView.text = asset.warehouseStr
            binding.warehouseAreaTextView.text = asset.warehouseAreaStr
            binding.origWarehouseTextView.text = asset.originalWarehouseStr
            binding.origWarehouseAreaTextView.text = asset.originalWarehouseAreaStr
            binding.assetStatusTextView.text = asset.assetStatus.description
            binding.manufacturerTextView.text = asset.manufacturer
            binding.modelTextView.text = asset.model
            binding.serialNumberTextView.text = asset.serialNumber
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

        val tableDescription = table.description
        description = "$tableDescription: $description".take(255)

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