package com.dacosys.assetControl.ui.activities.route

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.route.RouteDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessStepsDbHelper
import com.dacosys.assetControl.data.model.route.Route
import com.dacosys.assetControl.data.model.route.Route.CREATOR.getAvailableRoutes
import com.dacosys.assetControl.data.model.route.RouteProcess
import com.dacosys.assetControl.databinding.RouteSelectActivityBinding
import com.dacosys.assetControl.ui.adapters.route.RouteAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.route.RouteSelectFilterFragment
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels
import java.util.*

class RouteSelectActivity : AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    RouteSelectFilterFragment.FragmentListener, RouteAdapter.DataSetChangedListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        adapter?.refreshListeners()
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
    private var adapter: RouteAdapter? = null
    private var panelBottomIsExpanded = true

    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        closeKeyboard(this)
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

        if (adapter != null) {
            b.putParcelable("lastSelected", adapter?.currentRoute())
            b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: 0)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (!t1.isNullOrEmpty()) t1 else getString(R.string.select_route)
        // endregion

        // PANELS
        if (b.containsKey("panelBottomIsExpanded"))
            panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")

        // ADAPTER
        multiSelect = b.getBoolean("multiSelect", false)
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_route)
    }

    private lateinit var binding: RouteSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = RouteSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
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

        setupUI(binding.root, this)
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

    private fun routeSelect() {
        if (adapter != null) {
            val currentRoute = adapter?.currentRoute() ?: return

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
        if (adapter != null) {
            val currentRoute = adapter?.currentRoute() ?: return

            val rpArray = RouteProcessDbHelper().selectByRouteIdNoCompleted(currentRoute.routeId)

            if (rpArray.size < 1) {
                makeText(
                    binding.root,
                    getString(R.string.no_processes_started),
                    SnackBarType.INFO
                )
                return
            }

            if (Statics.DEMO_MODE) {
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
        fillAdapter(
            routeArray = routeList,
            routeIdOnProcess = routeIdOnProcess,
            routeIdToSend = routeIdToSend
        )
    }

    private fun fillAdapter(
        routeArray: ArrayList<Route>,
        routeIdOnProcess: ArrayList<Long>,
        routeIdToSend: ArrayList<Long>,
    ) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (adapter != null) {
                    lastSelected = adapter?.currentRoute()
                    firstVisiblePos = adapter?.firstVisiblePos()
                }

                if (adapter == null || routeArray.size > 0) {
                    adapter = RouteAdapter(
                        activity = this,
                        resource = R.layout.route_row,
                        routes = routeArray,
                        routeIdOnProcess = routeIdOnProcess,
                        routeIdToSend = routeIdToSend,
                        listView = binding.routeListView,
                        checkedIdArray = ArrayList(),
                        multiSelect = false,
                        checkedChangedListener = null,
                        dataSetChangedListener = this
                    )
                } else {
                    // IMPORTANTE:
                    // Se deben actualizar los listeners, si no
                    // las variables de esta actividad pueden
                    // tener valores antiguos en del adaptador.

                    adapter?.refreshListeners(dataSetChangedListener = this)
                    adapter?.refresh()
                }

                while (binding.routeListView.adapter == null) {
                    // Horrible wait for full load
                }

                if (adapter != null) {
                    adapter?.setSelectItemAndScrollPos(
                        lastSelected,
                        firstVisiblePos
                    )
                }

                if (Statics.DEMO_MODE) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
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
        closeKeyboard(this)
        fillListView()
    }

    private fun demo() {
        if (!Statics.DEMO_MODE) return

        val t = adapter?.itemCount ?: 0
        adapter?.selectItem(Random().nextInt(t))
        routeSelect()
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

    override fun onDataSetChanged() {
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = (adapter?.itemCount ?: 0) == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.routeListView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }
}