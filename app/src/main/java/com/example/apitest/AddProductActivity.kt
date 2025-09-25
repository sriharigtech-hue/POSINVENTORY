package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.adapter.ProductVariationAdapter
import com.example.apitest.dataModel.*
import com.example.apitest.network.ApiClient
import com.example.apitest.databinding.ActivityAddProductBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var categorySpinner: Spinner
    private lateinit var subCategorySpinner: Spinner
    private lateinit var subCategoryTitle: TextView
    private lateinit var subCategoryLayout: LinearLayout

    private var categoryList: List<Category> = emptyList()
    private var subCategoryList: List<SubCategoryDetails> = emptyList()

    private lateinit var variationAdapter: ProductVariationAdapter
    private val localVariations = mutableListOf<StockProductData>()

    private var showMRP = true
    private var showWholesale = true
    private var showTax = false
    private var isEditMode = false

    private var selectedCategoryId: Int? = null
    private var selectedSubCategoryId: Int? = null

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        loadPreferences()
        setupRecyclerView()
        setupAddVariationButton()
        loadCategories()

        binding.saveButton.setOnClickListener { saveProduct() }
    }

    private fun bindViews() {
        categorySpinner = binding.categorySpinner
        subCategorySpinner = binding.subCategorySpinner
        subCategoryTitle = binding.subCategorySpinnerTitleLayout
        subCategoryLayout = binding.subCategorySpinnerLayout
        subCategoryTitle.visibility = View.GONE
        subCategoryLayout.visibility = View.GONE
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val stockEnabled = prefs.getBoolean("stock_enabled", false)
        val typeMethod = intent?.getIntExtra("type_method", 1) ?: 1
        val titleView = findViewById<AppCompatTextView>(R.id.title)

        isEditMode = typeMethod == 2
        titleView.text = if (isEditMode) "Edit Product" else "Add Product"

        if (isEditMode) {
            val productId = intent.getIntExtra("product_id", -1)
            if (productId != -1) fetchProductDetails(productId)
        }

        showMRP = prefs.getBoolean("show_mrp", true)
        showWholesale = prefs.getBoolean("show_wholesale", true)
        showTax = prefs.getBoolean("show_tax", false)

        binding.stockLayout.visibility = if (stockEnabled) View.VISIBLE else View.GONE
        binding.onOffButton.isOn = stockEnabled
        binding.hint7.visibility = if (showTax) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        variationAdapter = ProductVariationAdapter(
            localVariations,
            onEditClick = { item, pos -> showVariationDialog(item, pos, true) },
            onDeleteClick = { pos -> deleteVariation(pos) }
        )
        binding.productVariation.adapter = variationAdapter
        binding.productVariation.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAddVariationButton() {
        binding.addVariation.setOnClickListener {
            showVariationDialog(null, -1, false)
        }
    }

    private fun fetchProductDetails(productId: Int) {
        val input = InputField(product_id = productId, status = "1")
        ApiClient.instance.singleProductDetail(jwtToken, input)
            ?.enqueue(object : Callback<AddProductOutput?> {
                override fun onResponse(
                    call: Call<AddProductOutput?>,
                    response: Response<AddProductOutput?>
                ) {
                    val product = response.body()?.categoryList?.firstOrNull { it.product_id == productId }
                    product?.let { populateProductFields(it) }
                        ?: run { Toast.makeText(this@AddProductActivity, "Product data not found", Toast.LENGTH_SHORT).show() }
                }

                override fun onFailure(call: Call<AddProductOutput?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed to load product", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun populateProductFields(product: AddProductInput) {
        binding.productName.setText(product.product_name)
        binding.productTax.setText(product.product_tax ?: "")
        binding.onOffButton.isOn = product.stock_status == "1"

        loadCategories(product.category_id, product.sub_category_id)

        localVariations.clear()
        product.product_price?.forEach { price ->
            localVariations.add(
                StockProductData(
                    productVariationName = price.product_variation ?: "",
                    product_price = price.product_price ?: "",
                    stockCount = price.stock_quantity?.toIntOrNull(),
                    low_stock_alert = price.low_stock_alert?.toIntOrNull(),
                    mrp_price = price.mrp_price,
                    whole_sale_price = price.whole_sale_price
                )
            )
        }
        variationAdapter.notifyDataSetChanged()
        updateVariationVisibility()
    }

    private fun loadCategories(selectedCatId: Int? = null, selectedSubCatId: Int? = null) {
        ApiClient.instance.stockCategoryApi(jwtToken, CategoryInput(status="1"))
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(call: Call<CategoryListOutput>, response: Response<CategoryListOutput>) {
                    categoryList = response.body()?.data ?: emptyList()
                    val adapter = ArrayAdapter(this@AddProductActivity, android.R.layout.simple_spinner_item, categoryList.map { it.category_name })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter

                    categorySpinner.post {
                        val index = selectedCatId?.let { categoryList.indexOfFirst { it.category_id == selectedCatId } } ?: 0
                        categorySpinner.setSelection(if (index != -1) index else 0)
                    }

                    categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            loadSubCategories(categoryList[position].category_id, selectedSubCatId)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {}
            })
    }

    private fun loadSubCategories(categoryId: Int, selectedSubCategoryId: Int? = null) {
        val input = Input(category_id = categoryId.toString(), status = "1")
        ApiClient.instance.addEditSubCategoryApi(jwtToken, input)?.enqueue(object : Callback<SubCategoryOutput?> {
            override fun onResponse(call: Call<SubCategoryOutput?>, response: Response<SubCategoryOutput?>) {
                val subCategories = response.body()?.data.orEmpty()
                if (subCategories.isNotEmpty()) {
                    subCategoryList = subCategories
                    val adapter = ArrayAdapter(this@AddProductActivity, android.R.layout.simple_spinner_item, subCategoryList.map { it.subcategoryName ?: "Unnamed" })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subCategorySpinner.adapter = adapter
                    subCategoryTitle.visibility = View.VISIBLE
                    subCategoryLayout.visibility = View.VISIBLE

                    val index = selectedSubCategoryId?.let { subCategoryList.indexOfFirst { it.subcategoryId == selectedSubCategoryId } } ?: 0
                    subCategorySpinner.setSelection(if (index != -1) index else 0)
                } else {
                    subCategoryTitle.visibility = View.GONE
                    subCategoryLayout.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                Toast.makeText(this@AddProductActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setFieldVisibility(editText: TextInputEditText?, hintLayout: TextInputLayout?, show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        editText?.visibility = visibility
        hintLayout?.visibility = visibility
    }

    private fun showVariationDialog(item: StockProductData?, position: Int, isEdit: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_variation, null)

        val productVariation = dialogView.findViewById<TextInputEditText>(R.id.productVariation)
        val productPrice = dialogView.findViewById<TextInputEditText>(R.id.productPrice)
        val productLowStock = dialogView.findViewById<TextInputEditText>(R.id.productLowStockAlert)
        val productStockQty = dialogView.findViewById<TextInputEditText>(R.id.productStockQuantity)
        val productMrp = dialogView.findViewById<TextInputEditText>(R.id.mrpPrice)
        val productWholeSale = dialogView.findViewById<TextInputEditText>(R.id.productWholeSalePrice)

        val hintLowStock = dialogView.findViewById<TextInputLayout>(R.id.hint2)
        val hintStockQty = dialogView.findViewById<TextInputLayout>(R.id.hint3)
        val hintMrp = dialogView.findViewById<TextInputLayout>(R.id.hint5)
        val hintWholeSale = dialogView.findViewById<TextInputLayout>(R.id.hint6)

        val saveBtn = dialogView.findViewById<MaterialTextView>(R.id.save)
        val cancelBtn = dialogView.findViewById<ImageView>(R.id.cancel)
        val isStockOn = binding.onOffButton.isOn

        // Prefill for edit
        item?.let {
            productVariation.setText(it.productVariationName)
            productPrice.setText(it.product_price)
            productLowStock.setText(it.low_stock_alert?.toString())
            productStockQty.setText(it.stockCount?.toString())
            productMrp.setText(it.mrp_price)
            productWholeSale.setText(it.whole_sale_price)
        }

        setFieldVisibility(productMrp, hintMrp, showMRP)
        setFieldVisibility(productWholeSale, hintWholeSale, showWholesale)
        setFieldVisibility(productLowStock, hintLowStock, isStockOn)
        setFieldVisibility(productStockQty, hintStockQty, isStockOn)

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        cancelBtn.setOnClickListener { dialog.dismiss() }

        saveBtn.setOnClickListener {
            val variationName = productVariation.text.toString().trim()
            val price = productPrice.text.toString().trim()
            val lowStock = productLowStock.text.toString().trim()
            val stockQty = productStockQty.text.toString().trim()
            val mrp = productMrp.text.toString().trim()
            val wholeSale = productWholeSale.text.toString().trim()

            if (variationName.isEmpty() || price.isEmpty() ||
                (isStockOn && (lowStock.isEmpty() || stockQty.isEmpty())) ||
                (showMRP && mrp.isEmpty()) ||
                (showWholesale && wholeSale.isEmpty())
            ) {
                Toast.makeText(this, "Please enter all required details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newVariation = StockProductData(
                productVariationId = item?.productVariationId,
                productVariationName = variationName,
                product_price = price,
                stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null,
                mrp_price = if (showMRP) mrp else null,
                whole_sale_price = if (showWholesale) wholeSale else null
            )

            if (isEdit && position >= 0) {
                localVariations[position] = newVariation
                variationAdapter.notifyItemChanged(position)
                Toast.makeText(this, "Variation updated", Toast.LENGTH_SHORT).show()
            } else {
                localVariations.add(newVariation)
                variationAdapter.notifyItemInserted(localVariations.size - 1)
                Toast.makeText(this, "Variation added", Toast.LENGTH_SHORT).show()
            }
            updateVariationVisibility()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateVariationVisibility() {
        binding.productVariation.visibility = if (localVariations.isEmpty()) View.GONE else View.VISIBLE
        findViewById<LinearLayout>(R.id.no_list_error)?.visibility = if (localVariations.isEmpty()) View.VISIBLE else View.GONE
    }
    private fun saveProduct() {
        val itemName = binding.productName.text.toString().trim()
        if (itemName.isEmpty()) {
            binding.productName.error = "Please enter item name"
            return
        }
        if (localVariations.isEmpty()) {
            Toast.makeText(this, "Please add at least one variation", Toast.LENGTH_SHORT).show()
            return
        }

        val tax = if (showTax) binding.productTax.text.toString().trim().takeIf { it.isNotEmpty() } else null
        if (showTax && tax.isNullOrEmpty()) {
            binding.productTax.error = "Please enter tax"
            return
        }

        // ✅ ADD THIS CHECK HERE
        val prodId = intent.getIntExtra("product_id", -1)
        if (isEditMode && prodId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            return
        }
        val categoryId = categoryList.getOrNull(categorySpinner.selectedItemPosition)?.category_id
        val subCategoryId = if (subCategoryLayout.visibility == View.VISIBLE &&
            subCategoryList.isNotEmpty()
        ) subCategoryList.getOrNull(subCategorySpinner.selectedItemPosition)?.subcategoryId
        else null


        if (categoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        fetchUserId { userId ->
            if (userId == null) {
                Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
                return@fetchUserId
            }

            // Map local variations to API model
            val variationsForApi = localVariations.map { variation ->
                AddProductPrice(
                    product_variation_id = variation.productVariationId ?: 0, // 0 for new
                    product_variation = variation.productVariationName,
                    product_price = variation.product_price,
                    stock_quantity = variation.stockCount?.toString(),
                    low_stock_alert = variation.low_stock_alert?.toString(),
                    mrp_price = variation.mrp_price,
                    whole_sale_price = variation.whole_sale_price,
                    product_tax = tax
                )
            }



            // Prepare input
            val productInput = AddProductInput(
                product_id = if (isEditMode) intent.getIntExtra("product_id", -1) else null,
                product_name = itemName,
                category_id = categoryId,
                sub_category_id = subCategoryId,
                product_status = 1,
                stock_status = if (binding.onOffButton.isOn) "1" else "0",
                product_price = variationsForApi,
                product_tax = tax,
                user_id = userId,
                status = "1"
            )

            Log.d("AddProduct", "Request body: $productInput")

            val apiCall = if (isEditMode) {

                ApiClient.instance.editProduct(jwtToken, productInput)
            } else {
                ApiClient.instance.addProduct(jwtToken, productInput)
            }

            apiCall?.enqueue(object : Callback<StatusResponse?> {
                override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                    Log.d("AddProduct", "Response: ${response.body()}")
                    if (response.isSuccessful && response.body()?.status == true) {

                        Toast.makeText(
                            this@AddProductActivity,
                            if (isEditMode) "Product updated successfully" else "Product added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("AddProduct", "Product saved successfully")

                        val resultIntent = Intent().apply {
                            putExtra("item_name", itemName)
                            putExtra("tax", tax)
                            putExtra("category_id", categoryId)
                            putExtra("sub_category_id", subCategoryId)
                            putParcelableArrayListExtra("new_products", ArrayList(localVariations))
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e("AddProduct", "Failed to save product: ${response.errorBody()?.string()}")
                        Toast.makeText(this@AddProductActivity, "Failed to save product", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    // Fetch user_id automatically from profile API
    private fun fetchUserId(callback: (Int?) -> Unit) {

        val input = Input(status = "1")  // Pass a valid body instead of null
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(
                    call: Call<ProfileOutput?>,
                    response: Response<ProfileOutput?>
                ) {
                    Log.d("AddProduct", "Response: ${response.body()}")
                    if (response.isSuccessful) {

                        val userId = response.body()?.userDetails?.userId
                        Log.d("AddProduct", "User ID: $userId")
                        callback(userId)
                    } else {
                        Log.e("AddProduct", "Failed to fetch user profile: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@AddProductActivity,
                            "Failed to fetch user profile",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(
                        this@AddProductActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(null)
                }
            })
    }


    private fun deleteVariation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Variation")
            .setMessage("Are you sure you want to delete this variation?")
            .setPositiveButton("Yes") { dialog, _ ->
                localVariations.removeAt(position)
                variationAdapter.notifyItemRemoved(position)
                variationAdapter.notifyItemRangeChanged(position, localVariations.size)
                updateVariationVisibility()
                Toast.makeText(this, "Variation deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // saveProduct() and fetchUserId() remain the same as your original code
}
