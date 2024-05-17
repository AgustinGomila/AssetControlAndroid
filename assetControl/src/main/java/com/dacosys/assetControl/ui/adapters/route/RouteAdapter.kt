package com.dacosys.assetControl.ui.adapters.route

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.entity.route.Route
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.isTablet
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Agustin on 18/01/2017.
 */

class RouteAdapter : ArrayAdapter<Route>, Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    private var multiSelect: Boolean = false
    private var lastSelectedPos = -1

    var suspendReport = false

    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    private var fullList: ArrayList<Route> = ArrayList()
    private var suggestedList: ArrayList<Route> = ArrayList()

    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var routeIdOnProcess: ArrayList<Long> = ArrayList()
    private var routeIdToSend: ArrayList<Long> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        routes: ArrayList<Route>,
        routeIdOnProcess: ArrayList<Long>,
        routeIdToSend: ArrayList<Long>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<Long>,
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) : super(AssetControlApp.getContext(), resource, routes) {
        this.activity = activity
        this.resource = resource
        this.multiSelect = multiSelect

        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener

        this.routeIdOnProcess = routeIdOnProcess
        this.routeIdToSend = routeIdToSend
        this.checkedIdArray = checkedIdArray

        this.listView = listView
        this.fullList = routes
    }

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        routes: ArrayList<Route>,
        suggestedList: ArrayList<Route>,
    ) : super(AssetControlApp.getContext(), resource, suggestedList) {
        this.activity = activity
        this.resource = resource
        this.fullList = routes
        this.suggestedList = suggestedList
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener? = null,
        dataSetChangedListener: DataSetChangedListener? = null,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    interface DataSetChangedListener {
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        fun onCheckedChanged(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(route: Route?) {
        if (route != null) {
            if (!getAll().contains(route)) {
                activity.runOnUiThread {
                    super.add(route)
                }

                reportRouteAdded(arrayListOf(route))
            }
        }
    }

    fun add(routes: ArrayList<Route>) {
        val routesToAdd: ArrayList<Route> = ArrayList()

        activity.runOnUiThread {
            for (w in routes) {
                if (!getAll().contains(w)) {
                    routesToAdd.add(w)
                    super.add(w)
                }
            }

            if (routesToAdd.size > 0) {
                reportRouteAdded(routesToAdd)
            }
        }
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
            dataSetChangedListener?.onDataSetChanged()
        }
    }

    override fun remove(route: Route?) {
        if (route != null) {
            remove(arrayListOf(route))
        }
    }

    fun remove(routes: ArrayList<Route>) {
        lastSelectedPos = currentPos()

        val routesRemoved: ArrayList<Route> = ArrayList()
        activity.runOnUiThread {
            for (w in routes) {
                if (getAll().contains(w)) {
                    routesRemoved.add(w)
                    checkedIdArray.remove(w.id)
                    super.remove(w)
                }
            }

            if (routesRemoved.size > 0) {
                reportRouteRemoved(routesRemoved)
            }
        }

        activity.runOnUiThread { sort(customComparator) }

        refresh()

        activity.runOnUiThread {
            listView?.smoothScrollToPosition(lastSelectedPos)
        }
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
        dataSetChangedListener?.onDataSetChanged()
    }

    fun setSelectItemAndScrollPos(r: Route?, tScrollPos: Int?) {
        var pos = -1
        if (r != null) pos = getPosition(r)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(r: Route?) {
        var pos = -1
        if (r != null) pos = getPosition(r)
        selectItem(pos)
    }

    fun selectItem(pos: Int) {
        selectItem(pos, pos, true)
    }

    private fun selectItem(pos: Int, scrollPos: Int, smoothScroll: Boolean) {
        if (listView == null) {
            return
        }

        listView?.clearChoices()

        // Quitar selección cuando:
        //   - Estaba previamente seleccionado
        //   - La posición es negativa
        //   - La cantidad de ítems es cero o menos

        activity.runOnUiThread {
            if (pos == lastSelectedPos || pos < 0 || count <= 0) {
                listView?.setItemChecked(-1, true)
                listView?.setSelection(-1)
            } else {
                listView?.setItemChecked(pos, true)
                listView?.setSelection(pos)
            }
        }

        lastSelectedPos = currentPos()

        refresh()

        activity.runOnUiThread {
            if (smoothScroll) {
                listView?.smoothScrollToPosition(scrollPos)
            } else {
                listView?.setSelection(scrollPos)
            }
        }
    }

    fun selectPrev() {
        if (listView == null) {
            return
        }

        val pos = currentPos()
        listView?.clearChoices()

        var prevIndex = if (pos == 0) count - 1 else pos - 1

        while (true) {
            if (prevIndex == pos) {
                break
            }

            if (prevIndex == 0) {
                prevIndex = count - 1
                continue
            }

            val z = getItem(prevIndex)
            if (z == null) {
                prevIndex--
                continue
            }

            // Seleccionar la fila correcta
            selectItem(z)
            break
        }
    }

    fun selectNext() {
        if (listView == null) {
            return
        }

        val pos = currentPos()
        listView?.clearChoices()

        var nextIndex = if (count == pos + 1) 0 else pos + 1

        while (true) {
            if (nextIndex == pos) {
                break
            }

            if (nextIndex == count) {
                nextIndex = 0
                continue
            }

            val z = getItem(nextIndex)
            if (z == null) {
                nextIndex++
                continue
            }

            // Seleccionar la fila correcta
            selectItem(z)
            break
        }
    }

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportRouteAdded(routes: ArrayList<Route>) {
        if (suspendReport) {
            return
        }
        if (routes.size <= 0) {
            return
        }

        var res = ""
        for (route in routes) {
            res += "${route.description}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (routes.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.added_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackBarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportRouteRemoved(routes: ArrayList<Route>) {
        // En modo arqueo no se muestran los carteles de eliminación de routes
        // porque nunca se eliminan, en cambio, se ponen en cero
        if (suspendReport) {
            return
        }
        if (routes.size <= 0) {
            return
        }

        var res = ""
        for (route in routes) {
            res += "${route.description}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (routes.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.removed_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackBarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    fun currentRoute(): Route? {
        return (0 until count)
            .firstOrNull { isSelected(it) }
            ?.let {
                val t = getItem(it)
                t
            }
    }

    fun currentPos(): Int {
        return (0 until count)
            .firstOrNull { isSelected(it) } ?: -1
    }

    fun firstVisiblePos(): Int {
        var pos = (listView ?: return -1).firstVisiblePosition
        if ((listView ?: return -1).childCount > 1 && (listView
                ?: return -1).getChildAt(0).top < 0
        ) pos++
        return pos
    }

    private fun isSelected(position: Int): Boolean {
        return position >= 0 && listView?.isItemChecked(position) ?: false
    }

    private fun getIndex(`object`: Route): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as Route)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    val itemCount: Int
        get() {
            return count
        }

    fun maxHeightNeeded(): Int {
        return itemCount * defaultDropDownItemHeight()
    }

    fun getAllId(): ArrayList<Long> {
        val r: ArrayList<Long> = ArrayList()
        for (i in 0 until count) {
            val it = getItem(i)
            if (it != null) {
                r.add(it.id)
            }
        }
        return r
    }

    fun getAll(): ArrayList<Route> {
        val r: ArrayList<Route> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun countChecked(): Int {
        return checkedIdArray.size
    }

    fun getAllChecked(): ArrayList<Long> {
        return checkedIdArray
    }

    private var isFilling = false
    fun setChecked(items: ArrayList<Route>, isChecked: Boolean) {
        if (isFilling) {
            return
        }
        isFilling = true

        for (i in items) {
            setChecked(i, isChecked)
        }

        isFilling = false
        refresh()
    }

    fun setChecked(item: Route, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val position = getIndex(item)
        if (isChecked) {
            if (!checkedIdArray.contains(item.id)) {
                checkedIdArray.add(item.id)
            }
        } else {
            checkedIdArray.remove(item.id)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<Route>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    private fun clearChecked() {
        checkedIdArray.clear()
    }

    private var weakRefListView: WeakReference<ListView?>? = null
        set(newValue) {
            field = newValue
            val l = listView
            if (l != null) {
                activity.runOnUiThread {
                    l.adapter = this
                }

                l.setOnItemClickListener { _, _, position, _ ->
                    selectItem(position)
                }
            }
        }

    var listView: ListView?
        get() {
            return weakRefListView?.get()
        }
        set(newValue) {
            if (newValue == null) {
                return
            }
            weakRefListView = WeakReference(newValue)
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        // Seleccionamos el layout dependiendo si es
        // un row visible u oculto según su AsseStatus.

        val currentLayout: Int =
            if (listView == null) {
                // Estamos trabajando en un Dropdown
                R.layout.route_dropdown_row
            } else when {
                // Estamos trabajando en un ListView
                isSelected(position) -> R.layout.route_row_expanded
                else -> R.layout.route_row
            }

        if (v == null || v.tag == null) {
            // El view todavía no fue creado, crearlo con el layout correspondiente.
            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)

            alreadyExists = false
        } else {
            // El view ya existe, comprobar que no necesite cambiar de layout.
            if (
            // Row null cambiando...
                v.tag is String && currentLayout == R.layout.route_row ||
                v.tag is String && currentLayout == R.layout.route_row_expanded ||
                v.tag is String && currentLayout == R.layout.route_dropdown_row ||

                v.tag is ItemListViewHolder && currentLayout != R.layout.route_row ||
                v.tag is ExpandedViewHolder && currentLayout != R.layout.route_row_expanded ||
                v.tag is ItemListViewHolder && currentLayout != R.layout.route_dropdown_row
            ) {
                // Ya fue creado, si es un row normal que está siendo seleccionada
                // o un row expandido que está siendo des seleccionado
                // debe cambiar de layout, por lo tanto, volver a crearse.
                val vi = LayoutInflater.from(context)
                v = vi.inflate(currentLayout, parent, false)

                alreadyExists = false
            }
        }

        v = when (currentLayout) {
            R.layout.route_dropdown_row -> fillSimpleView(position, v!!, alreadyExists)
            R.layout.route_row_expanded -> fillExpandedItemView(position, v!!, alreadyExists)
            else -> fillSimpleView(position, v!!, alreadyExists)
        }
        return v
    }

    @SuppressLint("ClickableVieroutecessibility", "ClickableViewAccessibility")
    private fun fillExpandedItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ExpandedViewHolder()
        if (alreadyExists) {
            holder = v.tag as ExpandedViewHolder
        } else {
            holder.descriptionTextView = v.findViewById(R.id.routeStr)
            holder.checkBox = v.findViewById(R.id.checkBox)
            holder.statusStrTextView = v.findViewById(R.id.routeStatus)

            if (multiSelect) {
                holder.checkBox?.visibility = VISIBLE
            } else {
                holder.checkBox?.visibility = GONE
            }

            v.tag = holder
        }

        if (position >= 0) {
            val route = getItem(position)

            if (route != null) {
                val onProcess = routeIdOnProcess.contains(route.id)
                val toSend = routeIdToSend.contains(route.id)
                holder.descriptionTextView?.text = route.description

                val str = when {
                    onProcess -> context.getString(R.string.on_process)
                    toSend -> context.getString(R.string.to_send)
                    else -> context.getString(R.string.unstarted)
                }
                holder.statusStrTextView?.text = str

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(route, isChecked, true)
                        }

                    val pressHoldListener =
                        View.OnLongClickListener { // Do something when your hold starts here.
                            isSpeakButtonLongPressed = true
                            true
                        }

                    val pressTouchListener = View.OnTouchListener { pView, pEvent ->
                        pView.onTouchEvent(pEvent)
                        // We're only interested in when the button is released.
                        if (pEvent.action == MotionEvent.ACTION_UP) {
                            // We're only interested in anything if our speak button is currently pressed.
                            if (isSpeakButtonLongPressed) {
                                // Do something when the button is released.
                                if (!isFilling) {
                                    holder.checkBox!!.setOnCheckedChangeListener(null)
                                    val newState = !holder.checkBox!!.isChecked
                                    this.setChecked(getAll(), newState)
                                }
                                isSpeakButtonLongPressed = false
                            }
                        }
                        return@OnTouchListener true
                    }

                    //Important to remove previous checkedChangedListener before calling setChecked
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(route.id)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background layouts
                val layoutOnProcess = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_blue,
                    null
                )

                // Background layouts
                val layoutToSend = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )

                // Font colors
                val white =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_light,
                        null
                    )
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_dark,
                        null
                    )

                // CheckBox color
                val darkslategray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkslategray,
                        null
                    )
                val lightgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.lightgray,
                        null
                    )
                val whitesmoke =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.whitesmoke,
                        null
                    )

                val foreColor: Int
                val titleForeColor: Int
                val isSelected = isSelected(position)
                when {
                    route.active != 1 -> {
                        v.setBackgroundColor(lightgray)
                        foreColor = if (isSelected) {
                            white
                        } else {
                            black
                        }
                        titleForeColor = if (isSelected) {
                            lightgray
                        } else {
                            darkslategray
                        }
                    }

                    onProcess -> {
                        v.background = layoutOnProcess!!
                        foreColor = white
                        titleForeColor = lightgray
                    }

                    toSend -> {
                        v.background = layoutToSend!!
                        foreColor = if (isSelected) white else black
                        titleForeColor = lightgray
                    }

                    else -> {
                        v.setBackgroundColor(whitesmoke)
                        foreColor = if (isSelected) {
                            white
                        } else {
                            black
                        }
                        titleForeColor = if (isSelected) {
                            lightgray
                        } else {
                            darkslategray
                        }
                    }
                }
                holder.descriptionTextView?.setTextColor(foreColor)
                holder.statusStrTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(
                                colorId = R.color.lightslategray,
                                alpha = 240
                            ),
                            BlendModeCompat.MODULATE
                        )
                } else {
                    v.background.colorFilter = null
                }
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    @SuppressLint("ClickableVieroutecessibility", "ClickableViewAccessibility")
    private fun fillSimpleView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ItemListViewHolder()
        if (alreadyExists) {
            holder = v.tag as ItemListViewHolder
        } else {
            holder.descriptionTextView = v.findViewById(R.id.routeStr)
            holder.checkBox = v.findViewById(R.id.checkBox)

            if (multiSelect) {
                holder.checkBox?.visibility = VISIBLE
            } else {
                holder.checkBox?.visibility = GONE
            }

            v.tag = holder
        }

        if (position >= 0) {
            val route = getItem(position)

            if (route != null) {
                val onProcess = routeIdOnProcess.contains(route.id)
                val toSend = routeIdToSend.contains(route.id)
                holder.descriptionTextView?.text = route.description

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(route, isChecked, true)
                        }

                    val pressHoldListener =
                        View.OnLongClickListener { // Do something when your hold starts here.
                            isSpeakButtonLongPressed = true
                            true
                        }

                    val pressTouchListener = View.OnTouchListener { pView, pEvent ->
                        pView.onTouchEvent(pEvent)
                        // We're only interested in when the button is released.
                        if (pEvent.action == MotionEvent.ACTION_UP) {
                            // We're only interested in anything if our speak button is currently pressed.
                            if (isSpeakButtonLongPressed) {
                                // Do something when the button is released.
                                if (!isFilling) {
                                    holder.checkBox!!.setOnCheckedChangeListener(null)
                                    val newState = !holder.checkBox!!.isChecked
                                    this.setChecked(getAll(), newState)
                                }
                                isSpeakButtonLongPressed = false
                            }
                        }
                        return@OnTouchListener true
                    }

                    //Important to remove previous checkedChangedListener before calling setChecked
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(route.id)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                if (resource == R.layout.route_dropdown_row) {
                    // Background colors
                    val lightgray =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.lightgray,
                            null
                        )
                    val whitesmoke =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.whitesmoke,
                            null
                        )

                    // Font colors
                    val black =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.text_dark,
                            null
                        )
                    val dimgray =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.dimgray,
                            null
                        )

                    when {
                        route.active != 1 -> {
                            v.setBackgroundColor(lightgray)
                            holder.descriptionTextView?.setTextColor(dimgray)

                        }

                        else -> {
                            v.setBackgroundColor(whitesmoke)
                            holder.descriptionTextView?.setTextColor(black)

                        }
                    }
                } else {
                    // Background layouts
                    val layoutOnProcess = ResourcesCompat.getDrawable(
                        AssetControlApp.getContext().resources,
                        R.drawable.layout_thin_border_blue,
                        null
                    )

                    // Background layouts
                    val layoutToSend = ResourcesCompat.getDrawable(
                        AssetControlApp.getContext().resources,
                        R.drawable.layout_thin_border_yellow,
                        null
                    )

                    // Font colors
                    val white =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.text_light,
                            null
                        )
                    val black =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.text_dark,
                            null
                        )

                    // CheckBox color
                    val darkslategray =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.darkslategray,
                            null
                        )
                    val lightgray =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.lightgray,
                            null
                        )
                    val whitesmoke =
                        ResourcesCompat.getColor(
                            AssetControlApp.getContext().resources,
                            R.color.whitesmoke,
                            null
                        )

                    val foreColor: Int
                    val titleForeColor: Int
                    val isSelected = isSelected(position)
                    when {
                        route.active != 1 -> {
                            v.setBackgroundColor(lightgray)
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                            titleForeColor = if (isSelected) {
                                lightgray
                            } else {
                                darkslategray
                            }
                        }

                        onProcess -> {
                            v.background = layoutOnProcess!!
                            foreColor = white
                            titleForeColor = lightgray
                        }

                        toSend -> {
                            v.background = layoutToSend!!
                            foreColor = if (isSelected) white else black
                            titleForeColor = lightgray
                        }

                        else -> {
                            v.setBackgroundColor(whitesmoke)
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                            titleForeColor = if (isSelected) {
                                lightgray
                            } else {
                                darkslategray
                            }
                        }
                    }
                    holder.descriptionTextView?.setTextColor(foreColor)
                    holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
                }
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(
                                colorId = R.color.lightslategray,
                                alpha = 240
                            ),
                            BlendModeCompat.MODULATE
                        )
                } else {
                    v.background.colorFilter = null
                }
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    internal class ItemListViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    internal class ExpandedViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var statusStrTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    override fun sort(comparator: Comparator<in Route>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: Route?, o2: Route? ->
            RouteComparator().compareNullable(
                o1,
                o2
            )
        }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<Route> = ArrayList()
                var filterString = ""

                if (constraint != null) {
                    filterString = constraint.toString().lowercase(Locale.getDefault())
                    var filterableIc: Route

                    for (i in 0 until fullList.size) {
                        filterableIc = fullList[i]
                        if (filterableIc.description.contains(filterString, true)) {
                            r.add(filterableIc)
                        }
                    }
                }

                Collections.sort(r, RouteComparator(filterString))
                results.values = r
                results.count = r.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                if (results != null && results.count > 0) {
                    suggestedList.addAll(results.values as ArrayList<Route>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    companion object {

        fun defaultRowHeight(): Int {
            return if (isTablet()) 33 else 54
        }

        fun defaultDropDownItemHeight(): Int {
            return if (isTablet()) 45 else 90
        }

        class RouteComparator(private val priorityText: String) : Comparator<Route> {
            constructor() : this("")

            fun compareNullable(o1: Route?, o2: Route?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: Route, o2: Route): Int {
                return try {
                    val firstField: Int

                    val fieldValue1: String = o1.description
                    val fieldValue2: String = o2.description

                    firstField = if (priorityText.isNotEmpty()) {
                        if (fieldValue2.startsWith(priorityText, true) &&
                            !fieldValue1.startsWith(priorityText, true)
                        ) (return 1)
                        else if (fieldValue1.startsWith(priorityText, true) &&
                            !fieldValue2.startsWith(priorityText, true)
                        ) (return -1)
                        else if (fieldValue1.startsWith(priorityText, true) &&
                            fieldValue2.startsWith(priorityText, true)
                        ) (return fieldValue1.compareTo(fieldValue2))
                        else (fieldValue1.compareTo(fieldValue2))
                    } else {
                        fieldValue1.compareTo(fieldValue2)
                    }

                    return firstField
                } catch (ex: Exception) {
                    0
                }
            }
        }
    }
}