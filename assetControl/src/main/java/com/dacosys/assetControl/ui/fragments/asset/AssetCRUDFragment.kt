package com.dacosys.assetControl.ui.fragments.asset

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
import com.dacosys.assetControl.data.crud.asset.AssetCRUD
import com.dacosys.assetControl.data.enums.asset.AssetCondition
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.asset.OwnershipStatus
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.category.ItemCategoryRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseRepository
import com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject
import com.dacosys.assetControl.data.webservice.asset.AssetObject
import com.dacosys.assetControl.databinding.AssetCrudFragmentBinding
import com.dacosys.assetControl.ui.activities.category.ItemCategorySelectActivity
import com.dacosys.assetControl.ui.activities.location.LocationSelectActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels

class AssetCRUDFragment : Fragment() {
    private var rejectNewInstances = false

    private var asset: Asset? = null
    private var itemCategory: ItemCategory? = null
    private var currWarehouseArea: WarehouseArea? = null
    private var origWarehouseArea: WarehouseArea? = null
    private var assetStatus: AssetStatus? = null
    private var serialNumber = ""
    private var model = ""
    private var manufacturer = ""
    private var ean = ""
    private var code = ""
    private var description = ""
    private var active = true

    private var assetStatusSpinnerFragment: AssetStatusSpinnerFragment? = null

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("itemCategory", itemCategory)
        savedInstanceState.putParcelable("currWarehouseArea", currWarehouseArea)
        savedInstanceState.putParcelable("origWarehouseArea", origWarehouseArea)
        savedInstanceState.putParcelable("assetStatus", assetStatusSpinnerFragment?.selectedAssetStatus)
        savedInstanceState.putBoolean("active", binding.activeCheckBox.isChecked)
        savedInstanceState.putString(
            "serialNumber",
            binding.serialNumberEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putString(
            "model",
            binding.modelEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putString(
            "manufacturer",
            binding.manufacturerEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putString("ean", binding.eanEditText.text?.toString().orEmpty())
        savedInstanceState.putString(
            "code",
            binding.codeEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putString(
            "description",
            binding.descriptionEditText.text?.toString().orEmpty()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            asset = requireArguments().parcelable("asset")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            itemCategory = savedInstanceState.parcelable("itemCategory")
            currWarehouseArea = savedInstanceState.parcelable("currWarehouseArea")
            origWarehouseArea = savedInstanceState.parcelable("origWarehouseArea")
            assetStatus = savedInstanceState.parcelable("assetStatus")
            serialNumber = savedInstanceState.getString("serialNumber") ?: ""
            model = savedInstanceState.getString("model") ?: ""
            manufacturer = savedInstanceState.getString("manufacturer") ?: ""
            ean = savedInstanceState.getString("ean") ?: ""
            code = savedInstanceState.getString("code") ?: ""
            description = savedInstanceState.getString("description") ?: ""
            active = savedInstanceState.getBoolean("active")
        }
    }

    private var _binding: AssetCrudFragmentBinding? = null
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
        _binding = AssetCrudFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        _justRefresh = savedInstanceState != null

        assetStatusSpinnerFragment =
            childFragmentManager.findFragmentById(R.id.assetStatusSpinnerFragment) as AssetStatusSpinnerFragment

        binding.categoryTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true
            _justRefresh = true // Para onResume al regresar de la actividad

            val intent = Intent(requireContext(), ItemCategorySelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("itemCategory", itemCategory)
            intent.putExtra("itemCategoryStr", itemCategory?.description ?: "")
            intent.putExtra("title", getString(R.string.select_category))
            resultForCategorySelect.launch(intent)
        }

        binding.currentWarehouseAreaTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true
            _justRefresh = true // Para onResume al regresar de la actividad

            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("warehouseAreaDescription", currWarehouseArea?.description ?: "")
            intent.putExtra("title", getString(R.string.select_location))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", true)
            resultForCurrentLocationSelect.launch(intent)
        }

        binding.originalWarehouseAreaTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true
            _justRefresh = true // Para onResume al regresar de la actividad

            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("warehouseAreaDescription", origWarehouseArea?.description ?: "")
            intent.putExtra("title", getString(R.string.select_location))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", true)
            resultForOrigLocationSelect.launch(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fillControls(_justRefresh)
    }

    private val resultForOrigLocationSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    origWarehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.parcelable("warehouseArea"))
                    setOrigWarehouseAreaText()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private val resultForCurrentLocationSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    currWarehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.parcelable("warehouseArea"))
                    setCurrWarehouseAreaText()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private val resultForCategorySelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    itemCategory =
                        Parcels.unwrap<ItemCategory>(data.parcelable("itemCategory"))
                    setCategoryText()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun fillControls(restoreState: Boolean) {
        if (asset == null && !restoreState) {
            clearControl()
            return
        }

        if (restoreState) {
            if (_binding != null) {
                binding.descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
                binding.codeEditText.setText(code, TextView.BufferType.EDITABLE)
                binding.eanEditText.setText(ean, TextView.BufferType.EDITABLE)
                binding.modelEditText.setText(model, TextView.BufferType.EDITABLE)
                binding.serialNumberEditText.setText(serialNumber, TextView.BufferType.EDITABLE)
                binding.manufacturerEditText.setText(manufacturer, TextView.BufferType.EDITABLE)
                assetStatusSpinnerFragment?.selectedAssetStatus = assetStatus
                binding.activeCheckBox.isChecked = active
            }
        } else {
            if (_binding != null) {
                binding.descriptionEditText.setText(
                    asset?.description,
                    TextView.BufferType.EDITABLE
                )
                binding.activeCheckBox.isChecked = asset?.active == 1
                binding.codeEditText.setText(asset?.code, TextView.BufferType.EDITABLE)
                binding.eanEditText.setText(asset?.ean, TextView.BufferType.EDITABLE)
                binding.modelEditText.setText(asset?.model, TextView.BufferType.EDITABLE)
                binding.serialNumberEditText.setText(
                    asset?.serialNumber,
                    TextView.BufferType.EDITABLE
                )
                binding.manufacturerEditText.setText(
                    asset?.manufacturer,
                    TextView.BufferType.EDITABLE
                )
            }
            assetStatusSpinnerFragment?.selectedAssetStatus = asset?.assetStatus
            itemCategory =
                if (asset?.itemCategoryId != null) {
                    ItemCategoryRepository().selectById(asset?.itemCategoryId!!)
                } else {
                    null
                }
            currWarehouseArea =
                if (asset?.warehouseAreaId != null) {
                    WarehouseAreaRepository().selectById(asset?.warehouseAreaId!!)
                } else {
                    null
                }
            origWarehouseArea =
                if (asset?.warehouseAreaId != null) {
                    WarehouseAreaRepository().selectById(asset?.originalWarehouseAreaId!!)
                } else {
                    null
                }
        }

        setCategoryText()
        setOrigWarehouseAreaText()
        setCurrWarehouseAreaText()
    }

