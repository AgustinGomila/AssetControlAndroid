package com.dacosys.assetControl.ui.fragments.dataCollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R.layout.custom_spinner_comma_separated_item
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding

/**
 * A simple [Fragment] subclass.
 * Use the [CommaSeparatedSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommaSeparatedSpinnerFragment : Fragment() {
    private val optionSeparator = ';'
    private var itemSelectedListener: OnItemSelectedListener? = null
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(dccList: DccFragmentListener) {
        dccFragmentListener = dccList
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
        destroyLocals()
    }

    private fun destroyLocals() {
        dccFragmentListener?.onFragmentDestroy()
        itemSelectedListener = null
        dccFragmentListener = null

        _binding = null
    }

    private var _tempIsEnabled: Boolean = true
    private var _tempDescription: String = ""
    private var _tempValue: String = ""
    private var _tempCommaSeparatedOptions: String = ""

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = if (b.containsKey("isEnabled")) b.getBoolean("isEnabled") else true
        _tempValue = b.getString("currentValue") ?: defaultValue
        _tempDescription = b.getString("description") ?: ""
        val c = b.getString(argCommaSeparatedOptions)
        if (c != null) _tempCommaSeparatedOptions = c
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
                        itemSelectedListener?.onItemSelected(parent.getItemAtPosition(position) as String)
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

        return view
    }

    private fun setValues() {
        commaSeparatedOptions = _tempCommaSeparatedOptions
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
        dccFragmentListener?.onFragmentStarted()
    }

    override fun onDetach() {
        super.onDetach()
        itemSelectedListener = null
    }

    private fun fillAdapter() {
        val composition = commaSeparatedOptions.trim().trimEnd(optionSeparator)
        val allOptions = ArrayList(composition.split(optionSeparator)).sorted()

        val spinnerArrayAdapter = ArrayAdapter(
            AssetControlApp.context,
            custom_spinner_comma_separated_item,
            allOptions
        )

        // Step 3: Tell the strOptionSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter

        if (_tempValue.isNotEmpty()) {
            selectedStrOption = _tempValue
        }
    }

    var isEnabled: Boolean = true

    private var commaSeparatedOptions: String = ""

    var selectedStrOption: String
        get() {
            if (_binding == null) return defaultValue
            val temp = binding.fragmentSpinner.selectedItem ?: defaultValue
            return temp as String
        }
        set(strOption) {
            if (_binding == null) return
            if (strOption.isEmpty()) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as ArrayAdapter<*>
            for (i in 0 until adapter.count) {
                if (equals(strOption, adapter.getItem(i))) {
                    binding.fragmentSpinner.setSelection(i)
                    break
                }
            }
        }

    var defaultValue: String = ""

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
        fun onItemSelected(strOption: String?)
    }
    // endregion

    companion object {
        private const val argCommaSeparatedOptions = "commaSeparatedOptions"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param commaSeparatedOptions Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(
            commaSeparatedOptions: String,
            description: String,
            value: String? = null, isEnabled: Boolean = true,
        ): CommaSeparatedSpinnerFragment {
            val fragment = CommaSeparatedSpinnerFragment()

            val args = Bundle()
            args.putString(argCommaSeparatedOptions, commaSeparatedOptions)
            args.putString("description", description)
            args.putBoolean("isEnabled", isEnabled)
            if (value != null) args.putString("currentValue", value)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}