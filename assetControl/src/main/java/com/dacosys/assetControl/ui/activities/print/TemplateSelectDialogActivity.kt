package com.dacosys.assetControl.ui.activities.print

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
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.barcode.BarcodeLabelCustomAdapter
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.databinding.CodeSelectActivityBinding
import com.dacosys.assetControl.model.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.model.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.ui.common.views.custom.ContractsAutoCompleteTextView
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.showKeyboard
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import kotlin.concurrent.thread

class TemplateSelectDialogActivity : AppCompatActivity(),
    ContractsAutoCompleteTextView.OnContractsAvailability,
    KeyboardVisibilityEventListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        closeKeyboard(this)
        binding.autoCompleteTextView.setOnContractsAvailability(null)
        binding.autoCompleteTextView.setAdapter(null)
    }

    private var onlyActive: Boolean = true
    private var barcodeLabelCustom: BarcodeLabelCustom? = null
    private var barcodeLabelTarget: BarcodeLabelTarget? = null

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean("onlyActive", onlyActive)
        savedInstanceState.putParcelable("barcodeLabelCustom", barcodeLabelCustom)
        savedInstanceState.putParcelable("barcodeLabelTarget", barcodeLabelTarget)
    }

    private lateinit var binding: CodeSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = CodeSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permite finalizar la actividad si se toca la pantalla
        // fuera de la ventana. Esta actividad se ve como un diálogo.
        setFinishOnTouchOutside(true)

        var tempTitle = getString(R.string.select_template)
        if (savedInstanceState != null) {
            // Dejo de escuchar estos eventos hasta pasar los valores guardados
            // más adelante se reconectan
            binding.autoCompleteTextView.setOnEditorActionListener(null)
            binding.autoCompleteTextView.onItemClickListener = null
            binding.autoCompleteTextView.setOnTouchListener(null)
            binding.autoCompleteTextView.onFocusChangeListener = null
            binding.autoCompleteTextView.setOnDismissListener(null)

            onlyActive = savedInstanceState.getBoolean("onlyActive")
            barcodeLabelCustom = savedInstanceState.getParcelable("barcodeLabelCustom")
            barcodeLabelTarget = savedInstanceState.getParcelable("barcodeLabelTarget")
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (t1 != null && t1.isNotEmpty()) {
                    tempTitle = t1
                }

                if (extras.containsKey("onlyActive")) onlyActive = extras.getBoolean("onlyActive")
                barcodeLabelCustom = extras.getParcelable("barcodeLabelCustom")
                barcodeLabelTarget = extras.getParcelable("barcodeLabelTarget")
            }
        }

        title = tempTitle
        binding.codeSelect.setOnClickListener { onBackPressed() }

        binding.clearImageView.setOnClickListener {
            barcodeLabelCustom = null
            refreshBarcodeLabelCustomText(cleanText = true, focus = true)
        }

        // region Setup CATEGORY_CATEGORY ID AUTOCOMPLETE
        // Set an barcodeLabelCustom click checkedChangedListener for auto complete text view
        binding.autoCompleteTextView.threshold = 0
        binding.autoCompleteTextView.hint = tempTitle
        binding.autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (binding.autoCompleteTextView.adapter != null && binding.autoCompleteTextView.adapter is BarcodeLabelCustomAdapter) {
                    val it =
                        (binding.autoCompleteTextView.adapter as BarcodeLabelCustomAdapter).getItem(
                            position
                        )
                    if (it != null) {
                        barcodeLabelCustom = it
                    }
                }
                barcodeLabelTemplateSelected()
            }
        binding.autoCompleteTextView.setOnContractsAvailability(this)
        binding.autoCompleteTextView.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.autoCompleteTextView.text.trim().length >= binding.autoCompleteTextView.threshold &&
                    binding.autoCompleteTextView.adapter != null &&
                    (binding.autoCompleteTextView.adapter as BarcodeLabelCustomAdapter).count > 0 &&
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
                showKeyboard(this)
                adjustDropDownHeight()
                return@setOnTouchListener false
            } else if (motionEvent.action == MotionEvent.BUTTON_BACK) {
                closeKeyboard(this)

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
                    if (binding.autoCompleteTextView.adapter != null && binding.autoCompleteTextView.adapter is BarcodeLabelCustomAdapter) {
                        val all =
                            (binding.autoCompleteTextView.adapter as BarcodeLabelCustomAdapter).getAll()
                        if (all.any()) {
                            var founded = false
                            for (a in all) {
                                if (a.description.startsWith(
                                        binding.autoCompleteTextView.text.toString().trim(),
                                        true
                                    )
                                ) {
                                    barcodeLabelCustom = a
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
                                        barcodeLabelCustom = a
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
                barcodeLabelTemplateSelected()
                true
            } else {
                false
            }
        }
        // endregion

        KeyboardVisibilityEvent.registerEventListener(this, this)
        refreshBarcodeLabelCustomText(cleanText = false, focus = true)
        thread { fillAdapter() }
    }

    private fun showProgressBar(visibility: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.progressBar.visibility = visibility
            }
        }, 20)
    }

    private fun refreshBarcodeLabelCustomText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.autoCompleteTextView.setText(
                if (barcodeLabelCustom == null) {
                    if (cleanText) {
                        ""
                    } else {
                        binding.autoCompleteTextView.text.toString()
                    }
                } else {
                    barcodeLabelCustom?.description ?: ""
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

    private fun barcodeLabelTemplateSelected() {
        closeKeyboard(this)

        val data = Intent()
        data.putExtra("barcodeLabelCustom", barcodeLabelCustom)
        setResult(RESULT_OK, data)
        finish()
    }

    var isFilling = false
    private fun fillAdapter() {
        if (isFilling) return
        isFilling = true

        var itemArray: ArrayList<BarcodeLabelCustom> = ArrayList()
        try {
            Log.d(this::class.java.simpleName, "Selecting item templates...")
            itemArray =
                if (barcodeLabelTarget != null)
                    BarcodeLabelCustomDbHelper().selectByBarcodeLabelTargetId(
                        (barcodeLabelTarget ?: return).id, onlyActive
                    )
                else BarcodeLabelCustomDbHelper().select(onlyActive)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }

        val adapter = BarcodeLabelCustomAdapter(
            this,
            R.layout.barcode_label_custom_row,
            itemArray,
            ArrayList()
        )

        runOnUiThread {
            binding.autoCompleteTextView.setAdapter(adapter)
            (binding.autoCompleteTextView.adapter as BarcodeLabelCustomAdapter).notifyDataSetChanged()

            while (binding.autoCompleteTextView.adapter == null) {
                // Wait for complete loaded
            }
            refreshBarcodeLabelCustomText(cleanText = false, focus = false)
            showProgressBar(GONE)
        }

        isFilling = false
    }

    override fun onBackPressed() {
        closeKeyboard(this)

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
        val maxItems = newHeight / BarcodeLabelCustomAdapter.defaultRowHeight()
        newHeight = maxItems * BarcodeLabelCustomAdapter.defaultRowHeight()
        val maxNeeded =
            ((binding.autoCompleteTextView.adapter
                ?: return) as BarcodeLabelCustomAdapter).maxHeightNeeded()
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

        set.clone(binding.codeSelect)
        set.clear(binding.gralLayout.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.codeSelect)

        isTopping = false
    }

    private var isCentring = false
    private fun centerLayout() {
        if (isCentring) return
        isCentring = true
        val set = ConstraintSet()

        set.clone(binding.codeSelect)
        set.connect(
            binding.gralLayout.id,
            ConstraintSet.BOTTOM,
            binding.codeSelect.id,
            ConstraintSet.BOTTOM,
            0
        )
        set.applyTo(binding.codeSelect)

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