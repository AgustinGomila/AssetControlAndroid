package com.dacosys.assetControl.ui.activities.category

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.ItemCategoryCrudActivityBinding
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.category.ItemCategoryCRUD
import com.dacosys.assetControl.model.category.ItemCategoryCRUD.Companion.RC_ERROR_INSERT
import com.dacosys.assetControl.model.category.ItemCategoryCRUD.Companion.RC_ERROR_OBJECT_NULL
import com.dacosys.assetControl.model.category.ItemCategoryCRUD.Companion.RC_ERROR_UPDATE
import com.dacosys.assetControl.model.category.ItemCategoryCRUD.Companion.RC_INSERT_OK
import com.dacosys.assetControl.model.category.ItemCategoryCRUD.Companion.RC_UPDATE_OK
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.category.ItemCategoryCRUDFragment
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.ui.fragments.ImageControlButtonsFragment
import org.parceler.Parcels

class ItemCategoryCRUDActivity : AppCompatActivity(), ItemCategoryCRUD.Companion.TaskCompleted,
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
    override fun onTaskCompleted(result: ItemCategoryCRUD.Companion.ItemCategoryCRUDResult) {
        if (isDestroyed || isFinishing) return

        when (result.resultCode) {
            RC_UPDATE_OK -> {
                makeText(
                    binding.root,
                    getString(R.string.category_modified_correctly),
                    SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && result.itemCategory != null) {
                    imageControlFragment?.saveImages(true)
                }

                if (returnOnSuccess) {
                    Statics.closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("itemCategory", Parcels.wrap(result.itemCategory))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }
            RC_INSERT_OK -> {
                makeText(
                    binding.root, getString(R.string.category_added_correctly), SnackBarType.SUCCESS
                )
                if (imageControlFragment != null && result.itemCategory != null) {
                    imageControlFragment?.updateObjectId1(result.itemCategory!!.itemCategoryId)
                    imageControlFragment?.saveImages(false)
                }

                if (returnOnSuccess) {
                    Statics.closeKeyboard(this)

                    val data = Intent()
                    data.putExtra("itemCategory", Parcels.wrap(result.itemCategory))
                    setResult(RESULT_OK, data)
                    finish()
                } else {
                    clearControl()
                }
            }
            RC_ERROR_OBJECT_NULL -> makeText(
                binding.root, getString(R.string.error_null_object), SnackBarType.ERROR
            )
            RC_ERROR_UPDATE -> makeText(
                binding.root, getString(R.string.error_updating_category), SnackBarType.ERROR
            )
            RC_ERROR_INSERT -> makeText(
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, motionEvent ->
                Statics.closeKeyboard(this)
                if (view is Button && view !is Switch && view !is CheckBox) {
                    touchButton(motionEvent, view)
                    true
                } else {
                    false
                }
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            (0 until view.childCount).map { view.getChildAt(it) }.forEach { setupUI(it) }
        }
    }

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
        Statics.setScreenRotation(this)
        binding = ItemCategoryCrudActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permitir escanear códigos dentro de la actividad
        // Si ya está cargado un categoría preguntar si descartar cambios

        title = getString(R.string.edit_category)

        itemCategoryCRUDFragment =
            supportFragmentManager.findFragmentById(binding.crudFragment.id) as ItemCategoryCRUDFragment
        if (savedInstanceState != null) {
            val t1 = savedInstanceState.getParcelable<ItemCategory>("itemCategory")
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
                itemCategory = extras.getParcelable("itemCategory")
                returnOnSuccess = extras.getBoolean("return_on_success", false)
                isNew = extras.getBoolean("is_new", false)
            }
        }

        binding.selectButton.setOnClickListener { selectItemCategory() }
        binding.saveButton.setOnClickListener { itemCategoryCRUDFragment?.saveItemCategory(this) }

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false

        fillControls()
    }

    override fun onBackPressed() {
        cancelItemCategoryModify()
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
            itemCategoryId = itemCategory!!.itemCategoryId
            description = itemCategory!!.description
        }

        val tableName = Table.itemCategory.tableName
        description = "$tableName: $description"
        if (description.length > 255) {
            description.substring(0, 255)
        }

        if (imageControlFragment == null) {
            imageControlFragment = ImageControlButtonsFragment.newInstance(
                Table.itemCategory.tableId, itemCategoryId, null
            )

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)

            val fm = supportFragmentManager

            if (!isFinishing) runOnUiThread {
                fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                        binding.imageControlFragment.id,
                        imageControlFragment ?: return@runOnUiThread
                    ).commit()

                if (!Statics.prefsGetBoolean(Preference.useImageControl)) {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(imageControlFragment as Fragment).commitAllowingStateLoss()
                } else {
                    fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show((imageControlFragment ?: return@runOnUiThread) as Fragment)
                        .commitAllowingStateLoss()
                }
            }
        } else {
            imageControlFragment?.setTableId(Table.itemCategory.tableId)
            imageControlFragment?.setObjectId1(itemCategoryId)
            imageControlFragment?.setObjectId2(null)

            if (description.isNotEmpty()) {
                imageControlFragment?.setDescription(description)
            }

            // Callback para actualizar la descripción
            imageControlFragment?.setListener(this)
        }

        // OCULTAR BOTÓN DE FIRMA
        imageControlFragment?.showSignButton = false
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
                Statics.closeKeyboard(this)

                setResult(RESULT_CANCELED)
                finish()
            }

            alert.show()
        } else {
            Statics.closeKeyboard(this)

            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun touchButton(motionEvent: MotionEvent, button: Button) {
        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> {
                button.isPressed = false
                button.performClick()
            }
            MotionEvent.ACTION_DOWN -> {
                button.isPressed = true
            }
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
                    val iC = Parcels.unwrap<ItemCategory>(data.getParcelableExtra("itemCategory"))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}