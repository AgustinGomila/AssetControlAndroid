package com.dacosys.assetControl.views.routes.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.databinding.FragmentStringBinding


/**
 * A simple [Fragment] subclass.
 * Use the [StringFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StringFragment : Fragment() {
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(dccList: DccFragmentListener) {
        dccFragmentListener = dccList
    }

    private var _binding: FragmentStringBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()

        dccFragmentListener?.onFragmentDestroy()
        dccFragmentListener = null

        binding.stringEditText.clearFocus()
        _binding = null
    }

    private var _tempIsEnabled: Boolean = true
    private var _tempDescription: String = ""
    private var _tempValue: String = ""

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = if (b.containsKey("isEnabled")) b.getBoolean("isEnabled") else true
        _tempValue = b.getString("currentValue") ?: defaultValue
        _tempDescription = b.getString("description") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStringBinding.inflate(inflater, container, false)
        val view = binding.root

        if (arguments != null)
            loadBundleValues(requireArguments())

        binding.stringEditText.setOnEditorActionListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                                keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                dccFragmentListener?.onFragmentOk()
                true
            } else {
                false
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
        value = _tempValue
        isEnabled = _tempIsEnabled
    }

    override fun onStart() {
        super.onStart()
        if (isEnabled) {
            binding.stringEditText.requestFocus()
        }
        dccFragmentListener?.onFragmentStarted()
    }

    var isEnabled: Boolean = true

    var value: String
        get() {
            if (_binding == null) return defaultValue
            return binding.stringEditText.text.toString()
        }
        set(value) {
            if (_binding == null) return
            binding.stringEditText.setText(value, TextView.BufferType.EDITABLE)
            binding.stringEditText.clearFocus()
            return
        }

    var defaultValue: String = ""

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment unitType_spinner.
         */
        fun newInstance(
            description: String,
            value: String? = null,
            isEnabled: Boolean = true,
        ): StringFragment {
            val fragment = StringFragment()

            val args = Bundle()
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