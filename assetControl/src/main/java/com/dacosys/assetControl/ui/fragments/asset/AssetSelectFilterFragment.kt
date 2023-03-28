package com.dacosys.assetControl.ui.fragments.asset

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.AssetSelectFilterFragmentBinding
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.ui.activities.asset.CodeSelectDialogActivity
import com.dacosys.assetControl.ui.activities.category.ItemCategorySelectActivity
import com.dacosys.assetControl.ui.activities.location.LocationSelectActivity
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetStringSet
import com.dacosys.assetControl.utils.Preferences.Companion.prefsPutBoolean
import com.dacosys.assetControl.utils.Preferences.Companion.prefsPutStringSet
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.Preference
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AssetSelectFilterFragment.FragmentListener] interface
 * to handle interaction events.
 * Use the [AssetSelectFilterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("UNCHECKED_CAST")
class AssetSelectFilterFragment : Fragment() {
    private var fragmentListener: FragmentListener? = null
    private var rejectNewInstances = false

    // Configuración guardada de los controles que se ven o no se ven
    var visibleStatusArray: ArrayList<AssetStatus> = ArrayList()
    var itemCode: String = ""
    var itemCategory: ItemCategory? = null
    var warehouseArea: WarehouseArea? = null
    var onlyActive: Boolean = true

    // Container Activity must implement this interface
    interface FragmentListener {
        fun onFilterChanged(
            code: String,
            itemCategory: ItemCategory?,
            warehouseArea: WarehouseArea?,
            onlyActive: Boolean,
        )
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
            code = itemCode,
            itemCategory = itemCategory,
            warehouseArea = warehouseArea,
            onlyActive = onlyActive
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
        prefsPutBoolean(
            Preference.selectAssetOnlyActive.key, onlyActive
        )
        val set = HashSet<String>()
        for (i in visibleStatusArray) set.add(i.id.toString())
        prefsPutStringSet(
            Preference.assetSelectFragmentVisibleStatus.key, set
        )
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
        itemCategory = b.getParcelable(argItemCategory)
        warehouseArea = b.getParcelable(argWarehouseArea)
        itemCode = b.getString(argItemCode) ?: ""
        if (b.containsKey(argOnlyActive)) onlyActive = b.getBoolean(argOnlyActive)

        visibleStatusArray.clear()
        if (b.containsKey("visibleStatusArray")) {
            val t3 = b.getParcelableArrayList<AssetStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            loadDefaultVisibleStatus()
        }
    }

    private fun loadDefaultValues() {
        warehouseArea = null
        itemCategory = null
        itemCode = ""
        onlyActive = prefsGetBoolean(Preference.selectAssetOnlyActive)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleStatusArray.clear()
        var set = prefsGetStringSet(
            Preference.assetSelectFragmentVisibleStatus.key,
            Preference.assetSelectFragmentVisibleStatus.defaultValue as ArrayList<String>
        )
        if (set == null) set = AssetStatus.getAllIdAsString().toSet()

        for (i in set) {
            val status = AssetStatus.getById(i.toInt())
            if (status != null && !visibleStatusArray.contains(status)) {
                visibleStatusArray.add(status)
            }
        }
    }

    private fun saveBundleValues(b: Bundle) {
        b.putBoolean(argOnlyActive, onlyActive)
        b.putString(argItemCode, itemCode)
        b.putParcelable(argItemCategory, itemCategory)
        b.putParcelable(argWarehouseArea, warehouseArea)
        b.putParcelableArrayList(argVisibleStatusArray, visibleStatusArray)
    }

    private var _binding: AssetSelectFilterFragmentBinding? = null

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
        _binding = AssetSelectFilterFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        }

        binding.onlyActiveSwitch.setOnCheckedChangeListener(null)
        binding.onlyActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            performChecked(isChecked)
        }

        binding.codeTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            showCodeSelectActivity()
        }

        binding.itemCategoryTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            showItemCategorySelectActivity()
        }

        binding.warehouseAreaTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            showLocationSelectActivity()
        }

        binding.codeSearchImageView.setOnClickListener { binding.codeTextView.performClick() }
        binding.codeClearImageView.setOnClickListener {
            itemCode = ""

            setCodeText()
            sendMessage()
        }
        binding.categorySearchImageView.setOnClickListener { binding.itemCategoryTextView.performClick() }
        binding.categoryClearImageView.setOnClickListener {
            itemCategory = null

            setCategoryText()
            sendMessage()
        }
        binding.warehouseAreaSearchImageView.setOnClickListener { binding.warehouseAreaTextView.performClick() }
        binding.warehouseAreaClearImageView.setOnClickListener {
            warehouseArea = null

            setWarehouseAreaText()
            sendMessage()
        }

        refreshViews()

        return view
    }

    private fun showCodeSelectActivity() {
        val intent = Intent(requireContext(), CodeSelectDialogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("title", getString(R.string.search_by_code_description_ean))
        intent.putExtra(argItemCode, itemCode)
        intent.putExtra(argVisibleStatusArray, visibleStatusArray)
        intent.putExtra("onlyActive", onlyActive)
        resultForItemSelect.launch(intent)
    }

    private val resultForItemSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    itemCode = data.getStringExtra(argItemCode) ?: return@registerForActivityResult

                    setCodeText()
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
        intent.putExtra(argWarehouseArea, warehouseArea)
        intent.putExtra("title", getString(R.string.select_location))
        intent.putExtra("warehouseVisible", true)
        intent.putExtra("warehouseAreaVisible", true)
        intent.putExtra("onlyActive", onlyActive)
        resultForLocationSelect.launch(intent)
    }

    private val resultForLocationSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    warehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra(argWarehouseArea))
                            ?: return@registerForActivityResult

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

    private fun showItemCategorySelectActivity() {
        val intent = Intent(requireContext(), ItemCategorySelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(argItemCategory, itemCategory)
        intent.putExtra("title", getString(R.string.select_category))
        intent.putExtra("onlyActive", onlyActive)
        resultForCategorySelect.launch(intent)
    }

    private val resultForCategorySelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    itemCategory =
                        Parcels.unwrap<ItemCategory>(data.getParcelableExtra(argItemCategory))
                            ?: return@registerForActivityResult

                    setCategoryText()
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

            setCodeText()
            setCategoryText()
            setWarehouseAreaText()
        }
    }

    private fun performChecked(isChecked: Boolean) {
        onlyActive = isChecked

        sendMessage()
    }

    private fun setCodeText() {
        activity?.runOnUiThread {
            if (itemCode.isEmpty()) {
                binding.codeTextView.typeface = Typeface.DEFAULT
                binding.codeTextView.text = getString(R.string.search_by_code_description_ean)
            } else {
                binding.codeTextView.typeface = Typeface.DEFAULT_BOLD
                binding.codeTextView.text = itemCode
            }
        }
    }

    private fun setCategoryText() {
        activity?.runOnUiThread {
            if (itemCategory == null) {
                binding.itemCategoryTextView.typeface = Typeface.DEFAULT
                binding.itemCategoryTextView.text = getString(R.string.search_by_category)
            } else {
                binding.itemCategoryTextView.typeface = Typeface.DEFAULT_BOLD
                binding.itemCategoryTextView.text =
                    (itemCategory ?: return@runOnUiThread).description
            }
        }
    }

    private fun setWarehouseAreaText() {
        activity?.runOnUiThread {
            if (warehouseArea == null) {
                binding.warehouseAreaTextView.typeface = Typeface.DEFAULT
                binding.warehouseAreaTextView.text = getString(R.string.search_by_area)
            } else {
                binding.warehouseAreaTextView.typeface = Typeface.DEFAULT_BOLD
                binding.warehouseAreaTextView.text =
                    (warehouseArea ?: return@runOnUiThread).description
            }
        }
    }

    companion object {

        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val argItemCode = "itemCode"
        private const val argItemCategory = "itemCategory"
        private const val argWarehouseArea = "warehouseArea"
        private const val argOnlyActive = "onlyActive"
        private const val argVisibleStatusArray = "visibleStatusArray"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            itemCode: String,
            itemCategory: ItemCategory?,
            warehouseArea: WarehouseArea?,
        ): AssetSelectFilterFragment {
            val fragment = AssetSelectFilterFragment()

            val args = Bundle()
            args.putBoolean(argOnlyActive, true)
            args.putString(argItemCode, itemCode)
            args.putParcelable(argItemCategory, itemCategory)
            args.putParcelable(argWarehouseArea, warehouseArea)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}