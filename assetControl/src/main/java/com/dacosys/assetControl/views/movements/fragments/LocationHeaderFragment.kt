package com.dacosys.assetControl.views.movements.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.LocationHeaderFragmentBinding
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.locations.locationSelect.LocationSelectActivity
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Use the [LocationHeaderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationHeaderFragment : Fragment() {
    interface LocationChangedListener {
        fun onLocationChanged(
            warehouse: Warehouse,
            warehouseArea: WarehouseArea,
        )
    }

    private var locationChangedListener: LocationChangedListener? = null

    var tempTitle = ""
    var warehouse: Warehouse? = null
    var warehouseArea: WarehouseArea? = null

    private var showChangePosButton: Boolean = true

    fun setChangeLocationListener(listener: LocationChangedListener) {
        this.locationChangedListener = listener
    }

    fun showChangePostButton(show: Boolean) {
        showChangePosButton = show
        setButtonPanelVisibility()
    }

    fun setTitle(title: String) {
        tempTitle = title
    }

    fun fill(warehouseAreaId: Long) {
        this.warehouseArea = WarehouseAreaDbHelper().selectById(warehouseAreaId)
        if (warehouseArea != null) {
            fill(warehouseArea ?: return)
        }
    }

    fun fill(warehouseArea: WarehouseArea) {
        this.warehouseArea = warehouseArea
        this.warehouse = WarehouseDbHelper().selectById(warehouseArea.warehouseId)

        fillControls()
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
        warehouse = b.getParcelable("warehouse")
        warehouseArea = b.getParcelable("warehouseArea")
        tempTitle = b.getString("title") ?: ""
    }

    private fun loadDefaultValues() {
        warehouseArea = null
        warehouse = null
        tempTitle = ""
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putParcelable("warehouse", warehouse)
        b.putParcelable("warehouseArea", warehouseArea)
    }

    private var _binding: LocationHeaderFragmentBinding? = null

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
        _binding = LocationHeaderFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        }

        binding.titleTextView.text = tempTitle

        binding.changePositionButton.setOnClickListener { changePosition() }
        TooltipCompat.setTooltipText(
            binding.changePositionButton,
            getString(R.string.change_position)
        )

        setButtonPanelVisibility()

        fillControls()

        return view
    }

    private fun setButtonPanelVisibility() {
        if (_binding == null) return
        if (showChangePosButton) {
            binding.changePosPanel.visibility = VISIBLE
        } else {
            binding.changePosPanel.visibility = GONE
        }
    }

    private fun changePosition() {
        val intent = Intent(context, LocationSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("title", getString(R.string.select_warehouse_area))
        intent.putExtra("warehouseVisible", true)
        intent.putExtra("warehouseAreaVisible", true)
        resultForLocationSelect.launch(intent)
    }

    private val resultForLocationSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val wa = Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))

                    if (wa == null || wa == warehouseArea) {
                        return@registerForActivityResult
                    }

                    warehouseArea = wa
                    warehouse = WarehouseDbHelper().selectById(wa.warehouseId)

                    fillControls()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            }
        }

    private fun clearDestinationControls() {
        if (_binding == null) return
        binding.warehouseTextView.text = ""
        TooltipCompat.setTooltipText(binding.warehouseTextView, "")

        binding.warehouseAreaTextView.text = ""
        TooltipCompat.setTooltipText(binding.warehouseAreaTextView, "")
    }

    private fun fillControls() {
        clearDestinationControls()

        if (_binding != null) {
            if (warehouse != null) {
                binding.warehouseTextView.text = (warehouse ?: return).description
                TooltipCompat.setTooltipText(
                    binding.warehouseTextView,
                    (warehouse ?: return).description
                )
            }

            if (warehouseArea != null) {
                binding.warehouseAreaTextView.text = (warehouseArea ?: return).description
                TooltipCompat.setTooltipText(
                    binding.warehouseAreaTextView,
                    (warehouseArea ?: return).description
                )
            }
        }

        if (locationChangedListener != null) {
            locationChangedListener?.onLocationChanged(
                warehouse = warehouse ?: return,
                warehouseArea = warehouseArea ?: return
            )
        }
    }

    companion object {

        fun newInstance(
            locationChangedListener: LocationChangedListener,
            warehouseArea: WarehouseArea,
        ): LocationHeaderFragment {
            val fragment = LocationHeaderFragment()

            val args = Bundle()
            args.putParcelable("warehouseArea", warehouseArea)

            fragment.arguments = args
            fragment.locationChangedListener = locationChangedListener

            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}