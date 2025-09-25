package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class AddProductInput(
    @Json(name = "expense_id")
    var expense_id: Int? = null,
    @Json(name = "product_id")
    var product_id: Int? = null,
    @Json(name = "product_name")
    var product_name: String? = null,
    @Json(name = "category_id")
    var category_id: Int? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "product_status")
    var product_status: Int? = null,
    @Json(name = "sub_category_id")   // ✅ corrected key
    var sub_category_id: Int? = null,
    @Json(name = "stock_status")
    var stock_status: String? = null,
    @Json(name = "product_price")
    var product_price: List<AddProductPrice>? = null,
    @Json(name = "status")
    var status: String? = null,
    @Json(name = "product_tax")
    var product_tax: String? = null,
    @Json(name = "sku")
    var sku: String? = null,
    @Json(name = "user_id")          // <-- Add this
    var user_id: Int? = null
)