package com.dacosys.assetControl.views.routes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.databinding.FragmentBooleanBinding

/**
 * A simple [Fragment] subclass.
 * Use the [BooleanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BooleanFragment : Fragment() {
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(gf: GeneralFragment) {
        dccFragmentListener = gf
    }

    private var _binding: FragmentBooleanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()

        dccFragmentListener?.onFragmentDestroy()
        dccFragmentListener = null

        _binding = null
    }

    private var _tempIsEnabled: Boolean = true
    private var _tempDescription: String = ""
    private var _tempValue: Boolean = true

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = if (b.containsKey("isEnabled")) b.getBoolean("isEnabled") else true
        _tempValue = b.getBoolean("currentValue")
        _tempDescription = b.getString("description") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBooleanBinding.inflate(inflater, container, false)
        val view = binding.root

        if (arguments != null)
            loadBundleValues(requireArguments())

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
        value = _tempValue
        isEnabled = _tempIsEnabled
    }

    override fun onStart() {
        super.onStart()
        if (isEnabled) {
            binding.booleanCheckBox.requestFocus()
        }
    }

    var isEnabled: Boolean = true

    var value: Boolean
        get() {
            if (_binding == null) return defaultValue
            return binding.booleanCheckBox.isChecked
        }
        set(value) {
            if (_binding == null) return
            binding.booleanCheckBox.isChecked = value
            return
        }

    var defaultValue: Boolean = true

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment unitType_spinner.
         */
        fun newInstance(
            description: String,
            value: Boolean? = null,
            isEnabled: Boolean = true,
        ): BooleanFragment {
            val fragment = BooleanFragment()

            val args = Bundle()
            args.putString("description", description)
            args.putBoolean("isEnabled", isEnabled)
            if (value != null) args.putBoolean("currentValue", value)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}