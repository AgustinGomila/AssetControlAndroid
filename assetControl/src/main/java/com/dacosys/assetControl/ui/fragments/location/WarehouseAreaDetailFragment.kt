package com.dacosys.assetControl.ui.fragments.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.model.location.WarehouseArea
import com.dacosys.assetControl.databinding.WarehouseAreaDetailFragmentBinding

/**
 * A simple [Fragment] subclass.
 * Use the [WarehouseAreaDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WarehouseAreaDetailFragment : DialogFragment() {

    private var warehouseArea: WarehouseArea? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            warehouseArea = requireArguments().getParcelable("warehouseArea")
        }
    }

    private var _binding: WarehouseAreaDetailFragmentBinding? = null

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
        _binding = WarehouseAreaDetailFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        fillControls()
        return view
    }

    override fun onResume() {
        super.onResume()

        if (dialog != null) {
            (dialog ?: return).setTitle(getString(R.string.warehouse_area_detail))

            val params = ((dialog ?: return).window ?: return).attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            ((dialog ?: return).window ?: return).attributes = params as WindowManager.LayoutParams
        }
    }

    private fun fillControls() {
        if (warehouseArea != null) {
            if (warehouseArea!!.description.isEmpty()) {
                binding.descriptionAutoResizeTextView.text = ""
                binding.descriptionAutoResizeTextView.visibility = View.GONE
            } else {
                binding.descriptionAutoResizeTextView.text = warehouseArea!!.description
                binding.descriptionAutoResizeTextView.visibility = View.VISIBLE
            }

            if (warehouseArea!!.warehouseStr.isEmpty()) {
                binding.warehouseAutoResizeTextView.text = ""
                binding.warehouseAutoResizeTextView.visibility = View.GONE
                binding.warehouseTextView.visibility = View.GONE
            } else {
                binding.warehouseAutoResizeTextView.text = warehouseArea!!.warehouseStr
                binding.warehouseAutoResizeTextView.visibility = View.VISIBLE
                binding.warehouseTextView.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param warehouseArea Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(warehouseArea: WarehouseArea): WarehouseAreaDetailFragment {
            val fragment = WarehouseAreaDetailFragment()

            val args = Bundle()
            args.putParcelable("warehouseArea", warehouseArea)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}