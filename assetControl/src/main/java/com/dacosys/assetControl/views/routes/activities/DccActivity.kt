package com.dacosys.assetControl.views.routes.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.databinding.DataCollectionContentActivityBinding
import com.dacosys.assetControl.utils.UTCDataTime
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.`object`.AttributeComposition
import com.dacosys.assetControl.model.assets.attributes.attributeComposition.dbHelper.AttributeCompositionDbHelper
import com.dacosys.assetControl.model.assets.attributes.attributeCompositionType.AttributeCompositionType
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.assets.units.unitType.UnitType
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.permissions.PermissionEntry
import com.dacosys.assetControl.model.routes.commons.KeyLevelPos
import com.dacosys.assetControl.model.routes.commons.Parameter
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.`object`.DataCollection
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.`object`.DataCollectionContent
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.FragmentDataDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRule.`object`.DataCollectionRule
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.`object`.DataCollectionRuleContent
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleContent.dbHelper.DataCollectionRuleContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleResult.DcrResult
import com.dacosys.assetControl.model.routes.routeProcess.`object`.RouteProcess
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.routes.fragment.*
import com.dacosys.imageControl.fragments.ImageControlButtonsFragment
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.parceler.Parcels
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom


class DccActivity : AppCompatActivity(),
    Scanner.ScannerListener,
    CommaSeparatedSpinnerFragment.OnItemSelectedListener,
    UnitTypeSpinnerFragment.OnItemSelectedListener,
    DccFragmentListener,
    Rfid.RfidDeviceListener,
    KeyboardVisibilityEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        Statics.closeKeyboard(this)
        imageControlFragment?.onDestroy()
        imageControlFragment = null
        historicDataFragment?.onDestroy()
        historicDataFragment = null
    }

    override fun onItemSelected(strOption: String?) {
    }

    override fun onItemSelected(unitType: UnitType?) {
    }

    private var dataStart: String = ""

    private var rpc: RouteProcessContent? = null
    private var collectorRouteProcessId: Long? = null
    private var dcr: DataCollectionRule? = null
    private var dcrContArray: ArrayList<DataCollectionRuleContent> = ArrayList()
    private var dcContArray: ArrayList<DataCollectionContent> = ArrayList()

    /////////////////////////////////////////////////////////////////////////////////
    // TARGETS: Sólo cuando se ingresa a una recolección de datos de forma directa //
    // sin que sea parte de un ruta. A través del botón Recolección de datos del   //
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
            (0 until view.childCount).map { view.getChildAt(it) }.forEach { setupUI(it) }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putInt("level", currentLevel)
        b.putInt("pos", currentPos)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            b,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment)

        if (historicDataFragment is HistoricDataFragment) supportFragmentManager.putFragment(b,
            "historicDataFragment",
            historicDataFragment as HistoricDataFragment)

        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        b.putParcelable("dcr", dcr)
        b.putParcelable("rpc", rpc)

        b.putBoolean("fragCollectionCreated", fragCollectionCreated)

        saveFragStatus()
    }

    private fun loadBundleValuesFromExtras(b: Bundle) {
        val tempRpc = Parcels.unwrap<RouteProcessContent>(b.getParcelable("routeProcessContent"))
        if (tempRpc != null) setRouteProcessContent(tempRpc)

        val tempDcr = Parcels.unwrap<DataCollectionRule>(b.getParcelable("dataCollectionRule"))
        if (tempDcr != null) dcr = tempDcr

        val tempAsset = Parcels.unwrap<Asset>(b.getParcelable("asset"))
        if (tempAsset != null) targetAsset = tempAsset

        val tempWarehouseArea = Parcels.unwrap<WarehouseArea>(b.getParcelable("warehouseArea"))
        if (tempWarehouseArea != null) targetWarehouseArea = tempWarehouseArea

        val tempItemCategory = Parcels.unwrap<ItemCategory>(b.getParcelable("itemCategory"))
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

        rpc = b.getParcelable("rpc")
        if (rpc != null) setRouteProcessContent(rpc ?: return)

        fragCollectionCreated = b.getBoolean("fragCollectionCreated")
    }

    private lateinit var binding: DataCollectionContentActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = DataCollectionContentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.data_collection)

        historicDataFragment =
            supportFragmentManager.findFragmentById(binding.historicDataFragment.id) as HistoricDataFragment?
        dataStart = UTCDataTime.getUTCDateTimeAsString()

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

        when {
            targetWarehouseArea != null -> makeText(binding.root,
                (targetWarehouseArea ?: return).description,
                SnackbarType.INFO)
            targetItemCategory != null -> makeText(binding.root,
                (targetItemCategory ?: return).description,
                SnackbarType.INFO)
            targetAsset != null -> makeText(binding.root,
                (targetAsset ?: return).description,
                SnackbarType.INFO)
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

        // Llenar la grilla
        setPanels()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
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
        dcr = DataCollectionRule(id = (rpc ?: return).dataCollectionRuleId, doChecks = false)

        val rp = RouteProcess(id = (rpc ?: return).routeProcessId, doChecks = false)
        collectorRouteProcessId = rp.collectorRouteProcessId

        dcrContArray =
            ArrayList(DataCollectionRuleContentDbHelper().selectByDataCollectionRuleIdActive((dcr
                ?: return).dataCollectionRuleId)
                .sortedWith(compareBy({ it.level }, { it.position })))
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
        var description = "${(dcr ?: return).description}, ${
            if ((rpc ?: return).assetStr != null && ((rpc ?: return).assetStr ?: return).isNotEmpty()) (rpc ?: return).assetStr
            else if ((rpc ?: return).warehouseAreaStr != null && ((rpc ?: return).warehouseAreaStr ?: return).isNotEmpty()) (rpc ?: return).warehouseAreaStr
            else if ((rpc ?: return).warehouseStr != null && ((rpc ?: return).warehouseStr ?: return).isNotEmpty()) (rpc ?: return).warehouseStr
            else getString(R.string.no_name)
        }"

        val tableName = Table.routeProcess.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment =
                ImageControlButtonsFragment.newInstance(Table.routeProcess.tableId,
                    (rpc ?: return).routeProcessId,
                    null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            val fm = supportFragmentManager

            if (!isFinishing) runOnUiThread {
                fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                        binding.imageControlFragment.id,
                        imageControlFragment ?: return@runOnUiThread).commit()

                if (!Statics.prefsGetBoolean(Preference.useImageControl)) {
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
            imageControlFragment?.setTableId(Table.routeProcess.tableId)
            imageControlFragment?.setObjectId1((rpc ?: return).routeProcessId)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }
        }
    }

    override fun onBackPressed() {
        cancelDcc()
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
            allFrags = ArrayList(allFrags.sortedWith(compareBy({ it.level },
                { it.position },
                { it.attrCompId })))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun createFragmentCollection(fdArray: ArrayList<GeneralFragment.FragmentData>) {
        // Limpiar toda la colección de controles
        allFrags.clear()

        // Construyo un control por cada contenido de la regla
        for (fd in fdArray) {
            if (fd.dcrContId == null) continue
            val dcrCont = DataCollectionRuleContentDbHelper().selectById(fd.dcrContId) ?: continue

            try {
                val y = GeneralFragment()
                y.refreshListeners(this)
                y.dataCollectionRuleContent = dcrCont

                val tempAttrCompTypeId = fd.attrCompTypeId ?: 0
                if (tempAttrCompTypeId > 0) {
                    val attrCompType =
                        AttributeCompositionType.getById(tempAttrCompTypeId) ?: continue

                    y.attributeCompositionType = attrCompType
                    y.lastValue =
                        GeneralFragment.convertStringToTypedAttr(attrCompType, fd.valueStr)
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

        allFrags = ArrayList(allFrags.sortedWith(compareBy({ it.level },
            { it.position },
            { it.attrCompId })))
    }

    private fun createFragmentCollection() {
        // Limpiar toda la colección de controles
        allFrags.clear()

        // Construyo un control por cada contenido de la regla
        for (dcrCont in dcrContArray.sortedWith(compareBy({ it.level }, { it.position }))) {
            try {
                val y = GeneralFragment()
                y.refreshListeners(this)
                y.dataCollectionRuleContent = dcrCont

                val tempAttrCompId = dcrCont.attributeCompositionId
                if (tempAttrCompId > 0) {
                    val attrComp = AttributeComposition(id = tempAttrCompId, doChecks = false)
                    val attrCompType = attrComp.attributeCompositionType ?: continue

                    y.attributeCompositionType = attrCompType
                    y.lastValue = GeneralFragment.convertStringToTypedAttr(attrCompType,
                        attrComp.defaultValue)
                    y.valueStr = attrComp.defaultValue
                }
                y.isEnabled = true

                // Agrego el control a la colección
                allFrags.add(y)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
        }

        allFrags = ArrayList(allFrags.sortedWith(compareBy({ it.level },
            { it.position },
            { it.attrCompId })))
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
                val str = dcrCont.description + " - " + dcrCont.attributeStr

                runOnUiThread {
                    binding.attributeTextView.setText(str, TextView.BufferType.EDITABLE)
                }
            }
        }

        // Agrego la composición correspondiente al panel
        // Si no está especificada es la primer composición (!isAttribute),
        // sino busco el AttributeCompositionId correspondiente.
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

        // Del atributo en sí mismo no se registran datos pero puede contener una expresión condicional
        // que se evalúa luego de evaluar los datos registrados para las composiciones.

        attrFrags.clear()
        val r: ArrayList<GeneralFragment> = ArrayList()

        if (allFrags.size == 0) {
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
        if (isFinishing || isDestroyed) return

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
                if (!isFinishing) fragmentTransaction.remove(oldFragment).commitAllowingStateLoss()
            } catch (ex: java.lang.Exception) {
                Log.e(this.javaClass.simpleName, ex.message.toString())
            }
        }

        // Close keyboard in transition
        if (currentFocus != null) {
            val inputManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow((currentFocus ?: return).windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
        }

        if (newFragment != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.setCustomAnimations(R.anim.animation_fade_in,
                    R.anim.animation_fade_out)
                try {
                    if (!isFinishing) fragmentTransaction.replace(binding.fragmentLayout.id,
                        newFragment).commitAllowingStateLoss()
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

        if (Statics.demoMode) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
    }

    private fun fillPreviousData(f: GeneralFragment) {
        if (!User.hasPermission(PermissionEntry.ShowPreviousDataCollectionRegistry)) {
            return
        }

        var tempDcc: ArrayList<DataCollectionContent> = ArrayList()

        binding.enableCheckBox.isChecked = f.isEnabled

        if (rpc != null && f.dataCollectionRuleContent != null) {
            val dccDbHelper = DataCollectionContentDbHelper()
            val dcrCont = f.dataCollectionRuleContent ?: return
            when {
                (rpc ?: return).assetId != null && ((rpc ?: return).assetId ?: return) > 0 -> {
                    binding.codeTextView.setText((rpc ?: return).assetCode ?: "",
                        TextView.BufferType.EDITABLE)
                    binding.descriptionTextView.setText((rpc ?: return).assetStr ?: "",
                        TextView.BufferType.EDITABLE)

                    tempDcc = ArrayList(dccDbHelper.selectByDataCollectionRuleContentIdAssetId(
                        dcrCont.dataCollectionRuleContentId,
                        (rpc ?: return).assetId ?: return)
                        .sortedWith(compareBy { it.dataCollectionDate }).reversed())
                }
                (rpc ?: return).warehouseId != null && ((rpc ?: return).warehouseId
                    ?: return) > 0 -> {
                    binding.codeTextView.setText((rpc ?: return).warehouseId.toString(),
                        TextView.BufferType.EDITABLE)
                    binding.descriptionTextView.setText((rpc ?: return).warehouseStr ?: "",
                        TextView.BufferType.EDITABLE)

                    tempDcc = ArrayList(dccDbHelper.selectByDataCollectionRuleContentIdWarehouseId(
                        dcrCont.dataCollectionRuleContentId,
                        (rpc ?: return).warehouseId ?: return)
                        .sortedWith(compareBy { it.dataCollectionDate }).reversed())
                }
                (rpc ?: return).warehouseAreaId != null && ((rpc ?: return).warehouseAreaId
                    ?: return) > 0 -> {
                    binding.codeTextView.setText((rpc ?: return).warehouseAreaId.toString(),
                        TextView.BufferType.EDITABLE)
                    binding.descriptionTextView.setText((rpc ?: return).warehouseAreaStr ?: "",
                        TextView.BufferType.EDITABLE)

                    tempDcc =
                        ArrayList(dccDbHelper.selectByDataCollectionRuleContentIdWarehouseAreaId(
                            dcrCont.dataCollectionRuleContentId,
                            (rpc ?: return).warehouseAreaId ?: return)
                            .sortedWith(compareBy { it.dataCollectionDate }).reversed())
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

        val attrCompArray =
            AttributeCompositionDbHelper().selectByAttributeId((currentFragment?.dataCollectionRuleContent
                ?: return).attributeId)
        val currentAttrCompId =
            (currentFragment?.dataCollectionRuleContent ?: return).attributeCompositionId

        var currentIndex = 0
        for (attrComp in attrCompArray) {
            if (attrComp.attributeCompositionId == currentAttrCompId) {
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

        val attrCompArray =
            AttributeCompositionDbHelper().selectByAttributeId((currentFragment?.dataCollectionRuleContent
                ?: return).attributeId)
        val currentAttrCompId =
            (currentFragment?.dataCollectionRuleContent ?: return).attributeCompositionId

        var currentIndex = 0
        for (attrComp in attrCompArray) {
            if (attrComp.attributeCompositionId == currentAttrCompId) {
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
        if (attrFrags.size <= 0) {
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

        if (attrFrags.size <= 0) {
            return
        }

        // Evalúo el primer control
        val f = attrFrags[0]
        val z = f.dataCollectionRuleContent ?: return

        if (!f.isEnabled && z.mandatory || f.valueStr == null && z.mandatory) {
            // No puede seguir, valor obligatorio
            val str = "${getString(R.string.mandatory_value_for)} ${z.description}"
            makeText(binding.root, str, SnackbarType.ERROR)
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
            if (dc.collectorDataCollectionContentId.toString().startsWith(completePath)) {
                dataToRemove.add(dc)
            }
        }

        // Eliminando datos antiguos
        dcContArray.removeAll(dataToRemove.toSet())

        // Agrego el nuevo registro si el control está activo
        if (f.isEnabled) {
            // Agregando datos a la colección, ID virtual: completePath
            val dcc = DataCollectionContent(dcrc = f.dataCollectionRuleContent ?: return,
                virtualId = virtualId,
                result = result,
                valueStr = valueStr)

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
            val paramSplited = param.paramName.split(separator)
            var realParameter = ""
            var index = 0
            for (s in paramSplited.reversed()) {
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
        Log.d(this::class.java.simpleName,
            "${getString(R.string.evaluating_data)} L:$level/P:$position/A:$attrCompId")

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
                makeText(binding.root, str, SnackbarType.ERROR)
                return
            }

            if (result != null) {
                val res = if (result == true) {
                    dcrCont.trueResult
                } else {
                    dcrCont.falseResult
                }

                when {
                    res == DcrResult.cont.id -> {
                        cont(f)
                    }
                    res == DcrResult.noContinue.id -> {
                        makeText(binding.root,
                            "${getString(R.string.value_does_not_allow_to_continue_for)} ${dcrCont.description}",
                            SnackbarType.ERROR)
                    }
                    res == DcrResult.end.id -> {
                        saveDataCollection()
                    }
                    res > 0 -> {
                        // Agrego el paso a la colección de pasos
                        stepsHistory.add(KeyLevelPos(level = dcrCont.level,
                            pos = dcrCont.position,
                            compId = dcrCont.attributeCompositionId))
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
                        makeText(binding.root,
                            "${getString(R.string.invalid_value_for)} ${dcrCont.description}",
                            SnackbarType.ERROR)
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
        makeText(binding.root, getString(R.string.saving_data_collection), SnackbarType.INFO)

        for (targetControl in attrFrags) {
            saveFragValues(targetControl)
        }

        ///////////////////////////////////////////
        ///////////// DATA COLLECTION /////////////
        val dcDbHelper =
            com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionDbHelper()

        val r = ThreadLocalRandom.current()
        val fakeRouteProcessId = r.nextLong(-888888 - -999999)

        var dc: DataCollection? = null
        when {
            rpc != null -> dc =
                dcDbHelper.insert(rpc ?: return, dataStart, collectorRouteProcessId ?: return)
            targetAsset != null -> dc =
                dcDbHelper.insert(targetAsset ?: return, dataStart, fakeRouteProcessId)
            targetWarehouseArea != null -> dc =
                dcDbHelper.insert(targetWarehouseArea ?: return, dataStart, fakeRouteProcessId)
        }

        if (dc == null) {
            makeText(binding.root,
                getString(R.string.error_saving_data_collection),
                SnackbarType.ERROR)
            return
        }
        ///////////////////////////////////////////

        val total = dcContArray.size
        var qty = 0

        ///////////////////////////////////////////
        ///////// DATA COLLECTION CONTENT /////////
        var error = false

        val dccDbHelper = DataCollectionContentDbHelper()
        for (dcc in dcContArray) {
            qty++
            Log.d(this::class.java.simpleName,
                "${getString(R.string.saving_data_collection)} $qty/$total")

            if (!dccDbHelper.insert(dc.collectorDataCollectionId, dcc)) {
                error = true
                break
            }
        }
        ///////////////////////////////////////////

        ///////////////////////////////////////////
        ////////////// IMAGE CONTROL //////////////
        if (imageControlFragment != null) {
            imageControlFragment?.saveImages(false)
        }
        ///////////////////////////////////////////

        if (!error) {
            Statics.closeKeyboard(this)

            allFrags.clear()
            attrFrags.clear()

            val data = Intent()
            data.putExtra("dataCollection", Parcels.wrap(dc))
            setResult(RESULT_OK, data)
            finish()
        } else {
            makeText(binding.root,
                getString(R.string.failed_to_save_the_data_collection_contents),
                SnackbarType.ERROR)
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
            val f = eval.toString().split(',')
            try {
                if (levelsToNavigate.size < 1) {
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

            if (levelsToNavigate.size > 0) {
                // Agrego el paso a la colección de pasos
                stepsHistory.add(KeyLevelPos(level = tempControl.level,
                    pos = tempControl.position,
                    compId = tempControl.attrCompId))

                fillControl(levelsToNavigate[0], 1)
                levelsToNavigate.remove(levelsToNavigate[0])
            }
        } else if (eval is Int) {
            when {
                eval.toInt() > 0 -> {
                    // Agrego el paso a la colección de pasos
                    stepsHistory.add(KeyLevelPos(level = tempControl.level,
                        pos = tempControl.position,
                        compId = tempControl.attrCompId))

                    fillControl(eval.toInt(), 1)
                }
                eval.toInt() == DcrResult.cont.id -> {
                    cont(tempControl)
                }
                eval.toInt() == DcrResult.back.id -> {
                    goBack()
                }
                eval.toInt() == DcrResult.end.id -> {
                    saveDataCollection()
                }
                eval.toInt() == DcrResult.noContinue.id -> {
                    makeText(binding.root,
                        "${getString(R.string.value_does_not_allow_to_continue_for)} ${z.description}",
                        SnackbarType.ERROR)
                }
            }
        }
    }

    private fun goBack() {
        if (levelsToNavigate.size > 0) {
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
            if (stepsHistory.size > 0) {
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
                makeText(binding.root,
                    getString(R.string.the_previous_control_is_not_found),
                    SnackbarType.ERROR)
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
                    makeText(binding.root,
                        getString(R.string.the_previous_control_is_not_found),
                        SnackbarType.ERROR)
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
                Statics.closeKeyboard(this)

                setResult(RESULT_CANCELED, null)
                finish()
            }

            alert.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) JotterListener.onRequestPermissionsResult(
            this,
            requestCode,
            permissions,
            grantResults)
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)

        try {
            makeText(binding.root, getString(R.string.ok), SnackbarType.SUCCESS)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    public override fun onResume() {
        super.onResume()

        if (!fragCollectionCreated) {
            // Es una nueva sesión de recolección de datos
            createFragmentCollection()
            fragCollectionCreated = true
        } else {
            // Carga del estado del adaptador desde la DB temporal
            val allFragData = FragmentDataDbHelper().tempTableSelect()
            createFragmentCollection(allFragData)
        }

        fillControl(level = currentLevel, position = currentPos)

        // El Listener del currentFragment se destruye luego
        // del cambio de orientación de la pantalla y necesitamos
        // volver a activar/desactivar el fragmento.
        currentFragment?.setFragmentEnables(currentFragment?.isEnabled ?: false)

        setImageControlFragment()
    }

    private fun saveFragStatus() {
        currentFragment?.saveLastValue()
        val allFragData: ArrayList<GeneralFragment.FragmentData> =
            allFrags.mapTo(ArrayList()) { it.getFragmentData() }
        FragmentDataDbHelper().tempTableInsert(allFragData.toTypedArray())
    }

    override fun onFragmentStarted() {
        val f = currentFragment?.getFragment() ?: return
        Handler(Looper.getMainLooper()).postDelayed({
            if (f is StringFragment || f is DateFragment || f is TimeFragment || f is DecimalFragment) {
                if (panelBottomIsExpanded) {
                    binding.expandHistoricPanelButton?.performClick()
                }
                Statics.showKeyboard(this)
            } else {
                Statics.closeKeyboard(this)
            }
        }, 20)
    }

    override fun onFragmentDestroy() {
    }

    override fun onFragmentOk() {
        binding.nextButton.performClick()
    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen && panelBottomIsExpanded) {
            binding.expandHistoricPanelButton?.performClick()
        }
    }

    private fun demo() {
        if (!Statics.demoMode) {
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
                val attrComp = AttributeComposition(fragment.attrCompId, true)
                var composition = ""
                if (attrComp.composition != null) {
                    composition = (attrComp.composition ?: return).trim().trimEnd(';')
                }
                val allOptions = ArrayList(composition.split(';')).sorted()
                val random: Int = ThreadLocalRandom.current().nextInt(0, allOptions.size)

                fragment.valueStr = allOptions[random]
            }
        }

        next()
    }

// region READERS Reception

    override fun onNewIntent(intent: Intent) {
        /*
          This method gets called, when a new Intent gets associated with the current activity instance.
          Instead of creating a new activity, onNewIntent will be called. For more information have a look
          at the documentation.

          In our case this method gets called, when the user attaches a className to the device.
         */
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onReadCompleted(scanCode: String) {
        scannerCompleted(scanCode)
    }

//endregion READERS Reception

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