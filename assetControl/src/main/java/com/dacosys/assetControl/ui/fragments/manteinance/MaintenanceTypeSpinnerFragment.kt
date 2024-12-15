package com.dacosys.assetControl.ui.fragments.manteinance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.data.room.dto.maintenance.MaintenanceType
import com.dacosys.assetControl.data.room.repository.maintenance.MaintenanceTypeRepository
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.ui.adapters.manteinance.MaintenanceTypeAdapter
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MaintenanceTypeSpinnerFragment.OnItemSelectedListener] interface
 * to handle interaction events.
 * Use the [MaintenanceTypeSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaintenanceTypeSpinnerFragment : Fragment() {
    private var types: ArrayList<MaintenanceType>? = ArrayList()
    private var showGeneralLevel = true
    private var oldPos = -1
    private var mCallback: OnItemSelectedListener? = null

    var selectedId: Long?
        get() {
            if (_binding == null) return null
            return when (val temp = binding.fragmentSpinner.selectedItemId) {
                0L -> null
                else -> temp
            }
        }
        set(id) {
            if (_binding == null) return
            if (id == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as MaintenanceTypeAdapter
            for (i in 0 until adapter.count) {
                if (equals(id, adapter.getItem(i)?.id)) {
                    binding.fragmentSpinner.setSelection(i)
                    break
                }
            }
        }

    var selectedType: MaintenanceType?
        get() {
            if (_binding == null) return null
            val temp = binding.fragmentSpinner.selectedItem
            return when {
                temp != null -> {
                    val r = temp as MaintenanceType
                    when (r.id) {
                        0L -> null
                        else -> r
                    }
                }

                else -> null
            }
        }
        set(type) {
            if (_binding == null) return
            if (type == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as MaintenanceTypeAdapter
            for (i in 0 until adapter.count) {
                if (equals(type, adapter.getItem(i))) {
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
            types =
                requireArguments().parcelableArrayList(ARG_ALL_MANTEINANCE_TYPE)
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
                        mCallback?.onItemSelected(parent.getItemAtPosition(position) as MaintenanceType)
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
        types = ArrayList(MaintenanceTypeRepository().getAll())

        (types
            ?: return).sortWith { v1, v2 -> v1.description.compareTo(v2.description) }

        if (types == null || (types ?: return).isEmpty()) {
            types = ArrayList()
            (types ?: return).add(
                MaintenanceType(
                    id = 0,
                    description = getString(R.string.no_maintenances),
                    active = 1,
                    maintenanceTypeGroupId = 0
                )
            )
        } else if (showGeneralLevel) {
            (types ?: return).add(
                MaintenanceType(
                    id = 0,
                    description = getString(R.string.nothing_selected),
                    active = 1,
                    maintenanceTypeGroupId = 0
                )
            )
        }

        val spinnerArrayAdapter = MaintenanceTypeAdapter(
            custom_spinner_dropdown_item,
            types ?: return,
            binding.fragmentSpinner
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter
    }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(maintenanceType: MaintenanceType?)
    }

    companion object {
        private const val ARG_ALL_MANTEINANCE_TYPE = "allManteinanceType"
        private const val ARG_SHOW_GENERAL_LEVEL = "showGeneralLevel"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param allMaintenanceType Parameter 1.
         * @return A new instance of fragment manteinanceType_spinner.
         */
        fun newInstance(
            allMaintenanceType: ArrayList<MaintenanceType>,
            showGeneralLevel: Boolean,
        ): MaintenanceTypeSpinnerFragment {
            val fragment = MaintenanceTypeSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_ALL_MANTEINANCE_TYPE, Parcels.wrap(allMaintenanceType))
            args.putBoolean(ARG_SHOW_GENERAL_LEVEL, showGeneralLevel)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}