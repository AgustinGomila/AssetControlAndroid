package com.dacosys.assetControl.views.commons.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.databinding.CrudActivityBinding
import com.dacosys.assetControl.views.assets.asset.activities.AssetCRUDActivity
import com.dacosys.assetControl.views.assets.itemCategory.activities.ItemCategoryCRUDActivity
import com.dacosys.assetControl.views.locations.warehouse.activities.WarehouseCRUDActivity
import com.dacosys.assetControl.views.locations.warehouseArea.activities.WarehouseAreaCRUDActivity

class CRUDActivity : AppCompatActivity() {
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
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .forEach { setupUI(it) }
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

    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private var rejectNewInstances = false

    private lateinit var binding: CrudActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = CrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.register_modification)

        binding.assetCRUDButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, AssetCRUDActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }

        binding.warehouseAreaCRUDButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, WarehouseAreaCRUDActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }

        binding.warehouseCRUDButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, WarehouseCRUDActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }

        binding.itemCategoryCRUDButton.setOnClickListener {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, ItemCategoryCRUDActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

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