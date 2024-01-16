package com.dacosys.assetControl.ui.activities.movement

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.movement.WmcRecyclerAdapter
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.databinding.WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.movement.WarehouseMovementContent
import com.dacosys.assetControl.model.movement.WarehouseMovementContentStatus
import com.dacosys.assetControl.model.status.ConfirmStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.activities.common.ObservationsActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import org.parceler.Parcels

class WmcConfirmActivity : AppCompatActivity(),
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
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var adapter: WmcRecyclerAdapter? = null
    private var completeList: ArrayList<WarehouseMovementContent> = ArrayList()
    private var checkedIdArray: java.util.ArrayList<Long> = ArrayList()
    private var lastSelected: WarehouseMovementContent? = null
    private var currentScrollPosition: Int = 0
    private var firstVisiblePos: Int? = null

    private var obs = ""
    private var rejectNewInstances = false

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable("warehouseArea", headerFragment?.warehouseArea)
        savedInstanceState.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        savedInstanceState.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )

        savedInstanceState.putParcelable("lastSelected", adapter?.currentItem())
        savedInstanceState.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: RecyclerView.NO_POSITION)
        savedInstanceState.putLongArray("checkedIdArray", adapter?.checkedIdArray?.map { it }?.toLongArray())
        savedInstanceState.putInt("currentScrollPosition", currentScrollPosition)
    }

    private lateinit var binding: WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseMovementContentConfirmActivityBottomPanelCollapsedBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
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
            checkedIdArray =
                (savedInstanceState.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
            lastSelected = savedInstanceState.getParcelable("lastSelected")
            firstVisiblePos =
                if (savedInstanceState.containsKey("firstVisiblePos")) savedInstanceState.getInt("firstVisiblePos") else -1
            currentScrollPosition = savedInstanceState.getInt("currentScrollPosition")
        } else {
            // Inicializar la actividad

            // Traer los parámetros que recibe la actividad
            val extras = intent.extras
            if (extras != null) {
                tempWarehouseArea = Parcels.unwrap<WarehouseArea>(extras.getParcelable("warehouseArea"))
            }
        }

        // Cargamos la revisión desde la tabla temporal
        completeList.clear()
        val tempCont = WarehouseMovementContentDbHelper().selectByTempId(1)
        if (tempCont.any()) {
            val r: ArrayList<WarehouseMovementContent> = ArrayList()
            for (tempItem in tempCont) {
                // Tanto los que se van a mover como los que se encontraron en el área
                if (tempItem.warehouseAreaId != tempWarehouseArea?.warehouseAreaId ||
                    tempItem.assetStatusId == AssetStatus.missing.id
                ) {
                    r.add(tempItem)
                }
            }
            completeList = r
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

        setupUI(binding.root, this)
    }

    override fun onStart() {
        super.onStart()
        fillAdapter(completeList)
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
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
            else currentLayout.load(this, R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed)
        } else {
            if (panelTopIsExpanded) currentLayout.load(
                this,
                R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed
            )
            else currentLayout.load(this, R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed)
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
            binding.warehouseMovementContentConfirm, transition
        )

        currentLayout.applyTo(binding.warehouseMovementContentConfirm)

        if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandBottomPanelButton?.text = getString(R.string.more_options)

        if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandTopPanelButton?.text = getString(R.string.select_destination)
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
                    R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed
                )
                else nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
                else nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed)
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
                binding.warehouseMovementContentConfirm, transition
            )

            nextLayout.applyTo(binding.warehouseMovementContentConfirm)

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
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.warehouse_movement_content_confirm_activity_top_panel_collapsed
                )
                else nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity)
            } else {
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.warehouse_movement_content_confirm_activity_both_panels_collapsed
                )
                else nextLayout.load(this, R.layout.warehouse_movement_content_confirm_activity_bottom_panel_collapsed)
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
                binding.warehouseMovementContentConfirm, transition
            )

            nextLayout.applyTo(binding.warehouseMovementContentConfirm)

            if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
            else binding.expandTopPanelButton?.text = getString(R.string.select_destination)
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWmCont.isRefreshing = show
        }
    }

    private fun setImageControlFragment() {
        val wa = headerFragment?.warehouseArea ?: return

        var description = wa.description
        val tableName = Table.warehouseMovement.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        val obs = "${getString(R.string.user)}: ${Statics.currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = Table.warehouseMovement.tableId.toLong(),
                objectId1 = "0"
            )

            setFragmentValues(description, "", obs)

            val fm = supportFragmentManager

            if (!isFinishing) runOnUiThread {
                fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                        binding.imageControlFragment.id,
                        imageControlFragment ?: return@runOnUiThread
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
        } else {
            imageControlFragment?.setTableId(Table.warehouseMovement.tableId)
            imageControlFragment?.setObjectId1(0)
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

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        modifyMovement()
    }

    private fun setupTextView() {
        val assetToMove = adapter?.assetsToMove ?: 0
        val tempText = if (assetToMove == 1) {
            getString(R.string.asset)
        } else {
            getString(R.string.assets)
        }

        runOnUiThread {
            binding.toMoveTextView.text = String.format("%s %s", assetToMove.toString(), tempText)
        }
    }

    private fun fillAdapter(contents: ArrayList<WarehouseMovementContent>) {
        showProgressBar(true)

        runOnUiThread {
            try {
                if (adapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = adapter?.currentItem()
                }

                adapter = WmcRecyclerAdapter(
                    recyclerView = binding.recyclerView,
                    fullList = contents,
                    checkedIdArray = checkedIdArray,
                    visibleStatus = WarehouseMovementContentStatus.getAll()
                )

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = adapter

                while (binding.recyclerView.adapter == null) {
                    // Horrible wait for a full load
                }

                // Estas variables locales evitar posteriores cambios de estado.
                val ls = lastSelected ?: contents.firstOrNull()
                val cs = currentScrollPosition
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter?.selectItem(ls, false)
                    adapter?.scrollToPos(cs, true)
                }, 200)

                setupTextView()
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

    private fun confirmMovement() {
        if (prefsGetBoolean(Preference.signReviewsAndMovements) && !(imageControlFragment
                ?: return).isSigned
        ) {
            makeText(
                binding.root, getString(R.string.mandatory_sign), SnackBarType.ERROR
            )
        } else {
            closeKeyboard(this)

            /////////////// ImageControl //////////////
            imageControlFragment?.saveImages(false)
            ///////////////////////////////////////////

            val data = Intent()
            data.putExtra("confirmStatus", Parcels.wrap(ConfirmStatus.confirm))
            data.putExtra("obs", obs)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun modifyMovement() {
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
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
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
}