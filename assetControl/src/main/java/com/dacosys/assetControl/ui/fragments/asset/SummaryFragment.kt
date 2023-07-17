package com.dacosys.assetControl.ui.fragments.asset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.databinding.SummaryFragmentBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SummaryFragment : Fragment() {

    var total: Int = 0
    var checked: Int = 0
    var onInventory: Int = 0
    var missing: Int = 0
    var removed: Int = 0

    fun fill(
        total: Int? = null,
        checked: Int? = null,
        onInventory: Int? = null,
        missing: Int? = null,
        removed: Int? = null
    ) {
        if (total != null) this.total = total
        if (checked != null) this.checked = checked
        if (onInventory != null) this.onInventory = onInventory
        if (missing != null) this.missing = missing
        if (removed != null) this.removed = removed

        fillControls()
    }

    fun setTitles(totalTitle: String, selectedTitle: String) {
        binding.totalLabelTextView.text = totalTitle
        binding.selectedLabelTextView.text = selectedTitle
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) loadBundleValues(requireArguments()) else loadDefaultValues()
    }

    private fun loadBundleValues(b: Bundle) {
        total = b.getInt("total")
        checked = b.getInt("checked")
        onInventory = b.getInt("onInventory")
        missing = b.getInt("missing")
        removed = b.getInt("removed")
    }

    private fun loadDefaultValues() {
        total = 0
        checked = 0
        onInventory = 0
        missing = 0
        removed = 0
    }

    private fun saveBundleValues(b: Bundle) {
        b.putInt("total", total)
        b.putInt("checked", checked)
        b.putInt("onInventory", onInventory)
        b.putInt("missing", missing)
        b.putInt("removed", removed)
    }

    private var _binding: SummaryFragmentBinding? = null

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
        _binding = SummaryFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        }

        fillControls()

        return view
    }

    private fun fillControls() {
        binding.totalTextView.text = total.toString()
        binding.selectedTextView.text = checked.toString()
        binding.onInventoryTextView.text = onInventory.toString()
        binding.missedTextView.text = missing.toString()
        binding.removedTextView.text = removed.toString()
    }

    companion object {
        fun newInstance(
            total: Int,
            checked: Int,
            onInventory: Int,
            missing: Int,
            removed: Int
        ): SummaryFragment {
            val fragment = SummaryFragment()

            val args = Bundle()
            args.putInt("total", total)
            args.putInt("checked", checked)
            args.putInt("onInventory", onInventory)
            args.putInt("missing", missing)
            args.putInt("removed", removed)

            fragment.arguments = args

            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}