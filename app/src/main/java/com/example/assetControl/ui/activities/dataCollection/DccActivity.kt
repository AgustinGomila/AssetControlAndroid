package com.example.assetControl.ui.activities.dataCollection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.dacosys.imageControl.ui.snackBar.SnackBarEventData
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.enums.attribute.AttributeCompositionType
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.enums.dataCollection.DcrResult
import com.example.assetControl.data.enums.permission.PermissionEntry
import com.example.assetControl.data.enums.unit.UnitType
import com.example.assetControl.data.model.common.KeyLevelPos
import com.example.assetControl.data.model.common.Parameter
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.dto.dataCollection.DataCollection
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.data.room.dto.user.User
import com.example.assetControl.data.room.entity.fragment.FragmentDataEntity
import com.example.assetControl.data.room.repository.attribute.AttributeCompositionRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionContentRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRuleContentRepository
import com.example.assetControl.data.room.repository.dataCollection.DataCollectionRuleRepository
import com.example.assetControl.data.room.repository.fragment.FragmentDataRepository
import com.example.assetControl.data.room.repository.route.RouteProcessRepository
import com.example.assetControl.databinding.DataCollectionContentActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.fragments.dataCollection.CommaSeparatedSpinnerFragment
import com.example.assetControl.ui.fragments.dataCollection.DccFragmentListener
import com.example.assetControl.ui.fragments.dataCollection.GeneralFragment
import com.example.assetControl.ui.fragments.dataCollection.HistoricDataFragment
import com.example.assetControl.ui.fragments.dataCollection.UnitTypeSpinnerFragment
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import kotlinx.coroutines.runBlocking
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.parceler.Parcels
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class DccActivity : AppCompatActivity(), Scanner.ScannerListener,
    CommaSeparatedSpinnerFragment.OnItemSelectedListener,
    UnitTypeSpinnerFragment.OnItemSelectedListener, Rfid.RfidDeviceListener, DccFragmentListener,
    KeyboardVisibilityEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
        historicDataFragment?.onDestroy()
        historicDataFragment = null
    }

    override fun onItemSelected(strOption: String?) {
    }

    override fun onItemSelected(unitType: UnitType?) {
    }

    private var rpc: RouteProcessContent? = null
    private var routeProcessId: Long? = null
    private var dcr: DataCollectionRule? = null
    private var dcrContArray: ArrayList<DataCollectionRuleContent> = ArrayList()
    private var dcContArray: ArrayList<DataCollectionContent> = ArrayList()

    /////////////////////////////////////////////////////////////////////////////////
    // TARGETS: Solo cuando se ingresa a una recolección de datos de forma directa //
    // sin que sea parte de una ruta. A través del botón Recolección de datos del  //
    // menú principal                                                              //
    private var targetAsset: Asset? = null
    private var targetWarehouseArea: WarehouseArea? = null
    private var targetItemCategory: ItemCategory? = null
    /////////////////////////////////////////////////////////////////////////////////

    private val allParameters: ArrayList<Parameter> = ArrayList()
    private val levelsToNavigate: ArrayList<Int> = ArrayList()
    private val stepsHistory = ArrayList<KeyLevelPos>()
    private val separator = '.'
    private var isEvaluating = false

    private var fragCollectionCreated = false

    private var attrFrags: ArrayList<GeneralFragment> = ArrayList()
    private var allFrags: ArrayList<GeneralFragment> = ArrayList()
    private var currentFragment: GeneralFragment? = null
    private var currentLevel = 1
    private var currentPos = 1

    private var imageControlFragment: ImageControlButtonsFragment? = null

    private var panelBottomIsExpanded = true

    private var historicDataFragment: HistoricDataFragment? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveBundleValues(outState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putInt("level", currentLevel)
        b.putInt("pos", currentPos)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            b, "imageControlFragment", imageControlFragment as ImageControlButtonsFragment
        )

        if (historicDataFragment is HistoricDataFragment) supportFragmentManager.putFragment(
            b, "historicDataFragment", historicDataFragment as HistoricDataFragment
        )

        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        b.putParcelable("dcr", dcr)
        b.putParcelable("rpc", rpc)

        b.putBoolean("fragCollectionCreated", fragCollectionCreated)

        saveFragStatus()
    }

    private fun loadBundleValuesFromExtras(b: Bundle) {
        val tempRpc = Parcels.unwrap<RouteProcessContent>(b.parcelable("routeProcessContent"))
        if (tempRpc != null) setRouteProcessContent(tempRpc)

        val tempDcr = Parcels.unwrap<DataCollectionRule>(b.parcelable("dataCollectionRule"))
        if (tempDcr != null) dcr = tempDcr

        val tempAsset = Parcels.unwrap<Asset>(b.parcelable("asset"))
        if (tempAsset != null) targetAsset = tempAsset

        val tempWarehouseArea = Parcels.unwrap<WarehouseArea>(b.parcelable("warehouseArea"))
        if (tempWarehouseArea != null) targetWarehouseArea = tempWarehouseArea

        val tempItemCategory = Parcels.unwrap<ItemCategory>(b.parcelable("itemCategory"))
        if (tempItemCategory != null) targetItemCategory = tempItemCategory
    }

    private fun loadBundleValues(b: Bundle) {
        currentLevel = b.getInt("level")
        currentPos = b.getInt("pos")

        //Restore the fragment's instance
        val icF = supportFragmentManager.getFragment(b, "imageControlFragment")
        if (icF is ImageControlButtonsFragment) imageControlFragment = icF

        val hf = supportFragmentManager.getFragment(b, "historicDataFragment")
        if (hf is HistoricDataFragment) historicDataFragment = hf

        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")

        rpc = b.parcelable("rpc")
        if (rpc != null) setRouteProcessContent(rpc ?: return)

        fragCollectionCreated = b.getBoolean("fragCollectionCreated")
    }

    private lateinit var binding: DataCollectionContentActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = DataCollectionContentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.data_collection)

        historicDataFragment =
            supportFragmentManager.findFragmentById(binding.historicDataFragment.id) as HistoricDataFragment?

        if (savedInstanceState != null) {
            // Ya estamos dentro de una recolección
            // La aplicación reanudó después de una rotación de pantalla
            loadBundleValues(savedInstanceState)
        } else {
            // Inicializar la actividad
            // Traer los parámetros que recibe la actividad
            val extras = intent.extras
            if (extras != null) loadBundleValuesFromExtras(extras)
        }

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()

        val targetWarehouseArea = targetWarehouseArea
        val targetItemCategory = targetItemCategory
        val targetAsset = targetAsset

        when {
            targetWarehouseArea != null -> showMessage(
                targetWarehouseArea.description, SnackBarType.INFO
            )

            targetItemCategory != null -> showMessage(
                targetItemCategory.description, SnackBarType.INFO
            )

            targetAsset != null -> showMessage(
                targetAsset.description, SnackBarType.INFO
            )
        }

        binding.nextButton.setOnClickListener { next() }
        binding.prevButton.setOnClickListener { previous() }

        binding.shadowView.setOnTouchListener { v: View?, _: MotionEvent? ->
            // Setting on Touch Listener for handling the touch inside ScrollView
            // Disallow the touch request for parent scroll on touch of child view
            v!!.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.enableCheckBox.setOnCheckedChangeListener { _, isChecked ->
            currentFragment?.isEnabled = isChecked
            setEnable(isChecked)
        }

        KeyboardVisibilityEvent.registerEventListener(this, this)

        setPanels()

        // Llenar la grilla
        initDcc()
    }

    private fun initDcc() {
        if (!fragCollectionCreated) {
            // Es una nueva sesión de recolección de datos
            createFragmentCollection()
            fragCollectionCreated = true
        } else {
            // Carga del estado del adaptador desde la DB temporal
            val allFragData = ArrayList(FragmentDataRepository().select())
            createFragmentCollection(allFragData)
        }

        fillControl(level = currentLevel, position = currentPos)
    }

    private fun setEnable(isChecked: Boolean) {
        runOnUiThread {
            if (isChecked) {
                Log.d(this::class.java.simpleName, "Toggle shadowView visibility to GONE")
                binding.shadowView.visibility = GONE
            } else {
                Log.d(this::class.java.simpleName, "Toggle shadowView visibility to VISIBLE")
                binding.shadowView.visibility = VISIBLE
            }
        }
    }

    private fun setRouteProcessContent(tempRpc: RouteProcessContent) {
        rpc = tempRpc
        val rpc = rpc ?: return

        dcr = DataCollectionRuleRepository().selectById(id = rpc.dataCollectionRuleId)
        val dcr = dcr ?: return

        val rp = RouteProcessRepository().selectById(id = rpc.routeProcessId) ?: return
        routeProcessId = rp.id

        val ruleContents = DataCollectionRuleContentRepository().selectByDataCollectionRuleIdActive(dcr.id)
            .sortedWith(compareBy({ it.level }, { it.position }))

        dcrContArray = ArrayList(ruleContents)
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            currentLayout.load(this, R.layout.data_collection_content_activity)
        } else {
            currentLayout.load(this, R.layout.data_collection_content_historic_panel_collapsed)
        }

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                historicDataFragment?.refreshPreviousData()
            }

            override fun onTransitionCancel(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(binding.dataCollectionContent, transition)

        currentLayout.applyTo(binding.dataCollectionContent)

        when {
            panelBottomIsExpanded -> {
                binding.expandHistoricPanelButton?.text = getString(R.string.collapse_panel)
            }

            else -> {
                binding.expandHistoricPanelButton?.text = getString(R.string.previous_records)
            }
        }
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandHistoricPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                nextLayout.load(this, R.layout.data_collection_content_historic_panel_collapsed)
            } else {
                nextLayout.load(this, R.layout.data_collection_content_activity)
            }

            panelBottomIsExpanded = !panelBottomIsExpanded
            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    historicDataFragment?.refreshPreviousData()
                }

                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(binding.dataCollectionContent, transition)

            nextLayout.applyTo(binding.dataCollectionContent)

            when {
                panelBottomIsExpanded -> {
                    binding.expandHistoricPanelButton?.text = getString(R.string.collapse_panel)
                }

                else -> {
                    binding.expandHistoricPanelButton?.text = getString(R.string.previous_records)
                }
            }
        }
    }

    private fun setImageControlFragment() {
        if (!svm.useImageControl) return
        val dcr = dcr ?: return
        val rpc = rpc ?: return

        var description = "${dcr.description}, ${
            rpc.assetStr.ifEmpty {
                rpc.warehouseAreaStr.ifEmpty {
                    rpc.warehouseStr.ifEmpty {
                        getString(R.string.no_name)
                    }
                }
            }
        }"

        val reference = "${getString(R.string.asset)}: ${rpc.assetCode}"
        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        val tableDescription = Table.routeProcess.description
        description = "$tableDescription: $description".take(255)

        try {
            if (imageControlFragment == null) {
                imageControlFragment = ImageControlButtonsFragment.newInstance(
                    Table.routeProcess.id.toLong(), rpc.routeProcessId.toString()
                )

                setFragmentValues(description, reference, obs)

                val fm = supportFragmentManager

                if (!isFinishing && !isDestroyed) {
                    runOnUiThread {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(binding.imageControlFragment.id, imageControlFragment ?: return@runOnUiThread)
                            .commit()

                        if (!svm.useImageControl) {
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
                }
            } else {
                imageControlFragment?.setTableId(Table.routeProcess.id)
                imageControlFragment?.setObjectId1(rpc.routeProcessId)
                imageControlFragment?.setObjectId2(null)

                setFragmentValues(description, reference, obs)
            }
        } catch (_: Exception) {
            showMessage(getString(R.string.imagecontrol_isnt_available), ERROR)
            svm.useImageControl = false
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

    private fun previous() {
        if (isEvaluating) {
            return
        }

        if (attributeHasMultipleComposition()) {
            prevComposition()
        } else {
            prevAttribute()
        }
    }

    private fun next() {
        if (isEvaluating) {
            return
        }

        if (attributeHasMultipleComposition()) {
            nextComposition(false)
        } else {
            continueRoute()
        }
    }

    private fun continueRoute() {
        if (currentFragment?.isEnabled == true) {
            confirmValueControl()
        } else {
            currentFragment?.valueStr = ""
            nextAttribute()
        }
    }

    private fun saveFragValues(frag: GeneralFragment) {
        try {
            // Busco el control correspondiente en la colección de controles
            val oldFrag = allFrags.firstOrNull {
                it.level == frag.level && it.position == frag.position && it.attrCompId == frag.attrCompId
            }

            allFrags.remove(oldFrag)
            allFrags.add(frag)
            allFrags = ArrayList(
                allFrags.sortedWith(
                    compareBy(
                        { it.level },
                        { it.position },
                        { it.attrCompId })
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun createFragmentCollection(fdArray: ArrayList<FragmentDataEntity>) {
        // Limpiar toda la colección de controles
        allFrags.clear()

        // Construyo un control por cada contenido de la regla
        for (fd in fdArray) {
            val dcrCont = DataCollectionRuleContentRepository().selectById(fd.dataCollectionRuleContentId) ?: continue

            try {
                val y = GeneralFragment(this)
                y.dataCollectionRuleContent = dcrCont

                val tempAttrCompTypeId = fd.attributeCompositionTypeId
                if (tempAttrCompTypeId > 0) {
                    val attrCompType =
                        AttributeCompositionType.getById(tempAttrCompTypeId) ?: continue

                    y.attributeCompositionType = attrCompType
                    y.valueStr = fd.valueStr
                }
                y.isEnabled = fd.isEnabled

                // Agrego el control a la colección
                allFrags.add(y)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
        }

        allFrags = ArrayList(
            allFrags.sortedWith(
                compareBy(
                    { it.level },
                    { it.position },
                    { it.attrCompId })
            )
        )
    }

    private fun createFragmentCollection() {
        // Limpiar toda la colección de controles
        allFrags.clear()

        // Construyo un control por cada contenido de la regla
        for (dcrCont in dcrContArray.sortedWith(compareBy({ it.level }, { it.position }))) {
            try {
                val y = GeneralFragment(this)
                y.dataCollectionRuleContent = dcrCont

                val tempAttrCompId = dcrCont.attributeCompositionId
                if (tempAttrCompId > 0) {
                    val attrComp = AttributeCompositionRepository().selectById(id = tempAttrCompId)
                    if (attrComp != null) {
                        val attrCompType = AttributeCompositionType.getById(attrComp.attributeCompositionTypeId)
                        y.attributeCompositionType = attrCompType
                        y.valueStr = attrComp.defaultValue
                    }
                }
                y.isEnabled = true

                // Agrego el control a la colección
                allFrags.add(y)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
        }

        allFrags = ArrayList(
            allFrags.sortedWith(
                compareBy(
                    { it.level },
                    { it.position },
                    { it.attrCompId })
            )
        )
    }

    private fun fillControl(level: Int, position: Int) {
        if (dcr == null) {
            return
        }

        // Guardamos y limpiamos los controles visibles
        for (frag in attrFrags) {
            saveFragValues(frag)
        }

        setAttributesFragments(level = level, position = position)

        // Crear una colección con los fragmentos del propio atributo que estamos registrando
        // Es decir, con su atributo y composición. Se usará para navegar con los botones prevAttrCompButton y nextAttrCompButton
        for (f in attrFrags) {
            if (f.isAttribute) {
                val dcrCont = f.dataCollectionRuleContent ?: return
                val str = "${dcrCont.description} - ${dcrCont.attributeStr}"

                runOnUiThread {
                    binding.attributeTextView.setText(str, TextView.BufferType.EDITABLE)
                }
            }
        }

        // Agrego la composición correspondiente al panel
        // Si no está especificada es la primera composición (!isAttribute),
        // sino, busco el AttributeCompositionId correspondiente.
        var f: GeneralFragment? = null
        for (tempFr in attrFrags) {
            if (!tempFr.isAttribute) {
                if (tempFr.level == level && tempFr.position >= position) {
                    f = tempFr
                    break
                }
            }
        }

        if (f != null) {
            fillPanel(f)
        }
    }

    private fun attributeHasMultipleComposition(): Boolean {
        return attrFrags.size > 2
    }

    private fun setTextView(level: Int, position: Int, attrCompPos: Int) {
        // Limpio los controles visibles
        clearEditText()

        // Contar la cantidad de atributos, sin composición, en el mismo nivel
        val allPos = dcrContArray.count {
            it.attributeCompositionId <= 0 && it.active && it.level == level
        }

        val currPos = position - dcrContArray.count {
            it.attributeCompositionId > 0 && it.active && it.level == level && it.position <= position
        }

        var posText = "$currPos/$allPos"
        if (attrFrags.size > 2) {
            // Hay más de una composición, agregar al texto de navegación
            posText += " ($attrCompPos/${attrFrags.size - 1})"
        }

        runOnUiThread {
            binding.positionTextView.setText(posText, TextView.BufferType.EDITABLE)
        }
    }

    private fun setAttributesFragments(level: Int, position: Int) {
        // Esta función llena la colección de fragmentos propios del atributo que estamos registrando en este momento.

        // La colección resultante contiene tanto las composiciones del atributo como el atributo en sí mismo.

        // Del atributo en sí mismo no se registran datos, pero puede contener una expresión condicional
        // que se evalúa luego de evaluar los datos registrados para las composiciones.

        attrFrags.clear()
        val r: ArrayList<GeneralFragment> = ArrayList()

        if (allFrags.isEmpty()) {
            return
        }

        for (i in 0 until allFrags.size) {
            if (allFrags[i].level != level) {
                continue
            }

            if (allFrags[i].position == position) {
                var attributeAdded = false
                if ((allFrags[i].dataCollectionRuleContent ?: return).active) {
                    if (allFrags[i].isAttribute) {
                        attributeAdded = true
                    }
                    r.add(allFrags[i])
                }

                // Busco hacia adelante desde la posición dada, todos
                // los fragmentos que forman parte del atributo
                for (j in i + 1 until allFrags.size) {
                    if (allFrags[j].isAttribute) {
                        // Hemos llegado al siguiente atributo
                        break
                    }

                    if ((allFrags[j].dataCollectionRuleContent ?: return).active) {
                        r.add(allFrags[j])
                    }
                }

                // Busco hacia atrás desde la posición dada, todos
                // los fragmentos que forman parte del atributo, incluído
                // el atributo
                if (!attributeAdded) {
                    for (j in i - 1 downTo 0) {
                        if (allFrags[j].isAttribute) {
                            // Hemos llegado al atributo original
                            if ((allFrags[j].dataCollectionRuleContent ?: return).active) {
                                r.add(allFrags[j])
                            }
                            break
                        }

                        if ((allFrags[j].dataCollectionRuleContent ?: return).active) {
                            r.add(allFrags[j])
                        }
                    }
                }

                break
            }
        }

        attrFrags = ArrayList(r.sortedWith(compareBy({ it.level }, { it.position })))
    }

    private fun clearEditText() {
        runOnUiThread {
            binding.codeTextView.setText("", TextView.BufferType.EDITABLE)
            binding.descriptionTextView.setText("", TextView.BufferType.EDITABLE)

            historicDataFragment?.clearPreviousData()
        }
    }

    private fun fillPanel(f: GeneralFragment) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        var newFragment: Fragment? = null
        if (f.getFragment() != null) {
            newFragment = f.getFragment() as Fragment
        }

        var oldFragment: Fragment? = null
        if (currentFragment != null) {
            oldFragment = currentFragment?.getFragment() as Fragment
        }

        var fragmentTransaction = supportFragmentManager.beginTransaction()
        if (oldFragment != null) {
            try {
                if (!isFinishing && !isDestroyed) {
                    fragmentTransaction
                        .remove(oldFragment)
                        .commitAllowingStateLoss()
                }
            } catch (ex: java.lang.Exception) {
                Log.e(this.javaClass.simpleName, ex.message.toString())
            }
        }

        // Close keyboard in transition
        if (currentFocus != null) {
            val inputManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                (currentFocus ?: return).windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        if (newFragment != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(
                    R.anim.animation_fade_in, R.anim.animation_fade_out
                )
                try {
                    if (!isFinishing && !isDestroyed) {
                        fragmentTransaction
                            .replace(binding.fragmentLayout.id, newFragment)
                            .commitAllowingStateLoss()
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e(this.javaClass.simpleName, ex.message.toString())
                }
            }, 20)
        }

        currentFragment = f

        var attrCompPos = 0
        for (tempFr in attrFrags) {
            if (!tempFr.isAttribute) {
                attrCompPos++
                if (tempFr == currentFragment) {
                    break
                }
            }
        }

        if (currentFragment != null) {
            currentPos = currentFragment?.position ?: -1
            currentLevel = currentFragment?.level ?: -1

            setTextView(currentFragment?.level ?: -1, currentFragment?.position ?: -1, attrCompPos)
            fillPreviousData(currentFragment ?: return)
        }

        if (Statics.DEMO_MODE) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
    }

    private fun fillPreviousData(f: GeneralFragment) {
        if (!User.hasPermission(PermissionEntry.ShowPreviousDataCollectionRegistry)) {
            return
        }

        var tempDcc: ArrayList<DataCollectionContent> = ArrayList()
        val rpc = rpc

        binding.enableCheckBox.isChecked = f.isEnabled

        if (rpc != null && f.dataCollectionRuleContent != null) {
            val contentRepository = DataCollectionContentRepository()
            val dcrCont = f.dataCollectionRuleContent ?: return

            val assetId = rpc.assetId ?: 0
            val warehouseAreaId = rpc.warehouseAreaId ?: 0
            val warehouseId = rpc.warehouseId ?: 0

            when {
                rpc.assetId != null && assetId > 0 -> {
                    binding.codeTextView.setText(rpc.assetCode, TextView.BufferType.EDITABLE)
                    binding.descriptionTextView.setText(
                        rpc.assetStr, TextView.BufferType.EDITABLE
                    )

                    tempDcc = ArrayList(
                        contentRepository.selectByDataCollectionRuleContentIdAssetId(
                            dcrCont.id, assetId
                        ).sortedWith(compareBy { it.dataCollectionDate }).reversed()
                    )
                }

                rpc.warehouseId != null && warehouseId > 0 -> {
                    binding.codeTextView.setText(
                        rpc.warehouseId.toString(), TextView.BufferType.EDITABLE
                    )
                    binding.descriptionTextView.setText(
                        rpc.warehouseStr, TextView.BufferType.EDITABLE
                    )

                    tempDcc = ArrayList(
                        contentRepository.selectByDataCollectionRuleContentIdWarehouseId(
                            dcrCont.id, warehouseId
                        ).sortedWith(compareBy { it.dataCollectionDate }).reversed()
                    )
                }

                rpc.warehouseAreaId != null && warehouseAreaId > 0 -> {
                    binding.codeTextView.setText(
                        rpc.warehouseAreaId.toString(), TextView.BufferType.EDITABLE
                    )
                    binding.descriptionTextView.setText(
                        rpc.warehouseAreaStr, TextView.BufferType.EDITABLE
                    )

                    tempDcc = ArrayList(
                        contentRepository.selectByDataCollectionRuleContentIdWarehouseAreaId(
                            dcrCont.id, warehouseAreaId
                        ).sortedWith(compareBy { it.dataCollectionDate }).reversed()
                    )
                }
            }
        }

        historicDataFragment?.setDccArray(tempDcc)
    }

    private fun saveControlStatus() {
        // Guardo el estado de todos los controles visibles.
        for (f in attrFrags) {
            saveFragValues(f)
        }

        // Le paso todos los parámetros al resto de los controles visibles
        for (f in attrFrags) {
            addExternalParametersToFragment(f)
            f.evaluate()
        }
    }

    private fun nextComposition(fragAlreadyEvaluated: Boolean) {
        if (!attributeHasMultipleComposition()) {
            return
        }

        if (currentFragment == null) {
            return
        }

        val dcrCont = currentFragment?.dataCollectionRuleContent ?: return

        val attrCompArray = AttributeCompositionRepository().selectByAttributeId(dcrCont.attributeId)
        val currentAttrCompId = dcrCont.attributeCompositionId

        var currentIndex = 0
        for (attrComp in attrCompArray) {
            if (attrComp.id == currentAttrCompId) {
                currentIndex++
                break
            }
            currentIndex++
        }

        // Guardo el estado del control actual
        saveControlStatus()

        // Si no tiene más composiciones, que salte al siguiente atributo
        // Se usa -1 porque el primer fragmento es el atributo (el resto composiciones)
        if (currentIndex + 1 > attrFrags.size - 1) {
            continueRoute()
            return
        }

        propagateData()
        currentFragment?.evaluate()

        if (!fragAlreadyEvaluated) {
            // Si el fragmento no fue evaluado, acá es donde se hace.

            // Si el resultado de la evaluación es CONTINUAR, el flujo
            // del código lo regresará a esta función con fragAlreadyEvaluated = false
            evaluateFragment(currentFragment ?: return)
            return
        }

        val tc = attrFrags[currentIndex + 1]
        fillPanel(tc)
    }

    private fun propagateData() {
        if (currentFragment != null) {
            addParameterToCollection(currentFragment ?: return)
            addDataCollectionContentToCollection(currentFragment ?: return)
            addExternalParametersToFragment(currentFragment ?: return)
        }
    }

    private fun prevComposition() {
        if (!attributeHasMultipleComposition()) {
            return
        }

        if (currentFragment == null) {
            return
        }

        val attrCompArray = AttributeCompositionRepository().selectByAttributeId(
            (currentFragment?.dataCollectionRuleContent ?: return).attributeId
        )
        val currentAttrCompId =
            (currentFragment?.dataCollectionRuleContent ?: return).attributeCompositionId

        var currentIndex = 0
        for (attrComp in attrCompArray) {
            if (attrComp.id == currentAttrCompId) {
                currentIndex++
                break
            }
            currentIndex++
        }

        // Guardo el estado del control actual
        saveControlStatus()

        if (currentIndex - 1 > 0) {
            val tc = attrFrags[currentIndex - 1]
            fillPanel(tc)
        } else {
            // Si no hay más composiciones volver al
            // atributo anterior.
            prevAttribute()
        }
    }

    private fun prevAttribute() {
        if (attrFrags.isEmpty()) {
            return
        }

        // Evalúo el primer control
        val f = attrFrags[0]
        val prevFrag = allFrags.filter { t -> t.isAttribute }.sortedByDescending { t -> t.position }
            .firstOrNull {
                it.level == f.level && it.position < f.position
            }

        if (prevFrag != null) {
            fillControl(level = prevFrag.level, position = prevFrag.position)
        } else {
            cancelDcc()
        }
    }

    private fun nextAttribute() {
        // NEXT
        // Evalúa la expresión del atributo si es que tiene y ejecuta el proceso correspondiente.
        // Al regresar de este proceso pueden pasar 3 cosas:
        //      1. salta al siguiente atributo dentro del mismo nivel
        //      2. vuelve al nivel padre
        //      3. termina el proceso.

        // Guardo el estado de todos los controles
        saveControlStatus()

        if (attrFrags.isEmpty()) {
            return
        }

        // Evalúo el primer control
        val f = attrFrags[0]
        val z = f.dataCollectionRuleContent ?: return

        if (!f.isEnabled && z.mandatory || f.valueStr == null && z.mandatory) {
            // No puede seguir, valor obligatorio
            val str = "${getString(R.string.mandatory_value_for)} ${z.description}"
            showMessage(str, ERROR)
            return
        }

        if (z.trueResult == DcrResult.cont.id && z.falseResult == DcrResult.cont.id) {
            evaluateFragment(f)
        }
    }

    private fun confirmValueControl() {
        try {
            if (currentFragment?.isEnabled == false) {
                currentFragment?.valueStr = ""
            }

            propagateData()
            currentFragment?.evaluate()
            evaluateFragment(currentFragment ?: return)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun addDataCollectionContentToCollection(f: GeneralFragment) {
        var valueStr = f.valueStr
        if (valueStr == null) {
            valueStr = ""
        }

        var result: Any? = null
        if (f.isEnabled) {
            result = f.evaluate()
        }

        // Creo un ID virtual con la ruta completa y el nivel y posición actual
        var completePath = ""
        for (step in stepsHistory) {
            completePath = completePath + step.level + step.pos + step.compId
        }
        completePath = completePath + f.level + f.position + f.attrCompId

        val virtualId: Long?
        try {
            virtualId = completePath.toLong()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        }

        // Elimino cada registro que empieza con el virtual ID.
        val dataToRemove: ArrayList<DataCollectionContent> = ArrayList()
        for (dc in dcContArray) {
            if (dc.id.toString().startsWith(completePath)) {
                dataToRemove.add(dc)
            }
        }

        // Eliminando datos antiguos
        dcContArray.removeAll(dataToRemove.toSet())

        // Agrego el nuevo registro si el control está activo
        if (f.isEnabled) {
            val ruleContent = f.dataCollectionRuleContent ?: return
            val attrCompType = f.attributeCompositionType

            // Agregando datos a la colección, ID virtual: completePath
            val dcc = DataCollectionContent(
                virtualId = virtualId,
                ruleContent = ruleContent,
                attributeCompositionType = attrCompType,
                anyResult = result,
                valueStr = valueStr
            )

            dcContArray.add(dcc)
        }
    }

    private fun addParameterToCollection(f: GeneralFragment) {
        // Tomo los pasos previos
        var previousSteps = ""
        for (steps in stepsHistory) {
            previousSteps =
                previousSteps + separator + steps.level + separator + steps.pos + separator + steps.compId
        }
        previousSteps.trimStart(separator)

        // Si no está activado, elimino los parámetros
        if (!f.isEnabled) {
            for (param in f.ownParameters) {
                // "Eliminando parámetros antiguos de la colección: previousSteps, param.key, separator
                val paramName = (previousSteps + param.paramName + separator).trimStart(separator)
                allParameters.remove(allParameters.first { it.paramName == paramName })
            }
            return
        }

        // Agrego los parámetros a la colección de parámetros
        for (param in f.ownParameters) {
            // Si no existe, lo agrego, sino actualizó su valor
            val paramName = (previousSteps + param.paramName + separator).trimStart(separator)

            if (allParameters.firstOrNull { it.paramName == paramName } == null) {
                // "Agregando parámetros a la colección: {0}. Valor: {1}", paramName, param.paramValue
                allParameters.add(Parameter(paramName, param.paramValue))
            } else {
                //"Actualizando parámetros de la colección: {0}. Nuevo valor: {1}", paramName, param.paramValue
                allParameters.first { it.paramName == paramName }.paramValue = param.paramValue
            }
        }
    }

    private fun addExternalParametersToFragment(f: GeneralFragment) {
        // Formato de la parte evaluable de los parámetros:
        // 1.2.1400000042
        // El resto, a la izquierda, es la ruta por la que se llegó a ese parámetro

        // Tomo los pasos previos
        var previousSteps = ""
        for (steps in stepsHistory) {
            previousSteps =
                previousSteps + separator + steps.level + separator + steps.pos + separator + steps.compId
        }
        previousSteps.trimStart(separator)

        // Elimino los parámetros viejos
        f.clearExternalParameters()

        for (param in allParameters) {
            // Quedarse con la parte real del parámetro
            val paramSplit = param.paramName.split(separator)
            var realParameter = ""
            var index = 0
            for (s in paramSplit.reversed()) {
                if (index > 3) {
                    break
                }

                realParameter = s + separator + realParameter
                index++
            }

            realParameter.trimEnd(separator)
            val pathParameter = param.paramName.replace(realParameter, "").trimEnd(separator)
            if (pathParameter == previousSteps && param.paramValue != null) {
                //"Agregando parámetros externos al control: {0}. Valor: {1}", realParameter, param.Value
                f.addExternalParameter(Parameter(realParameter, param.paramValue))
            }
        }
    }

    private fun evaluateFragment(generalFragment: GeneralFragment) {
        val level = generalFragment.level
        val position = generalFragment.position
        val attrCompId = generalFragment.attrCompId

        isEvaluating = true
        Log.d(
            this::class.java.simpleName,
            "${getString(R.string.evaluating_data)} L:$level/P:$position/A:$attrCompId"
        )

        var f: GeneralFragment? = null
        for (fr in allFrags) {
            if (fr.level == level && fr.position == position) {
                f = fr
                break
            }
        }

        try {
            if (f == null) {
                return
            }

            val dcrCont = f.dataCollectionRuleContent ?: return
            val result = f.evaluate()

            if (!f.isEnabled && dcrCont.mandatory || f.valueStr == null && dcrCont.mandatory) {
                // No puede seguir, valor obligatorio
                val str = "${getString(R.string.mandatory_value_for)} ${dcrCont.description}"
                showMessage(str, ERROR)
                return
            }

            if (result != null) {
                val res: Int = if (result == true) {
                    dcrCont.trueResult
                } else {
                    dcrCont.falseResult
                }

                when {
                    res == DcrResult.cont.id -> {
                        cont(f)
                    }

                    res == DcrResult.noContinue.id -> {
                        showMessage(
                            "${getString(R.string.value_does_not_allow_to_continue_for)} ${dcrCont.description}",
                            ERROR
                        )
                    }

                    res == DcrResult.end.id -> {
                        saveDataCollection()
                    }

                    res > 0 -> {
                        // Agrego el paso a la colección de pasos
                        stepsHistory.add(
                            KeyLevelPos(
                                level = dcrCont.level,
                                pos = dcrCont.position,
                                compId = dcrCont.attributeCompositionId
                            )
                        )
                        fillControl(res, 1)
                    }

                    res == DcrResult.levelX.id -> {
                        levelX(f, dcrCont)
                    }

                    res == DcrResult.back.id -> {
                        goBack()
                    }
                }
            } else {
                when {
                    dcrCont.trueResult == DcrResult.cont.id && dcrCont.falseResult == DcrResult.cont.id -> {
                        // En caso de que tanto trueResult como falseResult sean 0
                        cont(f)
                    }

                    else -> {
                        // No puede seguir
                        showMessage(
                            "${getString(R.string.invalid_value_for)} ${dcrCont.description}",
                            ERROR
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            isEvaluating = false
        }
    }

    private fun saveDataCollection() {
        showMessage(getString(R.string.saving_data_collection), SnackBarType.INFO)

        for (targetControl in attrFrags) {
            saveFragValues(targetControl)
        }

        ///////////////////////////////////////////
        ///////////// DATA COLLECTION /////////////
        val collectionRepository = DataCollectionRepository()

        val routeProcessId = routeProcessId
        var dc: DataCollection? = null
        val rpc = rpc
        val targetAsset = targetAsset
        val targetWarehouseArea = targetWarehouseArea

        when {
            rpc != null && routeProcessId != null -> {
                dc = collectionRepository.insert(
                    rpc = rpc,
                    dateStart = Date(),
                    routeProcessId = routeProcessId
                )
            }

            targetAsset != null -> {
                dc = collectionRepository.insert(
                    asset = targetAsset,
                    dateStart = Date()
                )
            }

            targetWarehouseArea != null -> {
                dc = collectionRepository.insert(
                    warehouseArea = targetWarehouseArea,
                    dateStart = Date()
                )
            }
        }

        if (dc == null) {
            showMessage(
                getString(R.string.error_saving_data_collection), ERROR
            )
            return
        }
        ///////////////////////////////////////////

        val total = dcContArray.size
        var qty = 0

        ///////////////////////////////////////////
        ///////// DATA COLLECTION CONTENT /////////
        var error = false

        val contentRepository = DataCollectionContentRepository()
        for (dcc in dcContArray) {
            qty++
            Log.d(
                this::class.java.simpleName,
                "${getString(R.string.saving_data_collection)} $qty/$total"
            )

            if (!contentRepository.insert(dc.id, dcc)) {
                error = true
                break
            }
        }
        ///////////////////////////////////////////

        /////////////// ImageControl //////////////
        imageControlFragment?.saveImages(false)
        ///////////////////////////////////////////

        if (!error) {
            allFrags.clear()
            attrFrags.clear()

            val data = Intent()
            data.putExtra("dataCollection", Parcels.wrap(dc))
            setResult(RESULT_OK, data)
            finish()
        } else {
            showMessage(
                getString(R.string.failed_to_save_the_data_collection_contents),
                ERROR
            )
        }
    }

    private fun cont(tempControl: GeneralFragment) {
        // CONTINUAR - No hay evaluación
        // Si es un atributo, debe pasar al siguiente atributo de ese nivel
        if (tempControl.isAttribute) {
            // ¿Hay otro atributo en el mismo nivel?
            var f: GeneralFragment? = null
            for (fr in allFrags) {
                // Estamos buscando el siguiente atributo en el mismo nivel...
                // La siguiente posición es igual a la posición que estamos analizando
                // más el largo del atributo con sus composiciones.

                if (fr.isAttribute && fr.level == tempControl.level && fr.position == tempControl.position + attrFrags.size) {
                    f = fr
                    break
                }
            }

            // Si no existe
            if (f == null) {
                // Si está en un nivel mayor a 1, debe volver.
                if (tempControl.level > 1) {
                    goBack()
                    return
                }

                // FIN DEL PROCESO
                saveDataCollection()
                return
            }

            // LLENAR CON EL SIGUIENTE ATRIBUTO
            isEvaluating = false
            fillControl(f.level, f.position)
        } else {
            // Es una composición, debe seguir con la siguiente composición
            // del mismo atributo (siguiente posición), si existe
            var f: GeneralFragment? = null
            for (fr in allFrags) {
                if (!fr.isAttribute && fr.level == tempControl.level && fr.position == tempControl.position + 1) {
                    f = fr
                    break
                }
            }

            // No hay más composiciones en este atributo
            if (f == null) {
                // Debe seguir con el siguiente atributo si existe.
                isEvaluating = false
                nextAttribute() // Botón continuar...
            } else {
                // Existe otra composición, salto al siguiente panel
                // Como viene de ser procesado y devolver CONTINUE, no hay que
                // volver a evaluar la composición actual y saltar a la siguiente.
                nextComposition(true)
            }
        }
    }

    private fun levelX(tempControl: GeneralFragment, z: DataCollectionRuleContent) {
        val eval = tempControl.evaluate()
        if (eval is String) {
            val f = eval.split(',')
            try {
                if (levelsToNavigate.isEmpty()) {
                    levelsToNavigate.clear()
                }

                for (h in f) {
                    levelsToNavigate.add(h.toInt())
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                return
            }

            if (levelsToNavigate.isNotEmpty()) {
                // Agrego el paso a la colección de pasos
                stepsHistory.add(
                    KeyLevelPos(
                        level = tempControl.level,
                        pos = tempControl.position,
                        compId = tempControl.attrCompId
                    )
                )

                fillControl(levelsToNavigate[0], 1)
                levelsToNavigate.remove(levelsToNavigate[0])
            }
        } else if (eval is Int) {
            when {
                eval > 0 -> {
                    // Agrego el paso a la colección de pasos
                    stepsHistory.add(
                        KeyLevelPos(
                            level = tempControl.level,
                            pos = tempControl.position,
                            compId = tempControl.attrCompId
                        )
                    )

                    fillControl(eval, 1)
                }

                eval == DcrResult.cont.id -> {
                    cont(tempControl)
                }

                eval == DcrResult.back.id -> {
                    goBack()
                }

                eval == DcrResult.end.id -> {
                    saveDataCollection()
                }

                eval == DcrResult.noContinue.id -> {
                    showMessage(
                        "${getString(R.string.value_does_not_allow_to_continue_for)} ${z.description}",
                        ERROR
                    )
                }
            }
        }
    }

    private fun goBack() {
        if (levelsToNavigate.isNotEmpty()) {
            // Agrego el paso a la colección de pasos
            var attrCompId = 0L
            for (drcCont in dcrContArray) {
                if (drcCont.level == levelsToNavigate[0] && drcCont.position == 1) {
                    attrCompId = drcCont.attributeCompositionId
                    break
                }
            }

            stepsHistory.add(KeyLevelPos(levelsToNavigate[0], 1, attrCompId))

            fillControl(levelsToNavigate[0], 1)
            levelsToNavigate.remove(levelsToNavigate[0])
        } else {
            // Tomo el último paso
            var tempStep: KeyLevelPos? = null
            if (stepsHistory.isNotEmpty()) {
                tempStep = stepsHistory.last()
            }

            if (tempStep == null) {
                return
            }

            // Busco el control desde donde vino
            var prevControl: GeneralFragment? = null
            for (fr in allFrags) {
                if (fr.level == tempStep.level && fr.position == tempStep.pos) {
                    prevControl = fr
                }
            }

            // ¡¿Qué pasó?!
            if (prevControl == null) {
                showMessage(
                    getString(R.string.the_previous_control_is_not_found),
                    ERROR
                )
                return
            }

            // Elimino el último paso de la colección de pasos
            stepsHistory.remove(stepsHistory.last())

            // Si es un atributo tengo que buscar el siguiente atributo del mismo nivel
            // La posición es la inmediata superior.
            if (prevControl.isAttribute) {
                var f: GeneralFragment? = null
                for (fr in allFrags) {
                    if (fr.isAttribute && fr.level == prevControl.level && fr.position > prevControl.position) {
                        f = fr
                    }
                }

                // No existe
                if (f == null) {
                    showMessage(
                        getString(R.string.the_previous_control_is_not_found),
                        ERROR
                    )
                    return
                }

                fillControl(f.level, f.position)
            } else {
                // Es una composición, entonces tomo la siguiente posición
                var f: GeneralFragment? = null
                for (fr in allFrags) {
                    if (fr.level == prevControl.level && fr.position == prevControl.position && fr.attrCompId > prevControl.attrCompId) {
                        f = fr
                    }
                }

                // No existe
                if (f == null) {
                    // No hay más composiciones, si está en el primer nivel
                    if (prevControl.level == 1) {
                        // FIN DEL PROCESO
                        saveDataCollection()
                        return
                    }
                    // VOLVER
                    goBack()
                    return
                }

                // Es el atributo siguiente, lleno el control
                if (f.isAttribute) {
                    fillControl(f.level, f.position)
                    return
                }

                evaluateFragment(f)
            }
        }
    }

    private fun cancelDcc() {
        try {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_the_data_collection))
            alert.setMessage(getString(R.string.you_want_to_cancel_the_current_data_collection))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)
                setResult(RESULT_CANCELED)
                finish()
            }
            alert.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            ScannerManager.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return svm.showScannedCode
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) showMessage(scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, false)
    }

    public override fun onResume() {
        super.onResume()

        setImageControlFragment()
    }

    private fun saveFragStatus() {
        currentFragment?.saveLastValue()
        val allFragData = allFrags.map { it.getFragmentData() }.toList()
        FragmentDataRepository().insert(allFragData)
    }

    override fun onFragmentStarted() {}

    override fun onFragmentDestroy() {}

    override fun onFragmentOk() {
        binding.nextButton.performClick()
    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen && panelBottomIsExpanded) {
            binding.expandHistoricPanelButton?.performClick()
        }
    }

    private fun demo() {
        if (!Statics.DEMO_MODE) {
            return
        }

        val fragment = currentFragment ?: return
        if (fragment.isAttribute) return

        when (fragment.attributeCompositionType) {
            AttributeCompositionType.TypeBool -> {
                val random: Int = ThreadLocalRandom.current().nextInt(0, 1)
                fragment.valueStr = (random > 0).toString()
            }

            AttributeCompositionType.TypeDate -> {
                val calendar = Calendar.getInstance()

                calendar[Calendar.YEAR] = 2021
                calendar[Calendar.MONTH] = 1
                calendar[Calendar.DATE] = 1
                val startDate = calendar.time.time

                calendar[Calendar.YEAR] = 2022
                calendar[Calendar.MONTH] = 1
                calendar[Calendar.DATE] = 1
                val endDate = calendar.time.time

                val random: Long = ThreadLocalRandom.current().nextLong(startDate, endDate)
                val date = Date(random)
                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                val valueStr = sdf.format(date.time)
                fragment.valueStr = valueStr
            }

            AttributeCompositionType.TypeTime -> {
                val calendar = Calendar.getInstance()

                calendar[Calendar.HOUR] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                val startTime = calendar.time.time

                calendar[Calendar.HOUR] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                val endTime = calendar.time.time

                val random: Long = ThreadLocalRandom.current().nextLong(startTime, endTime)
                val time = Time(random)
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val valueStr = sdf.format(time.time)
                fragment.valueStr = valueStr
            }

            AttributeCompositionType.TypeIntNumber,
            AttributeCompositionType.TypeDecimalNumber,
            AttributeCompositionType.TypeCurrency,
                -> {
                val random: Int = ThreadLocalRandom.current().nextInt(0, 100)
                fragment.valueStr = random.toString()
            }

            AttributeCompositionType.TypeTextLong,
            AttributeCompositionType.TypeTextShort,
                -> {
                fragment.valueStr = "Texto de prueba"
            }

            AttributeCompositionType.TypeOptions -> {
                val attrComp = AttributeCompositionRepository().selectById(fragment.attrCompId)
                if (attrComp != null) {
                    var composition = ""
                    if (attrComp.composition != null) {
                        composition = attrComp.composition.trim().trimEnd(';')
                    }
                    val allOptions = ArrayList(composition.split(';')).sorted()
                    val random: Int = ThreadLocalRandom.current().nextInt(0, allOptions.size)

                    fragment.valueStr = allOptions[random]
                }
            }
        }

        val randomPhoto: Int = ThreadLocalRandom.current().nextInt(0, 9)
        if (randomPhoto == 0) {
            runBlocking {
                addDemoPhoto(
                    onSnackBarEvent = {
                        showMessage(it.text, SnackBarType.getById(it.snackBarType.snackBarTypeId))
                    },
                    onFinished = {
                        Log.i(this::class.java.simpleName, "Imagen de Demostración guardada OK.")
                        runOnUiThread { next() }
                    },
                    onCrash = {
                        Log.e(this::class.java.simpleName, "Error al guardar imagen de Demostración.")
                        runOnUiThread { next() }
                    }
                )
            }
        } else {
            next()
        }
    }

    private fun addDemoPhoto(
        onSnackBarEvent: (SnackBarEventData) -> Unit,
        onFinished: () -> Unit,
        onCrash: () -> Unit
    ) {
        if (imageControlFragment == null) return

        val programId = imageControlFragment?.programId ?: return
        val objectId1 = imageControlFragment?.objectId1 ?: return
        val objectId2 = imageControlFragment?.objectId2 ?: ""
        val tableId = imageControlFragment?.tableId ?: return

        val dcrDescription = dcr?.description ?: ""
        val rpcAsset = rpc?.assetStr ?: ""
        val rpcWa = rpc?.warehouseAreaStr ?: ""
        val rpcW = rpc?.warehouseStr
        val rpcAssetCode = rpc?.assetCode ?: ""

        Log.d(this::class.java.simpleName, "Guardando imagen de Demostración...")

        val image: Bitmap = generateRandomBitmap(svm.maxHeightOrWidth, svm.maxHeightOrWidth)

        val photoFilePath = ImageCoroutines().addJpgPhotoToGallery(
            context = applicationContext,
            activity = this,
            image = image
        )

        val reference = "${getString(R.string.asset)}: $rpcAssetCode"
        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        var description = "${dcrDescription}, ${
            when {
                rpcAsset.isNotEmpty() -> rpcAsset
                rpcWa.isNotEmpty() -> rpcWa
                !rpcW.isNullOrEmpty() -> rpcW
                else -> getString(R.string.no_name)
            }
        }"

        val tableDescription = Table.routeProcess.description
        description = "$tableDescription: $description".take(255)

        ImageCoroutines().savePhotoInDb(
            context = applicationContext,
            photoFilePath = photoFilePath,
            programId = programId,
            programObjectId = tableId,
            objectId1 = objectId1,
            objectId2 = objectId2,
            description = description,
            reference = reference,
            obs = obs,
            onSnackBarEvent = onSnackBarEvent,
            onFinished = onFinished,
            onCrash = onCrash
        )
    }

    private fun generateRandomBitmap(width: Int, height: Int): Bitmap {
        val gridSize = 3
        val cellSize = width / gridSize // Tamaño de cada celda en el Bitmap
        val bitmap = createBitmap(width, height)
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val color = generateRandomColor()
                for (x in col * cellSize until (col + 1) * cellSize) {
                    for (y in row * cellSize until (row + 1) * cellSize) {
                        bitmap[x, y] = color
                    }
                }
            }
        }
        return bitmap
    }

    private fun generateRandomColor(): Int {
        val red = (0..255).random()
        val green = (0..255).random()
        val blue = (0..255).random()
        return Color.rgb(red, green, blue)
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
        if (svm.rfidShowConnectedMessage) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    showMessage(
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    showMessage(
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    showMessage(
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

    // endregion READERS Reception

    private fun isBackPressed() {
        cancelDcc()
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

    private fun showMessage(msg: String, type: SnackBarType) {
        if (isFinishing || isDestroyed) return
        if (type == ERROR) logError(msg)
        showMessage(msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)
}