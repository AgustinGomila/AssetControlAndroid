package com.dacosys.assetControl.ui.fragments.location

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.crud.location.WarehouseAreaCRUD
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.repository.location.WarehouseRepository
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject
import com.dacosys.assetControl.databinding.WarehouseAreaCrudFragmentBinding
import com.dacosys.assetControl.ui.activities.location.LocationSelectActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels

class WarehouseAreaCRUDFragment : Fragment() {
    private var rejectNewInstances = false

    private var warehouseArea: WarehouseArea? = null
    private var warehouse: Warehouse? = null
    private var description = ""
    private var active: Boolean = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("warehouseArea", warehouseArea)
        savedInstanceState.putParcelable("warehouse", warehouse)
        savedInstanceState.putString(
            "description",
            binding.descriptionEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putBoolean("active", binding.activeCheckBox.isChecked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            warehouseArea = requireArguments().parcelable("warehouseArea")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            description = savedInstanceState.getString("description") ?: ""
            warehouseArea = savedInstanceState.parcelable<WarehouseArea>("warehouseArea")
            warehouse = savedInstanceState.parcelable<Warehouse>("warehouse")
        }
    }

    private var _binding: WarehouseAreaCrudFragmentBinding? = null
    private var _justRefresh = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = WarehouseAreaCrudFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        _justRefresh = savedInstanceState != null

        binding.warehouseTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true
            _justRefresh = true // Para onResume al regresar de la actividad

            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("warehouseDescription", warehouse?.description ?: "")
            intent.putExtra("title", getString(R.string.select_location))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", false)
            resultForWarehouseSelect.launch(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fillControls(_justRefresh)
    }

    private val resultForWarehouseSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    try {
                        warehouse = Parcels.unwrap<Warehouse>(data.parcelable("warehouse"))
                        setWarehouseText()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(activity, this::class.java.simpleName, ex)
                    }

                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun fillControls(restoreState: Boolean) {
        val wa = warehouseArea
        if (wa == null && !restoreState) {
            clearControl()
            return
        }
        if (wa == null) return

        if (restoreState) {
            if (_binding != null) {
                binding.descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
                binding.activeCheckBox.isChecked = active
            }
        } else {
            if (_binding != null) {
                binding.descriptionEditText.setText(wa.description, TextView.BufferType.EDITABLE)
                binding.activeCheckBox.isChecked = wa.active
            }
            warehouse = WarehouseRepository().selectById(wa.warehouseId)
        }

        setWarehouseText()
    }

    private fun clearControl() {
        if (_binding == null) return

        binding.descriptionEditText.setText("", TextView.BufferType.EDITABLE)
        binding.warehouseTextView.setText("", TextView.BufferType.EDITABLE)
        binding.activeCheckBox.isChecked = true
        binding.warehouseTextView.typeface = Typeface.DEFAULT
        binding.warehouseTextView.text = getString(R.string.search_warehouse)
    }

    private fun setWarehouseText() {
        if (_binding == null) return

        val warehouse = warehouse
        if (warehouse == null) {
            binding.warehouseTextView.typeface = Typeface.DEFAULT
            binding.warehouseTextView.text = getString(R.string.search_warehouse)
        } else {
            binding.warehouseTextView.typeface = Typeface.DEFAULT_BOLD
            binding.warehouseTextView.text = warehouse.description
        }
    }

    private fun checkValidData(): Boolean {
        if (_binding == null) return false

        if (binding.descriptionEditText.text.trim().toString().isEmpty()) {
            makeText(
                binding.root,
                getString(R.string.you_must_enter_a_description_for_the_warehouse_area),
                SnackBarType.INFO
            )
            binding.descriptionEditText.requestFocus()
            return false
        }

        if (warehouse == null) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_a_warehouse_for_the_area),
                SnackBarType.INFO
            )
            return false
        }

        return true
    }

    fun saveWarehouseArea(callback: CrudCompleted) {
        if (!checkValidData()) return

        // 1. El activo es NULL cuando se está agregando uno nuevo.
        // 2. Puede no ser NULL cuando proviene de escanear un código
        // desconocido en revisiones de activos.
        if (warehouseArea == null) {
            if (!User.hasPermission(PermissionEntry.AddWarehouse)) {
                makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_add_warehouse_areas),
                    SnackBarType.ERROR
                )
                return
            }

            val tempWarehouseArea = createWsWarehouseArea()
            if (tempWarehouseArea != null) {
                val warehouseAreaAdd = WarehouseAreaCRUD.WarehouseAreaAdd()
                warehouseAreaAdd.addParams(callback, tempWarehouseArea)
                warehouseAreaAdd.execute()

            }
        } else {
            if (!User.hasPermission(PermissionEntry.ModifyWarehouse)) {
                makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_modify_warehouse_areas),
                    SnackBarType.ERROR
                )
                return
            }

            updateWarehouseArea()
            if (warehouseArea != null) {
                val tempWarehouseArea = WarehouseAreaObject(warehouseArea!!)

                val updateWarehouseArea = WarehouseAreaCRUD.WarehouseAreaUpdate()
                updateWarehouseArea.addParams(callback, tempWarehouseArea)
                updateWarehouseArea.execute()
            }
        }
    }

    private fun updateWarehouseArea() {
        val wa = warehouseArea ?: return
        val wId = warehouse?.id ?: return

        // Create CurrentWarehouseArea Object
        // Main Information

        wa.description = binding.descriptionEditText.text.trim().toString()
        wa.warehouseId = wId
        wa.active = binding.activeCheckBox.isChecked
    }

    private fun createWsWarehouseArea(): WarehouseAreaObject? {
        val wId = warehouse?.id ?: return null

        val tempWarehouseArea = WarehouseArea(
            description = binding.descriptionEditText.text.trim().toString(),
            mActive = if (binding.activeCheckBox.isChecked) 1 else 0,
            warehouseId = wId,
            transferred = 0
        )
        return WarehouseAreaObject(tempWarehouseArea)
    }

    fun getDescription(): String {
        if (_binding == null) return ""
        return binding.descriptionEditText.text?.trim().toString()
    }

    fun setWarehouseArea(warehouseArea: WarehouseArea?) {
        this.warehouseArea = warehouseArea
        fillControls(false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param warehouseArea Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(warehouseArea: WarehouseArea?): WarehouseAreaCRUDFragment {
            val fragment = WarehouseAreaCRUDFragment()

            val args = Bundle()
            args.putParcelable("warehouseArea", warehouseArea)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}