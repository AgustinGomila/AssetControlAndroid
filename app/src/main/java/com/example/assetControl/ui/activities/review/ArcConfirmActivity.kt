package com.example.assetControl.ui.activities.review

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.ConfirmStatus
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.enums.review.AssetReviewContentStatus
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.repository.review.TempReviewContentRepository
import com.example.assetControl.databinding.AssetReviewContentConfirmBottomPanelCollapsedBinding
import com.example.assetControl.ui.activities.common.ObservationsActivity
import com.example.assetControl.ui.adapters.interfaces.Interfaces
import com.example.assetControl.ui.adapters.review.ArcRecyclerAdapter
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutBoolean
import org.parceler.Parcels

class ArcConfirmActivity : AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener, Interfaces.DataSetChangedListener {
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
        adapter?.refreshListeners()
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
        adapter?.refreshListeners()
    }

    private var rejectNewInstances = false

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var adapter: ArcRecyclerAdapter? = null
    private var assetReview: AssetReview? = null
    private var completeList: java.util.ArrayList<AssetReviewContent> = java.util.ArrayList()
    private var checkedIdArray: java.util.ArrayList<Long> = java.util.ArrayList()
    private var lastSelected: AssetReviewContent? = null
    private var currentScrollPosition: Int = 0
    private var firstVisiblePos: Int? = null
    private var obs = ""

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true
    private var showImages
        get() = prefsGetBoolean(Preference.reviewContentShowImages)
        set(value) {
            prefsPutBoolean(Preference.reviewContentShowImages.key, value)
        }

    override fun onStart() {
        super.onStart()
        fillAdapter(completeList)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("assetReview", assetReview)
        outState.putBoolean(
            "completedSwitch", binding.completedSwitch.isChecked
        )
        outState.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        outState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            outState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )

        outState.putParcelable("lastSelected", adapter?.currentItem())
        outState.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: RecyclerView.NO_POSITION)
        outState.putLongArray("checkedIdArray", adapter?.checkedIdArray?.map { it }?.toLongArray())
        outState.putInt("currentScrollPosition", currentScrollPosition)
    }

    private lateinit var binding: AssetReviewContentConfirmBottomPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetReviewContentConfirmBottomPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.confirm_asset_review)

        headerFragment =
            supportFragmentManager.findFragmentById(binding.headerFragment.id) as LocationHeaderFragment?
        if (savedInstanceState != null) {
            assetReview = savedInstanceState.parcelable("assetReview")
            if (assetReview != null) obs = assetReview?.obs ?: ""

            binding.completedSwitch.isChecked = savedInstanceState.getBoolean("completedSwitch")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF

            panelBottomIsExpanded = savedInstanceState.getBoolean("panelBottomIsExpanded")
            panelTopIsExpanded = savedInstanceState.getBoolean("panelTopIsExpanded")

            // Adapter
            checkedIdArray =
                (savedInstanceState.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
            lastSelected = savedInstanceState.parcelable("lastSelected")
            firstVisiblePos =
                if (savedInstanceState.containsKey("firstVisiblePos")) savedInstanceState.getInt("firstVisiblePos") else -1
            currentScrollPosition = savedInstanceState.getInt("currentScrollPosition")
        } else {
            val extras = intent.extras
            if (extras != null) {
                assetReview = Parcels.unwrap<AssetReview>(extras.parcelable("assetReview"))

                if (assetReview != null) obs = assetReview?.obs ?: ""
            }
        }

        // Cargamos la revisión desde la tabla temporal
        completeList.clear()
        val tempCont = ArrayList(TempReviewContentRepository().selectByTempId(assetReview?.id ?: 0))
        if (tempCont.any()) completeList = tempCont

        setHeaderTextBox()

        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                currentScrollPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }
        })

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.obsButton.setOnClickListener { addObservations() }
        binding.completedTextView.setOnClickListener { binding.completedSwitch.performClick() }
        binding.completedSwitch.isChecked = prefsGetBoolean(Preference.assetReviewCompletedCheckBox)
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
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.asset_review_content_confirm_activity)
            else currentLayout.load(this, R.layout.asset_review_content_confirm_top_panel_collapsed)
        } else {
            if (panelTopIsExpanded) currentLayout.load(
                this,
                R.layout.asset_review_content_confirm_bottom_panel_collapsed
            )
            else currentLayout.load(this, R.layout.asset_review_content_confirm_both_panels_collapsed)
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

        if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandBottomPanelButton?.text = getString(R.string.more_options)

        if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.asset_review_content_confirm_bottom_panel_collapsed
                )
                else nextLayout.load(this, R.layout.asset_review_content_confirm_both_panels_collapsed)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_confirm_activity)
                else nextLayout.load(this, R.layout.asset_review_content_confirm_top_panel_collapsed)
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

            if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text = getString(R.string.more_options)
        }
    }

    private fun setTopPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandTopPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_confirm_top_panel_collapsed)
                else nextLayout.load(this, R.layout.asset_review_content_confirm_activity)
            } else {
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.asset_review_content_confirm_both_panels_collapsed
                )
                else nextLayout.load(this, R.layout.asset_review_content_confirm_bottom_panel_collapsed)
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

            if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
            else binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
        }
    }

    private fun setImageControlFragment() {
        val ar = assetReview ?: return

        var description = ar.warehouseAreaStr
        val tableDescription = Table.assetReview.description
        description = "$tableDescription: $description".take(255)

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment =
                ImageControlButtonsFragment.newInstance(
                    tableId = Table.assetReview.id.toLong(),
                    objectId1 = ar.id.toString()
                )
        }

        setFragmentValues(description, "", obs)

        val fm = supportFragmentManager

        if (!isFinishing && !isDestroyed) {
            runOnUiThread {
                fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(binding.imageControlFragment.id, imageControlFragment ?: return@runOnUiThread)
                    .commit()

                if (!prefsGetBoolean(Preference.useImageControl)) {
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
            imageControlFragment?.setTableId(Table.assetReview.id)
            imageControlFragment?.setObjectId1(ar.id)
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

    private fun setupTextView() {
        val assetsMissed = adapter?.countItemsMissed ?: 0
        val assetsAdded = adapter?.countItemsAdded ?: 0
        val assetsRevised = adapter?.countItemsRevised ?: 0

        runOnUiThread {
            if (adapter != null) {
                binding.missedTextView.text = assetsMissed.toString()
                binding.addedTextView.text = assetsAdded.toString()
                binding.revisedTextView.text = assetsRevised.toString()
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = show
        }
    }

    private fun fillAdapter(contents: ArrayList<AssetReviewContent>) {
        showProgressBar(true)

        runOnUiThread {
            try {
                if (adapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = adapter?.currentItem()
                }

                adapter = ArcRecyclerAdapter(
                    recyclerView = binding.recyclerView,
                    fullList = contents,
                    checkedIdArray = checkedIdArray,
                    showImages = showImages,
                    showImagesChanged = { showImages = it },
                    visibleStatus = ArrayList(AssetReviewContentStatus.getAllConfirm())
                )

                adapter?.refreshListeners(dataSetChangedListener = this)

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = adapter

                while (binding.recyclerView.adapter == null) {
                    // Horrible wait for a full load
                }

                // Recuperar la última posición seleccionada
                val ls = lastSelected ?: contents.firstOrNull()
                val cs = currentScrollPosition
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter?.selectItem(ls, false)
                    adapter?.scrollToPos(cs, true)
                }, 200)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                gentlyReturn()
            }
        }
    }

    private fun gentlyReturn() {
        closeKeyboard(this)
        rejectNewInstances = false
        showProgressBar(false)
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
            /////////////// ImageControl //////////////
            imageControlFragment?.saveImages(false)
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

        /////////////// ImageControl //////////////
        imageControlFragment?.saveImages(false)
        ///////////////////////////////////////////

        val data = Intent()
        data.putExtra("confirmStatus", Parcels.wrap(ConfirmStatus.modify))
        data.putExtra("obs", obs)
        setResult(RESULT_OK, data)
        finish()
    }

    private fun isBackPressed() {
        modifyCount()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
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

    override fun onDataSetChanged() {
        setupTextView()
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = (adapter?.itemCount ?: 0) == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.recyclerView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }
}