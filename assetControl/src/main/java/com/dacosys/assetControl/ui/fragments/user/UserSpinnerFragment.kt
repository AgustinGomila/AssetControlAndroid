package com.dacosys.assetControl.ui.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.R.layout.custom_spinner_dropdown_item
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.repository.user.UserRepository
import com.dacosys.assetControl.databinding.FragmentSpinnerBinding
import com.dacosys.assetControl.ui.adapters.user.UserAdapter
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList
import org.parceler.Parcels

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [UserSpinnerFragment.OnItemSelectedListener] interface
 * to handle interaction events.
 * Use the [UserSpinnerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserSpinnerFragment : Fragment() {
    private var allUser: ArrayList<User>? = ArrayList()
    private val defaultValue = -1L
    private var showGeneralLevel = false
    private var oldPos = -1
    private var mCallback: OnItemSelectedListener? = null

    val selectedUserPass: String
        get() {
            if (_binding == null) return ""
            val temp = binding.fragmentSpinner.selectedItem
            return when {
                temp != null -> {
                    val r = temp as User
                    when {
                        r.id <= 0 -> ""
                        else -> r.password.orEmpty()
                    }
                }

                else -> ""
            }
        }

    var selectedUserId: Long?
        get() {
            if (_binding == null) return defaultValue
            val temp = binding.fragmentSpinner.selectedItem
            return when {
                temp != null -> {
                    val r = temp as User
                    when (r.id) {
                        0L -> null
                        else -> r.id
                    }
                }

                else -> null
            }
        }
        set(id) {
            if (_binding == null) return
            if (id == null || id <= 0) {
                binding.fragmentSpinner.setSelection(0)
                return
            }

            val adapter = binding.fragmentSpinner.adapter as UserAdapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == null) {
                    continue
                }

                if (equals(id, adapter.getItem(i)!!.id)) {
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
            allUser = requireArguments().parcelableArrayList(ARG_ALL_USER)
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
                        mCallback?.onItemSelected(parent.getItemAtPosition(position) as User)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    oldPos = defaultValue.toInt()
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

    fun fillAdapter(): Boolean {
        if (_binding == null) return false

        oldPos = defaultValue.toInt()
        var result = true

        allUser = ArrayList(UserRepository().select())

        allUser!!.sortWith { v1, v2 -> v1.name.compareTo(v2.name) }

        if (allUser == null || allUser!!.isEmpty()) {
            result = false
            allUser = ArrayList()
            allUser!!.add(
                0,
                User(
                    id = 0,
                    name = getString(R.string.no_users),
                    externalId = "",
                    email = "",
                    active = 1,
                    password = ""
                )
            )
        } else if (showGeneralLevel) {
            allUser!!.add(
                0,
                User(
                    id = 0,
                    name = getString(R.string.nothing_selected),
                    externalId = "",
                    email = "",
                    active = 1,
                    password = ""
                )
            )
        }

        allUser!!.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        val spinnerArrayAdapter = UserAdapter(
            custom_spinner_dropdown_item,
            allUser!!,
            binding.fragmentSpinner
        )

        // Step 3: Tell the binding.fragmentSpinner about our adapter
        binding.fragmentSpinner.adapter = spinnerArrayAdapter
        return result
    }

    // Container Activity must implement this interface
    interface OnItemSelectedListener {
        fun onItemSelected(user: User?)
    }

    companion object {
        private const val ARG_ALL_USER = "allUser"
        private const val ARG_SHOW_GENERAL_LEVEL = "showGeneralLevel"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param allUser Parameter 1.
         * @return A new instance of fragment user_spinner.
         */
        fun newInstance(allUser: ArrayList<User>, showGeneralLevel: Boolean): UserSpinnerFragment {
            val fragment = UserSpinnerFragment()

            val args = Bundle()
            args.putParcelable(ARG_ALL_USER, Parcels.wrap(allUser))
            args.putBoolean(ARG_SHOW_GENERAL_LEVEL, showGeneralLevel)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}