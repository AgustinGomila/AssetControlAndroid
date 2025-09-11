package com.example.assetControl.ui.fragments.dataCollection

import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.assetControl.databinding.FragmentDecimalBinding
import com.example.assetControl.ui.common.views.filters.DecimalDigitsInputFilter
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * A simple [Fragment] subclass.
 * Use the [DecimalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DecimalFragment : Fragment() {
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(gf: DccFragmentListener) {
        dccFragmentListener = gf
    }

    private var _binding: FragmentDecimalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        destroyLocals()
    }

    private fun destroyLocals() {
        dccFragmentListener?.onFragmentDestroy()
        dccFragmentListener = null

        binding.decimalEditText.clearFocus()
        _binding = null
    }

    private var _tempIsEnabled: Boolean = true
    private var _tempDescription: String = ""
    private var _tempValue: Float = 0f
    private var _tempDecimalPlaces: Int = 3

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = if (b.containsKey("isEnabled")) b.getBoolean("isEnabled") else true
        _tempValue = b.getFloat("currentValue")
        _tempDecimalPlaces = b.getInt("decimalPlaces")
        _tempDescription = b.getString("description") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDecimalBinding.inflate(inflater, container, false)
        val view = binding.root

        if (arguments != null)
            loadBundleValues(requireArguments())

        binding.decimalEditText.filters =
            arrayOf<InputFilter>(DecimalDigitsInputFilter(7, _tempDecimalPlaces))
        binding.decimalEditText.setOnEditorActionListener { _, keyCode, keyEvent ->
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
        isEnabled = _tempIsEnabled
        value = _tempValue
    }

    override fun onStart() {
        super.onStart()
        if (isEnabled) {
            binding.decimalEditText.requestFocus()
        }
        dccFragmentListener?.onFragmentStarted()
    }

    var isEnabled: Boolean = true

    var value: Float?
        get() {
            if (_binding == null) return defaultValue
            return binding.decimalEditText.text.toString().toFloatOrNull()
        }
        set(value) {
            if (_binding == null) return
            binding.decimalEditText.setText(
                roundToString(
                    value ?: defaultValue,
                    _tempDecimalPlaces
                ), TextView.BufferType.EDITABLE
            )
            binding.decimalEditText.clearFocus()
            return
        }

    var defaultValue: Float = 0f

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment unitType_spinner.
         */
        fun newInstance(
            decimalPlaces: Int,
            description: String,
            value: Float? = null, isEnabled: Boolean = true,
        ): DecimalFragment {
            val fragment = DecimalFragment()

            val args = Bundle()
            args.putInt("decimalPlaces", decimalPlaces)
            args.putString("description", description)
            args.putBoolean("isEnabled", isEnabled)
            if (value != null) args.putFloat("currentValue", value)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }

        private fun roundToString(d: Double, decimalPlaces: Int): String {
            val r = round(d, decimalPlaces).toString()
            return if (decimalPlaces == 0 || d % 1 == 0.0) {
                r.substring(0, r.indexOf('.'))
            } else {
                r
            }
        }

        fun roundToString(d: Float, decimalPlaces: Int): String {
            return roundToString(d.toDouble(), decimalPlaces)
        }

        @Suppress("unused")
        fun round(d: Float, decimalPlaces: Int): Double {
            return round(d.toDouble(), decimalPlaces)
        }

        private fun round(d: Double, decimalPlaces: Int): Double {
            var bd = BigDecimal(d.toString())
            bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }
}