    private fun clearControl() {
        if (_binding == null) return

        binding.descriptionEditText.setText("", TextView.BufferType.EDITABLE)
        binding.codeEditText.setText("", TextView.BufferType.EDITABLE)
        binding.eanEditText.setText("", TextView.BufferType.EDITABLE)
        binding.modelEditText.setText("", TextView.BufferType.EDITABLE)
        binding.serialNumberEditText.setText("", TextView.BufferType.EDITABLE)
        binding.manufacturerEditText.setText("", TextView.BufferType.EDITABLE)
        assetStatusSpinnerFragment?.selectedAssetStatus = null
        binding.activeCheckBox.isChecked = true

        binding.categoryTextView.typeface = Typeface.DEFAULT
        binding.categoryTextView.text = getString(R.string.search_category)

        binding.currentWarehouseAreaTextView.typeface = Typeface.DEFAULT
        binding.currentWarehouseAreaTextView.text = getString(R.string.search_area)

        binding.originalWarehouseAreaTextView.typeface = Typeface.DEFAULT
        binding.originalWarehouseAreaTextView.text = getString(R.string.search_area)
    }

    private fun setCategoryText() {
        if (_binding == null) return

        if (itemCategory == null) {
            binding.categoryTextView.typeface = Typeface.DEFAULT
            binding.categoryTextView.text = getString(R.string.search_category)
        } else {
            binding.categoryTextView.typeface = Typeface.DEFAULT_BOLD
            binding.categoryTextView.text = itemCategory?.description
        }
    }

