package com.example.assetControl.ui.activities.category

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.repository.category.ItemCategoryRepository
import com.example.assetControl.databinding.ItemCategorySelectActivityBinding
import com.example.assetControl.ui.adapters.category.ItemCategoryAdapter
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.common.utils.Screen.Companion.showKeyboard
import com.example.assetControl.ui.common.views.custom.ContractsAutoCompleteTextView
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.parceler.Parcels
import kotlin.concurrent.thread

class ItemCategorySelectActivity :
    AppCompatActivity(),
    ItemCategoryChangedObserver,
    KeyboardVisibilityEventListener, ContractsAutoCompleteTextView.OnContractsAvailability {
    override fun onItemCategoryChanged(w: ItemCategory?) {
    }

    private var icChangedListener: ItemCategoryChangedObserver? = null

    private var onlyActive: Boolean = true
    private var itemCategory: ItemCategory? = null
    private var itemCategoryStr: String = ""

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("title", title.toString())

        outState.putBoolean("onlyActive", onlyActive)
        outState.putParcelable("itemCategory", itemCategory)
        outState.putString("itemCategoryStr", binding.category.text.toString())
    }


    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        closeKeyboard(this)
        binding.category.setOnContractsAvailability(null)
        binding.category.setAdapter(null)
    }

    private lateinit var binding: ItemCategorySelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = ItemCategorySelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var tempTitle = getString(R.string.select_item_category)

        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getString("title")
            if (!t1.isNullOrEmpty()) tempTitle = t1

            binding.category.setOnEditorActionListener(null)
            binding.category.onItemClickListener = null
            binding.category.onFocusChangeListener = null
            binding.category.setOnDismissListener(null)

            onlyActive = savedInstanceState.getBoolean("onlyActive")
            itemCategory = savedInstanceState.parcelable("itemCategory")
            itemCategoryStr = savedInstanceState.getString("itemCategoryStr") ?: ""
        } else {
            val extras = intent.extras
            if (extras != null) {
                val t1 = extras.getString("title")
                if (!t1.isNullOrEmpty()) tempTitle = t1

                val t2 = extras.parcelable("itemCategory") as ItemCategory?
                if (t2 != null) itemCategory = t2
                if (extras.containsKey("onlyActive")) onlyActive = extras.getBoolean("onlyActive")
                itemCategoryStr = extras.getString("itemCategoryStr") ?: ""
            }
        }

        title = tempTitle
        binding.categorySelect.setOnClickListener { isBackPressed() }

        binding.selectButton.setOnClickListener { itemSelected() }

        binding.clearImageView.setOnClickListener {
            itemCategory = null
            refreshItemCategoryText(cleanText = true, focus = true)
        }

        // region Setup CATEGORY_CATEGORY ID AUTOCOMPLETE
        // Set an itemCategory click checkedChangedListener for auto complete text view
        binding.category.threshold = 1
        binding.category.hint = tempTitle
        binding.category.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (binding.category.adapter != null && binding.category.adapter is ItemCategoryAdapter
                ) {
                    val it =
                        (binding.category.adapter as ItemCategoryAdapter).getItem(
                            position
                        )
                    if (it != null) {
                        itemCategory = it
                    }
                }
                itemSelected()
            }
        binding.category.setOnContractsAvailability(this)
        binding.category.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus &&
                    binding.category.text.trim().length >= binding.category.threshold &&
                    binding.category.adapter != null &&
                    (binding.category.adapter as ItemCategoryAdapter).count > 0 &&
                    !binding.category.isPopupShowing
                ) {
                    // Display the suggestion dropdown on focus
                    Handler(Looper.getMainLooper()).post {
                        run {
                            adjustAndShowDropDown()
                        }
                    }
                }
            }
        binding.category.setOnTouchListener { _, motionEvent ->
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
        binding.category.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (binding.category.text.trim().length >= binding.category.threshold) {
                    if (binding.category.adapter != null && binding.category.adapter is ItemCategoryAdapter) {
                        val all = (binding.category.adapter as ItemCategoryAdapter).getAll()
                        if (all.any()) {
                            var founded = false
                            for (a in all) {
                                if (a.description.startsWith(
                                        binding.category.text.toString().trim(),
                                        true
                                    )
                                ) {
                                    itemCategory = a
                                    founded = true
                                    break
                                }
                            }
                            if (!founded) {
                                for (a in all) {
                                    if (a.description.contains(
                                            binding.category.text.toString().trim(),
                                            true
                                        )
                                    ) {
                                        itemCategory = a
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
        refreshItemCategoryText(cleanText = false, focus = true)

        thread { fillAdapter() }

        setupUI(binding.root, this)
    }

    private fun refreshItemCategoryText(cleanText: Boolean, focus: Boolean) {
        runOnUiThread {
            binding.category.setText(
                if (itemCategory == null) {
                    if (cleanText) {
                        ""
                    } else {
                        binding.category.text.toString()
                    }
                } else {
                    itemCategory?.description ?: ""
                }
            )

            binding.category.post {
                binding.category.setSelection(
                    binding.category.length()
                )
            }

            if (focus) {
                binding.category.requestFocus()
            }
        }
    }

    private var oldSelectedItemCategory: ItemCategory? = null
    private fun setItemCategory(ic: ItemCategory?) {
        if (ic == null) {
            if (oldSelectedItemCategory == null) {
                return
            }

            oldSelectedItemCategory = itemCategory
            itemCategory = null

            icChangedListener?.onItemCategoryChanged(itemCategory)
        } else {
            if (oldSelectedItemCategory != null && oldSelectedItemCategory == ic) {
                return
            }

            oldSelectedItemCategory = itemCategory
            itemCategory = ic

            icChangedListener?.onItemCategoryChanged(itemCategory)
        }
    }

    var isFilling = false
    private fun fillAdapter() {
        if (isFilling) return
        isFilling = true

        var itemArray: ArrayList<ItemCategory> = ArrayList()
        try {
            itemArray = ArrayList(ItemCategoryRepository().select(onlyActive))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }

        val adapter = ItemCategoryAdapter(
            resource = R.layout.item_category_row,
            icArray = itemArray,
            suggestedList = ArrayList()
        )

        runOnUiThread {
            showProgressBar(VISIBLE)

            binding.category.setAdapter(adapter)
            (binding.category.adapter as ItemCategoryAdapter).notifyDataSetChanged()

            while (binding.category.adapter == null) {
                // Wait for complete loaded
            }
            showProgressBar(GONE)
        }

        refreshItemCategoryText(cleanText = false, focus = false)
        isFilling = false
    }

    private fun showProgressBar(visibility: Int) {
        runOnUiThread {
            binding.categoryProgressBar.visibility = visibility
        }
    }

    private fun itemSelected() {
        closeKeyboard(this)

        when {
            itemCategory != null -> {
                val data = Intent()
                data.putExtra("itemCategory", Parcels.wrap(itemCategory))
                setResult(RESULT_OK, data)
                finish()
            }

            else -> {
                setResult(RESULT_CANCELED, null)
                finish()
            }
        }
    }

    private fun isBackPressed() {
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
        val offsetY = binding.category.y + binding.category.height
        var newHeight = (height - offsetY).toInt()
        val maxItems = newHeight / ItemCategoryAdapter.defaultDropDownItemHeight()
        newHeight = maxItems * ItemCategoryAdapter.defaultDropDownItemHeight()
        val maxNeeded =
            ((binding.category.adapter
                ?: return) as ItemCategoryAdapter).maxHeightNeeded()
        if (maxNeeded < newHeight) {
            newHeight = maxNeeded
        }
        binding.category.dropDownHeight = newHeight
        binding.category.showDropDown()
        isAdjusting = false
    }

    private var isTopping = false
    private fun topLayout() {
        if (isTopping) return
        isTopping = true
        val set = ConstraintSet()

        set.clone(binding.categorySelect)
        set.clear(binding.gralLayout.id, ConstraintSet.BOTTOM)
        set.applyTo(binding.categorySelect)

        isTopping = false
    }

    private var isCentring = false
    private fun centerLayout() {
        if (isCentring) return
        isCentring = true
        val set = ConstraintSet()

        set.clone(binding.categorySelect)
        set.connect(
            binding.gralLayout.id,
            ConstraintSet.BOTTOM,
            binding.categorySelect.id,
            ConstraintSet.BOTTOM,
            0
        )
        set.applyTo(binding.categorySelect)

        isCentring = false
    }

    private fun adjustDropDownHeight() {
        runOnUiThread {
            when {
                binding.category.isPopupShowing -> {
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