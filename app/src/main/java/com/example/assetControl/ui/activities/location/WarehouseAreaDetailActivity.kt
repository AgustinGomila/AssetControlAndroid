package com.example.assetControl.ui.activities.location

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.imageControl.dto.DocumentContent
import com.dacosys.imageControl.dto.DocumentContentRequestResult
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.databinding.WarehouseAreaDetailActivityBinding
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.utils.parcel.Parcelables.parcelable

class WarehouseAreaDetailActivity : AppCompatActivity() {
    private var warehouseArea: WarehouseArea? = null
    private var rejectNewInstances = false


    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private fun isBackPressed() {
        finish()
    }

    private lateinit var binding: WarehouseAreaDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseAreaDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.area_information)

        val extras = intent.extras
        if (extras != null) {
            warehouseArea = extras.parcelable("warehouseArea")
        }

        binding.showImagesButton.setOnClickListener { showImages() }

        val wa = warehouseArea
        if (wa != null) {
            binding.descriptionTextView.text = wa.description
            binding.warehouseStrTextView.text = wa.warehouseStr
        }

        setupUI(binding.root, this)
    }

    private fun showImages() {
        val warehouseArea = warehouseArea

        if (warehouseArea != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true
                WsFunction().documentContentGetBy12(
                    programObjectId = Table.warehouseArea.id,
                    objectId1 = warehouseArea.warehouseId.toString()
                ) { it2 -> if (it2 != null) fillResults(it2) }
            }
        }
    }

    private fun fillResults(docContReqResObj: DocumentContentRequestResult) {
        val warehouseArea = warehouseArea ?: return

        if (docContReqResObj.documentContentArray.isEmpty()) {
            showMessage(getString(R.string.no_images), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        var anyAvailable = false
        for (docCont in docContReqResObj.documentContentArray) {
            if (docCont.available) {
                anyAvailable = true
                break
            }
        }

        if (!anyAvailable) {
            showMessage(getString(R.string.images_not_yet_processed), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        val intent = Intent(baseContext, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(ImageControlGridActivity.ARG_PROGRAM_OBJECT_ID, Table.warehouseArea.id.toLong())
        intent.putExtra(ImageControlGridActivity.ARG_OBJECT_ID_1, warehouseArea.id.toString())
        intent.putExtra(ImageControlGridActivity.ARG_DOC_CONT_OBJ_ARRAY_LIST, ArrayList<DocumentContent>())
        startActivity(intent)
    }

    private fun showMessage(msg: String, type: SnackBarType) {
        if (isFinishing || isDestroyed) return
        if (type == ERROR) logError(msg)
        makeText(binding.root, msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)
}