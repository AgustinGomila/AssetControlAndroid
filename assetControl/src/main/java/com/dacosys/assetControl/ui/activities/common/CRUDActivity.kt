package com.dacosys.assetControl.ui.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.CrudActivityBinding
import com.dacosys.assetControl.ui.activities.asset.AssetCRUDActivity
import com.dacosys.assetControl.ui.activities.category.ItemCategoryCRUDActivity
import com.dacosys.assetControl.ui.activities.location.WarehouseAreaCRUDActivity
import com.dacosys.assetControl.ui.activities.location.WarehouseCRUDActivity
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI

class CRUDActivity : AppCompatActivity() {


    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private var rejectNewInstances = false

    private lateinit var binding: CrudActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = CrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
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

        setupUI(binding.root, this)
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