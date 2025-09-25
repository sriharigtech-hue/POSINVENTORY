package com.example.apitest.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.AddCategoryActivity
import com.example.apitest.EditCategoryActivity
import com.example.apitest.R
import com.example.apitest.adapter.CategoryAdapter
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.example.apitest.dataModel.StatusUpdateInput
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryFragment : Fragment() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"
    private lateinit var searchView: SearchView
    private lateinit var btnAddCategory: FloatingActionButton
    private lateinit var addCategoryLauncher: ActivityResultLauncher<Intent>
    private lateinit var editCategoryLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CategoryAdapter(categories)
        recyclerView.adapter = adapter
        searchView = view.findViewById(R.id.searchView)
        searchView.isIconified = false
        searchView.clearFocus()

        btnAddCategory = view.findViewById(R.id.btnAddCategory)


        addCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchCategories() // refresh all categories after add
            }
        }


        // Edit category launcher
        editCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedId = data?.getStringExtra("category_id") ?: return@registerForActivityResult
                val updatedName = data.getStringExtra("category_name") ?: return@registerForActivityResult

                // Update in RecyclerView
                val updatedCategory = categories.find { it.category_id.toString() == updatedId }?.copy(category_name = updatedName)
                if (updatedCategory != null) {
                    adapter.updateCategory(updatedCategory)
                }
            }
        }

        btnAddCategory.setOnClickListener {
            val intent = Intent(requireContext(), AddCategoryActivity::class.java)
            addCategoryLauncher.launch(intent)
        }


        //  Attach search filter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })


        // Adapter edit click
        adapter.setOnEditClickListener { category ->
            val intent = Intent(requireContext(), EditCategoryActivity::class.java)
            intent.putExtra("category_id", category.category_id.toString())
            intent.putExtra("category_name", category.category_name)
            intent.putExtra("category_image", category.category_image ?: "")
            editCategoryLauncher.launch(intent)
        }

        // Adapter delete click
        adapter.setOnDeleteClickListener { category ->
            showDeleteDialog(category)
        }


        fetchCategories()
        return view
    }
    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.category_name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteCategory(category: Category) {
        val input = StatusUpdateInput(
            categoryId = category.category_id.toString(), // convert to String if needed
            categoryStatus = "0", // or whatever value
            status = 0
        )

        ApiClient.instance.deleteCategory(jwtToken, input)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                        adapter.removeCategory(category)
                    } else {
                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun fetchCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val newList = response.body()?.data ?: emptyList()
                        categories.clear()
                        categories.addAll(newList)
                        adapter.updateData(newList)
                    } else {
                        Toast.makeText(requireContext(), "Error fetching categories", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

