package com.example.apitest.dataModel


import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
@Keep
@Parcelize
@JsonClass(generateAdapter = true)
data class StockProductData(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "seq_no")
    var seq_no: String? = null,
    @Json(name = "product_id")
    var productId: Int? = null,
    @Json(name = "product_image")
    var productImage: String? = null,
    @Json(name = "product_price")
    var product_price: String? = null,
    @Json(name = "mrp_price")
    var mrp_price: String? = null,
    @Json(name = "whole_sale_price")
    var whole_sale_price: String? = null,
    @Json(name = "product_name")
    var productName: String? = null,
    @Json(name = "product_status")
    var productStatus: Int? = null,
    @Json(name = "recommended_status")
    var recommended_status: Int? = null,
    @Json(name = "product_variation_id")
    var productVariationId: Int? = null,
    @Json(name = "product_variation_name")
    var productVariationName: String? = null,
    @Json(name = "stock_count")
    var stockCount: Int? = null,
    @Json(name = "low_stock_alert")
    var low_stock_alert: Int? = null,
    @Json(name = "stock_update_status")
    var stock_update_status: Int? = null,
    @Json(name = "category_id")
    var categoryId: String? = null,
    @Json(name = "category_status")
    var categoryStatus: String? = null
): Parcelable