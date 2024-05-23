package com.dacosys.assetControl.ui.activities.category

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.AssetControlApp.Companion.currentUser
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.CrudCompleted
import com.dacosys.assetControl.data.enums.common.CrudResult
import com.dacosys.assetControl.data.enums.common.CrudStatus.*
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.databinding.ItemCategoryCrudActivityBinding
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.category.ItemCategoryCRUDFragment
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import org.parceler.Parcels

class ItemCategoryCRUDActivity : AppCompatActivity(), CrudCompleted,
    ImageControlButtonsFragment.DescriptionRequired {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        imageControlFragment?.onDestroy()
        imageControlFragment = null
    }

    /**
     * Interface que recibe los resultados del alta/modificación
     * de la categoría
     */
    override fun <T> onCompleted(result: CrudResult<T?>) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val itemCategory: ItemCategory? = (result.itemResult as ItemCategory?)
        when (result.status) {
            UPDATE_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.category_modified_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && itemCategory != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("itemCategory", Parcels.wrap(itemCategory))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            INSERT_OK -> {
                makeText(
                    binding.root, getString(R.string.category_added_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && itemCategory != null) {
                    imageControlFragment?.updateObjectId1(itemCategory.id)
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("itemCategory", Parcels.wrap(itemCategory))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }

            ERROR_OBJECT_NULL -> makeText(
                binding.root, getString(R.string.error_null_object), SnackBarType.ERROR
            )

            ERROR_UPDATE -> makeText(
                binding.root, getString(R.string.error_updating_category), SnackBarType.ERROR
            )

            ERROR_INSERT -> makeText(
                binding.root, getString(R.string.error_adding_category), SnackBarType.ERROR
            )
        }
    }

    private var isNew = true
    private var itemCategory: ItemCategory? = null

    private var imageControlFragment: ImageControlButtonsFragment? = null
    private var itemCategoryCRUDFragment: ItemCategoryCRUDFragment? = null

    private var returnOnSuccess: Boolean = false
    private var rejectNewInstances = false


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putParcelable("itemCategory", itemCategory)
        savedInstanceState.putBoolean("returnOnSuccess", returnOnSuccess)
        savedInstanceState.putBoolean("isNew", isNew)

        if (imageControlFragment is ImageControlButtonsFragment) supportFragmentManager.putFragment(
            savedInstanceState,
            "imageControlFragment",
            imageControlFragment as ImageControlButtonsFragment
        )
        supportFragmentManager.putFragment(
            savedInstanceState, "itemCategoryCRUDFragment", itemCategoryCRUDFragment as Fragment
        )
    }

    private lateinit var binding: ItemCategoryCrudActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = ItemCategoryCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permitir escanear códigos dentro de la actividad
        // Si ya está cargado una categoría preguntar si descartar cambios

        title = getString(R.string.edit_category)

        itemCategoryCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as ItemCategoryCRUDFragment
        if (savedInstanceState != null) {
            val t1 = savedInstanceState.parcelable<ItemCategory>("itemCategory")
            if (t1 != null) {
                itemCategory = t1
            }

            returnOnSuccess = savedInstanceState.getBoolean("returnOnSuccess")
            isNew = savedInstanceState.getBoolean("isNew")

            //Restore the fragment's instance
            val icF = supportFragmentManager.getFragment(savedInstanceState, "imageControlFragment")
            if (icF is ImageControlButtonsFragment) imageControlFragment = icF
            itemCategoryCRUDFragment = supportFragmentManager.getFragment(
                savedInstanceState, "itemCategoryCRUDFragment"
            ) as ItemCategoryCRUDFragment
        } else {
            val extras = intent.extras
            if (extras != null) {
                itemCategory = extras.parcelable("itemCategory")
                returnOnSuccess = extras.getBoolean("return_on_success", false)
                isNew = extras.getBoolean("is_new", false)
            }
        }

        binding.selectButton.setOnClickListener { selectItemCategory() }
        binding.saveButton.setOnClickListener { itemCategoryCRUDFragment?.saveItemCategory(this) }

        setupUI(binding.root, this)
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false

        fillControls()
    }

    private fun fillControls() {
        if (itemCategory == null) {
            setImageControlFragment()
            return
        }

        isNew = false

        runOnUiThread {
            itemCategoryCRUDFragment?.setItemCategory(itemCategory)
            setImageControlFragment()
        }
    }

    private fun clearControl() {
        runOnUiThread {
            itemCategory = null
            isNew = true
            itemCategoryCRUDFragment?.setItemCategory(itemCategory)
        }
    }

    private fun setImageControlFragment() {
        var itemCategoryId = 0L
        var description = ""
        if (itemCategory != null) {
            itemCategoryId = itemCategory!!.id
            description = itemCategory!!.description
        }

        val tableDescription = Table.itemCategory.description
        description = "$tableDescription: $description".take(255)

        val obs = "${getString(R.string.user)}: ${currentUser()?.name}"

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                tableId = Table.itemCategory.id.toLong(),
                objectId1 = itemCategoryId.toString()
            )

            setFragmentValues(description, "", obs)

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)

            val fm = supportFragmentManager

            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                            binding.imageControlFragment.id,
                            imageControlFragment ?: return@runOnUiThread
                        ).commit()

                    if (!prefsGetBoolean(Preference.useImageControl)) {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(imageControlFragment as Fragment)
                            .commitAllowingStateLoss()
                    } else {
                        fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                            .commitAllowingStateLoss()
                    }
                }
            }
        } else {
            imageControlFragment?.setTableId(Table.itemCategory.id)
            imageControlFragment?.setObjectId1(itemCategoryId)
            imageControlFragment?.setObjectId2(null)

            setFragmentValues(description, "", obs)

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)
        }

        // OCULTAR BOTÓN DE FIRMA
        imageControlFragment?.showSignButton = false
    }

    private fun setFragmentValues(description: String, reference: String, obs: String) {
        if (description.isNotEmpty()) {
            imageControlFragment?.setDescription(description)
        }

        if (reference.isNotEmpty()) {
            imageControlFragment?.setReference(reference)
        }

        if (obs.isNotEmpty()) {
            imageControlFragment?.setObs(obs)
        }
    }

    private fun changeItemCategory(tempItemCategory: ItemCategory) {
        if (itemCategory != null || (itemCategoryCRUDFragment?.getDescription()
                ?: "").isNotEmpty()
        ) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_category_registration_modification))
            alert.setMessage(getString(R.string.discard_changes_and_load_the_selected_category_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                itemCategory = tempItemCategory
                fillControls()
            }

            alert.show()
        } else {
            itemCategory = tempItemCategory
            fillControls()
        }
    }

    private fun cancelItemCategoryModify() {
        if (itemCategory != null || (itemCategoryCRUDFragment?.getDescription()
                ?: "").isNotEmpty()
        ) {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cancel_category_registration_modification))
            alert.setMessage(getString(R.string.discard_changes_and_return_to_the_main_menu_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)
                setResult(RESULT_CANCELED)
                finish()
            }

            alert.show()
        } else {
            closeKeyboard(this)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun selectItemCategory() {
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(baseContext, ItemCategorySelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("title", getString(R.string.select_category))
            intent.putExtra("onlyActive", true)
            resultForCategorySelect.launch(intent)
        }
    }

    private val resultForCategorySelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val iC = Parcels.unwrap<ItemCategory>(data.parcelable("itemCategory"))
                        ?: return@registerForActivityResult

                    try {
                        changeItemCategory(iC)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        makeText(
                            binding.root,
                            getString(R.string.error_trying_to_add_category),
                            SnackBarType.ERROR
                        )
                        ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    override fun onDescriptionRequired() {
        imageControlFragment?.setDescription(itemCategoryCRUDFragment?.getDescription() ?: "")
    }

    private fun isBackPressed() {
        cancelItemCategoryModify()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}