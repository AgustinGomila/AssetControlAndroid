package com.dacosys.assetControl.views.locations.warehouseArea.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.WarehouseAreaDetailActivityBinding
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.views.commons.snackbar.MakeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.imageControl.activities.ImageControlGridActivity
import com.dacosys.imageControl.main.GetImagesTask
import com.dacosys.imageControl.main.ImagesTask
import com.dacosys.imageControl.wsObject.DocumentContentObject
import com.dacosys.imageControl.wsObject.DocumentContentRequestResultObject

class WarehouseAreaDetailActivity : AppCompatActivity() {
    private var warehouseArea: WarehouseArea? = null
    private var rejectNewInstances = false

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

    private lateinit var binding: WarehouseAreaDetailActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
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

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun showImages() {
        if (warehouseArea != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val getDocs = GetImagesTask()
                getDocs.addParams(
                    programId = Statics.INTERNAL_IMAGE_CONTROL_APP_ID,
                    programObjectId = Table.warehouseArea.tableId,
                    objId1 = (warehouseArea ?: return).warehouseId.toString(),
                    objId2 = "",
                    onImagesProgress = { onGetImages(it) }
                )
                getDocs.downloadFiles(false)
                getDocs.execute()
            }
        }
    }

    private fun onGetImages(it: ImagesTask) {
        if (isDestroyed || isFinishing) return

        val status: ProgressStatus = ProgressStatus.getById(it.status.id) ?: ProgressStatus.unknown
        val docContReqResObj: DocumentContentRequestResultObject? = it.docContReqResObj
        val msg = it.msg

        if (status == ProgressStatus.finished) {
            if (docContReqResObj == null) {
                MakeText.makeText(binding.root, msg, SnackBarType.INFO)
                rejectNewInstances = false
                return
            }

            if (docContReqResObj.documentContentArray == null ||
                (docContReqResObj.documentContentArray ?: return).isEmpty()
            ) {
                MakeText.makeText(binding.root, getString(R.string.no_images), SnackBarType.INFO)
                rejectNewInstances = false
                return
            }

            var anyAvailable = false
            for (docCont in docContReqResObj.documentContentArray ?: return) {
                if (docCont.available) {
                    anyAvailable = true
                    break
                }
            }

            if (!anyAvailable) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.images_not_yet_processed),
                    SnackBarType.INFO
                )
                rejectNewInstances = false
                return
            }

            val intent = Intent(baseContext, ImageControlGridActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("programId", Statics.INTERNAL_IMAGE_CONTROL_APP_ID)
            intent.putExtra("programObjectId", Table.warehouseArea.tableId)
            intent.putExtra("objectId1", (warehouseArea ?: return).warehouseAreaId.toString())
            intent.putExtra("objectId2", "")
            intent.putExtra("docContObjArrayList", ArrayList<DocumentContentObject>())
            startActivity(intent)
        }
    }
}