package com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
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
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.routes.routeProcessStatus.`object`.RouteProcessStatus
import com.dacosys.assetControl.utils.Statics.Companion.getColorWithAlpha
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import java.lang.ref.WeakReference

/**
 * Created by Agustin on 18/01/2017.
 */

class RouteProcessContentAdapter :
    ArrayAdapter<RouteProcessContent>,
    Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    private var lastSelectedPos = -1
    private var multiSelect: Boolean = false

    private var visibleStatus: ArrayList<RouteProcessStatus> = ArrayList()
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    private var routeProcessContentArray: ArrayList<RouteProcessContent> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        routeProcessContents: ArrayList<RouteProcessContent>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<Long>,
        visibleStatus: ArrayList<RouteProcessStatus>,
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,

        ) : super(AssetControlApp.getContext(), resource, routeProcessContents) {
        this.activity = activity
        this.resource = resource
        this.visibleStatus = visibleStatus
        this.checkedIdArray = checkedIdArray

        this.multiSelect = multiSelect

        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener

        this.listView = listView
        this.routeProcessContentArray = routeProcessContents
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    interface DataSetChangedListener {
        // Define data you like to return from AysncTask
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        // Define data you like to return from AysncTask
        fun onCheckedChanged(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(routeProcessContent: RouteProcessContent?) {
        if (routeProcessContent != null) {
            if (!getAll().contains(routeProcessContent)) {
                activity.runOnUiThread {
                    super.add(routeProcessContent)
                }

                reportRouteProcessContentAdded(arrayListOf(routeProcessContent))
            }
        }

        // Refrescar las vistas
        refresh()
    }

    fun add(routeProcessContents: ArrayList<RouteProcessContent>) {
        val routeProcessContentsAdded: ArrayList<RouteProcessContent> = ArrayList()

        activity.runOnUiThread {
            for (w in routeProcessContents) {
                if (!getAll().contains(w)) {
                    routeProcessContentsAdded.add(w)
                    super.add(w)
                }
            }

            if (routeProcessContentsAdded.size > 0) {
                reportRouteProcessContentAdded(routeProcessContentsAdded)
            }
        }

        // Refrescar las vistas
        refresh()
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    override fun remove(routeProcessContent: RouteProcessContent?) {
        if (routeProcessContent != null) {
            remove(arrayListOf(routeProcessContent))
        }
    }

    var suspendReport = false

    fun remove(routeProcessContents: ArrayList<RouteProcessContent>) {
        val routeProcessContentsRemoved: ArrayList<RouteProcessContent> = ArrayList()
        activity.runOnUiThread {
            for (w in routeProcessContents) {
                if (getAll().contains(w)) {
                    routeProcessContentsRemoved.add(w)
                    checkedIdArray.remove(w.routeProcessContentId)
                    super.remove(w)
                }
            }

            if (routeProcessContentsRemoved.size > 0) {
                reportRouteProcessContentRemoved(routeProcessContentsRemoved)
            }
        }

        refresh()
    }

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportRouteProcessContentAdded(routeProcessContentArray: ArrayList<RouteProcessContent>) {
        if (suspendReport) {
            return
        }
        if (routeProcessContentArray.size <= 0) {
            return
        }

        var res = ""
        for (routeProcessContent in routeProcessContentArray) {
            res += "${routeProcessContent.routeProcessContentId}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (routeProcessContentArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.added_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackBarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportRouteProcessContentRemoved(routeProcessContentArray: ArrayList<RouteProcessContent>) {
        if (suspendReport) {
            return
        }
        if (routeProcessContentArray.size <= 0) {
            return
        }

        var res = ""
        for (routeProcessContent in routeProcessContentArray) {
            res += "${routeProcessContent.routeProcessContentId}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (routeProcessContentArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.removed_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackBarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    private fun getIndex(routeProcessContent: RouteProcessContent): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as RouteProcessContent)
            if (t == routeProcessContent) {
                return i
            }
        }
        return -1
    }

    fun count(): Int {
        return count
    }

    fun countVisible(): Int {
        var visibleItems = 0
        for (i in 0 until count) {
            if (isStatusVisible(i)) {
                visibleItems++
            }
        }
        return visibleItems
    }

    fun getAll(): ArrayList<RouteProcessContent> {
        val r: ArrayList<RouteProcessContent> = ArrayList()
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
    fun setChecked(items: ArrayList<RouteProcessContent>, isChecked: Boolean) {
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

    fun setChecked(item: RouteProcessContent, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val position = getIndex(item)
        if (isChecked) {
            if (!checkedIdArray.contains(item.routeProcessContentId)) {
                checkedIdArray.add(item.routeProcessContentId)
            }
        } else {
            checkedIdArray.remove(item.routeProcessContentId)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<RouteProcessContent>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    fun clearChecked() {
        checkedIdArray.clear()
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
        dataSetChangedListener?.onDataSetChanged()
    }

    fun setSelectItemAndScrollPos(rpc: RouteProcessContent?, tScrollPos: Int?) {
        var pos = -1
        if (rpc != null) pos = getPosition(rpc)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectFirstNotProcessed() {
        for (i in 0 until count) {
            val t = (getItem(i) as RouteProcessContent)
            if (t.routeProcessStatusId == RouteProcessStatus.notProcessed.id) {
                selectItem(getPosition(t))
                break
            }
        }
    }

    fun selectItem(rpc: RouteProcessContent?) {
        var pos = -1
        if (rpc != null) pos = getPosition(rpc)
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

        // Deseleccionar cuando:
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

            if (z.routeProcessStatusId == RouteProcessStatus.processed.id ||
                z.routeProcessStatusId == RouteProcessStatus.skipped.id
            ) {
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

            if (z.routeProcessStatusId == RouteProcessStatus.processed.id ||
                z.routeProcessStatusId == RouteProcessStatus.skipped.id
            ) {
                nextIndex++
                continue
            }

            // Seleccionar la fila correcta
            selectItem(z)
            break
        }
    }

    fun currentLevel(): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as RouteProcessContent)
            return t.level
        }
        return 0
    }

    fun currentRouteProcessContent(): RouteProcessContent? {
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

    fun isStatusVisible(position: Int): Boolean {
        if (position < 0) return false
        return isStatusVisible(getItem(position))
    }

    fun isStatusVisible(rpc: RouteProcessContent?): Boolean {
        return if (rpc != null) {
            (visibleStatus.contains(RouteProcessStatus.getById(rpc.routeProcessStatusId)))
        } else false
    }

    fun getVisibleStatus(): ArrayList<RouteProcessStatus> {
        return visibleStatus
    }

    fun setVisibleStatus(status: ArrayList<RouteProcessStatus>) {
        visibleStatus = status
        refresh()
    }

    fun addVisibleStatus(status: RouteProcessStatus) {
        if (!visibleStatus.contains(status)) {
            visibleStatus.add(status)
            refresh()
        }
    }

    fun removeVisibleStatus(status: RouteProcessStatus) {
        if (visibleStatus.contains(status)) {
            visibleStatus.remove(status)
            refresh()
        }
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
                when {
                    isStatusVisible(position) -> R.layout.route_process_content_row
                    else -> R.layout.null_row
                }
            } else when {
                // Estamos trabajando en un ListView
                !isStatusVisible(position) -> R.layout.null_row
                else -> R.layout.route_process_content_row
            }

        if (v == null || v.tag == null) {
            // El view todavía no fue creado, crearlo con el layout correspondiente.
            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)

            alreadyExists = false
        } else if (
            v.tag is String && currentLayout == R.layout.route_process_content_row ||
            v.tag is ViewHolder && currentLayout != R.layout.route_process_content_row
        ) {
            // Row null cambiando...

            // Ya fue creado, si es un row normal que está siendo seleccionada
            // o un row expandido que está siendo deseleccionado
            // debe cambiar de layout, por lo tanto volver a crearse.

            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)

            alreadyExists = false
        }

        v = when (currentLayout) {
            R.layout.null_row -> fillNullView(v!!)
            else -> fillListView(position, v!!, alreadyExists)
        }
        return v
    }

    private fun fillNullView(v: View): View {
        v.tag = ""
        return v
    }

    private fun createViewHolder(v: View, holder: ViewHolder) {
        // Holder para los rows expandidos
        holder.statusTextView = v.findViewById(R.id.routeProcessStatusStr)
        holder.assetTextView = v.findViewById(R.id.assetStr)
        holder.codeTextView = v.findViewById(R.id.assetCode)
        holder.warehouseAreaTextView = v.findViewById(R.id.warehouseAreaStr)
        holder.warehouseTextView = v.findViewById(R.id.warehouseStr)
        holder.checkBox = v.findViewById(R.id.checkBox)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    @SuppressLint("ClickableViewAccessibility", "ObsoleteSdkInt")
    private fun fillListView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ViewHolder()
        if (alreadyExists) {
            if (v.tag is ViewHolder) {
                createViewHolder(v, holder)
            } else {
                holder = v.tag as ViewHolder
            }
        } else {
            createViewHolder(v, holder)
        }

        if (position >= 0) {
            val routeProcessContent = getItem(position)

            if (routeProcessContent != null) {
                holder.statusTextView?.text = routeProcessContent.routeProcessStatusStr
                holder.assetTextView?.text = routeProcessContent.assetStr
                holder.codeTextView?.text = routeProcessContent.assetCode
                holder.warehouseAreaTextView?.text = routeProcessContent.warehouseAreaStr
                holder.warehouseTextView?.text = routeProcessContent.warehouseStr

                if ((routeProcessContent.warehouseAreaStr == null ||
                            routeProcessContent.warehouseAreaStr!!.isEmpty()) &&
                    (routeProcessContent.warehouseStr == null ||
                            routeProcessContent.warehouseStr!!.isEmpty())
                ) {
                    holder.warehouseAreaTextView?.visibility = GONE
                    holder.warehouseTextView?.visibility = GONE
                } else {
                    holder.warehouseAreaTextView?.visibility = VISIBLE
                    holder.warehouseTextView?.visibility = VISIBLE
                }

                if (holder.checkBox != null) {
                    //Important to remove previous checkedChangedListener before calling setChecked
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked =
                        checkedIdArray.contains(routeProcessContent.routeProcessContentId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnClickListener { }
                    holder.checkBox!!.setOnCheckedChangeListener { _, isChecked ->
                        this.setChecked(routeProcessContent, isChecked)
                    }
                }

                // Background layouts
                val layoutProcessed = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutSkipped = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )
                val layoutNotProcessed = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_blue,
                    null
                )
                val layoutDefault = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border,
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

                val backColor: Drawable
                val foreColor: Int
                val titleForeColor: Int
                val isSelected = isSelected(position)
                when (routeProcessContent.routeProcessStatusId) {
                    RouteProcessStatus.processed.id -> {
                        backColor = layoutProcessed!!
                        foreColor = white
                        titleForeColor = lightgray
                    }
                    RouteProcessStatus.skipped.id -> {
                        backColor = layoutSkipped!!
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
                    RouteProcessStatus.notProcessed.id -> {
                        backColor = layoutNotProcessed!!
                        foreColor = white
                        titleForeColor = lightgray
                    }
                    else -> {
                        backColor = layoutDefault!!
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

                v.background = backColor
                holder.statusTextView?.setTextColor(foreColor)
                holder.assetTextView?.setTextColor(foreColor)
                holder.codeTextView?.setTextColor(foreColor)
                holder.warehouseAreaTextView?.setTextColor(foreColor)
                holder.warehouseTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
            }

            if (listView!!.isItemChecked(position)) {
                v.background.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
                        BlendModeCompat.MODULATE
                    )
            } else {
                v.background.colorFilter = null
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    internal class ViewHolder {
        var statusTextView: CheckedTextView? = null
        var assetTextView: CheckedTextView? = null
        var codeTextView: CheckedTextView? = null
        var warehouseAreaTextView: CheckedTextView? = null
        var warehouseTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }
}