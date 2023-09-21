package com.dacosys.assetControl.ui.activities.location

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.WarehouseAreaDetailActivityBinding
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.imageControl.dto.DocumentContent
import com.dacosys.imageControl.dto.DocumentContentRequestResult
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity

class WarehouseAreaDetailActivity : AppCompatActivity() {
    private var warehouseArea: WarehouseArea? = null
    private var rejectNewInstances = false


    override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    private lateinit var binding: WarehouseAreaDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseAreaDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.area_information)

        val extras = intent.extras
        if (extras != null) {
            warehouseArea = extras.getParcelable("warehouseArea")
        }

        binding.showImagesButton.setOnClickListener { showImages() }

        if (warehouseArea != null) {
            binding.descriptionTextView.text = (warehouseArea ?: return).description
            binding.warehouseStrTextView.text =
                ((warehouseArea ?: return).warehouse ?: return).description
        }

        setupUI(binding.root, this)
    }

    private fun showImages() {
        if (warehouseArea != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true
                WsFunction().documentContentGetBy12(
                    programObjectId = Table.warehouseArea.tableId,
                    objectId1 = (warehouseArea ?: return).warehouseId.toString()
                ) { it2 -> if (it2 != null) fillResults(it2) }
            }
        }
    }

    private fun fillResults(docContReqResObj: DocumentContentRequestResult) {
        if (docContReqResObj.documentContentArray.isEmpty()) {
            MakeText.makeText(binding.root, getString(R.string.no_images), SnackBarType.INFO)
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
            MakeText.makeText(
                binding.root, getString(R.string.images_not_yet_processed), SnackBarType.INFO
            )
            rejectNewInstances = false
            return
        }

        val intent = Intent(baseContext, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(ImageControlGridActivity.ARG_PROGRAM_OBJECT_ID, Table.warehouseArea.tableId.toLong())
        intent.putExtra(ImageControlGridActivity.ARG_OBJECT_ID_1, (warehouseArea ?: return).warehouseAreaId.toString())
        intent.putExtra(ImageControlGridActivity.ARG_DOC_CONT_OBJ_ARRAY_LIST, ArrayList<DocumentContent>())
        startActivity(intent)
    }
}