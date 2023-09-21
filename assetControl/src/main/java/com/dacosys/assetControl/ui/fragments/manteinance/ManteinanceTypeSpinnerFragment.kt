package com.dacosys.assetControl.ui.fragments.manteinance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.adapters.manteinance.ManteinanceTypeAdapter
import com.dacosys.assetControl.dataBase.manteinance.ManteinanceTypeDbHelper
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.model.manteinance.ManteinanceType
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ManteinanceTypeSpinnerFragment.OnItemSelectedListener] interface
 * to handle interaction events.
 * Use the [ManteinanceTypeSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManteinanceTypeSpinnerFragment : Fragment() {
    private var allManteinanceType: ArrayList<ManteinanceType>? = ArrayList()
    private var showGeneralLevel = true
    private var oldPos = -1
    private var mCallback: OnItemSelectedListener? = null

    var selectedManteinanceType: ManteinanceType?
        get() {
            if (_binding == null) return null
            val temp = binding.fragmentSpinner.selectedItem
            return when {
                temp != null -> {
                    val r = temp as ManteinanceType
                    when (r.manteinanceTypeId) {
                        0L -> null
                        else -> r
                    }
                }

                else -> null
            }
        }
        set(manteinanceType) {
            if (_binding == null) return
            if (manteinanceType == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as ManteinanceTypeAdapter
            for (i in 0 until adapter.count) {
                if (equals(manteinanceType, adapter.getItem(i))) {
                    binding.fragmentSpinner.setSelection(i)
                    break
                }
            }
        }

    val count: Int
        get() = when {
            _binding == null -> 0
            binding.fragmentSpinner.adapter != null -> binding.fragmentSpinner.adapter.count
            else -> 0
        }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putInt("oldPos", oldPos)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            oldPos = savedInstanceState.getInt("oldPos")
        }

        if (arguments != null) {
            allManteinanceType =
                requireArguments().getParcelableArrayList(ARG_ALL_MANTEINANCE_TYPE)
            showGeneralLevel = requireArguments().getBoolean(ARG_SHOW_GENERAL_LEVEL)
        }
    }

    private var _binding: FragmentSpinnerBinding? = null

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
        _binding = FragmentSpinnerBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.autoResizeTextView.visibility = View.GONE

        // Llenar el binding.fragmentSpinner
        fillAdapter()

        binding.fragmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (oldPos != position) {
                        oldPos = position
                        mCallback?.onItemSelected(parent.getItemAtPosition(position) as ManteinanceType)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    oldPos = -1
                    mCallback?.onItemSelected(null)
                }
            }

        return view
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        this.mCallback = null
    }

    override fun onStart() {
        super.onStart()
        if (activity is OnItemSelectedListener) {
            mCallback = activity as OnItemSelectedListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallback = null
    }

    private fun fillAdapter() {
        oldPos = -1
        allManteinanceType = ManteinanceTypeDbHelper().select()

        (allManteinanceType
            ?: return).sortWith { v1, v2 -> v1.description.compareTo(v2.description) }

        if (allManteinanceType == null || (allManteinanceType ?: return).size < 1) {
            allManteinanceType = ArrayList()
            (allManteinanceType ?: return).add(
                ManteinanceType(
                    0,
                    getString(R.string.no_maintenances),
                    true,
                    0
                )
            )
        } else if (showGeneralLevel) {
            (allManteinanceType ?: return).add(
                ManteinanceType(
                    0,
                    getString(R.string.nothing_selected),
                    true,
                    0
                )
            )
        }

        val spinnerArrayAdapter = ManteinanceTypeAdapter(
            custom_spinner_dropdown_item,
            allManteinanceType ?: return,
            binding.fragmentSpinner
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter
    }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(manteinanceType: ManteinanceType?)
    }

    companion object {
        private const val ARG_ALL_MANTEINANCE_TYPE = "allManteinanceType"
        private const val ARG_SHOW_GENERAL_LEVEL = "showGeneralLevel"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param allManteinanceType Parameter 1.
         * @return A new instance of fragment manteinanceType_spinner.
         */
        fun newInstance(
            allManteinanceType: ArrayList<ManteinanceType>,
            showGeneralLevel: Boolean,
        ): ManteinanceTypeSpinnerFragment {
            val fragment = ManteinanceTypeSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_ALL_MANTEINANCE_TYPE, Parcels.wrap(allManteinanceType))
            args.putBoolean(ARG_SHOW_GENERAL_LEVEL, showGeneralLevel)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}