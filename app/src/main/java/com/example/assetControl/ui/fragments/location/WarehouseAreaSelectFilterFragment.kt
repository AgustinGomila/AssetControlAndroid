package com.example.assetControl.ui.fragments.location

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.R
import com.example.assetControl.databinding.WarehouseAreaSelectFilterFragmentBinding
import com.example.assetControl.ui.activities.location.LocationSelectActivity
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.Preference

class WarehouseAreaSelectFilterFragment : Fragment() {
    private var fragmentListener: FragmentListener? = null
    private var rejectNewInstances = false

    var waDescription: String = ""
    var wDescription: String = ""
    var onlyActive: Boolean = true

    // Container Activity must implement this interface
    interface FragmentListener {
        fun onFilterChanged(waDescription: String, wDescription: String, onlyActive: Boolean)
    }

    override fun onStart() {
        super.onStart()
        if (fragmentListener is FragmentListener) {
            fragmentListener = activity as FragmentListener
        }
        sendMessage()
    }

    private fun sendMessage() {
        fragmentListener?.onFilterChanged(
            waDescription = waDescription, wDescription = wDescription, onlyActive = onlyActive
        )
    }

    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        saveSharedPreferences()
        this.fragmentListener = null
    }

    private fun saveSharedPreferences() {
        sr.prefsPutBoolean(
            Preference.selectWarehouseAreaOnlyActive.key, onlyActive
        )
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            loadBundleValues(requireArguments())
        } else {
            loadDefaultValues()
        }
    }

    private fun loadBundleValues(b: Bundle) {
        wDescription = b.getString(argWarehouseStr) ?: ""
        waDescription = b.getString(argWarehouseAreaStr) ?: ""
        if (b.containsKey(argOnlyActive)) onlyActive = b.getBoolean(argOnlyActive)
    }

    private fun loadDefaultValues() {
        wDescription = ""
        waDescription = ""
        onlyActive = sr.prefsGetBoolean(Preference.selectWarehouseAreaOnlyActive)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString(argWarehouseStr, wDescription)
        b.putString(argWarehouseAreaStr, waDescription)
        b.putBoolean(argOnlyActive, onlyActive)
    }

    private var _binding: WarehouseAreaSelectFilterFragmentBinding? = null

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
        _binding = WarehouseAreaSelectFilterFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        }

        binding.onlyActiveSwitch.setOnCheckedChangeListener(null)
        binding.onlyActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            performChecked(
                isChecked
            )
        }

        binding.descriptionTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            showLocationSelectActivity()
        }

        binding.warehouseTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("title", getString(R.string.select_warehouse))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", false)
            resultForWarehouseSelect.launch(intent)
        }

        binding.descriptionSearchImageView.setOnClickListener { binding.descriptionTextView.performClick() }
        binding.descriptionClearImageView.setOnClickListener {
            waDescription = ""

            setWarehouseAreaText()
            sendMessage()
        }
        binding.warehouseSearchImageView.setOnClickListener { binding.warehouseTextView.performClick() }
        binding.warehouseClearImageView.setOnClickListener {
            wDescription = ""

            setWarehouseText()
            sendMessage()
        }

        refreshViews()

        return view
    }

    private val resultForWarehouseSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    wDescription = data.getStringExtra(argWarehouseStr) ?: ""
                    setWarehouseText()
                    sendMessage()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun showLocationSelectActivity() {
        val intent = Intent(requireContext(), LocationSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("title", getString(R.string.select_warehouse_area))
        intent.putExtra("warehouseVisible", false)
        intent.putExtra("warehouseAreaVisible", true)
        resultForWarehouseAreaSelect.launch(intent)
    }

    private val resultForWarehouseAreaSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    waDescription = data.getStringExtra(argWarehouseAreaStr) ?: ""
                    setWarehouseAreaText()
                    sendMessage()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    fun setListener(listener: FragmentListener) {
        this.fragmentListener = listener
    }

    fun refreshViews() {
        activity?.runOnUiThread {
            binding.onlyActiveSwitch.setOnCheckedChangeListener(null)
            binding.onlyActiveSwitch.isChecked = onlyActive
            binding.onlyActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
                performChecked(
                    isChecked
                )
            }

            setWarehouseAreaText()
            setWarehouseText()
        }
    }

    private fun performChecked(isChecked: Boolean) {
        onlyActive = isChecked

        sendMessage()
    }

    private fun setWarehouseAreaText() {
        activity?.runOnUiThread {
            if (waDescription.isEmpty()) {
                binding.descriptionTextView.typeface = Typeface.DEFAULT
                binding.descriptionTextView.text = getString(R.string.search_by_area)
            } else {
                binding.descriptionTextView.typeface = Typeface.DEFAULT_BOLD
                binding.descriptionTextView.text = waDescription
            }
        }
    }

    private fun setWarehouseText() {
        activity?.runOnUiThread {
            if (wDescription.isEmpty()) {
                binding.warehouseTextView.typeface = Typeface.DEFAULT
                binding.warehouseTextView.text = getString(R.string.search_by_warehouse)
            } else {
                binding.warehouseTextView.typeface = Typeface.DEFAULT_BOLD
                binding.warehouseTextView.text = wDescription
            }
        }
    }

    companion object {

        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val argWarehouseAreaStr = "warehouseAreaDescription"
        private const val argWarehouseStr = "warehouseDescription"
        private const val argOnlyActive = "onlyActive"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            waDescription: String,
            wDescription: String,
        ): WarehouseAreaSelectFilterFragment {
            val fragment = WarehouseAreaSelectFilterFragment()

            val args = Bundle()
            args.putBoolean(argOnlyActive, true)
            args.putString(argWarehouseAreaStr, waDescription)
            args.putString(argWarehouseStr, wDescription)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}