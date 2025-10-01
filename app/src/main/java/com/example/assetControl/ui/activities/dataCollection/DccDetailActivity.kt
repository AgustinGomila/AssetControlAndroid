package com.example.assetControl.ui.activities.dataCollection

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.room.dto.dataCollection.DataCollection
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRepository
import com.example.assetControl.data.room.repository.user.UserRepository
import com.example.assetControl.databinding.DccDetailActivityBinding
import com.example.assetControl.ui.adapters.route.DccAdapter
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.utils.settings.config.Preference

class DccDetailActivity : AppCompatActivity() {
    private var dc: DataCollection? = null
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

    private lateinit var binding: DccDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = DccDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.data_collection)

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
        } else {
            val extras = intent.extras
            if (extras != null) {
                val dcId = extras.getLong("dataCollectionId")
                dc = DataCollectionRepository().selectById(dcId)
            }
        }

        val tempDc = dc
        if (tempDc != null) {
            val userName = UserRepository().selectById(tempDc.userId)?.name ?: ""

            binding.descriptionTextView.text = getDescription(tempDc)
            binding.codeTextView.text = tempDc.assetCode
            binding.userTextView.text = userName

            val dc = DataCollectionRepository().selectById(tempDc.id)
            if (dc != null) {
                fillAdapter(dc)
            }
        }

        binding.dccDetail.setOnClickListener { isBackPressed() }

        setImageControlFragment()

        setupUI(binding.root, this)
    }

    private fun getDescription(dc: DataCollection): String {
        return if (dc.assetStr.isNotEmpty()) dc.assetStr
        else if (dc.warehouseAreaStr.isNotEmpty()) dc.warehouseAreaStr
        else if (dc.warehouseStr.isNotEmpty()) dc.warehouseStr
        else ""
    }

    private fun getReference(dc: DataCollection): String {
        return if (dc.assetCode.isNotEmpty()) "${getString(R.string.asset)}: ${dc.assetCode}"
        else if ((dc.warehouseAreaId ?: 0) > 0L) "${getString(R.string.area)}: ${dc.warehouseAreaId}"
        else if ((dc.warehouseId ?: 0) > 0L) "${getString(R.string.warehouse)}: ${dc.warehouseId}"
        else ""
    }

    private fun fillAdapter(dc: DataCollection) {
        runOnUiThread {
            val contents = dc.contents()
            val adapter = DccAdapter(this, contents)
            binding.dccList.adapter = adapter
        }
    }

    private fun setImageControlFragment() {
        if (!svm.useImageControl) {
            runOnUiThread {
                binding.imageControlLayout.visibility = View.GONE
            }
            return
        }

        val tempDc = dc ?: return
        val table = Table.routeProcess
        val id = tempDc.routeProcessId

        var description = getDescription(tempDc)
        val reference = getReference(tempDc)

        val tableDescription = table.description
        description = "$tableDescription: $description".take(255)

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = table.id.toLong(),
                objectId1 = id.toString()
            )

            setFragmentValues(description, reference, obs)

            val fm = supportFragmentManager

            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                            binding.imageControlLayout.id, imageControlFragment ?: return@runOnUiThread
                        ).commit()

                    if (!sr.prefsGetBoolean(Preference.useImageControl)) {
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

            setFragmentValues(description, reference, obs)
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