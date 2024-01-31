package com.dacosys.assetControl.ui.fragments.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.model.asset.AssetCondition
import com.dacosys.assetControl.ui.adapters.asset.AssetConditionAdapter
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AssetConditionSpinnerFragment.OnItemSelectedListener] interface
 * to handle interaction events.
 * Use the [AssetConditionSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssetConditionSpinnerFragment : Fragment() {
    private var allAssetCondition: ArrayList<AssetCondition>? = ArrayList()
    private var oldPos = -1
    private var mCallback: OnItemSelectedListener? = null

    var selectedAssetCondition: AssetCondition?
        get() {
            if (_binding == null) return null
            val temp = binding.fragmentSpinner.selectedItem
            return if (temp != null) {
                temp as AssetCondition
            } else null
        }
        set(assetCondition) {
            if (_binding == null) return
            if (assetCondition == null) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as AssetConditionAdapter
            for (i in 0 until adapter.count) {
                if (equals(assetCondition, adapter.getItem(i))) {
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
            allAssetCondition =
                requireArguments().getParcelableArrayList(ARG_ALL_ASSET_CONDITION)
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
                        mCallback?.onItemSelected(parent.getItemAtPosition(position) as AssetCondition)
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
        allAssetCondition = AssetCondition.getAll()

        val spinnerAssetConditionAdapter = AssetConditionAdapter(
            custom_spinner_dropdown_item,
            allAssetCondition ?: return,
            binding.fragmentSpinner
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerAssetConditionAdapter
    }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(assetCondition: AssetCondition?)
    }

    companion object {
        private const val ARG_ALL_ASSET_CONDITION = "allAssetCondition"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param allAssetCondition Parameter 1.
         * @return A new instance of fragment assetCondition_spinner.
         */
        fun newInstance(allAssetCondition: ArrayList<AssetCondition>): AssetConditionSpinnerFragment {
            val fragment = AssetConditionSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_ALL_ASSET_CONDITION, Parcels.wrap(allAssetCondition))

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}