    private fun setCurrWarehouseAreaText() {
        if (_binding == null) return

        if (currWarehouseArea == null) {
            binding.currentWarehouseAreaTextView.typeface = Typeface.DEFAULT
            binding.currentWarehouseAreaTextView.text = getString(R.string.search_area)
        } else {
            binding.currentWarehouseAreaTextView.typeface = Typeface.DEFAULT_BOLD
            binding.currentWarehouseAreaTextView.text = currWarehouseArea?.description
        }
    }

    private fun setOrigWarehouseAreaText() {
        if (_binding == null) return

        if (origWarehouseArea == null) {
            binding.originalWarehouseAreaTextView.typeface = Typeface.DEFAULT
            binding.originalWarehouseAreaTextView.text = getString(R.string.search_area)
        } else {
            binding.originalWarehouseAreaTextView.typeface = Typeface.DEFAULT_BOLD
            binding.originalWarehouseAreaTextView.text =
                origWarehouseArea?.description
        }
    }

    private fun checkValidData(): Boolean {
        if (_binding == null) return false

        if (binding.descriptionEditText.text.trim().toString().isEmpty()) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_enter_a_description_for_the_asset),
                SnackBarType.INFO
            )
            binding.descriptionEditText.requestFocus()
            return false
        }

        if (binding.codeEditText.text.trim().toString().isEmpty()) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_enter_a_code_for_the_asset),
                SnackBarType.INFO
            )
            binding.codeEditText.requestFocus()
            return false
        }

        var assetId = 0L
        // Existing asset or not
        if (asset != null) {
            assetId = asset!!.id
        }

        if (AssetRepository().codeExists(
                binding.codeEditText.text.trim().toString(),
                assetId
            )
        ) {
            MakeText.makeText(
                binding.root,
                getString(R.string.the_code_entered_already_exists_for_another_asset),
                SnackBarType.ERROR
            )
            binding.codeEditText.requestFocus()
            return false
        }

        if (itemCategory == null) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_select_a_category_for_the_asset),
                SnackBarType.INFO
            )
            return false
        }

        if (origWarehouseArea == null) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_select_an_original_area_for_the_asset),
                SnackBarType.INFO
            )
            return false
        }

        if (currWarehouseArea == null) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_select_a_current_area_for_the_asset),
                SnackBarType.INFO
            )
            return false
        }

        val assetStatus = assetStatusSpinnerFragment!!.selectedAssetStatus
        if (assetStatus == null) {
            MakeText.makeText(
                binding.root,
                getString(R.string.you_must_select_a_status_for_the_asset),
                SnackBarType.INFO
            )
            return false
        }

        return true
    }

    fun saveAsset(callback: CrudCompleted) {
        if (!checkValidData()) return

        // 1. El activo es NULL cuando se está agregando uno nuevo.
        // 2. Puede no ser NULL cuando proviene de escanear un código
        // desconocido en revisiones de activos.
        if (asset == null) {
            if (!User.hasPermission(PermissionEntry.AddAsset)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_add_assets),
                    SnackBarType.ERROR
                )
                return
            }

            val tempAsset = createWsAsset()
            if (tempAsset != null) {
                val assetAdd = AssetCRUD.AssetAdd()
                assetAdd.addParams(callback, tempAsset)
                assetAdd.execute()
            }
        } else {
            if (!User.hasPermission(PermissionEntry.ModifyAsset)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_modify_assets),
                    SnackBarType.ERROR
                )
                return
            }

            updateAsset()

            val asset = asset
            if (asset != null) {
                val tempAsset = AssetCollectorObject(asset)
                val updateAsset = AssetCRUD.AssetUpdate()
                updateAsset.addParams(callback, tempAsset)
                updateAsset.execute()
            }
        }
    }

    private fun updateAsset() {
        val a = asset ?: return
        val cat = itemCategory ?: return
        val currWa = currWarehouseArea ?: return
        val origWa = origWarehouseArea ?: return
        val status = assetStatusSpinnerFragment?.selectedAssetStatus ?: return

        // Create CurrentAsset Object
        // Main Information
        a.description = binding.descriptionEditText.text.trim().toString()
        a.code = binding.codeEditText.text.trim().toString()
        a.itemCategoryId = cat.id

        //////////// Warehouse Area or Repairshop Area
        val allW = WarehouseRepository().select(false)
        var isInWarehouse = false
        for (w in allW) {
            if (a.warehouseId == w.id) {
                isInWarehouse = true
                break
            }
        }

        // Save only if the active is in a Warehouse (not if is in Repairshop)
        if (isInWarehouse) {
            a.warehouseAreaId = currWa.id
            a.warehouseId = currWa.warehouseId
        }
        ///////////////

        a.originalWarehouseAreaId = origWa.id
        a.originalWarehouseId = origWa.warehouseId

        a.status = status.id

        // Secondary Information
        a.serialNumber = binding.serialNumberEditText.text.trim().toString()
        a.ean = binding.eanEditText.text.trim().toString()
        a.active = if (binding.activeCheckBox.isChecked) 1 else 0
    }

    private fun createWsAsset(): AssetObject? {
        val cat = itemCategory ?: return null
        val currWa = currWarehouseArea ?: return null
        val origWa = origWarehouseArea ?: return null
        val status = assetStatusSpinnerFragment?.selectedAssetStatus ?: return null

        val asset = Asset(
            id = -1,
            description = binding.descriptionEditText.text.trim().toString(),
            code = binding.codeEditText.text.trim().toString(),
            itemCategoryId = cat.id,
            parentId = 0L,
            warehouseAreaId = currWa.id,
            warehouseId = currWa.warehouseId,
            originalWarehouseAreaId = origWa.id,
            originalWarehouseId = origWa.warehouseId,
            status = status.id,
            ownershipStatus = OwnershipStatus.owned.id,
            active = if (binding.activeCheckBox.isChecked) 1 else 0,
            manufacturer = binding.manufacturerEditText.text.trim().toString(),
            model = binding.modelEditText.text.trim().toString(),
            serialNumber = binding.serialNumberEditText.text.trim().toString(),
            condition = AssetCondition.good.id,
            ean = binding.eanEditText.text.trim().toString(),
            missingDate = "",
            lastAssetReviewDate = "",
        )
        return AssetObject(asset)
    }

    fun setCode(code: String) {
        if (_binding == null) return
        binding.codeEditText.setText(code, TextView.BufferType.EDITABLE)
    }

    fun getCode(): String {
        if (_binding == null) return ""
        return binding.codeEditText.text?.trim().toString()
    }

    fun getDescription(): String {
        if (_binding == null) return ""
        return binding.descriptionEditText.text?.trim().toString()
    }

    fun setAsset(asset: Asset?) {
        this.asset = asset
        fillControls(false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param asset Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(asset: Asset?): AssetCRUDFragment {
            val fragment = AssetCRUDFragment()

            val args = Bundle()
            args.putParcelable("asset", asset)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}