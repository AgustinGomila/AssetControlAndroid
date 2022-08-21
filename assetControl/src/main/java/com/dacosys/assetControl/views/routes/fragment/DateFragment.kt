package com.dacosys.assetControl.views.routes.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.databinding.FragmentDateBinding
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [DateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DateFragment : Fragment() {
    private var dccFragmentListener: DccFragmentListener? = null

    fun setListener(gf: GeneralFragment) {
        dccFragmentListener = gf
    }

    private var _binding: FragmentDateBinding? = null

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
    private var _tempValue: String = ""

    private fun loadBundleValues(b: Bundle) {
        _tempIsEnabled = b.getBoolean("isEnabled")
        _tempDescription = b.getString("description") ?: ""
        _tempValue = b.getString("currentValue") ?: defaultValue
    }

    private fun setValue(v: String) {
        try {
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = sdf.parse(v) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            value = cal
        } catch (ex: Exception) {
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = sdf.parse(sdf.format(Date())) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            value = cal
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDateBinding.inflate(inflater, container, false)
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
        setValue(_tempValue)
        isEnabled = _tempIsEnabled
    }

    override fun onStart() {
        super.onStart()

        if (isEnabled) {
            binding.datePicker.requestFocus()
        }
    }

    var isEnabled: Boolean = true

    var value: Calendar
        get() {
            if (_binding == null) return Calendar.getInstance()
            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month
            val year = binding.datePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            return calendar
        }
        set(value) {
            if (_binding == null) return
            binding.datePicker.updateDate(
                value.get(Calendar.YEAR),
                value.get(Calendar.MONTH),
                value.get(Calendar.DAY_OF_MONTH)
            )
            return
        }

    var defaultValue: String = ""
        get() {
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            return sdf.format(value.time)
        }
        set(value) {
            field = value
            return
        }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment unitType_spinner.
         */
        fun newInstance(
            description: String,
            value: Calendar? = null,
            isEnabled: Boolean = true,
        ): DateFragment {
            val fragment = DateFragment()

            var valueStr = ""
            if (value != null) {
                val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
                valueStr = sdf.format(value.time)
            }

            val args = Bundle()
            args.putString("description", description)
            args.putBoolean("isEnabled", isEnabled)
            if (value != null) args.putString("currentValue", valueStr)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }

        private const val dateFormat = "dd/MM/yyyy hh:mm:ss"
    }
}