package com.example.apitest.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.AddProductActivity
import com.example.apitest.adapter.ItemsAdapter
import com.example.apitest.adapter.SidebarCategoryAdapter
import com.example.apitest.adapter.SubCategoryHorizontalAdapter
import com.example.apitest.dataModel.*
import com.example.apitest.databinding.FragmentItemsBinding
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsFragment : Fragment() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"
    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private val categoriesList = mutableListOf<Category>()
    private val itemsList = mutableListOf<StockProductData>()
    private val subCategoryList = mutableListOf<SubCategoryDetails>()

    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var subCategoryAdapter: SubCategoryHorizontalAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Floating Add Product
        binding.btnAddProduct.setOnClickListener {
            val stockEnabled = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("stock_enabled", false)
            val intent = Intent(requireContext(), AddProductActivity::class.java)
            intent.putExtra("stock_enabled", stockEnabled)
            intent.putExtra("type_method", 1) // ✅ sending extra value
            addProductLauncher.launch(intent)
        }

        itemsAdapter = ItemsAdapter(itemsList)
        binding.centerRecyclerView.adapter = itemsAdapter

        // Handle Edit click
        // Handle Edit click
        itemsAdapter.setOnEditClickListener { product ->
            Log.d("ItemsFragment", "productId=${product.productId}, name=${product.productName}")

            val intent = Intent(requireContext(), AddProductActivity::class.java).apply {
                putExtra("type_method", 2)
                putExtra("product_id", product.productId)
            }
            addProductLauncher.launch(intent) // Use launcher instead of startActivity
        }





