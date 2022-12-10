package com.dacosys.assetControl.views.routes.activities

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.RouteSelectActivityBinding
import com.dacosys.assetControl.model.routes.route.`object`.Route
import com.dacosys.assetControl.model.routes.route.`object`.Route.CREATOR.getAvailableRoutes
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteAdapter
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteDbHelper
import com.dacosys.assetControl.model.routes.routeProcess.`object`.RouteProcess
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessDbHelper
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsDbHelper
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.assetControl.views.routes.fragment.RouteSelectFilterFragment
import org.parceler.Parcels

@Suppress("UNCHECKED_CAST")
class RouteSelectActivity : AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    RouteSelectFilterFragment.FragmentListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        arrayAdapter?.refreshListeners(checkedChangedListener = null, dataSetChangedListener = null)
        routeSelectFilterFragment?.onDestroy()
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshRoute.isRefreshing = false
            }
        }, 1000)
    }

    private var tempTitle = ""

    private var rejectNewInstances = false

    private var multiSelect = false
    private var routeSelectFilterFragment: RouteSelectFilterFragment? = null
    private var lastSelected: Route? = null
    private var firstVisiblePos: Int? = null
    private var arrayAdapter: RouteAdapter? = null
    private var panelBottomIsExpanded = true

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
        Statics.closeKeyboard(this)
        routeSelectFilterFragment?.refreshViews()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("multiSelect", multiSelect)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentRoute())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.select_route)
        // endregion

        // PANELS
        if (b.containsKey("panelBottomIsExpanded"))
            panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")

        // ADAPTER
        multiSelect = b.getBoolean("multiSelect", false)
        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_route)
    }

    private lateinit var binding: RouteSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = RouteSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        routeSelectFilterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as RouteSelectFilterFragment

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        routeSelectFilterFragment?.setListener(this)

        binding.swipeRefreshRoute.setOnRefreshListener(this)
        binding.swipeRefreshRoute.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()

        binding.okButton.setOnClickListener { routeSelect() }
        binding.cancelRouteProcessButton.setOnClickListener { cancelRouteProcess() }

        // Llenar la grilla
        setPanels()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            currentLayout.load(this, R.layout.route_select_activity)
        } else {
            currentLayout.load(
                this,
                R.layout.route_select_activity_bottom_panel_collapsed
            )
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
            binding.routeSelect,
            transition
        )

        currentLayout.applyTo(binding.routeSelect)

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text =
                    getString(R.string.collapse_panel)
            }
            else -> {
                binding.expandBottomPanelButton?.text =
                    getString(R.string.search_options)
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
                nextLayout.load(
                    this,
                    R.layout.route_select_activity_bottom_panel_collapsed
                )
            } else {
                nextLayout.load(this, R.layout.route_select_activity)
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
                binding.routeSelect,
                transition
            )

            nextLayout.applyTo(binding.routeSelect)

            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text =
                        getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandBottomPanelButton?.text =
                        getString(R.string.search_options)
                }
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshRoute.isRefreshing = show
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

    private fun routeSelect() {
        if (arrayAdapter != null) {
            val currentRoute = arrayAdapter?.currentRoute() ?: return

            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent =
                    Intent(baseContext, RouteProcessContentActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("route", Parcels.wrap(currentRoute))
                resultForRpSuccess.launch(intent)
            }
        }
    }

    private val resultForRpSuccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            if (it?.resultCode == RESULT_CANCELED) {
                val msg = data?.getStringExtra("error_msg")
                if (!msg.isNullOrEmpty()) {
                    makeText(binding.root, msg, SnackBarType.ERROR)
                }
            }
            fillListView()
        }

    private fun cancelRouteProcess() {
        if (arrayAdapter != null) {
            val currentRoute = arrayAdapter?.currentRoute() ?: return

            val rpArray = RouteProcessDbHelper().selectByRouteIdNoCompleted(currentRoute.routeId)

            if (rpArray.size < 1) {
                makeText(
                    binding.root,
                    getString(R.string.no_processes_started),
                    SnackBarType.INFO
                )
                return
            }

            if (Statics.demoMode) {
                removeRouteProcess(rpArray[0])
                fillListView()
            } else {
                val tempRouteProcess = rpArray[0]
                runOnUiThread {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.remove_started_processes))
                    alert.setMessage(getString(R.string.are_you_sure_to_cancel_the_process_started_on_this_route_question))
                    alert.setNegativeButton(
                        R.string.no
                    ) { _, _ ->
                        return@setNegativeButton
                    }
                    alert.setPositiveButton(
                        R.string.yes
                    ) { _, _ ->
                        removeRouteProcess(tempRouteProcess)
                        fillListView()
                    }
                    alert.show()
                }
            }
        }
    }

    private fun removeRouteProcess(rp: RouteProcess) {
        RouteProcessDbHelper().deleteById(rp.collectorRouteProcessId)
        RouteProcessContentDbHelper().deleteByRouteProcessId(rp.collectorRouteProcessId)
        RouteProcessStepsDbHelper().deleteByRouteProcessId(rp.collectorRouteProcessId)
    }

    private val routeIdToSend: ArrayList<Long>
        get() {
            val result: ArrayList<Long> = ArrayList()
            try {
                val rDbH = RouteProcessDbHelper()
                val tsArray = rDbH.selectByNoTransferred()
                if (tsArray.size > 0) {
                    for (rp in tsArray) {
                        result.add(rp.routeId)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
            return result
        }

    private val routeIdOnProcess: ArrayList<Long>
        get() {
            val result: ArrayList<Long> = ArrayList()
            try {
                val rDbH = RouteProcessDbHelper()
                val rpArray = rDbH.selectByNoCompleted()
                if (rpArray.size > 0) {
                    for (rp in rpArray) {
                        result.add(rp.routeId)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }

            return result
        }

    private val routeList: ArrayList<Route>
        get() {
            if (routeSelectFilterFragment == null) return ArrayList()
            val result: ArrayList<Route> = ArrayList()
            val r = routeSelectFilterFragment?.routeDescription ?: ""
            val onlyActive = routeSelectFilterFragment?.onlyActive ?: true

            result.addAll(
                getAvailableRoutes(
                    RouteDbHelper().selectByDescription(
                        r,
                        onlyActive
                    )
                )
            )
            return result
        }

    private fun fillListView() {
        try {
            fillRouteAdapter(
                routeArray = routeList,
                routeIdOnProcess = routeIdOnProcess,
                routeIdToSend = routeIdToSend
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    private fun fillRouteAdapter(
        routeArray: ArrayList<Route>,
        routeIdOnProcess: ArrayList<Long>,
        routeIdToSend: ArrayList<Long>,
    ) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (arrayAdapter != null) {
                    lastSelected = arrayAdapter?.currentRoute()
                    firstVisiblePos = arrayAdapter?.firstVisiblePos()
                }

                if (arrayAdapter == null || routeArray.size > 0) {
                    arrayAdapter = RouteAdapter(
                        activity = this,
                        resource = R.layout.route_row,
                        routes = routeArray,
                        routeIdOnProcess = routeIdOnProcess,
                        routeIdToSend = routeIdToSend,
                        listView = binding.routeListView,
                        checkedIdArray = ArrayList(),
                        multiSelect = false,
                        checkedChangedListener = null,
                        dataSetChangedListener = null
                    )
                } else if (routeArray.size <= 0) {
                    // IMPORTANTE:
                    // Se deben actualizar los listeners, sino
                    // las variables de esta actividad pueden
                    // tener valores antiguos en del adaptador.

                    arrayAdapter?.refreshListeners(
                        checkedChangedListener = null,
                        dataSetChangedListener = null
                    )

                    arrayAdapter?.refresh()
                }

                while (binding.routeListView.adapter == null) {
                    // Horrible wait for full load
                }

                if (arrayAdapter != null) {
                    arrayAdapter?.setSelectItemAndScrollPos(
                        lastSelected,
                        firstVisiblePos
                    )
                }

                if (Statics.demoMode) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    override fun onFilterChanged(routeDescription: String, onlyActive: Boolean) {
        Statics.closeKeyboard(this)
        fillListView()
    }

    private fun demo() {
        if (!Statics.demoMode) return

        arrayAdapter?.selectNext()
        routeSelect()
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
}