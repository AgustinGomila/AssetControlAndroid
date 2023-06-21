package com.dacosys.assetControl.ui.activities.review

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.review.AssetReviewContentAdapter
import com.dacosys.assetControl.databinding.AssetReviewContentConfirmBottomPanelCollapsedBinding
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.model.review.AssetReviewContentStatus
import com.dacosys.assetControl.model.status.ConfirmStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.activities.common.ObservationsActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutBoolean
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import org.parceler.Parcels

class AssetReviewContentConfirmActivity : AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 1000)
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    override fun onPause() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
        saveSharedPreferences()
        super.onPause()
    }

    private fun saveSharedPreferences() {
        prefsPutBoolean(
            "asset_review_completed_checkbox", binding.completedSwitch.isChecked
        )
    }

    private fun destroyLocals() {
        arContAdapter?.refreshListeners(null, null, null)
    }

    private var rejectNewInstances = false

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var arContAdapter: AssetReviewContentAdapter? = null
    private var arContArray: ArrayList<AssetReviewContent> = ArrayList()
    private var assetReview: AssetReview? = null
    private var obs = ""

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true


    override fun onResume() {
        super.onResume()
        rejectNewInstances = false

        fillAdapter()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("assetReview", assetReview)
        savedInstanceState.putBoolean(
            "completedSwitch", binding.completedSwitch.isChecked
        )
        savedInstanceState.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        savedInstanceState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )

        if (arContAdapter != null) {
            savedInstanceState.putParcelableArrayList(
                "arContArray", arContAdapter?.getAll()
            )
        }
    }

    private lateinit var binding: AssetReviewContentConfirmBottomPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetReviewContentConfirmBottomPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.confirm_asset_review)

        headerFragment =
            supportFragmentManager.findFragmentById(binding.headerFragment.id) as LocationHeaderFragment?
        if (savedInstanceState != null) {
            assetReview = savedInstanceState.getParcelable("assetReview")
            if (assetReview != null) {
                obs = (assetReview ?: return).obs
            }

            binding.completedSwitch.isChecked = savedInstanceState.getBoolean("completedSwitch")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF

            panelBottomIsExpanded = savedInstanceState.getBoolean("panelBottomIsExpanded")
            panelTopIsExpanded = savedInstanceState.getBoolean("panelTopIsExpanded")

            // Adapter
            arContArray.clear()
            val tempCont =
                savedInstanceState.getParcelableArrayList<AssetReviewContent>("arContArray")
            if (tempCont != null) {
                arContArray = tempCont
            }
        } else {
            val extras = intent.extras
            if (extras != null) {
                assetReview = Parcels.unwrap<AssetReview>(extras.getParcelable("assetReview"))

                if (assetReview != null) {
                    obs = (assetReview ?: return).obs
                }

                val t1 = extras.getParcelableArrayList<AssetReviewContent>("arContArray")
                if (t1 != null) {
                    arContArray = t1
                }
            }
        }

        setHeaderTextBox()

        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.obsButton.setOnClickListener { addObservations() }
        binding.completedSwitch.isChecked =
            prefsGetBoolean(Preference.assetReviewCompletedCheckBox)
        binding.confirmButton.setOnClickListener { confirmCount() }

        setPanels()

        setImageControlFragment()

        setupUI(binding.root, this)
    }

    private fun setHeaderTextBox() {
        headerFragment?.showChangePostButton(false)
        headerFragment?.setTitle(getString(R.string.area_revised))

        if (assetReview != null && headerFragment != null) {
            runOnUiThread {
                headerFragment?.fill(
                    (assetReview ?: return@runOnUiThread).warehouseAreaId
                )
            }
        }
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            if (panelTopIsExpanded) {
                currentLayout.load(this, R.layout.asset_review_content_confirm_activity)
            } else {
                currentLayout.load(
                    this, R.layout.asset_review_content_confirm_top_panel_collapsed
                )
            }
        } else {
            if (panelTopIsExpanded) {
                currentLayout.load(
                    this, R.layout.asset_review_content_confirm_bottom_panel_collapsed
                )
            } else {
                currentLayout.load(
                    this, R.layout.asset_review_content_confirm_both_panels_collapsed
                )
            }
        }

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(
            binding.assetReviewContentConfirm, transition
        )

        currentLayout.applyTo(binding.assetReviewContentConfirm)

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
            }
            else -> {
                binding.expandBottomPanelButton?.text = getString(R.string.more_options)
            }
        }

        when {
            panelTopIsExpanded -> {
                binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
            }
            else -> {
                binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
            }
        }
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_bottom_panel_collapsed
                    )
                } else {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_both_panels_collapsed
                    )
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.asset_review_content_confirm_activity)
                } else {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_top_panel_collapsed
                    )
                }
            }

            panelBottomIsExpanded = !panelBottomIsExpanded
            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.assetReviewContentConfirm, transition
            )

            nextLayout.applyTo(binding.assetReviewContentConfirm)

            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.more_options)
                }
            }
        }
    }

    private fun setTopPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandTopPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_top_panel_collapsed
                    )
                } else {
                    nextLayout.load(this, R.layout.asset_review_content_confirm_activity)
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_both_panels_collapsed
                    )
                } else {
                    nextLayout.load(
                        this, R.layout.asset_review_content_confirm_bottom_panel_collapsed
                    )
                }
            }

            panelTopIsExpanded = !panelTopIsExpanded

            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.assetReviewContentConfirm, transition
            )

            nextLayout.applyTo(binding.assetReviewContentConfirm)

            when {
                panelTopIsExpanded -> {
                    binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
                }
            }
        }
    }

    private fun setImageControlFragment() {
        val ar = assetReview ?: return

        var description = ar.warehouseAreaStr
        val tableName = Table.assetReview.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment =
                ImageControlButtonsFragment.newInstance(
                    Table.assetReview.tableId.toLong(), ar.collectorAssetReviewId.toString()
                )
        }

        if (description.isNotEmpty()) {
            imageControlFragment?.setDescription(description)
        }

        val fm = supportFragmentManager

        if (!isFinishing) runOnUiThread {
            fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                    binding.imageControlFragment.id, imageControlFragment ?: return@runOnUiThread
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
        } else {
            imageControlFragment?.setTableId(Table.assetReview.tableId)
            imageControlFragment?.setObjectId1(ar.collectorAssetReviewId)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }
        }
    }

    override fun onBackPressed() {
        modifyCount()
        super.onBackPressed()
    }

    private fun setupTextView() {
        val assetsMissed = arContAdapter?.assetsMissed ?: 0
        val assetsAdded = arContAdapter?.assetsAdded ?: 0
        val assetsRevised = arContAdapter?.assetsRevised ?: 0

        runOnUiThread {
            if (arContAdapter != null) {
                binding.missedTextView.text = assetsMissed.toString()
                binding.addedTextView.text = assetsAdded.toString()
                binding.revisedTextView.text = assetsRevised.toString()
            }
        }
    }

    private fun fillAdapter() {
        var lastSelected: AssetReviewContent? = null
        if (arContAdapter != null) {
            lastSelected = arContAdapter?.currentArCont()
        }

        try {
            runOnUiThread {
                arContAdapter = AssetReviewContentAdapter(
                    activity = this,
                    resource = R.layout.asset_row,
                    assetReviewContArray = arContArray,
                    suggestedList = arContArray,
                    listView = binding.assetReviewContentListView,
                    multiSelect = false,
                    checkedIdArray = ArrayList(),
                    visibleStatus = AssetReviewContentStatus.getAllConfirm()
                )
            }

            while (binding.assetReviewContentListView.adapter == null) {
                // Horrible wait for full load
            }

            if (arContAdapter != null) {
                if (lastSelected != null && arContAdapter!!.getItems().contains(lastSelected)) {
                    arContAdapter?.selectItem(
                        arc = lastSelected, smoothScroll = true
                    )
                } else if (countItems > 0) {
                    arContAdapter?.selectItem(pos = 0, smoothScroll = true)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            setupTextView()
        }
    }

    private val countItems: Int
        get() {
            return arContAdapter?.count() ?: 0
        }

    private fun addObservations() {
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, ObservationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("obs", obs)
            resultForObs.launch(intent)
        }
    }

    private val resultForObs =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    obs = data.getStringExtra("obs") ?: ""
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun confirmCount() {
        // Si tiene firma obligatoria, no está firmado y la revisión está completada, solicitar firma.
        if (prefsGetBoolean(Preference.signReviewsAndMovements) && !(imageControlFragment
                ?: return).isSigned && binding.completedSwitch.isChecked
        ) {
            makeText(
                binding.root, getString(R.string.mandatory_sign), SnackBarType.ERROR
            )
        } else {
            ///////////////////////////////////////////
            ////////////// IMAGE CONTROL //////////////
            if (imageControlFragment != null) {
                imageControlFragment?.saveImages(false)
            }
            ///////////////////////////////////////////

            closeKeyboard(this)

            val data = Intent()
            data.putExtra("confirmStatus", Parcels.wrap(ConfirmStatus.confirm))
            data.putExtra("obs", obs)
            data.putExtra("completed", binding.completedSwitch.isChecked)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun modifyCount() {
        closeKeyboard(this)

        ///////////////////////////////////////////
        ////////////// IMAGE CONTROL //////////////
        if (imageControlFragment != null) {
            imageControlFragment?.saveImages(false)
        }
        ///////////////////////////////////////////

        val data = Intent()
        data.putExtra("confirmStatus", Parcels.wrap(ConfirmStatus.modify))
        data.putExtra("obs", obs)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}