// Handle Delete click
        itemsAdapter.setOnDeleteClickListener { product ->
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete ${product.productName}?")
                .setPositiveButton("Yes") { _, _ ->
                    product.productId?.let { productId ->
                        deleteProduct(productId.toString(), product)
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }



        // Sidebar RecyclerView
        binding.serviceList.layoutManager = LinearLayoutManager(requireContext())
        binding.serviceList.adapter = SidebarCategoryAdapter(categoriesList) { category ->

            // Clear old products immediately
            itemsList.clear()
            itemsAdapter.notifyDataSetChanged()

            if (category.sub_category_status == "1") {
                fetchSubCategories(category.category_id.toString())
            } else {
                // Clear subcategory list and hide
                subCategoryList.clear()
                subCategoryAdapter.notifyDataSetChanged()
                binding.subCategoryList.visibility = View.GONE

                // Fetch items for this category only
                fetchItems(category.category_id.toString(), null)
            }
        }




        // Center RecyclerView
        binding.centerRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        // Subcategory horizontal RecyclerView
        binding.subCategoryList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        subCategoryAdapter = SubCategoryHorizontalAdapter(subCategoryList) { subCategory ->
            fetchItems(
                categoryId = subCategory.categoryId.toString(),
                subCategoryId = subCategory.subcategoryId?.toString()
            )
        }
        binding.subCategoryList.adapter = subCategoryAdapter

        fetchCategories()
        fetchUserProfile()
    }
    private val addProductLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val itemName = data.getStringExtra("item_name")
                val newProducts = data.getParcelableArrayListExtra<StockProductData>("new_products")

                newProducts?.forEach { product ->
                    product.productName = itemName
                }

                if (!newProducts.isNullOrEmpty()) {
                    itemsList.addAll(newProducts)
                    itemsAdapter.notifyItemRangeInserted(itemsList.size - newProducts.size, newProducts.size)
                    binding.centerRecyclerView.scrollToPosition(itemsList.size - 1)
                }

                // Determine category and subcategory properly
                val categoryId = data.getIntExtra("category_id", -1).takeIf { it != -1 }?.toString()
                val subCategoryId = data.getIntExtra("sub_category_id", -1).takeIf { it != -1 }?.toString()
                val subCategoryStatus = data.getIntExtra("sub_category_status", 0) // pass from AddProductActivity

                if (!categoryId.isNullOrEmpty()) {
                    if (subCategoryStatus == 1 && !subCategoryId.isNullOrEmpty()) {
                        // Only fetch by subcategory if it exists
                        fetchItems(categoryId, subCategoryId)
                    } else {
                        // Category has no subcategory
                        fetchItems(categoryId, null)
                    }
                }
            }
        }


    private fun fetchCategories() {
        itemsList.clear()
        itemsAdapter.notifyDataSetChanged()
        val input = CategoryInput(status = "1")
        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (!isAdded || _binding == null) return
                    if (response.isSuccessful) {
                        val categories = response.body()?.data ?: emptyList()
                        categoriesList.clear()
                        categoriesList.addAll(categories)
                        binding.serviceList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchItems(categoryId: String, subCategoryId: String? = null) {
        // Clear list immediately
        itemsList.clear()
        itemsAdapter.notifyDataSetChanged()  // Always notify

        val input = if (!subCategoryId.isNullOrEmpty()) {
            Input(category_id = categoryId, sub_category_id = subCategoryId, status = "1")
        } else {
            Input(category_id = categoryId, status = "1")
        }

        ApiClient.instance.getAllProduct(jwtToken, input)
            .enqueue(object : Callback<StockProductOutput> {
                override fun onResponse(
                    call: Call<StockProductOutput>,
                    response: Response<StockProductOutput>
                ) {
                    if (!isAdded || _binding == null) return

                    val products = if (response.isSuccessful && response.body()?.status == true) {
                        response.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Always update adapter
                    itemsList.clear()
                    itemsList.addAll(products)
                    itemsAdapter.notifyDataSetChanged()  // This ensures empty list is reflected

                    if (products.isEmpty()) {
                        Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StockProductOutput>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }







    private fun fetchSubCategories(categoryId: String? = null, status: String? = "1") {
        val input = Input(category_id = categoryId, status = status)
        ApiClient.instance.getAllSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(call: Call<SubCategoryOutput?>, response: Response<SubCategoryOutput?>) {
                    if (!isAdded || _binding == null) return
                    val data = response.body()?.data?.filter { it.categoryId.toString() == categoryId } ?: emptyList()
                    subCategoryList.clear()
                    subCategoryList.addAll(data)
                    subCategoryAdapter.notifyDataSetChanged()
                    binding.subCategoryList.visibility = if (data.isNotEmpty()) View.VISIBLE else View.GONE
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.subCategoryList.visibility = View.GONE
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchUserProfile(status: String? = "1") {
        val input = Input(status = "1")
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                    if (!isAdded || _binding == null) return
                    val profile = response.body() ?: return
                    val stockEnabled = profile.userDetails?.stock_status == "1"
                    val showMRP = profile.userDetails?.mrp_price_status == "1"
                    val showWholesale = profile.userDetails?.whole_sale_price_status == "1"
                    val showTax = profile.userDetails?.product_tax_status == "1"

                    requireContext().getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
                        .edit {
                            putBoolean("stock_enabled", stockEnabled)
                            putBoolean("show_mrp", showMRP)
                            putBoolean("show_wholesale", showWholesale)
                            putBoolean("show_tax", showTax)
                        }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun deleteProduct(productId: String, product: StockProductData) {
        val input = StatusUpdateInput(
            categoryId = "0",        // use string, or actual category ID if available
            categoryStatus = "0",    // status as string
            product_id = productId,
            status = 1
        )

        ApiClient.instance.deleteProduct(jwtToken, input)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(context, response.body()?.message ?: "Deleted successfully", Toast.LENGTH_SHORT).show()

                        // Remove from local list
                        val index = itemsList.indexOf(product)
                        if (index != -1) {
                            itemsList.removeAt(index)
                            itemsAdapter.notifyItemRemoved(index)
                            itemsAdapter.notifyItemRangeChanged(index, itemsList.size)
                        }
                    } else {
                        Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
