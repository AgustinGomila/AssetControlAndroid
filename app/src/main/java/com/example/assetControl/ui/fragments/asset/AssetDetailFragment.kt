package com.example.assetControl.ui.fragments.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.databinding.AssetDetailFragmentBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AssetDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssetDetailFragment : DialogFragment() {
    private var asset: Asset? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            asset = requireArguments().parcelable("asset")
        }
    }

    private var _binding: AssetDetailFragmentBinding? = null

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
        _binding = AssetDetailFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        fillControls()
        return view
    }

    override fun onResume() {
        super.onResume()

        if (dialog != null) {
            (dialog ?: return).setTitle(getString(R.string.asset_detail))

            val params = ((dialog ?: return).window ?: return).attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            ((dialog ?: return).window ?: return).attributes = params as WindowManager.LayoutParams
        }
    }

    private fun fillControls() {
        val a = asset ?: return

        if (a.code.isEmpty()) {
            binding.codeAutoResizeTextView.text = ""
            binding.codeAutoResizeTextView.visibility = View.GONE
        } else {
            binding.codeAutoResizeTextView.text = a.code
            binding.codeAutoResizeTextView.visibility = View.VISIBLE
        }

        if (a.description.isEmpty()) {
            binding.descriptionAutoResizeTextView.text = ""
            binding.descriptionAutoResizeTextView.visibility = View.GONE
        } else {
            binding.descriptionAutoResizeTextView.text = a.description
            binding.descriptionAutoResizeTextView.visibility = View.VISIBLE
        }

        binding.statusAutoResizeTextView.text = a.assetStatus.description
        binding.statusAutoResizeTextView.visibility = View.VISIBLE

        if (a.warehouseStr.isEmpty()) {
            binding.warehouseAutoResizeTextView.text = ""
            binding.warehouseAutoResizeTextView.visibility = View.GONE
            binding.warehouseTextView.visibility = View.GONE
        } else {
            binding.warehouseAutoResizeTextView.text = a.warehouseAreaStr
            binding.warehouseAutoResizeTextView.visibility = View.VISIBLE
            binding.warehouseTextView.visibility = View.VISIBLE
        }

        if (a.warehouseAreaStr.isEmpty()) {
            binding.warehouseAreaAutoResizeTextView.text = ""
            binding.warehouseAreaAutoResizeTextView.visibility = View.GONE
            binding.warehouseAreaTextView.visibility = View.GONE
        } else {
            binding.warehouseAreaAutoResizeTextView.text = a.warehouseAreaStr
            binding.warehouseAreaAutoResizeTextView.visibility = View.VISIBLE
            binding.warehouseAreaTextView.visibility = View.VISIBLE
        }

        if (a.itemCategoryStr.isEmpty()) {
            binding.categoryAutoResizeTextView.text = ""
            binding.categoryAutoResizeTextView.visibility = View.GONE
            binding.categoryTextView.visibility = View.GONE
        } else {
            binding.categoryAutoResizeTextView.text = a.itemCategoryStr
            binding.categoryAutoResizeTextView.visibility = View.VISIBLE
            binding.categoryTextView.visibility = View.VISIBLE
        }

        if (a.serialNumber.isNullOrEmpty()) {
            binding.serialNumberAutoResizeTextView.text = ""
            binding.serialNumberAutoResizeTextView.visibility = View.GONE
            binding.serialNumberTextView.visibility = View.GONE
        } else {
            binding.serialNumberAutoResizeTextView.text = a.serialNumber
            binding.serialNumberAutoResizeTextView.visibility = View.VISIBLE
            binding.serialNumberTextView.visibility = View.VISIBLE
        }

        if (a.ean.isNullOrEmpty()) {
            binding.eanAutoResizeTextView.text = ""
            binding.eanAutoResizeTextView.visibility = View.GONE
            binding.eanTextView.visibility = View.GONE
        } else {
            binding.eanAutoResizeTextView.text = a.ean
            binding.eanAutoResizeTextView.visibility = View.VISIBLE
            binding.eanTextView.visibility = View.VISIBLE
        }

        if (a.manufacturer.isNullOrEmpty()) {
            binding.manufacturerAutoResizeTextView.text = ""
            binding.manufacturerAutoResizeTextView.visibility = View.GONE
            binding.manufacturerTextView.visibility = View.GONE
        } else {
            binding.manufacturerAutoResizeTextView.text = a.manufacturer
            binding.manufacturerAutoResizeTextView.visibility = View.VISIBLE
            binding.manufacturerTextView.visibility = View.VISIBLE
        }

        if (a.model.isNullOrEmpty()) {
            binding.modelAutoResizeTextView.text = ""
            binding.modelAutoResizeTextView.visibility = View.GONE
            binding.modelTextView.visibility = View.GONE
        } else {
            binding.modelAutoResizeTextView.text = a.model
            binding.modelAutoResizeTextView.visibility = View.VISIBLE
            binding.modelTextView.visibility = View.VISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param asset Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(asset: Asset): AssetDetailFragment {
            val fragment = AssetDetailFragment()

            val args = Bundle()
            args.putParcelable("asset", asset)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}