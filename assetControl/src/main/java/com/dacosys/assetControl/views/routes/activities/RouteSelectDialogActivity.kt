package com.dacosys.assetControl.views.routes.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.RouteSelectDialogActivityBinding
import com.dacosys.assetControl.model.routes.route.`object`.Route
import com.dacosys.assetControl.model.routes.route.`object`.Route.CREATOR.getAvailableRoutes
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteAdapter
import com.dacosys.assetControl.model.routes.route.dbHelper.RouteDbHelper
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.commons.views.ContractsAutoCompleteTextView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import kotlin.concurrent.thread


@Suppress("UNCHECKED_CAST")
class RouteSelectDialogActivity : AppCompatActivity(),
    ContractsAutoCompleteTextView.OnContractsAvailability, KeyboardVisibilityEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        Statics.closeKeyboard(this)
        binding.autoCompleteTextView.setOnContractsAvailability(null)
        binding.autoCompleteTextView.setAdapter(null)
    }

    private var routeDescription: String = ""

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        refreshFilterData()
        savedInstanceState.putString("routeDescription", routeDescription)
    }

    private lateinit var binding: RouteSelectDialogActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = RouteSelectDialogActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var tempTitle = getString(R.string.search_by_description)
        if (savedInstanceState != null) {
            // Dejo de escuchar estos eventos hasta pasar los valores guardados
            // más adelante se reconectan
            binding.autoCompleteTextView.setOnEditorActionListener(null)
            binding.autoCompleteTextView.onItemClickListener = null
            binding.autoCompleteTextView.onFocusChangeListener = null
            binding.autoCompleteTextView.setOnDismissListener(null)
            routeDescription = savedInstanceState.getString("routeDescription") ?: ""
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (t1 != null && t1.isNotEmpty()) {
                    tempTitle = t1
                }

                routeDescription = extras.getString("routeDescription") ?: ""
            }
        }

        title = tempTitle
        binding.routeDialogSelect.setOnClickListener { onBackPressed() }

        binding.clearImageView.setOnClickListener {
            routeDescription = ""
            refreshRouteText(cleanText = true, focus = true)
        }

        // region Setup CATEGORY_CATEGORY ID AUTOCOMPLETE
        // Set an route click checkedChangedListener for auto complete text view
        binding.autoCompleteTextView.threshold = 1
        binding.autoCompleteTextView.hint = tempTitle
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (binding.autoCompleteTextView.adapter != null && binding.autoCompleteTextView.adapter is RouteAdapter
                ) {
                    val it =
                        (binding.autoCompleteTextView.adapter as RouteAdapter).getItem(position)
                    if (it != null) {
                        routeDescription = it.description
                    }
                }
                itemSelected()
            }
        binding.autoCompleteTextView.setOnContractsAvailability(this)
        binding.autoCompleteTextView.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.autoCompleteTextView.text.trim().length >= binding.autoCompleteTextView.threshold &&
                    binding.autoCompleteTextView.adapter != null &&
                    (binding.autoCompleteTextView.adapter as RouteAdapter).count > 0 &&
                    !binding.autoCompleteTextView.isPopupShowing
                ) {
                    // Display the suggestion dropdown on focus
                    Handler(Looper.getMainLooper()).post {
                        run {
                            adjustAndShowDropDown()
                        }
                    }
                }
            }
        binding.autoCompleteTextView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                Statics.showKeyboard(this)
                adjustDropDownHeight()
                return@setOnTouchListener false
            } else if (motionEvent.action == MotionEvent.BUTTON_BACK) {
                Statics.closeKeyboard(this)

                setResult(RESULT_CANCELED, null)
                finish()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        binding.autoCompleteTextView.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (binding.autoCompleteTextView.text.trim().length >= binding.autoCompleteTextView.threshold) {
                    if (binding.autoCompleteTextView.adapter != null && binding.autoCompleteTextView.adapter is RouteAdapter) {
                        val all = (binding.autoCompleteTextView.adapter as RouteAdapter).getAll()
                        if (all.any()) {
                            var founded = false
                            for (a in all) {
                                if (a.description.startsWith(
                                        binding.autoCompleteTextView.text.toString().trim(),
                                        true
                                    )
                                ) {
                                    routeDescription = a.description
                                    founded = true
                                    break
                                }
                            }
                            if (!founded) {
                                for (a in all) {
                                    if (a.description.contains(
                                            binding.autoCompleteTextView.text.toString().trim(),
                                            true
                                        )
                                    ) {
                                        routeDescription = a.description
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
                itemSelected()
                true
            } else {
                false
            }
        }
        // endregion

        KeyboardVisibilityEvent.registerEventListener(this, this)
        refreshRouteText(cleanText = false, focus = true)
        thread { fillAdapter() }
    }

    private fun itemSelected() {
        Statics.closeKeyboard(this)
        refreshFilterData()

        val data = Intent()
        data.putExtra("routeDescription", routeDescription)
        setResult(RESULT_OK, data)
        finish()
    }

    private fun showProgressBar(visibility: Int) {
        runOnUiThread {
            binding.progressBar.visibility = visibility
        }
    }

    private fun refreshRouteText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.autoCompleteTextView.setText(
                if (routeDescription == "") {
                    if (cleanText) {
                        ""
                    } else {
                        binding.autoCompleteTextView.text.toString()
                    }
                } else {
                    routeDescription
                }
            )

            binding.autoCompleteTextView.post {
                binding.autoCompleteTextView.setSelection(
                    binding.autoCompleteTextView.length()
                )
            }

            if (focus) {
                binding.autoCompleteTextView.requestFocus()
            }
        }
    }

    var isFilling = false
    private fun fillAdapter() {
        if (isFilling) return
        isFilling = true

        val itemArray: ArrayList<Route> = ArrayList()
        try {
            Log.d(this::class.java.simpleName, "Selecting routes...")
            itemArray.addAll(getAvailableRoutes(RouteDbHelper().select(true)))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }

        val adapter = RouteAdapter(
            activity = this,
            resource = R.layout.route_dropdown_row,
            routes = itemArray,
            suggestedList = ArrayList()
        )

        runOnUiThread {
            showProgressBar(VISIBLE)

            binding.autoCompleteTextView.setAdapter(adapter)
            (binding.autoCompleteTextView.adapter as RouteAdapter).notifyDataSetChanged()

            while (binding.autoCompleteTextView.adapter == null) {
                // Wait for complete loaded
            }
            showProgressBar(GONE)
        }

        refreshRouteText(cleanText = false, focus = false)
        isFilling = false
    }

    private fun refreshFilterData() {
        routeDescription = binding.autoCompleteTextView.text.toString()
    }

    override fun onBackPressed() {
        Statics.closeKeyboard(this)

        setResult(RESULT_CANCELED)
        finish()
    }

    // region SOFT KEYBOARD AND DROPDOWN ISSUES
    override fun onVisibilityChanged(isOpen: Boolean) {
        adjustDropDownHeight()
    }

    private fun calculateDropDownHeight(): Int {
        val r = Rect()
        val mRootWindow = window
        val view: View = mRootWindow.decorView
        view.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    private var isAdjusting = false
    private fun adjustAndShowDropDown() {
        if (isAdjusting) return
        isAdjusting = true
        // TOP LAYOUT
        topLayout()

        val height = calculateDropDownHeight()
        val offsetY = binding.autoCompleteTextView.y + binding.autoCompleteTextView.height
        var newHeight = (height - offsetY).toInt()
        val maxItems = newHeight / RouteAdapter.defaultDropDownItemHeight()
        newHeight = maxItems * RouteAdapter.defaultDropDownItemHeight()
        val maxNeeded =
            ((binding.autoCompleteTextView.adapter ?: return) as RouteAdapter).maxHeightNeeded()
        if (maxNeeded < newHeight) {
            newHeight = maxNeeded
        }
        binding.autoCompleteTextView.dropDownHeight = newHeight
        binding.autoCompleteTextView.showDropDown()
        isAdjusting = false
    }

    private var isTopping = false
    private fun topLayout() {
        if (isTopping) return
        isTopping = true
        val set = ConstraintSet()

        set.clone(binding.routeDialogSelect)
        set.clear(binding.gralLayout.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.routeDialogSelect)

        isTopping = false
    }

    private var isCentring = false
    private fun centerLayout() {
        if (isCentring) return
        isCentring = true
        val set = ConstraintSet()

        set.clone(binding.routeDialogSelect)
        set.connect(
            binding.gralLayout.id,
            ConstraintSet.BOTTOM,
            binding.routeDialogSelect.id,
            ConstraintSet.BOTTOM,
            0
        )
        set.applyTo(binding.routeDialogSelect)

        isCentring = false
    }

    private fun adjustDropDownHeight() {
        runOnUiThread {
            when {
                binding.autoCompleteTextView.isPopupShowing -> {
                    adjustAndShowDropDown()
                }
                else -> {
                    centerLayout()
                }
            }
        }
    }

    override fun contractsRetrieved(count: Int) {
        if (count > 0) {
            adjustDropDownHeight()
        } else {
            // CENTER LAYOUT
            centerLayout()
        }
    }
    // endregion SOFT KEYBOARD AND DROPDOWN ISSUES
}