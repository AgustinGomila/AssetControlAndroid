package com.dacosys.assetControl.ui.activities.route

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper
import com.dacosys.assetControl.data.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionDbHelper
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContentDbHelper
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionDbHelper
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteCompositionDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentDbHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessStepsDbHelper
import com.dacosys.assetControl.data.model.attribute.AttributeCompositionType
import com.dacosys.assetControl.data.model.common.SaveProgress
import com.dacosys.assetControl.data.model.dataCollection.DataCollection
import com.dacosys.assetControl.data.model.dataCollection.DcrResult
import com.dacosys.assetControl.data.model.route.*
import com.dacosys.assetControl.data.model.route.common.ExprResultIntString
import com.dacosys.assetControl.data.model.route.common.Parameter
import com.dacosys.assetControl.data.model.route.common.SaveRouteProcess
import com.dacosys.assetControl.data.model.route.common.SkipAll
import com.dacosys.assetControl.data.model.user.User
import com.dacosys.assetControl.data.model.user.permission.PermissionEntry
import com.dacosys.assetControl.databinding.ProgressBarDialogBinding
import com.dacosys.assetControl.databinding.RouteProcessContentActivityBinding
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.activities.asset.AssetDetailActivity
import com.dacosys.assetControl.ui.activities.location.WarehouseAreaDetailActivity
import com.dacosys.assetControl.ui.adapters.interfaces.Interfaces.*
import com.dacosys.assetControl.ui.adapters.route.RpcRecyclerAdapter
import com.dacosys.assetControl.ui.adapters.route.RpcRecyclerAdapter.PAYLOADS
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.viewModel.route.*
import com.dacosys.assetControl.viewModel.sync.SyncViewModel
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList
import com.udojava.evalex.Expression
import org.parceler.Parcels
import java.util.regex.Pattern
import kotlin.concurrent.thread

