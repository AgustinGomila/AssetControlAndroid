package com.dacosys.assetControl.ui.adapters.review

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
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Agustin on 18/01/2017.
 */

@Suppress("SpellCheckingInspection")
class AssetReviewAdapter :
    ArrayAdapter<AssetReview>,
    Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    private var lastSelectedPos = -1

    private var multiSelect: Boolean = false

    private var visibleStatus: ArrayList<AssetReviewStatus> = ArrayList()
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    private var assetReviewArray: ArrayList<AssetReview> = ArrayList()
    private var suggestedList: ArrayList<AssetReview> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        assetReviews: ArrayList<AssetReview>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<Long>,
        visibleStatus: ArrayList<AssetReviewStatus>,

        ) : super(AssetControlApp.getContext(), resource, assetReviews) {
        this.activity = activity
        this.resource = resource
        this.visibleStatus = visibleStatus
        this.checkedIdArray = checkedIdArray

        this.multiSelect = multiSelect

        this.listView = listView
        this.assetReviewArray = assetReviews
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

    override fun add(assetReview: AssetReview?) {
        if (assetReview != null) {
            if (!getAll().contains(assetReview)) {
                activity.runOnUiThread {
                    super.add(assetReview)
                }

                reportAssetReviewAdded(arrayListOf(assetReview))
            }
        }

        // Refrescar las vistas
        refresh()
    }

    fun add(assetReviews: ArrayList<AssetReview>) {
        val assetReviewsAdded: ArrayList<AssetReview> = ArrayList()

        activity.runOnUiThread {
            for (w in assetReviews) {
                if (!getAll().contains(w)) {
                    assetReviewsAdded.add(w)
                    super.add(w)
                }
            }

            if (assetReviewsAdded.size > 0) {
                reportAssetReviewAdded(assetReviewsAdded)
            }
        }

        // Refrescar las vistas
        refresh()
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
            dataSetChangedListener?.onDataSetChanged()
        }
    }

    override fun remove(assetReview: AssetReview?) {
        if (assetReview != null) {
            remove(arrayListOf(assetReview))
        }
    }

    var suspendReport = false

    fun remove(assetReviews: ArrayList<AssetReview>) {
        val assetReviewsRemoved: ArrayList<AssetReview> = ArrayList()
        activity.runOnUiThread {
            for (w in assetReviews) {
                if (getAll().contains(w)) {
                    assetReviewsRemoved.add(w)
                    checkedIdArray.remove(w.id)
                    super.remove(w)
                }
            }

            if (assetReviewsRemoved.size > 0) {
                reportAssetReviewRemoved(assetReviewsRemoved)
            }
        }

        refresh()
    }

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportAssetReviewAdded(assetReviewArray: ArrayList<AssetReview>) {
        if (suspendReport) {
            return
        }
        if (assetReviewArray.size <= 0) {
            return
        }

        var res = ""
        for (assetReview in assetReviewArray) {
            res += "${assetReview.id}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (assetReviewArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.added_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackBarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportAssetReviewRemoved(assetReviewArray: ArrayList<AssetReview>) {
        if (suspendReport) {
            return
        }
        if (assetReviewArray.size <= 0) {
            return
        }

        var res = ""
        for (assetReview in assetReviewArray) {
            res += "${assetReview.warehouseAreaStr} (${assetReview.assetReviewDate})"
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (assetReviewArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.removed_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackBarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    private fun getIndex(assetReview: AssetReview): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as AssetReview)
            if (t == assetReview) {
                return i
            }
        }
        return -1
    }

    val itemCount: Int
        get() {
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

    fun getAll(): ArrayList<AssetReview> {
        val r: ArrayList<AssetReview> = ArrayList()
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
    fun setChecked(items: ArrayList<AssetReview>, isChecked: Boolean) {
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

    fun setChecked(item: AssetReview, isChecked: Boolean, suspendRefresh: Boolean = false) {
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

    fun setChecked(checkedItems: ArrayList<AssetReview>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    private fun clearChecked() {
        checkedIdArray.clear()
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
        dataSetChangedListener?.onDataSetChanged()
    }

    fun setSelectItemAndScrollPos(assetReview: AssetReview?, tScrollPos: Int?) {
        var pos = -1
        if (assetReview != null) pos = getPosition(assetReview)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(assetReview: AssetReview?) {
        var pos = -1
        if (assetReview != null) pos = getPosition(assetReview)
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

    fun currentAssetReview(): AssetReview? {
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

    fun getItems(): ArrayList<AssetReview> {
        val r: ArrayList<AssetReview> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun isStatusVisible(position: Int): Boolean {
        if (position < 0) return false
        return isStatusVisible(getItem(position))
    }

    fun isStatusVisible(ar: AssetReview?): Boolean {
        return if (ar != null) {
            (visibleStatus.contains(AssetReviewStatus.getById(ar.statusId)))
        } else false
    }

    fun getVisibleStatus(): ArrayList<AssetReviewStatus> {
        return visibleStatus
    }

    fun setVisibleStatus(status: ArrayList<AssetReviewStatus>) {
        visibleStatus = status
        refresh()
    }

    fun addVisibleStatus(status: AssetReviewStatus) {
        if (!visibleStatus.contains(status)) {
            visibleStatus.add(status)
            refresh()
        }
    }

    fun removeVisibleStatus(status: AssetReviewStatus) {
        if (visibleStatus.contains(status)) {
            visibleStatus.remove(status)
            refresh()
        }
    }

    fun selectNearVisible() {
        val currentPos = currentPos()
        var newPos = -1

        val allItems = getAll()
        if (allItems.size > 0) {
            // Buscar el siguiente visible
            for (i in currentPos until allItems.size) {
                if (isStatusVisible(i)) {
                    newPos = i
                    break
                }
            }

            // No encontró otro visible hacia adelante...
            // ir hacia atrás
            if (newPos == -1) {
                // Buscar el anterior visible
                for (i in currentPos downTo 1) {
                    if (isStatusVisible(i)) {
                        newPos = i
                        break
                    }
                }
            }
        }

        selectItem(newPos)
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
                    isStatusVisible(position) -> R.layout.asset_review_row
                    else -> R.layout.null_row
                }
            } else when {
                // Estamos trabajando en un ListView
                !isStatusVisible(position) -> R.layout.null_row
                else -> R.layout.asset_review_row
            }

        if (v == null || v.tag == null) {
            // El view todavía no fue creado, crearlo con el layout correspondiente.
            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)

            alreadyExists = false
        } else if (
            v.tag is String && currentLayout == R.layout.asset_review_row ||
            v.tag is ViewHolder && currentLayout != R.layout.asset_review_row
        ) {
            // Row null cambiando...

            // Ya fue creado, si es un row normal que está siendo seleccionada
            // o un row expandido que está siendo des seleccionado
            // debe cambiar de layout, por lo tanto, volver a crearse.

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
        holder.wStrTextView = v.findViewById(R.id.warehouseStr)
        holder.waStrTextView = v.findViewById(R.id.warehouseAreaStr)
        holder.assetReviewDateTextView = v.findViewById(R.id.asset_review_date)
        holder.modificationDateTextView = v.findViewById(R.id.modification_date)
        holder.userNameTextView = v.findViewById(R.id.user_str)
        holder.statusTextView = v.findViewById(R.id.status_str)
        holder.obsTextView = v.findViewById(R.id.obs)
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
            val assetReview = getItem(position)

            if (assetReview != null) {
                holder.wStrTextView?.text = assetReview.warehouseStr
                holder.waStrTextView?.text = assetReview.warehouseAreaStr
                holder.assetReviewDateTextView?.text = assetReview.assetReviewDate.toString()
                holder.modificationDateTextView?.text = assetReview.modificationDate.toString()
                holder.userNameTextView?.text = assetReview.userStr
                holder.statusTextView?.text = AssetReviewStatus.getById(assetReview.statusId).description
                holder.obsTextView?.text = assetReview.obs.orEmpty()
                if (assetReview.obs.orEmpty().isEmpty()) {
                    holder.obsTextView?.visibility = GONE
                } else {
                    holder.obsTextView?.visibility = VISIBLE
                }

                if (holder.checkBox != null) {
                    //Important to remove previous checkedChangedListener before calling setChecked
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked =
                        checkedIdArray.contains(assetReview.id)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnClickListener { }
                    holder.checkBox!!.setOnCheckedChangeListener { _, isChecked ->
                        this.setChecked(assetReview, isChecked)
                    }
                }

                // Background layouts
                val layoutTransferred = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutOnProcess = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_blue,
                    null
                )
                val layoutCompleted = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
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
                when (assetReview.statusId) {
                    AssetReviewStatus.transferred.id -> {
                        backColor = layoutTransferred!!
                        foreColor = white
                        titleForeColor = lightgray
                    }

                    AssetReviewStatus.onProcess.id -> {
                        backColor = layoutOnProcess!!
                        foreColor = white
                        titleForeColor = lightgray
                    }

                    AssetReviewStatus.completed.id -> {
                        backColor = layoutCompleted!!
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
                holder.wStrTextView?.setTextColor(foreColor)
                holder.waStrTextView?.setTextColor(foreColor)
                holder.assetReviewDateTextView?.setTextColor(foreColor)
                holder.modificationDateTextView?.setTextColor(foreColor)
                holder.userNameTextView?.setTextColor(foreColor)
                holder.statusTextView?.setTextColor(foreColor)
                holder.obsTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
            }

            if (listView != null) {
                if (isSelected(position)) {
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<AssetReview> = ArrayList()

                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    var filterableItem: AssetReview

                    for (i in 0 until assetReviewArray.size) {
                        filterableItem = assetReviewArray[i]
                        if (filterableItem.obs.orEmpty().lowercase(Locale.getDefault())
                                .contains(filterString)
                        ) {
                            r.add(filterableItem)
                        }
                    }
                }

                results.values = r
                results.count = r.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                suggestedList.addAll(results?.values as ArrayList<AssetReview>)
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
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

    class ViewHolder {
        var wStrTextView: CheckedTextView? = null
        var waStrTextView: CheckedTextView? = null
        var assetReviewDateTextView: CheckedTextView? = null
        var modificationDateTextView: CheckedTextView? = null
        var userNameTextView: CheckedTextView? = null
        var statusTextView: CheckedTextView? = null
        var obsTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    companion object {

        fun defaultRowHeight(): Int {
            return 157
        }
    }
}