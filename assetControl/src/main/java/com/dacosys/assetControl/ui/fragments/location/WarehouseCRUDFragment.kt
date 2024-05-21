package com.dacosys.assetControl.ui.fragments.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.crud.location.WarehouseCRUD
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.webservice.location.WarehouseObject
import com.dacosys.assetControl.databinding.WarehouseCrudFragmentBinding
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable

class WarehouseCRUDFragment : Fragment() {
    private var warehouse: Warehouse? = null
    private var description = ""
    private var active: Boolean = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

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
            warehouse = requireArguments().parcelable("warehouse")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            description = savedInstanceState.getString("description") ?: ""
            warehouse = savedInstanceState.parcelable("warehouse")
        }
    }

    private var _binding: WarehouseCrudFragmentBinding? = null
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
        _binding = WarehouseCrudFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        _justRefresh = savedInstanceState != null

        return view
    }

    override fun onResume() {
        super.onResume()
        fillControls(_justRefresh)
    }

    private fun fillControls(restoreState: Boolean) {
        if (warehouse == null && !restoreState) {
            clearControl()
            return
        }

        if (restoreState) {
            if (_binding != null) {
                binding.descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
                binding.activeCheckBox.isChecked = active
            }
        } else {
            if (_binding != null) {
                binding.descriptionEditText.setText(
                    warehouse?.description ?: "",
                    TextView.BufferType.EDITABLE
                )
                binding.activeCheckBox.isChecked = warehouse?.active ?: false
            }
        }
    }

    private fun clearControl() {
        if (_binding == null) return

        binding.descriptionEditText.setText("", TextView.BufferType.EDITABLE)
        binding.activeCheckBox.isChecked = true
    }

    private fun checkValidData(): Boolean {
        if (_binding == null) return false

        if (binding.descriptionEditText.text.trim().toString().isEmpty()) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_enter_a_description_for_the_category),
                SnackBarType.INFO
            )
            binding.descriptionEditText.requestFocus()
            return false
        }

        return true
    }

    fun saveWarehouse(callback: CrudCompleted) {
        if (!checkValidData()) return

        // 1. El activo es NULL cuando se está agregando uno nuevo.
        // 2. Puede no ser NULL cuando proviene de escanear un código
        // desconocido en revisiones de activos.
        if (warehouse == null) {
            if (!User.hasPermission(PermissionEntry.AddWarehouse)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_add_warehouses),
                    SnackBarType.ERROR
                )
                return
            }

            val tempWarehouse = createWsWarehouse()
            val warehouseAdd = WarehouseCRUD.WarehouseAdd()
            warehouseAdd.addParams(callback, tempWarehouse)
            warehouseAdd.execute()
        } else {
            if (!User.hasPermission(PermissionEntry.ModifyWarehouse)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_modify_warehouses),
                    SnackBarType.ERROR
                )
                return
            }

            updateWarehouse()
            if (warehouse != null) {
                val tempWarehouse = WarehouseObject(warehouse!!)

                val updateWarehouse = WarehouseCRUD.WarehouseUpdate()
                updateWarehouse.addParams(callback, tempWarehouse)
                updateWarehouse.execute()
            }
        }
    }

    private fun updateWarehouse() {
        if (warehouse == null) {
            return
        }

        // Create CurrentWarehouse Object
        // Main Information
        warehouse?.description = binding.descriptionEditText.text.trim().toString()
        warehouse?.active = binding.activeCheckBox.isChecked
    }

    private fun createWsWarehouse(): WarehouseObject {
        val tempWarehouse = Warehouse(description = binding.descriptionEditText.text.trim().toString())
        tempWarehouse.active = binding.activeCheckBox.isChecked
        return WarehouseObject(tempWarehouse)
    }

    fun getDescription(): String {
        if (_binding == null) return ""
        return binding.descriptionEditText.text?.trim().toString()
    }

    fun setWarehouse(warehouse: Warehouse?) {
        this.warehouse = warehouse
        fillControls(false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param warehouse Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(warehouse: Warehouse?): WarehouseCRUDFragment {
            val fragment = WarehouseCRUDFragment()

            val args = Bundle()
            args.putParcelable("warehouse", warehouse)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}