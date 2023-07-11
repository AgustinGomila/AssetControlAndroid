package com.dacosys.assetControl.ui.fragments.print

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.*
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.databinding.PrinterFragmentBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.model.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.model.barcode.fields.AssetLabelField
import com.dacosys.assetControl.model.barcode.fields.BarcodeLabel
import com.dacosys.assetControl.model.barcode.fields.WarehouseAreaLabelField
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.dacosys.assetControl.ui.activities.location.WarehouseAreaPrintLabelActivity
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.activities.print.TemplateSelectDialogActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.ConfigHelper
import com.dacosys.assetControl.utils.Printer.Companion.printerBluetoothDevice
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.CounterHandler
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetLong
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutLong
import com.dacosys.assetControl.utils.settings.Preference
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PrinterFragment.FragmentListener] interface
 * to handle interaction events.
 * Use the [PrinterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PrinterFragment : Fragment(), Runnable, CounterHandler.CounterListener {
    private var fragmentListener: FragmentListener? = null
    private var rejectNewInstances = false

    private var ch: CounterHandler? = null

    private var isReturnedFromSettings = false

    // Configuración guardada de los controles que se ven o no se ven
    private var printer: String = ""
    var barcodeLabelCustom: BarcodeLabelCustom? = null
    var barcodeLabelTarget: BarcodeLabelTarget? = null
    var qty: Int = 1

    // Container Activity must implement this interface
    interface FragmentListener {
        fun onFilterChanged(
            printer: String,
            template: BarcodeLabelCustom?,
            qty: Int?,
        )

        fun onPrintRequested(
            printer: String,
            template: BarcodeLabelCustom,
            qty: Int,
        )

        fun onQtyTextViewFocusChanged(
            hasFocus: Boolean,
        )
    }

    override fun onResume() {
        super.onResume()

        if (isReturnedFromSettings) {
            rejectNewInstances = false
            isReturnedFromSettings = false

            loadPrinterPreferences()
            refreshViews()
        }
    }

    override fun onStart() {
        super.onStart()
        if (fragmentListener is FragmentListener) {
            fragmentListener = activity as FragmentListener
        }
        // VER ESTO, estamos enviando el mensaje cuando está lista la actividad
        sendMessage()
    }

    private fun sendMessage() {
        fragmentListener?.onFilterChanged(
            printer = printer,
            template = barcodeLabelCustom,
            qty = qty
        )
    }

    private fun saveSharedPreferences() {
        if (barcodeLabelTarget == BarcodeLabelTarget.Asset) {
            prefsPutLong(
                Preference.defaultBarcodeLabelCustomAsset.key,
                barcodeLabelCustom?.barcodeLabelCustomId ?: 0L
            )
        } else if (barcodeLabelTarget == BarcodeLabelTarget.WarehouseArea) {
            prefsPutLong(
                Preference.defaultBarcodeLabelCustomWa.key,
                barcodeLabelCustom?.barcodeLabelCustomId ?: 0L
            )
        }
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            loadBundleValues(requireArguments())
        } else {
            loadPrinterPreferences()
        }
    }

    private fun loadPrinterPreferences() {
        // region TARGET
        barcodeLabelTarget =
            when (requireActivity()) {
                is WarehouseAreaPrintLabelActivity -> BarcodeLabelTarget.WarehouseArea
                is AssetPrintLabelActivity -> BarcodeLabelTarget.Asset
                else -> null
            }
        // endregion TARGET

        // region PLANTILLA
        // Seleccionamos la primera plantilla del tipo deseado si existe alguna.
        barcodeLabelCustom = when (requireActivity()) {
            is WarehouseAreaPrintLabelActivity -> {
                val blcId =
                    prefsGetLong(Preference.defaultBarcodeLabelCustomWa)
                if (blcId > 0) {
                    BarcodeLabelCustomDbHelper().selectById(blcId)
                } else {
                    BarcodeLabelCustomDbHelper().selectByBarcodeLabelTargetId(
                        barcodeLabelTargetId = (barcodeLabelTarget ?: return).id,
                        onlyActive = true
                    ).firstOrNull()
                }
            }

            is AssetPrintLabelActivity -> {
                val blcId =
                    prefsGetLong(Preference.defaultBarcodeLabelCustomAsset)
                if (blcId > 0) {
                    BarcodeLabelCustomDbHelper().selectById(blcId)
                } else {
                    BarcodeLabelCustomDbHelper().selectByBarcodeLabelTargetId(
                        barcodeLabelTargetId = (barcodeLabelTarget ?: return).id,
                        onlyActive = true
                    ).firstOrNull()
                }
            }

            else -> null
        }
        // endregion PLANTILLA

        // Cantidad inicial 1
        if (qty <= 0) qty = 1

        // Impresora guardada en preferencias
        val useBtPrinter = prefsGetBoolean(Preference.useBtPrinter)
        val useNetPrinter = prefsGetBoolean(Preference.useNetPrinter)

        val pBt = prefsGetString(Preference.printerBtAddress)
        val pIp = prefsGetString(Preference.ipNetPrinter)
        val port = prefsGetString(Preference.portNetPrinter)

        printer = when {
            useBtPrinter -> pBt
            useNetPrinter -> "$pIp (${port})"
            else -> ""
        }

        if (useBtPrinter) initializeBtPrinter()
    }

    // region PRINTER

    // PRINT CONTROLS
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mBluetoothSocket: BluetoothSocket? = null

    private var mHandler = ConnectHandler(this)

    override fun run() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    AssetControlApp.getContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestConnectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
                return
            }
            mBluetoothSocket =
                mBluetoothDevice!!.createRfcommSocketToServiceRecord(mBluetoothDevice!!.uuids[0].uuid)
            mBluetoothAdapter!!.cancelDiscovery()

            mBluetoothSocket!!.connect()
            mHandler.sendEmptyMessage(0)
        } catch (eConnectException: IOException) {
            Log.d(this::class.java.simpleName, "CouldNotConnectToSocket", eConnectException)
            if (mBluetoothSocket != null) {
                mBluetoothSocket!!.close()
            }

            makeText(binding.root, getString(R.string.error_connecting_device), SnackBarType.ERROR)
            return
        }
    }

    private val requestConnectPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // returns boolean representing whether the
            // permission is granted or not
            if (isGranted) {
                // permission granted continues the normal workflow of app
                Log.i("DEBUG", "permission granted")
            } else {
                // if permission denied then check whether never ask
                // again is selected or not by making use of
                // !ActivityCompat.shouldShowRequestPermissionRationale(
                // requireActivity(), Manifest.permission.CAMERA)
                Log.i("DEBUG", "permission denied")
            }
        }

    private fun initializeBtPrinter() {
        if (printerBluetoothDevice == null) return

        val bluetoothManager = AssetControlApp.getContext()
            .getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            makeText(
                binding.root,
                getString(R.string.there_are_no_bluetooth_devices),
                SnackBarType.ERROR
            )
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                if (!rejectNewInstances) {
                    rejectNewInstances = true
                    val enablePrinter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enablePrinter.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestConnectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        return
                    }
                    resultForPrinterConnect.launch(enablePrinter)
                }
            } else {
                connectToPrinter(printerBluetoothDevice!!.address)
            }
        }
    }

    private val resultForPrinterConnect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it?.resultCode == CommonStatusCodes.SUCCESS || it?.resultCode == CommonStatusCodes.SUCCESS_CACHE) {
                connectToPrinter(printerBluetoothDevice!!.address)
            }
        }

    //endregion

    private fun loadBundleValues(b: Bundle) {
        barcodeLabelTarget = b.getParcelable(argBarcodeLabelTarget)
        barcodeLabelCustom = b.getParcelable(argBarcodeLabelCustom)
        printer = b.getString(argPrinter) ?: ""
        qty = b.getInt(argQty)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString(argPrinter, printer)
        b.putParcelable(argBarcodeLabelCustom, barcodeLabelCustom)
        b.putParcelable(argBarcodeLabelTarget, barcodeLabelTarget)
        b.putInt(argQty, Integer.parseInt(binding.qtyEditText.text.toString()))
    }

    private var _binding: PrinterFragmentBinding? = null

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
        _binding = PrinterFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        }

        binding.printerTextView.setOnClickListener { configApp() }

        binding.templateTextView.setOnClickListener {
            if (rejectNewInstances) return@setOnClickListener
            rejectNewInstances = true

            showSelectTemplateActivity()
        }

        // Esta clase controla el comportamiento de los botones (+) y (-)
        ch = CounterHandler.Builder()
            .incrementalView(binding.moreButton)
            .decrementalView(binding.lessButton)
            .minRange(1) // cant go any less than -50
            .maxRange(100) // cant go any further than 50
            .isCycle(true) // 49,50,-50,-49 and so on
            .counterDelay(50) // speed of counter
            .startNumber(qty.toLong())
            .counterStep(1)  // steps e.g. 0,2,4,6...
            .listener(this) // to listen to counter results and show them in app
            .build()

        binding.qtyEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // Filtro que devuelve un texto válido
                val validStr = getValidValue(source = s.toString())

                // Si es NULL no hay que hacer cambios en el texto
                // porque está dentro de las reglas del filtro
                if (validStr != null && validStr != s.toString()) {
                    s.clear()
                    s.insert(0, validStr)
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int,
            ) {
            }
        })
        binding.qtyEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_UP &&
                        (keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                binding.printButton.performClick()
            }
            false
        }
        // Cambia el modo del teclado en pantalla a tipo numérico
        // cuando este control lo necesita.
        binding.qtyEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        binding.qtyEditText.setOnFocusChangeListener { _, hasFocus ->
            fragmentListener?.onQtyTextViewFocusChanged(hasFocus)
        }

        binding.printerSearchImageView.setOnClickListener { binding.printerTextView.performClick() }
        binding.printerClearImageView.setOnClickListener {
            printer = ""

            setPrinterText()
            sendMessage()
        }
        binding.templateSearchImageView.setOnClickListener { binding.templateTextView.performClick() }
        binding.templateClearImageView.setOnClickListener {
            barcodeLabelCustom = null

            setTemplateText()
            sendMessage()
        }

        binding.printButton.setOnClickListener {
            if (printer.isEmpty()) {
                makeText(
                    binding.root,
                    getString(R.string.you_must_select_a_printer),
                    SnackBarType.ERROR
                )
                return@setOnClickListener
            }
            if (barcodeLabelCustom == null) {
                makeText(
                    binding.root,
                    getString(R.string.you_must_select_a_template),
                    SnackBarType.ERROR
                )
                return@setOnClickListener
            }
            if (qty <= 0) {
                makeText(
                    binding.root,
                    getString(R.string.you_must_select_the_amount_of_labels_to_print),
                    SnackBarType.ERROR
                )
                return@setOnClickListener
            }

            requestPrint()
        }

        refreshViews()

        return view
    }

    private fun configApp() {
        val realPass = prefsGetString(Preference.confPassword)
        if (realPass.isEmpty()) {
            attemptEnterConfig(realPass)
            return
        }

        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.enter_password))

        val inputLayout = TextInputLayout(requireContext())
        inputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        val input = TextInputEditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.isFocusable = true
        input.isFocusableInTouchMode = true
        input.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_UP &&
                        (keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                if (alertDialog != null) {
                    alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                }
            }
            false
        }

        inputLayout.addView(input)
        builder.setView(inputLayout)
        builder.setPositiveButton(R.string.accept) { _, _ ->
            attemptEnterConfig(input.text.toString())
        }
        builder.setNegativeButton(R.string.cancel, null)
        alertDialog = builder.create()

        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alertDialog.show()
        input.requestFocus()
    }

    private fun attemptEnterConfig(password: String) {
        val realPass = prefsGetString(Preference.confPassword)
        if (password == realPass) {
            ConfigHelper.setDebugConfigValues()

            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(requireContext(), SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            isReturnedFromSettings = true
        } else {
            makeText(
                binding.root,
                getString(R.string.invalid_password),
                SnackBarType.ERROR
            )
        }
    }

    private fun showSelectTemplateActivity() {
        val intent = Intent(requireContext(), TemplateSelectDialogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(argBarcodeLabelTarget, barcodeLabelTarget)
        intent.putExtra(argBarcodeLabelCustom, barcodeLabelCustom)
        intent.putExtra("title", getString(R.string.select_template))
        intent.putExtra("onlyActive", true)
        resultForTemplateSelect.launch(intent)
    }

    private val resultForTemplateSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    try {
                        barcodeLabelCustom = data.getParcelableExtra(argBarcodeLabelCustom)
                        setTemplateText()
                        sendMessage()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(activity, this::class.java.simpleName, ex)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private fun requestPrint() {
        fragmentListener?.onPrintRequested(
            printer = printer,
            template = barcodeLabelCustom ?: return,
            qty = qty
        )
    }

    fun setListener(listener: FragmentListener) {
        this.fragmentListener = listener
    }

    fun refreshViews() {
        activity?.runOnUiThread {
            binding.qtyEditText.setText(qty.toString(), TextView.BufferType.EDITABLE)
            setPrinterText()
            setTemplateText()
        }
    }

    private fun setPrinterText() {
        if (_binding == null) return

        activity?.runOnUiThread {
            if (printer.trim().isEmpty()) {
                binding.printerTextView.typeface = Typeface.DEFAULT
                binding.printerTextView.text = getString(R.string.select_printer_)
            } else {
                binding.printerTextView.typeface = Typeface.DEFAULT_BOLD
                binding.printerTextView.text = printer.trim()
            }
        }
    }

    private fun setTemplateText() {
        if (_binding == null) return

        activity?.runOnUiThread {
            if (barcodeLabelCustom == null) {
                binding.templateTextView.typeface = Typeface.DEFAULT
                binding.templateTextView.text = getString(R.string.search_label_template_)
            } else {
                binding.templateTextView.typeface = Typeface.DEFAULT_BOLD
                binding.templateTextView.text =
                    (barcodeLabelCustom ?: return@runOnUiThread).description
            }
        }
    }

    // region Filtro para aceptar solo números entre ciertos parámetros

    /**
     * Devuelve una cadena de texto formateada que se ajusta a los parámetros.
     * Devuelve una cadena vacía en caso de Exception.
     * Devuelve null si no es necesario cambiar la cadena ingresada porque ya se ajusta a los parámetros
     *          o porque es igual que la cadena original.
     */
    private fun getValidValue(
        source: String,
        maxIntegerPlaces: Int = 3,
        maxDecimalPlaces: Int = 0,
        maxValue: Double = 100.0,
        decimalSeparator: Char = '.',
    ): CharSequence? {
        if (source.isEmpty()) {
            return null
        } else {
            // Regex para eliminar caracteres no permitidos.
            var validText = source.replace("[^0-9?!\\" + decimalSeparator + "]".toRegex(), "")

            // Probamos convertir el valor, si no se puede
            // se devuelve una cadena vacía
            val numericValue: Double
            try {
                numericValue = java.lang.Double.parseDouble(validText)
            } catch (e: NumberFormatException) {
                return ""
            }

            // Si el valor numérico es mayor al valor máximo reemplazar el
            // texto válido por el valor máximo
            validText = if (numericValue > maxValue) {
                maxValue.toString()
            } else {
                validText
            }

            // Obtener la parte entera y decimal del valor en forma de texto
            var decimalPart = ""
            val integerPart: String
            if (validText.contains(decimalSeparator)) {
                decimalPart =
                    validText.substring(
                        validText.indexOf(decimalSeparator) + 1,
                        validText.length
                    )
                integerPart = validText.substring(0, validText.indexOf(decimalSeparator))
            } else {
                integerPart = validText
            }

            // Si la parte entera es más larga que el máximo de dígitos permitidos
            // retorna un carácter vacío.
            if (integerPart.length > maxIntegerPlaces) {
                return ""
            }

            // Si la cantidad de espacios decimales permitidos es cero devolver la parte entera
            // si no, concatenar la parte entera con el separador de decimales y
            // la cantidad permitida de decimales.
            val result = if (maxDecimalPlaces == 0) {
                integerPart
            } else
                integerPart +
                        decimalSeparator +
                        decimalPart.substring(
                            0,
                            if (decimalPart.length > maxDecimalPlaces) maxDecimalPlaces else decimalPart.length
                        )

            // Devolver solo si son valores positivos diferentes a los de originales.
            // NULL si no hay que hacer cambios sobre el texto original.
            return if (result != source) {
                result
            } else null
        }
    }

    // endregion

    override fun onIncrement(view: View?, number: Long) {
        binding.qtyEditText.setText(number.toString())
    }

    override fun onDecrement(view: View?, number: Long) {
        binding.qtyEditText.setText(number.toString())
    }

    private fun connectToPrinter(deviceAddress: String) {
        Log.v(this::class.java.simpleName, "Coming incoming address $deviceAddress")
        mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(deviceAddress)

        mBluetoothSocket?.close()

        val mBluetoothConnectThread = Thread(this)
        mBluetoothConnectThread.start()
    }

    fun printWaById(waIdArray: ArrayList<Long>) {
        if (waIdArray.size == 0) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_at_least_one_area),
                SnackBarType.ERROR
            )
            return
        }

        val was: ArrayList<WarehouseArea> = ArrayList()
        val waDb = WarehouseAreaDbHelper()
        for (id in waIdArray) {
            val a = waDb.selectById(id)
            if (a != null) {
                was.add(a)
            }
        }

        printWa(was)
    }

    fun printWa(was: ArrayList<WarehouseArea>) {
        if (was.size == 0) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_at_least_one_area),
                SnackBarType.ERROR
            )
            return
        }

        if (was.size == 1) {
            if (was[0].warehouseAreaId < 0) {
                makeText(
                    binding.root,
                    getString(R.string.the_selected_warehouse_area_was_not_uploaded_to_the_server_and_does_not_have_a_definitive_id),
                    SnackBarType.ERROR
                )
                return
            }
        }

        if (mBluetoothDevice == null) {
            makeText(
                binding.root,
                getString(R.string.there_is_no_selected_printer),
                SnackBarType.ERROR
            )
            return
        }

        if (barcodeLabelCustom == null ||
            (barcodeLabelCustom ?: return).barcodeLabelCustomId == 0L
        ) {
            makeText(
                binding.root,
                getString(R.string.no_template_selected),
                SnackBarType.ERROR
            )
            return
        }

        if (printer.isEmpty() || prefsGetBoolean(Preference.useBtPrinter) && mBluetoothDevice == null) {
            makeText(binding.root, getString(R.string.there_is_no_selected_printer), SnackBarType.ERROR)
            return
        }

        val qty = binding.qtyEditText.text.toString().toInt()
        val blType = barcodeLabelCustom

        var sendThis = ""
        for (wa in was) {
            val waLf = WarehouseAreaLabelField(wa)
            val barcodeLabel = BarcodeLabel((blType ?: return).template)
            barcodeLabel.barcodeFields = waLf.getField()

            sendThis += barcodeLabel.getBarcodeLabel(qty)
        }

        when {
            prefsGetBoolean(Preference.useBtPrinter) -> printBtItem(sendThis)

            prefsGetBoolean(Preference.useNetPrinter) -> printNetItem(sendThis)

            else -> makeText(
                binding.root,
                getString(R.string.there_is_no_selected_printer),
                SnackBarType.ERROR
            )
        }
    }

    fun printAssetById(assetIdArray: ArrayList<Long>) {
        if (assetIdArray.size == 0) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_at_least_one_asset),
                SnackBarType.ERROR
            )
            return
        }

        val assets: ArrayList<Asset> = ArrayList()
        val aDb = AssetDbHelper()
        for (id in assetIdArray) {
            val a = aDb.selectById(id)
            if (a != null) {
                assets.add(a)
            }
        }

        printAsset(assets)
    }

    fun printAsset(assets: ArrayList<Asset>) {
        if (assets.size == 0) {
            makeText(
                binding.root,
                getString(R.string.you_must_select_at_least_one_asset),
                SnackBarType.ERROR
            )
            return
        }

        if (assets.size == 1) {
            if (assets[0].assetId < 0) {
                makeText(
                    binding.root,
                    getString(R.string.the_selected_asset_was_not_uploaded_to_the_server_and_does_not_have_a_definitive_id),
                    SnackBarType.ERROR
                )
                return
            }
        }

        if (barcodeLabelCustom == null ||
            (barcodeLabelCustom ?: return).barcodeLabelCustomId == 0L
        ) {
            makeText(
                binding.root,
                getString(R.string.no_template_selected),
                SnackBarType.ERROR
            )
            return
        }

        if (printer.isEmpty() || prefsGetBoolean(Preference.useBtPrinter) && mBluetoothDevice == null) {
            makeText(binding.root, getString(R.string.there_is_no_selected_printer), SnackBarType.ERROR)
            return
        }

        val qty = binding.qtyEditText.text.toString().toInt()
        val blType = barcodeLabelCustom

        var sendThis = ""
        for (asset in assets) {
            val assetLf = AssetLabelField(asset, false)
            val barcodeLabel = BarcodeLabel((blType ?: return).template)
            barcodeLabel.barcodeFields = assetLf.getField()
            sendThis += barcodeLabel.getBarcodeLabel(qty)
        }

        when {
            prefsGetBoolean(Preference.useBtPrinter) -> printBtItem(sendThis)

            prefsGetBoolean(Preference.useNetPrinter) -> printNetItem(sendThis)

            else -> makeText(
                binding.root,
                getString(R.string.there_is_no_selected_printer),
                SnackBarType.ERROR
            )
        }
    }

    private fun printNetItem(sendThis: String) {
        val ipPrinter = prefsGetString(Preference.ipNetPrinter)
        val portPrinter =
            try {
                prefsGetString(Preference.portNetPrinter).toInt()
            } catch (e: NumberFormatException) {
                0
            }

        Log.v(this::class.java.simpleName, "Printer IP: $ipPrinter ($portPrinter)")
        Log.v(this::class.java.simpleName, sendThis)

        val t = object : Thread() {
            override fun run() {
                try {
                    val sock = Socket(ipPrinter, portPrinter)
                    val out = sock.getOutputStream()
                    out.write(sendThis.toByteArray())
                    out.flush()
                    sock.close()
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                    makeText(
                        binding.root,
                        "${getString(R.string.unknown_host)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                } catch (e: ConnectException) {
                    e.printStackTrace()
                    makeText(
                        binding.root,
                        "${getString(R.string.error_connecting_to)}: $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    makeText(
                        binding.root,
                        "${getString(R.string.error_printing_to)} $ipPrinter ($portPrinter)",
                        SnackBarType.ERROR
                    )
                }
            }
        }
        t.start()
    }

    private fun printBtItem(sendThis: String) {
        val t = object : Thread() {
            override fun run() {
                try {
                    val os = mBluetoothSocket?.outputStream ?: return
                    for (i in 0 until qty) {
                        os.write(sendThis.toByteArray())
                    }
                    os.flush()
                } catch (e: Exception) {
                    ErrorLog.writeLog(
                        requireActivity(),
                        this::class.java.simpleName,
                        "${getString(R.string.exception_error)}: " + e.message
                    )
                    makeText(
                        binding.root,
                        "${getString(R.string.error_connecting_to)}: ${prefsGetString(Preference.printerBtAddress)}",
                        SnackBarType.ERROR
                    )
                }
            }
        }
        t.start()
    }

    companion object {
        // region Fragment initialization parameters
        private const val argPrinter = "printer"
        private const val argBarcodeLabelCustom = "barcodeLabelCustom"
        private const val argBarcodeLabelTarget = "barcodeLabelTarget"
        private const val argQty = "qty"
        // endregion

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            printer: String = "",
            template: BarcodeLabelCustom? = null,
            target: BarcodeLabelTarget? = null,
            qty: Int = 1,
        ): PrinterFragment {
            val fragment = PrinterFragment()

            val args = Bundle()
            args.putString(argPrinter, printer)
            args.putParcelable(argBarcodeLabelCustom, template)
            args.putParcelable(argBarcodeLabelTarget, target)
            args.putInt(argQty, qty)

            fragment.arguments = args
            return fragment
        }

        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }

        class ConnectHandler(private val activity: PrinterFragment) :
            Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                makeText(
                    activity.binding.root,
                    activity.getString(R.string.device_connected),
                    SnackBarType.INFO
                )
            }
        }
    }
}