package com.example.assetControl.ui.fragments.route

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.assetControl.R
import com.example.assetControl.databinding.RouteSelectFilterFragmentBinding
import com.example.assetControl.ui.activities.route.RouteSelectDialogActivity
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutBoolean

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RouteSelectFilterFragment.FragmentListener] interface
 * to handle interaction events.
 * Use the [RouteSelectFilterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RouteSelectFilterFragment : Fragment() {
    private var fragmentListener: FragmentListener? = null
    private var rejectNewInstances = false

    // Configuración guardada de los controles que se ven o no se ven
    var routeDescription: String = ""
    var onlyActive: Boolean = true

    // Container Activity must implement this interface
    interface FragmentListener {
        fun onFilterChanged(routeDescription: String, onlyActive: Boolean)
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
            routeDescription = routeDescription,
            onlyActive = onlyActive
        )
    }

    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    private fun saveSharedPreferences() {
        prefsPutBoolean(
            Preference.selectRouteOnlyActive.key,
            onlyActive
        )
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        saveSharedPreferences()
        this.fragmentListener = null
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
        routeDescription = b.getString(argRouteDescription) ?: ""
        if (b.containsKey(argOnlyActive)) onlyActive = b.getBoolean(argOnlyActive)
    }

    private fun loadDefaultValues() {
        routeDescription = ""
        onlyActive =
            prefsGetBoolean(Preference.selectRouteOnlyActive)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putBoolean(argOnlyActive, onlyActive)
        b.putString(argRouteDescription, routeDescription)
    }

    private var _binding: RouteSelectFilterFragmentBinding? = null

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
        _binding = RouteSelectFilterFragmentBinding.inflate(inflater, container, false)
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

            showRouteSelectDialog()
        }

        binding.descriptionSearchImageView.setOnClickListener { binding.descriptionTextView.performClick() }
        binding.descriptionClearImageView.setOnClickListener {
            routeDescription = ""

            setRouteText()
            sendMessage()
        }

        refreshViews()

        return view
    }

    private fun showRouteSelectDialog() {
        val intent = Intent(requireContext(), RouteSelectDialogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(argRouteDescription, routeDescription)
        intent.putExtra("title", getString(R.string.select_route))
        resultForRouteSelect.launch(intent)
    }

    private val resultForRouteSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK) {
                    routeDescription = (data ?: return@registerForActivityResult).getStringExtra(
                        argRouteDescription
                    ) ?: ""

                    setRouteText()
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
            setRouteText()
        }
    }

    private fun performChecked(isChecked: Boolean) {
        onlyActive = isChecked

        sendMessage()
    }

    private fun setRouteText() {
        activity?.runOnUiThread {
            if (routeDescription.isEmpty()) {
                binding.descriptionTextView.typeface = Typeface.DEFAULT
                binding.descriptionTextView.text = getString(R.string.search_by_description)
            } else {
                binding.descriptionTextView.typeface = Typeface.DEFAULT_BOLD

                binding.descriptionTextView.text = routeDescription
            }
        }
    }

    companion object {

        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val argRouteDescription = "routeDescription"
        private const val argOnlyActive = "onlyActive"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(routeDescription: String): RouteSelectFilterFragment {
            val fragment = RouteSelectFilterFragment()

            val args = Bundle()
            args.putBoolean(argOnlyActive, true)
            args.putString(argRouteDescription, routeDescription)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}