class RouteProcessContentActivity : AppCompatActivity(), Scanner.ScannerListener,
    Rfid.RfidDeviceListener, SwipeRefreshLayout.OnRefreshListener,
    CheckedChangedListener,
    DataSetChangedListener,
    UiEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        adapter?.refreshListeners(null, null)
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun onSaveProgress(it: SaveProgress) {
        if (isDestroyed || isFinishing) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val progress: Int = it.progress
        val total: Int = it.total

        showProgressDialog(
            getContext().getString(R.string.saving_route_process), msg, taskStatus, progress, total
        )

        if (ProgressStatus.isFinishStatus(taskStatus)) {
            JotterListener.lockScanner(this, false)
        }

        if (taskStatus == ProgressStatus.crashed.id || taskStatus == ProgressStatus.canceled.id) {
            saving = false
            makeText(binding.root, msg, SnackBarType.ERROR)
        }
    }

    private fun onSkipAllProgress(it: SaveProgress) {
        if (isDestroyed || isFinishing) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val progress: Int = it.progress
        val total: Int = it.total

        showProgressDialog(
            getContext().getString(R.string.saving_route_process), msg, taskStatus, progress, total
        )

        if (ProgressStatus.isFinishStatus(taskStatus)) {
            JotterListener.lockScanner(this, false)
        }

        if (taskStatus == ProgressStatus.finished.id) {
            if ((adapter?.itemCount ?: 0) > 0)
                adapter?.notifyItemRangeChanged(0, adapter?.itemCount ?: 0, PAYLOADS.STATUS_CHANGE)

            // El nivel al que volví está completo
            if (isCurrentLevelCompleted() && adapter?.currentLevel() == 1) {
                runOnUiThread {
                    binding.confirmButton.isEnabled = true
                }
                return
            }

            next()
        } else if (taskStatus == ProgressStatus.crashed.id || taskStatus == ProgressStatus.canceled.id) {
            makeText(binding.root, msg, SnackBarType.ERROR)
        }
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 1000)
    }

    @Suppress("unused")
    private fun showProgressBar(show: Boolean) {
        if (isDestroyed || isFinishing) return
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = show
        }
    }

    fun onSyncTaskProgress(it: SyncProgress) {
        if (isDestroyed || isFinishing) return

        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        val progressStatusDesc = progressStatus.description
        var registryDesc = getString(R.string.all_tasks)
        if (registryType != null) {
            registryDesc = registryType.description
        }

        when (progressStatus) {
            ProgressStatus.bigStarting,
            ProgressStatus.starting,
            ProgressStatus.running,
            -> {
                showProgressDialog(
                    title = getString(R.string.synchronizing_),
                    msg = msg,
                    status = progressStatus.id,
                    progress = completedTask,
                    total = totalTask
                )
            }

            ProgressStatus.bigFinished -> {
                closeKeyboard(this)
                setResult(RESULT_OK)
                finish()
            }

            ProgressStatus.bigCrashed,
            ProgressStatus.canceled,
            -> {
                closeKeyboard(this)
                makeText(binding.root, msg, SnackBarType.ERROR)
                ErrorLog.writeLog(
                    this, this::class.java.simpleName, "$progressStatusDesc: $registryDesc ${
                        Statics.getPercentage(completedTask, totalTask)
                    }, $msg"
                )
                saving = false
            }

            else -> {
                Log.d(
                    this::class.java.simpleName, "$progressStatusDesc: $registryDesc ${
                        Statics.getPercentage(completedTask, totalTask)
                    }, $msg"
                )
            }
        }
    }

    private var tempTitle = ""

    private var saving: Boolean = false

    private var routeProcess: RouteProcess? = null
    private var rpcArray: ArrayList<RouteProcessContent> = ArrayList()

    private var route: Route? = null
    private var routeComposition: ArrayList<RouteComposition>? = null

    private var adapter: RpcRecyclerAdapter? = null
    private var lastSelected: RouteProcessContent? = null
    private var currentScrollPosition: Int = 0
    private var firstVisiblePos: Int? = null

    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var backLevelSteps: ArrayList<Int> = ArrayList()
    private val levelsToNavigate: ArrayList<Int> = ArrayList()
    private var allParameters: ArrayList<Parameter> = ArrayList()
    private var separator = '.'

    private var panelBottomIsExpanded = true

    private var buttonCollection: java.util.ArrayList<Button> = java.util.ArrayList()

    private var allowClicks = true
    private var rejectNewInstances = false


    // Se usa para hacer un reemplazo de los resultados de la expresión que corresponden
    // a una secuencia de niveles por un valor numérico y viceversa.
    private var tempResult: ArrayList<ExprResultIntString> = ArrayList()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveBundleValues(outState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putBoolean("saving", saving)

        b.putString("title", tempTitle)
        b.putParcelable("route", Parcels.wrap(route))
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (adapter != null) {
            b.putParcelable("lastSelected", adapter?.currentRpc())
            b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: NO_POSITION)
            b.putLongArray("checkedIdArray", adapter?.checkedIdArray?.map { it }?.toLongArray())
            b.putInt("currentScrollPosition", currentScrollPosition)

            b.putParcelableArrayList("rpcArray", adapter?.fullList)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        saving = b.getBoolean("saving")

        // region Recuperar el título de la ventana
        val t0 = b.getString("title")
        tempTitle = if (!t0.isNullOrEmpty()) t0 else getString(R.string.route_process)
        // endregion

        // Panels
        route = Parcels.unwrap<Route>(b.parcelable("route"))
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")

        // Adapter
        checkedIdArray = (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        currentScrollPosition = b.getInt("currentScrollPosition")

        rpcArray.clear()
        val t1 = b.parcelableArrayList<RouteProcessContent>("rpcArray")
        if (t1 != null) rpcArray = t1
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.route_process)
    }

    private lateinit var binding: RouteProcessContentActivityBinding
    private val saveViewModel: SaveRouteProcessViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = RouteProcessContentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        saveViewModel.saveProgress.observe(this) { if (it != null) onSaveProgress(it) }
        syncViewModel.syncUploadProgress.observe(this) { if (it != null) onSyncTaskProgress(it) }

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

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

        binding.routeStrTextView.text = (route ?: return).description

        binding.reentryButton.setOnClickListener { reentry() }
        binding.skipButton.setOnClickListener { skip() }
        binding.skipAllButton.setOnClickListener { skipAll() }
        binding.beginProcessButton.setOnClickListener { beginProcess() }
        binding.detailButton.setOnClickListener { detail() }
        binding.confirmButton.setOnClickListener { confirmLevel(true) }

        buttonCollection.add(binding.reentryButton)
        buttonCollection.add(binding.skipButton)
        buttonCollection.add(binding.skipAllButton)
        buttonCollection.add(binding.beginProcessButton)
        buttonCollection.add(binding.detailButton)
        buttonCollection.add(binding.confirmButton)

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()

        initRoute()
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) nextLayout.load(
                this, R.layout.route_process_content_activity_bottom_panel_collapsed
            )
            else nextLayout.load(this, R.layout.route_process_content_activity)

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

            TransitionManager.beginDelayedTransition(binding.routeProcessContent, transition)
            nextLayout.applyTo(binding.routeProcessContent)

            if (panelBottomIsExpanded) binding.expandButton?.text =
                getContext().getString(R.string.collapse_panel)
            else binding.expandButton?.text = getContext().getString(R.string.more_options)
        }
    }

    private fun onGetRouteProcess(it: RouteProcessResult) {
        if (isDestroyed || isFinishing) return

        val error = it.error
        if (error != null) {
            closeKeyboard(this)

            val data = Intent()
            data.putExtra("error_msg", error.errorMessage)
            setResult(RESULT_CANCELED, data)
            finish()
            return
        }

        val routeProcess = it.routeProcess ?: return
        this.routeProcess = routeProcess

        if (it.newProcess) {
            // NUEVO PROCESO DE RUTA
            fill(1)
        } else {
            // CONTINUAR PROCESO DE RUTA
            continueRouteProcess(routeProcess)
        }
    }

    private fun onGetContentProcess(it: GetRouteProcessContentResult) {
        if (isDestroyed || isFinishing) return

        // Contenidos del nivel solicitado
        val contents = it.currentRouteProcessContent
        val level = it.level

        runOnUiThread {
            if (adapter != null) {
                // Si el adapter es NULL es porque aún no fue creado.
                // Por lo tanto, puede ser que los valores de [lastSelected]
                // sean valores guardados de la instancia anterior y queremos preservarlos.
                lastSelected = adapter?.currentRpc()
            }

            adapter = RpcRecyclerAdapter(
                recyclerView = binding.recyclerView,
                fullList = contents,
                checkedIdArray = checkedIdArray,
                showCheckBoxes = false,
                showCheckBoxesChanged = { },
                visibleStatus = RouteProcessStatus.getAll()
            )

            refreshAdapterListeners()

            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter

            while (binding.recyclerView.adapter == null) {
                // Horrible wait for full load
            }

            Handler(Looper.getMainLooper()).postDelayed({
                // Activo o no el botón de confirmar si el nivel está completado
                binding.confirmButton.isEnabled = isCurrentLevelCompleted()

                next()
            }, 200)
        }

        // Agrego el paso que estoy procesando
        backLevelSteps.add(level)
    }

    private fun refreshAdapterListeners() {
        adapter?.refreshListeners(this, this)
        adapter?.refreshUiEventListener(this)
    }

    private fun initRoute() {
        val r = route ?: return

        GetRouteProcess(route = r, onProgress = { onGetRouteProcess(it) })
    }

    private fun continueRouteProcess(rp: RouteProcess) {
        routeComposition = ArrayList(
            RouteCompositionDbHelper().selectByRouteId(rp.routeId)
                .sortedWith(compareBy({ it.level }, { it.position }))
        )

        Log.d(this::class.java.simpleName, getString(R.string.getting_processed_content))

        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getWritableDb()
        try {
            db.beginTransaction()

            // Traemos la ruta para continuarla si aún no lo hemos hecho.
            if (!rpcArray.any()) {
                rpcArray = ArrayList(
                    RouteProcessContentDbHelper().selectByRouteProcessId(rp.collectorRouteProcessId)
                        .sortedWith(compareBy({ it.level }, { it.position }))
                )
            }

            Log.d(this::class.java.simpleName, getString(R.string.obtaining_collected_data))

            val tempDccArray = ArrayList(
                DataCollectionContentDbHelper().selectByCollectorRouteProcessId(rp.collectorRouteProcessId)
                    .sortedWith(compareBy({ it.level }, { it.position }))
            )

            Log.d(this::class.java.simpleName, getString(R.string.getting_previous_steps))

            val allSteps = ArrayList(
                RouteProcessStepsDbHelper().selectByRouteProcessId(rp.collectorRouteProcessId)
                    .sortedWith(compareBy { it.step })
            )

            db.setTransactionSuccessful()

            // Recorrer la ruta en busca del lugar donde quedó el operador.
            // Ir agregando los pasos a una colección de pasos.
            allParameters.clear()

            // RECORRER COMPOSICIÓN DE LA RUTA PARA CREAR LOS PARÁMETROS
            if (rpcArray.size > 0 && tempDccArray.size > 0) {
                var x1 = 0
                val x2 = rpcArray.size

                for (rpc in rpcArray) {
                    x1++
                    Log.d(
                        this::class.java.simpleName,
                        "${getString(R.string.processing_route)} ($x1/$x2)…"
                    )

                    var z1 = 0
                    val z2 = tempDccArray.size

                    for (dcc in tempDccArray) {
                        if (rpc.dataCollectionId == null || dcc.dataCollectionId == null) {
                            continue
                        }

                        val dc = DataCollection(rpc.dataCollectionId ?: return, false)
                        if (dc.collectorDataCollectionId == dcc.dataCollectionId) {
                            z1++
                            Log.d(
                                this::class.java.simpleName,
                                "${getString(R.string.processing_data)} ($z1/$z2)…"
                            )

                            // Agrego los parámetros y sus valores
                            val param =
                                rpc.level.toString() + separator + rpc.position + separator + dcc.level + separator + dcc.position + separator + dcc.attributeCompositionId

                            Log.d(
                                this::class.java.simpleName,
                                "${getString(R.string.adding_parameter)}: $param…"
                            )

                            allParameters.add(Parameter(param, dcc.valueStr))
                        }
                    }
                }
            }

            // Agrego el nivel 1 a la colección de niveles
            backLevelSteps.add(1)

            var y1 = 0
            val y2 = allSteps.size

            for (step in allSteps) {
                y1++
                Log.d(
                    this::class.java.simpleName, "${getString(R.string.analyzing_steps)} ($y1/$y2)…"
                )

                val rc = (routeComposition
                    ?: return).first { it.level == step.level && it.position == step.position }

                analiceStep(step, rc)
            }

            Log.d(this::class.java.simpleName, getString(R.string.finished_analysis))
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            db.endTransaction()
        }

        fill(backLevelSteps.last())
        backLevelSteps.remove(backLevelSteps.last())
    }

    private fun evaluate(rc: RouteComposition, parameters: ArrayList<Parameter>): Any? {
        try {
            if (rc.expression == null || (rc.expression ?: return null).trim().isEmpty()) {
                return null
            }

            val e = formatExpression(rc.expression ?: return null, parameters)
            return try {
                val res = e.eval().toInt()
                tempResult.firstOrNull { it.key == res }?.value ?: res
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return null
        }
    }

    /**
     * Formatea la expresión para que pueda ser evaluada
     */
    private fun formatExpression(exp: String, par: ArrayList<Parameter>): Expression {
        val value = "A"
        var charValue = value[0].code
        var expression = exp
        val parameters: ArrayList<Parameter> = ArrayList()

        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getReadableDb()

        val attrCompDbHelper = AttributeCompositionDbHelper()

        db.beginTransaction()
        try {
            for (p in par) {
                var nextLetter = charValue.toChar().toString()
                charValue++

                var secBrake = 0
                while (!nextLetter.matches(Regex("\\p{L}+"))) {
                    nextLetter = charValue.toChar().toString()
                    charValue++
                    secBrake++
                    if (secBrake >= 1000) {
                        break
                    }
                }

                // Reemplazar los valores de texto por valores numéricos que puedan ser comparados
                // a partir del AttributeCompositionId del ParamName
                val attrCompId = p.paramName.split('.').last().toLong()
                val attrComp = attrCompDbHelper.selectById(attrCompId)
                var pValue: Int

                if (attrComp!!.attributeCompositionTypeId == AttributeCompositionType.TypeOptions.id) {
                    var composition = ""
                    if (attrComp.composition != null) {
                        composition = attrComp.composition!!.trim().trimEnd(';')
                    }

                    val allOptions = ArrayList(composition.split(';')).sorted()

                    // Reemplazo los valores en Texto por Id a fin de poder compararlos
                    for (a in allOptions) {
                        pValue = allOptions.indexOf(a)
                        expression = expression.replace("'$a'", pValue.toString())
                    }

                    pValue = allOptions.indexOf(p.paramValue)
                    parameters.add(Parameter(nextLetter, pValue))
                } else {
                    parameters.add(Parameter(nextLetter, p.paramValue))
                }
                expression = expression.replace("[" + p.paramName + "]", nextLetter)
            }

            db.setTransactionSuccessful()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            db.endTransaction()
        }

        // Reemplazar los resultados de tipo secuencia de niveles (ej.: '3,4')
        // por un valor numérico falso para poder devolverse como resultado
        var pat = Pattern.compile("'([^']*)'")
        var fakeValue = 9000
        var m = pat.matcher(expression)
        tempResult.clear()

        while (m.find()) {
            fakeValue++
            tempResult.add(ExprResultIntString(fakeValue, m.group(1) ?: ""))
            expression = expression.replace("'" + m.group(1) + "'", fakeValue.toString())
        }

        // Finalmente, reemplazar todos aquellos parámetros que no fueron reemplazados antes
        // porque esos datos aún no fueron registrados
        pat = Pattern.compile("""\[([^]]+)]""")
        m = pat.matcher(expression)
        while (m.find()) {
            expression = expression.replace("[" + m.group(1) + "]", "ZZ")
        }

        val e = Expression(expression)
        for (p in parameters) {
            e.setVariable(p.paramName, p.paramValue.toString())
        }

        e.setVariable("ZZ", 999999.toString())

        return e
    }

    private fun analiceStep(step: RouteProcessSteps, rc: RouteComposition) {
        // Traigo los parámetros que afectan esa expresión, es decir del mismo nivel
        val param: ArrayList<Parameter> = ArrayList()
        for (p in allParameters) {
            if (p.paramName.startsWith(step.level.toString() + separator)) {
                param.add(p)
            }
        }

        // Evalúo la expresión
        val result = evaluate(rc, param)
        if (result == null && rc.trueResult != DcrResult.cont.id || result == null && rc.falseResult != DcrResult.cont.id) {
            return
        }

        // -3 - NO CONTINUAR
        /////////////////////////
        if (rc.trueResult == DcrResult.noContinue.id && result != null && result == true || rc.falseResult == DcrResult.noContinue.id && result != null && result == false) {
            return
        }

        // 0 - CONTINUAR
        /////////////////////
        // En caso de que tanto trueResult como falseResult sean 0
        // En caso de que trueResult sea 0 y la evaluación sea verdadera
        // En caso de que falseResult sea 0 y la evaluación sea negativa
        if (rc.trueResult == DcrResult.cont.id && rc.falseResult == DcrResult.cont.id || rc.trueResult == DcrResult.cont.id && result != null && result == true || rc.falseResult == DcrResult.cont.id && result != null && result == false) {
            // ¿Está ejecutando una secuencia de niveles?
            if (levelsToNavigate.size > 0) {
                // Elimino el último paso ejecutado así el último paso es el previo
                backLevelSteps.remove(backLevelSteps.last())
                backLevelSteps.add(levelsToNavigate[0])
                levelsToNavigate.remove(levelsToNavigate[0])
            }
            return
        }

        // -2 - FIN
        ////////////////
        if (result != null && (rc.trueResult == DcrResult.end.id && result == true || rc.falseResult == DcrResult.end.id && result == false)) {
            return
        }

        // Mayor de 0 - NIVEL
        //////////////////
        if (result != null && (rc.trueResult > 0 && result == true)) {
            backLevelSteps.add(rc.trueResult)
            return
        }

        if (result != null && (rc.falseResult > 0 && result == false)) {
            backLevelSteps.add(rc.falseResult)
            return
        }

        // -4 - NIVELX
        //////////////////
        if (rc.trueResult == DcrResult.levelX.id) {
            if (result != null) {
                // Niveles en formato de texto, separados por comas (,)
                if (result is String) {
                    val z = result.toString().split(',')

                    try {
                        if (levelsToNavigate.size < 1) {
                            levelsToNavigate.clear()
                        }

                        for (h in z) {
                            levelsToNavigate.add(h.toInt())
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                        return
                    }

                    if (levelsToNavigate.size > 0) {
                        backLevelSteps.add(levelsToNavigate[0])
                        levelsToNavigate.remove(levelsToNavigate[0])

                        return
                    }
                } else if (result is Int) // Niveles en formato numérico
                {
                    // IR AL NIVEL DETERMINADO
                    if (result > 0) {
                        backLevelSteps.add(result)
                        return
                    }
                    // CONTINUAR
                    if (result == DcrResult.cont.id) {
                        return
                    }

                    // VOLVER
                    if (result == DcrResult.back.id) {
                        if (levelsToNavigate.size > 0) {
                            // Elimino el último paso ejecutado así el último paso es el previo
                            backLevelSteps.remove(backLevelSteps.last())
                            backLevelSteps.add(levelsToNavigate[0])
                            levelsToNavigate.remove(levelsToNavigate[0])
                        } else {
                            // Elimino el último paso ejecutado así el último paso es el previo
                            backLevelSteps.remove(backLevelSteps.last())
                        }
                        return
                    }

                    // FIN
                    if (result == DcrResult.end.id) {
                        return
                    }

                    // NO CONTINUAR
                    if (result == DcrResult.noContinue.id) {
                        return
                    }
                }
            }
        }

        // Lo mismo de arriba, pero para resultados Falsos, no sé si es necesario, pero por las dudas
        if (rc.falseResult == DcrResult.levelX.id) {
            if (result != null) {
                if (result is String) {
                    val z = result.toString().split(',')

                    try {
                        if (levelsToNavigate.size < 1) {
                            levelsToNavigate.clear()
                        }

                        for (h in z) {
                            levelsToNavigate.add(h.toInt())
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                        return
                    }

                    if (levelsToNavigate.size > 0) {
                        backLevelSteps.add(levelsToNavigate[0])
                        levelsToNavigate.remove(levelsToNavigate[0])

                        return
                    }
                } else if (result is Int) {
                    if (result > 0) {
                        backLevelSteps.add(result)
                        return
                    }

                    // CONTINUAR
                    if (result == DcrResult.cont.id) {
                        return
                    }

                    // VOLVER
                    if (result == DcrResult.back.id) {
                        if (levelsToNavigate.size > 0) {
                            // Elimino el último paso ejecutado así el último paso es el previo
                            backLevelSteps.remove(backLevelSteps.last())
                            backLevelSteps.add(levelsToNavigate[0])
                            levelsToNavigate.remove(levelsToNavigate[0])
                        } else {
                            // Elimino el último paso ejecutado así el último paso es el previo
                            backLevelSteps.remove(backLevelSteps.last())
                        }
                        return
                    }

                    // FIN
                    if (result == DcrResult.end.id) {
                        return
                    }

                    // NO CONTINUAR
                    if (result == DcrResult.noContinue.id) {
                        return
                    }
                }
            }
        }

        // -1 - VOLVER
        ///////////////////
        if (result != null && (rc.trueResult == DcrResult.back.id && result == true || rc.falseResult == DcrResult.back.id && result == false)) {
            if (levelsToNavigate.size > 0) {
                // Elimino el último paso ejecutado así el último paso es el previo
                backLevelSteps.remove(backLevelSteps.last())
                backLevelSteps.add(levelsToNavigate[0])
                levelsToNavigate.remove(levelsToNavigate[0])
            } else {
                // Elimino el último paso ejecutado así el último paso es el previo
                backLevelSteps.remove(backLevelSteps.last())
            }
        }
    }

    private fun fill(level: Int) {
        // Sólo si todavía no fueron cargados.
        // Si accede a una nueva ruta no estarán cargados todavía
        val routeId = route?.routeId ?: return

        if (routeComposition == null) {
            Log.d(this::class.java.simpleName, getString(R.string.obtaining_route_composition))
            routeComposition = RouteCompositionDbHelper().selectByRouteId(routeId)
        }

        if ((routeComposition?.size ?: 0) <= 0) {
            closeKeyboard(this)

            val data = Intent()
            data.putExtra("error_msg", getContext().getString(R.string.empty_route))
            setResult(RESULT_CANCELED, data)
            finish()
            return
        }

        val routeProcessId = routeProcess?.collectorRouteProcessId ?: return

        try {
            GetRouteProcessContent(
                routeId = routeId,
                routeProcessId = routeProcessId,
                level = level,
                rpContArray = rpcArray
            ) { onGetContentProcess(it) }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun skip() {
        val rpc = adapter?.currentRpc() ?: return

        if (rpc.routeProcessStatusId == RouteProcessStatus.processed.id) {
            makeText(
                binding.root, getContext().getString(R.string.already_processed), SnackBarType.INFO
            )
            return
        }

        updateStatus(rpc = rpc, s = RouteProcessStatus.skipped, dc = null)

        // El nivel al que volví está completo
        if (isCurrentLevelCompleted()) {
            runOnUiThread {
                binding.confirmButton.isEnabled = true
            }
            return
        }

        next()
    }

    private fun skipAll() {
        if ((adapter?.itemCount ?: 0) > 0) {
            JotterListener.lockScanner(this, true)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getContext().getString(R.string.skip_remaining))
                alert.setMessage(getContext().getString(R.string.do_you_want_to_skip_the_remaining_steps_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { dialog, _ ->
                    dialog.dismiss()
                    positiveSkipAll()
                }

                alert.show()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.lockScanner(this, false)
            }
        }
    }

    private fun positiveSkipAll() {
        JotterListener.lockScanner(this, true)

        SkipAll(allRouteProcessContent = adapter?.fullList ?: ArrayList(),
            onProgress = { onSkipAllProgress(it) })
    }

    private fun updateStatus(
        rpc: RouteProcessContent,
        s: RouteProcessStatus,
        dc: DataCollection?,
        refreshLater: Boolean = false,
    ) {
        rpc.routeProcessStatusId = s.id

        if (dc != null && s != RouteProcessStatus.notProcessed) {
            rpc.dataCollectionId = dc.collectorDataCollectionId
        } else {
            rpc.dataCollectionId = null
        }

        // Actualizar la base de datos
        if (!rpc.saveChanges()) {
            makeText(
                binding.root,
                getContext().getString(R.string.error_updating_registered_data),
                SnackBarType.ERROR
            )
        }

        if (!refreshLater) {
            val index = adapter?.getIndexById(rpc.routeProcessContentId)
            if (index != null) adapter?.notifyItemChanged(index, PAYLOADS.STATUS_CHANGE)
        }

        if (s == RouteProcessStatus.processed || s == RouteProcessStatus.skipped) {
            runOnUiThread {
                binding.confirmButton.isEnabled = isCurrentLevelCompleted()
            }
        }
    }

    private fun reentry() {
        if (User.hasPermission(PermissionEntry.ReentryRouteProcessContent)) {
            val rpc = adapter?.currentRpc()

            if (rpc != null) {
                if (rpc.routeProcessStatusId != RouteProcessStatus.processed.id) {
                    makeText(
                        binding.root,
                        getContext().getString(R.string.not_processed),
                        SnackBarType.INFO
                    )
                } else {
                    JotterListener.lockScanner(this, true)
                    try {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle(getContext().getString(R.string.enter_record_again))
                        alert.setMessage(getContext().getString(R.string.are_you_sure_to_enter_this_record_again_question))
                        alert.setNegativeButton(R.string.cancel, null)
                        alert.setPositiveButton(R.string.accept) { _, _ ->
                            positiveReentry()
                        }

                        alert.show()
                    } catch (ex: java.lang.Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                    } finally {
                        JotterListener.lockScanner(this, false)
                    }
                }
            }
        } else {
            makeText(
                binding.root,
                getContext().getString(R.string.you_do_not_have_permission_to_enter_collected_data_again),
                SnackBarType.ERROR
            )
        }
    }

    private fun positiveReentry() {
        val rpc = adapter?.currentRpc() ?: return
        val dcId = rpc.dataCollectionId ?: return

        DataCollectionDbHelper().deleteById(dcId)
        DataCollectionContentDbHelper().deleteByDataCollectionId(dcId)
        RouteProcessStepsDbHelper().deleteByCollectorDataCollectionId(dcId)

        updateStatus(rpc = rpc, s = RouteProcessStatus.notProcessed, dc = null)

        runOnUiThread {
            binding.confirmButton.isEnabled = false
        }
    }

    private fun detail() {
        val rpc = adapter?.currentRpc() ?: return

        if (rpc.assetId != null) {
            val tempAsset = AssetDbHelper().selectById(rpc.assetId)
            if (tempAsset != null) {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(this, AssetDetailActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("asset", tempAsset)
                    startActivity(intent)
                }
            }
        } else if (rpc.warehouseAreaId != null) {
            val tempWa = WarehouseAreaDbHelper().selectById(rpc.warehouseAreaId)
            if (tempWa != null) {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(this, WarehouseAreaDetailActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("warehouseArea", tempWa)
                    startActivity(intent)
                }
            }
        }
    }

    private fun beginProcess() {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val rpc = adapter?.currentRpc() ?: return

        if (rpc.routeProcessStatusId == RouteProcessStatus.processed.id) {
            makeText(
                binding.root, getContext().getString(R.string.already_processed), SnackBarType.INFO
            )
            return
        }

        // Acá se procesa el contenido de la ruta seleccionado
        processRouteProcessContent()
    }

    private fun processRouteProcessContent() {
        val rpc = adapter?.currentRpc() ?: return

        if (rpc.routeProcessStatusId != RouteProcessStatus.processed.id) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, DccActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("routeProcessContent", Parcels.wrap(rpc))
                resultForFinishDcc.launch(intent)
            }
        }
    }

    private val resultForFinishDcc =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val dc =
                        Parcels.unwrap<DataCollection>(data.parcelable("dataCollection"))
                    processFinish(dc)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun confirmLevel(askConfirm: Boolean) {
        if (!binding.confirmButton.isEnabled) return

        // El nivel está completo
        if (isRouteFinished()) {
            if (askConfirm) {
                JotterListener.lockScanner(this, true)
                try {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle(getContext().getString(R.string.finished_route))
                    alert.setMessage(getContext().getString(R.string.the_route_is_complete_do_you_want_to_confirm_and_return_to_the_main_menu_question))
                    alert.setNegativeButton(R.string.cancel, null)
                    alert.setPositiveButton(R.string.accept) { dialog, _ ->
                        dialog.dismiss()
                        saveRouteProcess()
                    }
                    alert.show()
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                    ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                } finally {
                    JotterListener.lockScanner(this, false)
                }
            } else {
                saveRouteProcess()
            }
        } else {
            backLevel()
        }
    }


    private fun saveRouteProcess() {
        val rp = routeProcess ?: return

        saving = true

        JotterListener.lockScanner(this, true)

        thread {
            val saveRouteProcess = SaveRouteProcess()
            saveRouteProcess.addParams(routeProcess = rp,
                allRouteProcessContent = rpcArray,
                onSaveProgress = { saveViewModel.setSaveProgress(it) },
                onSyncProgress = { syncViewModel.setSyncUploadProgress(it) })
            saveRouteProcess.execute()
        }
    }

    @Suppress("unused")
    private fun next() {
        adapter?.selectNext()
    }

    @Suppress("unused")
    private fun prev() {
        adapter?.selectPrev()
    }

    private fun backLevel() {
        runOnUiThread {
            binding.confirmButton.isEnabled = false
        }

        if (levelsToNavigate.size > 0) {
            Log.d(this::class.java.simpleName, getString(R.string.continuing_sequence_of_levels))

            // Elimino el último paso ejecutado así el último paso es el previo
            backLevelSteps.remove(backLevelSteps.last())

            fill(levelsToNavigate[0])
            levelsToNavigate.remove(levelsToNavigate[0])
        } else if (backLevelSteps.size > 0) {
            Log.d(this::class.java.simpleName, getString(R.string.returning_to_the_previous_level))

            // Elimino el último paso ejecutado así el último paso es el previo
            backLevelSteps.remove(backLevelSteps.last())

            if (backLevelSteps.size > 0) {
                fill(backLevelSteps.last())

                // Elimino el paso que se agrega en el FILL porque si no quedan dos repetidos seguidos
                backLevelSteps.remove(backLevelSteps.last())
            }
        }

        // El nivel al que volví está completo
        if (isCurrentLevelCompleted()) {
            // Seguir bajando
            runOnUiThread {
                binding.confirmButton.isEnabled = true
            }
        }

        runDemo()
    }

    private fun runDemo() {
        if (!saving && Statics.DEMO_MODE) {
            Handler(Looper.getMainLooper()).postDelayed(
                { demo() }, 500
            )
        }
    }

    private fun isRouteFinished(): Boolean {
        return try {
            val level = adapter?.currentLevel() ?: 1
            isCurrentLevelCompleted() && level == 1
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            true
        }
    }

    private fun isCurrentLevelCompleted(): Boolean {
        val c = adapter?.itemCount ?: 0

        (0 until c).forEach { i ->
            val z = adapter?.getContentByIndex(i)
            if (z != null) {
                if (z.routeProcessStatusId == RouteProcessStatus.notProcessed.id || z.routeProcessStatusId == RouteProcessStatus.unknown.id) {
                    return false
                }
            }
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
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

        when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                return true
            }

            R.id.action_rfid_connect -> {
                JotterListener.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_trigger_scan -> {
                JotterListener.trigger(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_read_barcode -> {
                JotterListener.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun endProcess(dataCollection: DataCollection) {
        val rpc = adapter?.currentRpc() ?: return

        updateStatus(
            rpc = rpc, s = RouteProcessStatus.processed, dc = dataCollection, refreshLater = false
        )

        // PROCESO TERMINADO, SALIR
        runOnUiThread {
            binding.confirmButton.isEnabled = true
        }
    }

    private fun processFinish(dc: DataCollection) {
        val rpc = adapter?.currentRpc() ?: return
        val currentRc = routeComposition ?: return

        var rc: RouteComposition? = null
        for (r in currentRc) {
            if (r.level == rpc.level && r.position == rpc.position) {
                rc = r
                break
            }
        }

        // ERROR
        if (rc == null) {
            return
        }

        // Contenidos recolectados
        val dcCont = dc.contents

        // Tomar los parámetros devueltos por la regla y sus valores
        // Formato de los parámetros en las rutas es igual al de las reglas, pero se
        // agrega el nivel y la posición del contenido de la ruta al principio.
        // Ejemplo: [1.2.1.2.1500000359]
        //          Nivel de ruta: 1
        //          Posición de ruta: 2
        //          Nivel de regla: 1
        //          Posición de regla: 2
        //          Composición Id: 1500000359

        val parameters: ArrayList<Parameter> = ArrayList()
        for (c in dcCont) {
            if (c.attributeCompositionId <= 0) {
                continue
            }

            val paramName =
                rc.level.toString() + separator + rc.position + separator + c.level + separator + c.position + separator + c.attributeCompositionId

            parameters.add(Parameter(paramName, c.valueStr))
        }

        // Agrego los parámetros a la colección de parámetros
        for (p in parameters) {
            var isIn = false
            for (p1 in allParameters) {
                if (p1.paramName == p.paramName) {
                    p1.paramValue = p.paramValue
                    isIn = true
                    break
                }
            }

            if (!isIn) {
                allParameters.add(p)
            }
        }

        // Evalúo el contenido de ruta actual
        val result = evaluate(rc, allParameters)

        // Cuando no existe expresión para realizar una evaluación, pero el diseño de la ruta espera un resultado
        // determinado es porque está mal diseñada esa parte de la ruta.
        if (result == null && rc.expression.isNullOrEmpty() && (rc.trueResult != 0 || rc.falseResult != 0)) {
            // No puede seguir
            makeText(
                binding.root,
                getContext().getString(R.string.route_design_error),
                SnackBarType.ERROR
            )
            return
        }

        if (result == null && rc.trueResult != DcrResult.cont.id || result == null && rc.falseResult != DcrResult.cont.id) {
            // No puede seguir
            makeText(
                binding.root,
                getContext().getString(R.string.invalid_value_does_not_allow_to_continue),
                SnackBarType.ERROR
            )
            return
        }

        // region -3 - NO CONTINUAR
        /////////////////////////
        if (rc.trueResult == DcrResult.noContinue.id && result != null && result == true || rc.falseResult == DcrResult.noContinue.id && result != null && result == false) {
            // No puede seguir
            makeText(
                binding.root,
                getContext().getString(R.string.value_does_not_allow_to_continue),
                SnackBarType.ERROR
            )
            return
        }
        // endregion

        // region 0 - CONTINUAR
        /////////////////////
        // En caso de que tanto trueResult como falseResult sean 0
        // En caso de que trueResult sea 0 y la evaluación sea verdadera
        // En caso de que falseResult sea 0 y la evaluación sea negativa
        if (rc.trueResult == DcrResult.cont.id && rc.falseResult == DcrResult.cont.id || rc.trueResult == DcrResult.cont.id && result != null && result == true || rc.falseResult == DcrResult.cont.id && result != null && result == false) {
            // No hay evaluación, pasar al siguiente del nivel
            updateStatus(rpc = rpc, s = RouteProcessStatus.processed, dc = dc)

            if (isRouteFinished()) {
                endProcess(dc)
                return
            } else {
                next()
                return
            }
        }
        // endregion

        // region -2 - FIN
        ////////////////
        if (result != null && (rc.trueResult == DcrResult.end.id && result == true || rc.falseResult == DcrResult.end.id && result == false)) {
            endProcess(dc)
            return
        }
        // endregion

        // region Mayor de 0 - NIVEL
        //////////////////
        if (result != null && (rc.trueResult > 0 && result == true)) {
            updateStatus(rpc = rpc, s = RouteProcessStatus.processed, dc = dc)
            fill(rc.trueResult)
            return
        }

        if (result != null && (rc.falseResult > 0 && result == false)) {
            updateStatus(rpc = rpc, s = RouteProcessStatus.processed, dc = dc)
            fill(rc.falseResult)
            return
        }
        // endregion

        // region -4 - NIVELX
        //////////////////
        if (rc.trueResult == DcrResult.levelX.id || rc.falseResult == DcrResult.levelX.id) {
            if (result != null) {
                // Niveles en formato de texto, separados por comas (,)
                if (result is String) {
                    val z = result.toString().split(',')
                    try {
                        if (levelsToNavigate.size < 1) {
                            levelsToNavigate.clear()
                        }

                        for (h in z) {
                            levelsToNavigate.add(h.toInt())
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                        return
                    }

                    if (levelsToNavigate.size > 0) {
                        updateStatus(
                            rpc = rpc,
                            s = RouteProcessStatus.processed,
                            dc = dc,
                            refreshLater = false
                        )

                        fill(levelsToNavigate[0])
                        levelsToNavigate.remove(levelsToNavigate[0])

                        return
                    }
                } else if (result is Int) // Niveles en formato numérico
                {
                    if (result > 0) // IR AL NIVEL DETERMINADO
                    {
                        updateStatus(
                            rpc = rpc,
                            s = RouteProcessStatus.processed,
                            dc = dc,
                            refreshLater = false
                        )
                        fill(result)
                        return
                    }
                    if (result == DcrResult.cont.id) // CONTINUAR
                    {
                        updateStatus(
                            rpc = rpc,
                            s = RouteProcessStatus.processed,
                            dc = dc,
                            refreshLater = false
                        )
                        next()
                        return
                    }
                    if (result == DcrResult.back.id) // VOLVER
                    {
                        updateStatus(
                            rpc = rpc,
                            s = RouteProcessStatus.processed,
                            dc = dc,
                            refreshLater = false
                        )

                        runOnUiThread {
                            binding.confirmButton.isEnabled = true
                        }
                        return
                    }
                    if (result == DcrResult.end.id) // FIN
                    {
                        endProcess(dc)
                        return
                    }
                    if (result == DcrResult.noContinue.id)
                    // NO CONTINUAR
                    {
                        // No puede seguir
                        makeText(
                            binding.root,
                            getContext().getString(R.string.value_does_not_allow_to_continue),
                            SnackBarType.ERROR
                        )
                        return
                    }
                }
            }
        }
        // endregion

        // region -1 - VOLVER
        ///////////////////
        if (result != null && (rc.trueResult == DcrResult.back.id && result == true || rc.falseResult == DcrResult.back.id && result == false)) {
            updateStatus(rpc = rpc, s = RouteProcessStatus.processed, dc = dc)

            runOnUiThread {
                binding.confirmButton.isEnabled = true
            }
        }
        // endregion
    }

    private fun scannerHandleScanCompleted(scannedCode: String) {
        JotterListener.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scannedCode.trim().isEmpty()) {
                val res = this.getString(R.string.invalid_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                code = scannedCode,
                searchWarehouseAreaId = true,
                searchAssetCode = true,
                searchAssetSerial = true,
                searchAssetEan = true,
                validateId = true
            )

            if (sc.codeFound && sc.asset != null && sc.labelNbr == 0) {
                val res = this.getString(R.string.report_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && sc.asset != null && (sc.asset ?: return).labelNumber == null) {
                val res = this.getString(R.string.no_printed_label)
                makeText(binding.root, res, SnackBarType.ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && (sc.asset != null && (sc.asset
                    ?: return).labelNumber != sc.labelNbr && sc.labelNbr != null)
            ) {
                val res = this.getString(R.string.invalid_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            // Si ya se encontró un activo/área, utilizo su código real,
            // ya que el código escaneado puede contener caractéres especiales
            // que no aparecen en la lista
            var tempCode = scannedCode
            if (sc.asset != null) {
                tempCode = (sc.asset ?: return).code
            } else if (sc.warehouseArea != null) {
                tempCode = (sc.warehouseArea ?: return).warehouseAreaId.toString()
            }

            var rpc: RouteProcessContent? = null

            if (adapter != null && adapter?.fullList?.any() == true) {
                // Buscar primero en el adaptador de la lista
                (0 until adapter!!.itemCount).map { adapter!!.getContentByIndex(it) }.filter {
                    it != null && (sc.asset != null && it.assetCode != null && it.assetCode == tempCode || sc.warehouseArea != null && it.warehouseAreaId != null && it.warehouseAreaId.toString() == tempCode)
                }.forEach {
                    // Process the ROW
                    rpc = if ((it
                            ?: return@forEach).routeProcessStatusId == RouteProcessStatus.notProcessed.id
                    ) {
                        val res = this.getString(R.string.ok)
                        makeText(binding.root, res, SnackBarType.SUCCESS)
                        Log.d(this::class.java.simpleName, res)
                        it
                    } else {
                        val res = this.getString(R.string.already_registered)
                        makeText(binding.root, res, SnackBarType.INFO)
                        Log.d(this::class.java.simpleName, res)
                        it
                    }
                }
            }

            if (rpc != null) {
                // SELECT THE ITEM ROW
                adapter?.selectItem(rpc ?: return)
            }

            return
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    private fun demo() {
        if (!Statics.DEMO_MODE) return

        if (isCurrentLevelCompleted()) {
            confirmLevel(false)
            return
        }

        val currentRpc = adapter?.currentRpc()
        if (currentRpc != null) {
            when (currentRpc.routeProcessStatusId) {
                RouteProcessStatus.processed.id -> {
                    adapter?.selectNext()
                    if (!saving) demo()
                }

                RouteProcessStatus.notProcessed.id -> {
                    beginProcess()
                }

                RouteProcessStatus.skipped.id,
                RouteProcessStatus.unknown.id,
                -> {
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false
        runDemo()
    }

    @SuppressLint("MissingSuperCall")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        JotterListener.lockScanner(this, true)
        try {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getContext().getString(R.string.suspend_the_data_collection))
            alert.setMessage(getContext().getString(R.string.you_want_to_suspend_the_data_collection_process))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)

                setResult(RESULT_OK)
                finish()
            }
            alert.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
            allowClicks = true
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        scannerHandleScanCompleted(scanCode)
    }

    // region ProgressBar
// Aparece mientras se realizan operaciones sobre las bases remota y local
    private var progressDialog: AlertDialog? = null
    private lateinit var alertBinding: ProgressBarDialogBinding
    private fun createProgressDialog() {
        alertBinding = ProgressBarDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        //builder.setCancelable(false) // if you want user to wait for some process to finish
        builder.setView(alertBinding.root)
        progressDialog = builder.create()
    }

    private fun showProgressDialog(
        title: String,
        msg: String,
        status: Int,
        progress: Int? = null,
        total: Int? = null,
    ) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        runOnUiThread {
            if (progressDialog == null) {
                createProgressDialog()
            }

            val appColor =
                ResourcesCompat.getColor(getContext().resources, R.color.assetControl, null)

            when (status) {
                ProgressStatus.starting.id -> {
                    progressDialog?.setTitle(title)
                    //dialog?.setMessage(msg)
                    alertBinding.messageTextView.text = msg
                    alertBinding.progressBarHor.progress = 0
                    alertBinding.progressBarHor.max = 0
                    alertBinding.progressBarHor.visibility = View.GONE
                    alertBinding.progressTextView.visibility = View.GONE
                    alertBinding.progressBarHor.progressTintList = ColorStateList.valueOf(appColor)
                    alertBinding.progressBar.visibility = View.VISIBLE
                    alertBinding.progressBar.progressTintList = ColorStateList.valueOf(appColor)

                    progressDialog?.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getContext().getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing) progressDialog?.show()
                }

                ProgressStatus.running.id -> {
                    //dialog?.setMessage(msg)
                    if (msg != "") alertBinding.messageTextView.text = msg
                    if (progress != null && total != null && total > 0) {
                        alertBinding.progressBarHor.max = total
                        alertBinding.progressBarHor.progress = progress
                        alertBinding.progressBarHor.isIndeterminate = false
                        val t = "$progress / $total"
                        alertBinding.progressTextView.text = t

                        if (alertBinding.progressBarHor.visibility == View.GONE) {
                            alertBinding.progressBarHor.visibility = View.VISIBLE
                            alertBinding.progressTextView.visibility = View.VISIBLE
                        }

                        if (alertBinding.progressBar.visibility == View.VISIBLE) alertBinding.progressBar.visibility =
                            View.GONE
                    } else {
                        alertBinding.progressBar.progress = 0
                        alertBinding.progressBar.max = 0
                        alertBinding.progressBar.isIndeterminate = true

                        if (alertBinding.progressBarHor.visibility == View.VISIBLE) {
                            alertBinding.progressBarHor.visibility = View.GONE
                            alertBinding.progressTextView.visibility = View.GONE
                        }
                        if (alertBinding.progressBar.visibility == View.GONE) alertBinding.progressBar.visibility =
                            View.VISIBLE
                    }

                    progressDialog?.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getContext().getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing) progressDialog?.show()
                }

                ProgressStatus.finished.id, ProgressStatus.canceled.id, ProgressStatus.crashed.id -> {
                    progressDialog?.dismiss()
                    progressDialog = null
                }
            }
        }
    }
// endregion

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

// region READERS Reception

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onStateChanged(state: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (prefsGetBoolean(Preference.rfidShowConnectedMessage)) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    makeText(
                        binding.root,
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    makeText(
                        binding.root,
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    makeText(
                        binding.root,
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.INFO
                    )
                }
            }
        }
    }

    override fun onReadCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        scannerCompleted(scanCode)
    }
    //endregion READERS Reception

    override fun onUiEventRequired(it: AdapterProgress) {
    }

    override fun onDataSetChanged() {
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
    }
}