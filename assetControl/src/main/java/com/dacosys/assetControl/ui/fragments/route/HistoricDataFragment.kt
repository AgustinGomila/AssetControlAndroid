package com.dacosys.assetControl.ui.fragments.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.data.model.dataCollection.DataCollectionContent
import com.dacosys.assetControl.databinding.DccHistoricDataFragmentBinding

class HistoricDataFragment : Fragment() {
    private var dccArrayList: ArrayList<DataCollectionContent> = ArrayList()

    private var oldValue1: String = ""
    private var oldValue2: String = ""
    private var oldValue3: String = ""
    private var oldValue4: String = ""

    private var oldDate1: String = ""
    private var oldDate2: String = ""
    private var oldDate3: String = ""
    private var oldDate4: String = ""

    private fun destroyLocals() {
        dccArrayList.clear()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("oldDate1", oldDate1)
        savedInstanceState.putString("oldDate2", oldDate2)
        savedInstanceState.putString("oldDate3", oldDate3)
        savedInstanceState.putString("oldDate4", oldDate4)
        savedInstanceState.putString("oldValue1", oldValue1)
        savedInstanceState.putString("oldValue2", oldValue2)
        savedInstanceState.putString("oldValue3", oldValue3)
        savedInstanceState.putString("oldValue4", oldValue4)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            dccArrayList = requireArguments().getParcelableArrayList("dccArrayList")
                ?: ArrayList()
        }

        if (savedInstanceState != null) {
            oldDate1 = savedInstanceState.getString("oldDate1") ?: ""
            oldDate2 = savedInstanceState.getString("oldDate2") ?: ""
            oldDate3 = savedInstanceState.getString("oldDate3") ?: ""
            oldDate4 = savedInstanceState.getString("oldDate4") ?: ""
            oldValue1 = savedInstanceState.getString("oldValue1") ?: ""
            oldValue2 = savedInstanceState.getString("oldValue2") ?: ""
            oldValue3 = savedInstanceState.getString("oldValue3") ?: ""
            oldValue4 = savedInstanceState.getString("oldValue4") ?: ""
        }
    }

    private var _binding: DccHistoricDataFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()

        destroyLocals()
        _binding = null
    }

    private var _justRefresh = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DccHistoricDataFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        _justRefresh = savedInstanceState != null

        return view
    }

    override fun onStart() {
        super.onStart()

        fillPreviousData(_justRefresh)
    }

    private fun fillPreviousData(restoreState: Boolean) {
        if (!restoreState) {
            if (!dccArrayList.any()) {
                clearPreviousData()
                return
            } else {
                var z = 0
                for (x in dccArrayList) {
                    when (z) {
                        0 -> {
                            oldDate1 = x.dataCollectionDate
                            oldValue1 = x.valueStr
                        }

                        1 -> {
                            oldDate2 = x.dataCollectionDate
                            oldValue2 = x.valueStr
                        }

                        2 -> {
                            oldDate3 = x.dataCollectionDate
                            oldValue3 = x.valueStr
                        }

                        3 -> {
                            oldDate4 = x.dataCollectionDate
                            oldValue4 = x.valueStr
                        }
                    }

                    z++
                    if (z == 4) {
                        break
                    }
                }
            }
        }
        refreshPreviousData()
    }

    fun refreshPreviousData() {
        clearPreviousData()
        if (oldValue1.isEmpty() &&
            oldValue2.isEmpty() &&
            oldValue3.isEmpty() &&
            oldValue4.isEmpty() &&
            oldDate1.isEmpty() &&
            oldDate2.isEmpty() &&
            oldDate3.isEmpty() &&
            oldDate4.isEmpty()
        ) {
            return
        }

        if (_binding == null) return
        binding.noPreviousDataTextView.visibility = INVISIBLE

        binding.oldDateTextView1.setText(oldDate1, TextView.BufferType.EDITABLE)
        binding.oldValueTextView1.setText(oldValue1, TextView.BufferType.EDITABLE)

        binding.oldDateTextView2.setText(oldDate2, TextView.BufferType.EDITABLE)
        binding.oldValueTextView2.setText(oldValue2, TextView.BufferType.EDITABLE)

        binding.oldDateTextView3.setText(oldDate3, TextView.BufferType.EDITABLE)
        binding.oldValueTextView3.setText(oldValue3, TextView.BufferType.EDITABLE)

        binding.oldDateTextView4.setText(oldDate4, TextView.BufferType.EDITABLE)
        binding.oldValueTextView4.setText(oldValue4, TextView.BufferType.EDITABLE)
    }

    fun clearPreviousData() {
        if (_binding == null) return
        binding.noPreviousDataTextView.visibility = VISIBLE

        binding.oldDateTextView1.setText("", TextView.BufferType.EDITABLE)
        binding.oldValueTextView1.setText("", TextView.BufferType.EDITABLE)

        binding.oldDateTextView2.setText("", TextView.BufferType.EDITABLE)
        binding.oldValueTextView2.setText("", TextView.BufferType.EDITABLE)

        binding.oldDateTextView3.setText("", TextView.BufferType.EDITABLE)
        binding.oldValueTextView3.setText("", TextView.BufferType.EDITABLE)

        binding.oldDateTextView4.setText("", TextView.BufferType.EDITABLE)
        binding.oldValueTextView4.setText("", TextView.BufferType.EDITABLE)
    }

    fun setDccArray(dccArrayList: ArrayList<DataCollectionContent>) {
        this.dccArrayList = dccArrayList
        fillPreviousData(false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param dccArrayList Parameter 1.
         * @return A new instance of fragment strOption_spinner.
         */
        fun newInstance(dccArrayList: ArrayList<DataCollectionContent>?): HistoricDataFragment {
            val fragment = HistoricDataFragment()

            val args = Bundle()
            args.putParcelableArrayList("dccArrayList", dccArrayList)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}