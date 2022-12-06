package com.dacosys.assetControl.views.routes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.model.assets.units.unitType.UnitType
import com.dacosys.assetControl.model.assets.units.unitTypeCategory.UnitTypeCategory
import com.dacosys.assetControl.utils.Statics
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Use the [UnitTypeSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UnitTypeSpinnerFragment : Fragment() {
    private var itemSelectedListener: OnItemSelectedListener? = null
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(gf: GeneralFragment) {
        dccFragmentListener = gf
    }

    val count: Int
        get() = when {
            _binding == null -> 0
            binding.fragmentSpinner.adapter != null -> binding.fragmentSpinner.adapter.count
            else -> 0
        }

    private var _binding: FragmentSpinnerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()

        dccFragmentListener?.onFragmentDestroy()

        itemSelectedListener = null
        dccFragmentListener = null

        _tempValue = null
        _tempCat = null

        _binding = null
    }

    private var _tempIsEnabled: Boolean = true
    private var _tempDescription: String = ""
    private var _tempValue: UnitType? = null
    private var _tempCat: UnitTypeCategory? = null

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = if (b.containsKey("isEnabled")) b.getBoolean("isEnabled") else true
        _tempValue = b.getParcelable("currentValue")
        _tempCat = b.getParcelable(ARG_UNIT_TYPE_CATEGORY)
        _tempDescription = b.getString("description") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSpinnerBinding.inflate(inflater, container, false)
        val view = binding.root

        if (arguments != null)
            loadBundleValues(requireArguments())

        binding.fragmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (selectedPosition != position) {
                        itemSelectedListener?.onItemSelected(parent.getItemAtPosition(position) as UnitType)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    itemSelectedListener?.onItemSelected(null)
                }
            }

        binding.autoResizeTextView.text = _tempDescription
        binding.autoResizeTextView.visibility = if (_tempDescription.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        setValues()

        dccFragmentListener?.onFragmentStarted()

        return view
    }

    private fun setValues() {
        isEnabled = _tempIsEnabled

        // Llenar el binding.fragmentSpinner
        fillAdapter()
    }

    override fun onStart() {
        super.onStart()

        if (activity is OnItemSelectedListener) {
            itemSelectedListener = activity as OnItemSelectedListener
        }

        if (isEnabled) {
            binding.fragmentSpinner.requestFocus()
        }
    }

    override fun onDetach() {
        super.onDetach()
        itemSelectedListener = null
    }

    private fun fillAdapter() {
        val allUnitType = UnitType.getByUnitTypeCategory(_tempCat ?: return)
        allUnitType.sortedWith(compareBy { it.description })

        val spinnerArrayAdapter = ArrayAdapter(
            Statics.AssetControl.getContext(),
            custom_spinner_dropdown_item,
            allUnitType
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter

        if (_tempValue != null) {
            selectedUnitType = _tempValue
        }
    }

    var isEnabled: Boolean = true

    var selectedUnitType: UnitType?
        get() {
            if (_binding == null) return defaultValue
            val temp = binding.fragmentSpinner.selectedItem
            return if (temp != null) {
                temp as UnitType
            } else null
        }
        set(unitType) {
            if (_binding == null) return
            if (unitType == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as ArrayAdapter<*>
            for (i in 0 until adapter.count) {
                if (equals(unitType, adapter.getItem(i))) {
                    binding.fragmentSpinner.setSelection(i)
                    break
                }
            }
        }

    var defaultValue: UnitType? = null

    var selectedPosition: Int
        get() {
            if (_binding == null) return -1
            return binding.fragmentSpinner.selectedItemPosition
        }
        set(pos) {
            if (_binding == null) return
            return when {
                pos < 0 -> binding.fragmentSpinner.setSelection(0)
                else -> binding.fragmentSpinner.setSelection(pos)
            }
        }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(unitType: UnitType?)
    }
    // endregion

    companion object {
        private const val ARG_UNIT_TYPE_CATEGORY = "unitTypeCat"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param unitTypeCat Parameter 1.
         * @return A new instance of fragment unitType_spinner.
         */
        fun newInstance(
            unitTypeCat: UnitTypeCategory,
            description: String,
            value: UnitType? = null, isEnabled: Boolean = true,
        ): UnitTypeSpinnerFragment {
            val fragment = UnitTypeSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_UNIT_TYPE_CATEGORY, Parcels.wrap(unitTypeCat))
            args.putString("description", description)
            args.putBoolean("isEnabled", isEnabled)
            if (value != null) args.putParcelable("currentValue", value)
//
            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}