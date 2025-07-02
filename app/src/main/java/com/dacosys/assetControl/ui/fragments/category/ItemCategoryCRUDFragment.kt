package com.dacosys.assetControl.ui.fragments.category

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
import com.dacosys.assetControl.data.crud.category.ItemCategoryCRUD
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.repository.category.ItemCategoryRepository
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject
import com.dacosys.assetControl.databinding.ItemCategoryCrudFragmentBinding
import com.dacosys.assetControl.ui.activities.category.ItemCategorySelectActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels

class ItemCategoryCRUDFragment : Fragment() {
    private var rejectNewInstances = false

    private var itemCategory: ItemCategory? = null
    private var parentCategory: ItemCategory? = null
    private var description = ""
    private var active: Boolean = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("itemCategory", itemCategory)
        savedInstanceState.putParcelable("parentCategory", parentCategory)
        savedInstanceState.putString(
            "description",
            binding.descriptionEditText.text?.toString().orEmpty()
        )
        savedInstanceState.putBoolean("active", binding.activeCheckBox.isChecked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            itemCategory = requireArguments().parcelable("itemCategory")
        }

        if (savedInstanceState != null) {
            active = savedInstanceState.getBoolean("active")
            description = savedInstanceState.getString("description") ?: ""
            itemCategory = savedInstanceState.parcelable("itemCategory")
            parentCategory = savedInstanceState.parcelable("parentCategory")
        }
    }

    private var _binding: ItemCategoryCrudFragmentBinding? = null
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
        _binding = ItemCategoryCrudFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        _justRefresh = savedInstanceState != null

        binding.parentTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true
            _justRefresh = true // Para onResume al regresar de la actividad

            val intent = Intent(requireContext(), ItemCategorySelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("itemCategory", parentCategory)
            intent.putExtra("title", getString(R.string.select_category))
            resultForCategorySelect.launch(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fillControls(_justRefresh)
    }

    private val resultForCategorySelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    try {
                        parentCategory =
                            Parcels.unwrap<ItemCategory>(data.parcelable("itemCategory"))
                        setParentCategoryText()
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
        val ic = itemCategory
        if (ic == null && !restoreState) {
            clearControl()
            return
        }
        if (ic == null) return

        if (restoreState) {
            if (_binding != null) {
                binding.descriptionEditText.setText(description, TextView.BufferType.EDITABLE)
                binding.activeCheckBox.isChecked = active
            }
        } else {
            if (_binding != null) {
                binding.descriptionEditText.setText(ic.description, TextView.BufferType.EDITABLE)
                binding.activeCheckBox.isChecked = ic.active
            }
            parentCategory = if (ic.parentId > 0) {
                ItemCategoryRepository().selectById(ic.parentId)
            } else {
                null
            }
        }

        setParentCategoryText()
    }

    private fun clearControl() {
        if (_binding == null) return

        binding.descriptionEditText.setText("", TextView.BufferType.EDITABLE)
        binding.parentTextView.setText("", TextView.BufferType.EDITABLE)
        binding.activeCheckBox.isChecked = true
        binding.parentTextView.typeface = Typeface.DEFAULT
        binding.parentTextView.text = getString(R.string.search_category)
    }

    private fun setParentCategoryText() {
        if (_binding == null) return

        if (parentCategory == null) {
            binding.parentTextView.typeface = Typeface.DEFAULT
            binding.parentTextView.text = getString(R.string.search_category)
        } else {
            binding.parentTextView.typeface = Typeface.DEFAULT_BOLD
            binding.parentTextView.text = parentCategory!!.description
        }
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

    fun saveItemCategory(callback: CrudCompleted) {
        if (!checkValidData()) return

        // 1. El activo es NULL cuando se está agregando uno nuevo.
        // 2. Puede no ser NULL cuando proviene de escanear un código
        // desconocido en revisiones de activos.
        if (itemCategory == null) {
            if (!User.hasPermission(PermissionEntry.AddItemCategory)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_add_categories),
                    SnackBarType.ERROR
                )
                return
            }

            val tempItemCategory = createWsItemCategory()
            val itemCategoryAdd = ItemCategoryCRUD.ItemCategoryAdd()
            itemCategoryAdd.addParams(callback, tempItemCategory)
            itemCategoryAdd.execute()
        } else {
            if (!User.hasPermission(PermissionEntry.ModifyItemCategory)) {
                MakeText.makeText(
                    binding.root,
                    getString(R.string.you_do_not_have_permission_to_modify_categories),
                    SnackBarType.ERROR
                )
                return
            }

            updateItemCategory()
            if (itemCategory != null) {
                val tempItemCategory = ItemCategoryObject(itemCategory!!)

                val updateItemCategory = ItemCategoryCRUD.ItemCategoryUpdate()
                updateItemCategory.addParams(callback, tempItemCategory)
                updateItemCategory.execute()
            }
        }
    }

    private fun updateItemCategory() {
        if (itemCategory == null) {
            return
        }

        // Create CurrentItemCategory Object
        // Main Information
        itemCategory?.description = binding.descriptionEditText.text.trim().toString()
        itemCategory?.parentId = parentCategory?.id ?: 0
        itemCategory?.mActive = if (binding.activeCheckBox.isChecked) 1 else 0
    }

    private fun createWsItemCategory(): ItemCategoryObject {
        val tempItemCategory = ItemCategory(
            description = binding.descriptionEditText.text.trim().toString(),
            parentId = parentCategory?.id ?: 0,
            mActive = if (binding.activeCheckBox.isChecked) 1 else 0
        )
        return ItemCategoryObject(tempItemCategory)
    }

    fun getDescription(): String {
        if (_binding == null) return ""
        return binding.descriptionEditText.text?.trim().toString()
    }

    fun setItemCategory(itemCategory: ItemCategory?) {
        this.itemCategory = itemCategory
        fillControls(false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param itemCategory Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(itemCategory: ItemCategory?): ItemCategoryCRUDFragment {
            val fragment = ItemCategoryCRUDFragment()

            val args = Bundle()
            args.putParcelable("itemCategory", itemCategory)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}