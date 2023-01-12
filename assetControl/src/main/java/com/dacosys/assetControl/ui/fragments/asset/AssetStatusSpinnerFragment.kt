package com.dacosys.assetControl.ui.fragments.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.adapters.asset.AssetStatusAdapter
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.model.asset.AssetStatus
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AssetStatusSpinnerFragment.OnItemSelectedListener] interface
 * to handle interaction events.
 * Use the [AssetStatusSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssetStatusSpinnerFragment : Fragment() {
    private var allAssetStatus: ArrayList<AssetStatus>? = ArrayList()
    private var oldPos = -1
    private var mCallback: OnItemSelectedListener? = null

    var selectedAssetStatus: AssetStatus?
        get() {
            if (_binding == null) return null
            val temp = binding.fragmentSpinner.selectedItem
            return when {
                temp != null -> {
                    val r = temp as AssetStatus
                    when (r.id) {
                        0 -> null
                        else -> r
                    }
                }
                else -> null
            }
        }
        set(assetStatus) {
            if (_binding == null) return
            if (assetStatus == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as AssetStatusAdapter
            for (i in 0 until adapter.count) {
                if (equals(assetStatus, adapter.getItem(i))) {
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
            allAssetStatus = requireArguments().getParcelableArrayList(ARG_ALL_ASSET_STATUS)
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
                        mCallback?.onItemSelected(parent.getItemAtPosition(position) as AssetStatus)
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
        if (mCallback is OnItemSelectedListener) {
            mCallback = activity as OnItemSelectedListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallback = null
    }

    private fun fillAdapter() {
        oldPos = -1
        allAssetStatus = AssetStatus.getAll()

        val spinnerArrayAdapter = AssetStatusAdapter(
            custom_spinner_dropdown_item,
            allAssetStatus ?: return,
            binding.fragmentSpinner
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter
    }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(assetStatus: AssetStatus?)
    }

    companion object {
        private const val ARG_ALL_ASSET_STATUS = "allAssetStatus"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param allAssetStatus Parameter 1.
         * @return A new instance of fragment assetStatus_spinner.
         */
        fun newInstance(allAssetStatus: ArrayList<AssetStatus>): AssetStatusSpinnerFragment {
            val fragment = AssetStatusSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_ALL_ASSET_STATUS, Parcels.wrap(allAssetStatus))

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}