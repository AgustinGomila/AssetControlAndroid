package com.dacosys.assetControl.views.assets.asset.fragments

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
import com.dacosys.assetControl.databinding.AssetCrudFragmentBinding
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.`object`.AssetCRUD
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetCollectorObject
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetObject
import com.dacosys.assetControl.model.assets.assetCondition.AssetCondition
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryDbHelper
import com.dacosys.assetControl.model.assets.ownershipStatus.OwnershipStatus
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.permissions.PermissionEntry
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.assets.assetStatus.fragment.AssetStatusSpinnerFragment
import com.dacosys.assetControl.views.assets.itemCategory.activities.ItemCategorySelectActivity
import com.dacosys.assetControl.views.commons.snackbar.MakeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.assetControl.views.locations.locationSelect.LocationSelectActivity
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
        savedInstanceState.putParcelable(
            "assetStatus",
            assetStatusSpinnerFragment?.selectedAssetStatus
        )
        savedInstanceState.putBoolean("active", binding.activeCheckBox.isChecked)
        savedInstanceState.putString(
            "serialNumber",
            binding.serialNumberEditText.text?.toString() ?: ""
        )
        savedInstanceState.putString(
            "model",
            binding.modelEditText.text?.toString() ?: ""
        )
        savedInstanceState.putString(
            "manufacturer",
            binding.manufacturerEditText.text?.toString() ?: ""
        )
        savedInstanceState.putString("ean", binding.eanEditText.text?.toString() ?: "")
        savedInstanceState.putString(
            "code",
            binding.codeEditText.text?.toString() ?: ""
        )
        savedInstanceState.putString(
            "description",
            binding.descriptionEditText.text?.toString() ?: ""
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            asset = requireArguments().getParcelable("asset")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            itemCategory = savedInstanceState.getParcelable("itemCategory")
            currWarehouseArea = savedInstanceState.getParcelable("currWarehouseArea")
            origWarehouseArea = savedInstanceState.getParcelable("origWarehouseArea")
            assetStatus = savedInstanceState.getParcelable("assetStatus")
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
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))
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
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))
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
                        Parcels.unwrap<ItemCategory>(data.getParcelableExtra("itemCategory"))
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
                binding.activeCheckBox.isChecked = asset?.active ?: true
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
                    ItemCategoryDbHelper().selectById(asset?.itemCategoryId!!)
                } else {
                    null
                }
            currWarehouseArea =
                if (asset?.warehouseAreaId != null) {
                    WarehouseAreaDbHelper().selectById(asset?.warehouseAreaId!!)
                } else {
                    null
                }
            origWarehouseArea =
                if (asset?.warehouseAreaId != null) {
                    WarehouseAreaDbHelper().selectById(asset?.originalWarehouseAreaId!!)
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
            assetId = asset!!.assetId
        }

        if (AssetDbHelper().codeExists(
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

    fun saveAsset(callback: AssetCRUD.Companion.TaskCompleted) {
        if (!checkValidData()) {
            return
        }

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
            if (asset != null) {
                val tempAsset = AssetCollectorObject(asset ?: return)
                val updateAsset = AssetCRUD.AssetUpdate()
                updateAsset.addParams(callback, tempAsset)
                updateAsset.execute()
            }
        }
    }

    private fun updateAsset() {
        if (asset == null ||
            itemCategory == null ||
            currWarehouseArea == null ||
            origWarehouseArea == null ||
            assetStatusSpinnerFragment?.selectedAssetStatus == null
        ) {
            return
        }

        // Create CurrentAsset Object
        // Main Information
        asset?.description = binding.descriptionEditText.text.trim().toString()
        asset?.code = binding.codeEditText.text.trim().toString()
        asset?.itemCategoryId = itemCategory?.itemCategoryId ?: -1

        //////////// Warehouse Area or Repairshop Area
        val allW = WarehouseDbHelper().select(false)
        var isInWarehouse = false
        for (w in allW) {
            if (asset?.warehouseId == w.warehouseId) {
                isInWarehouse = true
                break
            }
        }

        // Save only if the active is in a Warehouse (not if is in Repairshop)
        if (isInWarehouse) {
            asset?.warehouseAreaId = currWarehouseArea?.warehouseAreaId ?: -1
            asset?.warehouseId = currWarehouseArea?.warehouseId ?: -1
        }
        ///////////////

        asset?.originalWarehouseAreaId = origWarehouseArea?.warehouseAreaId ?: -1
        asset?.originalWarehouseId = origWarehouseArea?.warehouseId ?: -1

        asset?.assetStatusId = assetStatusSpinnerFragment?.selectedAssetStatus?.id ?: -1

        // Secondary Information
        asset?.serialNumber = binding.serialNumberEditText.text.trim().toString()
        asset?.ean = binding.eanEditText.text.trim().toString()
        asset?.active = binding.activeCheckBox.isChecked
    }

    private fun createWsAsset(): AssetObject? {
        if (itemCategory == null ||
            currWarehouseArea == null ||
            origWarehouseArea == null ||
            assetStatusSpinnerFragment?.selectedAssetStatus == null
        ) {
            return null
        }

        val tempAsset = Asset()
        tempAsset.setDataRead()

        try {
            //Create CurrentAsset Object
            // Main Information
            tempAsset.description =
                binding.descriptionEditText.text.trim().toString()
            tempAsset.code = binding.codeEditText.text.trim().toString()

            tempAsset.itemCategoryId = (itemCategory ?: return null).itemCategoryId
            tempAsset.parentAssetId = 0L
            tempAsset.warehouseAreaId = (currWarehouseArea ?: return null).warehouseAreaId
            tempAsset.warehouseId = (currWarehouseArea ?: return null).warehouseId
            tempAsset.originalWarehouseAreaId = (origWarehouseArea ?: return null).warehouseAreaId
            tempAsset.originalWarehouseId = (origWarehouseArea ?: return null).warehouseId
            tempAsset.assetStatusId = assetStatusSpinnerFragment?.selectedAssetStatus?.id ?: -1
            tempAsset.ownershipStatusId = OwnershipStatus.owned.id
            tempAsset.active = binding.activeCheckBox.isChecked

            // Secondary Information
            tempAsset.manufacturer =
                binding.manufacturerEditText.text.trim().toString()
            tempAsset.model = binding.modelEditText.text.trim().toString()
            tempAsset.serialNumber =
                binding.serialNumberEditText.text.trim().toString()
            tempAsset.assetConditionId = AssetCondition.good.id
            tempAsset.ean = binding.eanEditText.text.trim().toString()

            // Dates
            tempAsset.missingDate = ""
            tempAsset.lastAssetReviewDate = ""
        } catch (ex: Exception) {
            ex.printStackTrace()
            MakeText.makeText(
                binding.root,
                getString(R.string.error_creating_the_asset),
                SnackBarType.ERROR
            )
            return null
        }

        return AssetObject(tempAsset)
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