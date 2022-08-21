package com.dacosys.assetControl.views.locations.warehouse.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.WarehouseCrudFragmentBinding
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouse.`object`.WarehouseCRUD
import com.dacosys.assetControl.model.locations.warehouse.wsObject.WarehouseObject
import com.dacosys.assetControl.model.permissions.PermissionEntry
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.views.commons.snackbar.MakeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import kotlin.concurrent.thread

class WarehouseCRUDFragment : Fragment() {
    private var warehouse: Warehouse? = null
    private var description = ""
    private var active: Boolean = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("warehouse", warehouse)
        savedInstanceState.putString(
            "description",
            binding.descriptionEditText.text?.toString() ?: ""
        )
        savedInstanceState.putBoolean("active", binding.activeCheckBox.isChecked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            warehouse = requireArguments().getParcelable("warehouse")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            description = savedInstanceState.getString("description") ?: ""
            warehouse = savedInstanceState.getParcelable("warehouse")
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
                binding.activeCheckBox.isChecked = warehouse?.active ?: true
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
                SnackbarType.INFO
            )
            binding.descriptionEditText.requestFocus()
            return false
        }

        return true
    }

    fun saveWarehouse(callback: WarehouseCRUD.Companion.TaskCompleted): Boolean {
        if (!checkValidData()) {
            return false
        }

        // 1. El activo es NULL cuando se está agregando uno nuevo.
        // 2. Puede no ser NULL cuando proviene de escanear un código
        // desconocido en revisiones de activos.
        if (warehouse == null) {
            if (!User.hasPermission(PermissionEntry.AddWarehouse)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_add_warehouses),
                    SnackbarType.ERROR
                )
                return false
            }

            val tempWarehouse = createWsWarehouse()
            if (tempWarehouse != null) {
                thread {
                    val warehouseAdd = WarehouseCRUD.WarehouseAdd()
                    warehouseAdd.addParams(callback, tempWarehouse)
                    warehouseAdd.execute()
                }
            }
        } else {
            if (!User.hasPermission(PermissionEntry.ModifyWarehouse)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_modify_warehouses),
                    SnackbarType.ERROR
                )
                return false
            }

            updateWarehouse()
            if (warehouse != null) {
                val tempWarehouse = WarehouseObject(warehouse!!)
                thread {
                    val updateWarehouse = WarehouseCRUD.WarehouseUpdate()
                    updateWarehouse.addParams(callback, tempWarehouse)
                    updateWarehouse.execute()
                }
            }
        }
        return false
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

    private fun createWsWarehouse(): WarehouseObject? {
        val tempWarehouse = Warehouse()
        tempWarehouse.setDataRead()

        try {
            //Create CurrentWarehouse Object
            // Main Information
            tempWarehouse.description = binding.descriptionEditText.text.trim().toString()
            tempWarehouse.active = binding.activeCheckBox.isChecked
        } catch (ex: Exception) {
            ex.printStackTrace()
            MakeText.makeText(
                binding.root,
                getString(R.string.error_creating_the_category),
                SnackbarType.ERROR
            )
            return null
        }

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