package com.dacosys.assetControl.views.movements.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
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
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.databinding.WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.confirmStatus.ConfirmStatus
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentAdapter
import com.dacosys.assetControl.model.movements.warehouseMovementContentStatus.WarehouseMovementContentStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.views.commons.activities.ObservationsActivity
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.movements.fragments.LocationHeaderFragment
import com.dacosys.imageControl.fragments.ImageControlButtonsFragment
import org.parceler.Parcels

class WarehouseMovementContentConfirmActivity :
    AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshWmCont.isRefreshing = false
            }
        }, 1000)
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        wmContAdapter?.refreshListeners(null, null, null)
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var wmContAdapter: WarehouseMovementContentAdapter? = null
    private var wmContArray: ArrayList<WarehouseMovementContent> = ArrayList()
    private var obs = ""
    private var rejectNewInstances = false

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

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

    override fun onResume() {
        super.onResume()
        rejectNewInstances = false

        fillAdapter()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable("warehouseArea", headerFragment?.warehouseArea)
        savedInstanceState.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        savedInstanceState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment)
            supportFragmentManager.putFragment(
                savedInstanceState,
                "imageControlFragment",
                imageControlFragment as ImageControlButtonsFragment
            )

        if (wmContAdapter != null) {
            savedInstanceState.putParcelableArrayList(
                "wmContArray",
                wmContAdapter?.getAll()
            )
        }
    }

    private lateinit var binding: WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.confirm_movement)

        headerFragment =
            supportFragmentManager.findFragmentById(binding.headerFragment.id) as LocationHeaderFragment?
        var tempWarehouseArea: WarehouseArea? = null
        if (savedInstanceState != null) {
            tempWarehouseArea = savedInstanceState.getParcelable("warehouseArea")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF

            panelBottomIsExpanded = savedInstanceState.getBoolean("panelBottomIsExpanded")
            panelTopIsExpanded = savedInstanceState.getBoolean("panelTopIsExpanded")

            // Adapter
            wmContArray.clear()
            val tempCont =
                savedInstanceState.getParcelableArrayList<WarehouseMovementContent>("wmContArray")
            if (tempCont != null) {
                wmContArray = tempCont
            }
        } else {
            // Inicializar la actividad

            // Traer los parámetros que recibe la actividad
            val extras = intent.extras
            if (extras != null) {
                tempWarehouseArea =
                    Parcels.unwrap<WarehouseArea>(extras.getParcelable("warehouseArea"))

                val t1 = extras.getParcelableArrayList<WarehouseMovementContent>("wmContArray")
                if (t1 != null) {
                    wmContArray = t1
                }
            }
        }

        setHeaderTextBox(tempWarehouseArea)

        binding.swipeRefreshWmCont.setOnRefreshListener(this)
        binding.swipeRefreshWmCont.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.obsButton.setOnClickListener { addObservations() }
        binding.confirmButton.setOnClickListener { confirmMovement() }

        setPanels()

        setImageControlFragment()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun setHeaderTextBox(warehouseArea: WarehouseArea?) {
        headerFragment?.showChangePostButton(false)
        headerFragment?.setTitle(getString(R.string.destination))

        if (warehouseArea != null && headerFragment != null) {
            runOnUiThread {
                headerFragment?.fill(warehouseArea)
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
                currentLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
            } else {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed
                )
            }
        } else {
            if (panelTopIsExpanded) {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed
                )
            } else {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed
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
            binding.warehouseMovementContentConfirm,
            transition
        )

        currentLayout.applyTo(binding.warehouseMovementContentConfirm)

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text =
                    getString(R.string.collapse_panel)
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
                binding.expandTopPanelButton?.text =
                    getString(R.string.select_destination)
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
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed
                    )
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed
                    )
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed
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
                binding.warehouseMovementContentConfirm,
                transition
            )

            nextLayout.applyTo(binding.warehouseMovementContentConfirm)

            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text =
                        getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandBottomPanelButton?.text =
                        getString(R.string.more_options)
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
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed
                    )
                } else {
                    nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed
                    )
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed
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
                binding.warehouseMovementContentConfirm,
                transition
            )

            nextLayout.applyTo(binding.warehouseMovementContentConfirm)

            when {
                panelTopIsExpanded -> {
                    binding.expandTopPanelButton?.text =
                        getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandTopPanelButton?.text =
                        getString(R.string.select_destination)
                }
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWmCont.isRefreshing = show
        }
    }

    private fun setImageControlFragment() {
        var description = (headerFragment?.warehouseArea ?: return).description
        val tableName = Table.warehouseMovement.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                Table.warehouseMovement.tableId,
                0,
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
                            binding.imageControlFragment.id,
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
            imageControlFragment?.setTableId(Table.warehouseMovement.tableId)
            imageControlFragment?.setObjectId1(0)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }
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

    override fun onBackPressed() {
        modifyMovement()
        super.onBackPressed()
    }

    private fun setupTextView() {
        val assetToMove = wmContAdapter?.assetsToMove ?: 0
        val tempText = if (assetToMove == 1) {
            getString(R.string.asset)
        } else {
            getString(R.string.assets)
        }

        runOnUiThread {
            binding.toMoveTextView.text = String.format("%s %s", assetToMove.toString(), tempText)
        }
    }

    private fun fillAdapter() {
        var lastSelected: WarehouseMovementContent? = null
        if (wmContAdapter != null) {
            lastSelected = wmContAdapter?.currentWmCont()
        }

        try {
            runOnUiThread {
                wmContAdapter = WarehouseMovementContentAdapter(
                    activity = this,
                    resource = R.layout.asset_row,
                    wmContArray = wmContArray,
                    suggestedList = wmContArray,
                    listView = binding.wmContentListView,
                    multiSelect = false,
                    checkedIdArray = ArrayList(),
                    visibleStatus = WarehouseMovementContentStatus.getAll()
                )
            }

            while (binding.wmContentListView.adapter == null) {
                // Horrible wait for full load
            }

            if (wmContAdapter != null) {
                if (lastSelected != null && wmContAdapter!!.getItems().contains(lastSelected)) {
                    wmContAdapter?.selectItem(
                        wmc = lastSelected,
                        smoothScroll = true
                    )
                } else if (countItems > 0) {
                    wmContAdapter?.selectItem(pos = 0, smoothScroll = true)
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
            return wmContAdapter?.count() ?: 0
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

    private fun confirmMovement() {
        if (Statics.prefsGetBoolean(Preference.signReviewsAndMovements) && !(imageControlFragment
                ?: return).isSigned
        ) {
            makeText(
                binding.root,
                getString(R.string.mandatory_sign),
                SnackbarType.ERROR
            )
        } else {
            Statics.closeKeyboard(this)

            ///////////////////////////////////////////
            ////////////// IMAGE CONTROL //////////////
            if (imageControlFragment != null) {
                imageControlFragment?.saveImages(false)
            }
            ///////////////////////////////////////////

            val data = Intent()
            data.putExtra("confirmStatus", Parcels.wrap(ConfirmStatus.confirm))
            data.putExtra("obs", obs)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun modifyMovement() {
        Statics.closeKeyboard(this)

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