package com.example.apitest

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.InputField
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddSubCategoryActivity : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"
    private lateinit var categorySpinner: Spinner
    private lateinit var subCategoryNameEt: TextInputEditText

    private var categoryList = mutableListOf<Category>()
    private var selectedCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_category)
        // Back button click
        findViewById<RelativeLayout>(R.id.backButton).setOnClickListener {
            finish() // Closes this activity and returns to previous screen
        }



        categorySpinner = findViewById(R.id.categorySpinner)
        subCategoryNameEt = findViewById(R.id.AddSubCategoryName)

        fetchCategories()

        findViewById<View>(R.id.saveButton).setOnClickListener {
            val subCategoryName = subCategoryNameEt.text.toString().trim()
            if (subCategoryName.isEmpty()) {
                Toast.makeText(this, "Enter Sub Category Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedCategoryId == null) {
                Toast.makeText(this, "Select a Category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addSubCategory(subCategoryName, selectedCategoryId!!)
        }
    }

    private fun fetchCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful ) {
                        categoryList.clear()
                        categoryList.addAll(response.body()!!.data!!)
                        setupCategorySpinner()
                    } else {
                        Toast.makeText(this@AddSubCategoryActivity, "No categories found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(this@AddSubCategoryActivity, "Error fails", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupCategorySpinner() {
        val categoryNames = categoryList.map { it.category_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter


        // Default selection: first category
        if (categoryList.isNotEmpty()) {
            categorySpinner.setSelection(0)
            selectedCategoryId = categoryList[0].category_id
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryId = categoryList[position].category_id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun addSubCategory(subCategoryName: String, categoryId: Int) {
        // Log the values before sending
        Log.d("AddSubCategory", "Adding SubCategory: $subCategoryName under CategoryId: $categoryId")

        val input = InputField(
            subCategoryName = subCategoryName,
            categoryId = categoryId,  // Ensure this is passed correctly
            status = "1",
        )

        // Log the request body
        Log.d("AddSubCategory", "Request Body: $input")

        ApiClient.instance.addSubCategory(jwtToken, input)
            ?.enqueue(object : Callback<StatusResponse?> {
                override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                    if (response.isSuccessful) {
                        Log.d("AddSubCategory", "Response Code: ${response.code()}")
                        Log.d("AddSubCategory", "Response Body: ${response.body()}")
                        if (response.body()?.status == true) {
                            Toast.makeText(this@AddSubCategoryActivity, "Sub Category added successfully", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this@AddSubCategoryActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("AddSubCategory", "Error Response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@AddSubCategoryActivity, "Failed to add Sub Category", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                    Log.e("AddSubCategory", "API Failure", t)
                    Toast.makeText(this@AddSubCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
