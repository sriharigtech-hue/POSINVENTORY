package com.example.apitest.network


import com.example.apitest.dataModel.AddProductInput
import com.example.apitest.dataModel.AddProductOutput
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.InputField

import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.dataModel.CategoryStatusUpdateInput
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.dataModel.StockProductOutput
import com.example.apitest.dataModel.SubCategoryStatusUpdateInput
import com.example.apitest.dataModel.UnitOutput

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body


import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {



    @Headers("Accept:application/json; charset=UTF-8")
    @POST("stock_category")
    fun stockCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: CategoryInput?,
    ): Call<CategoryListOutput>


    @Multipart
    @POST("add_category")
    fun uploadCategory(
        @Header("Authorization") jwtToken: String,
        @Part category_image: MultipartBody.Part?,
        @Part("category_name") type: RequestBody?,
        @Part("status") status: RequestBody?,
    ): Call<StatusResponse>

    @Multipart
    @POST("edit_category")
    fun uploadEditCategory(
        @Header("Authorization") jwtToken: String,
        @Part category_image: MultipartBody.Part?,
        @Part("category_id") category_id: RequestBody?,
        @Part("category_name") category_name: RequestBody?,
        @Part("status") status: RequestBody?,
    ): Call<StatusResponse>


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("delete_category")
    fun deleteCategory(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("category_status_update")
    fun categoryStatusUpdate(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: CategoryStatusUpdateInput?
    ): Call<StatusResponse>



    //sub_category api
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("all_sub_category")
    fun getAllSubCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<SubCategoryOutput?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("add_sub_category")
    fun addSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: InputField?
    ): Call<StatusResponse?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("edit_sub_category")
    fun editSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: InputField?
    ): Call<StatusResponse?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("delete_sub_category")
    fun deleteSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>

    //API for sub_category status update
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("subcategory_status_update")
    fun subCategoryStatusUpdate(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: SubCategoryStatusUpdateInput?
    ): Call<StatusResponse>

// API for displaying sub-category name based on category name, that we select


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("category_sub_category")
    fun addEditSubCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<SubCategoryOutput?>?


//    @Headers("Accept:application/json; charset=UTF-8")
//    @POST("category_all_sub_category")
//    fun subCategorySequenceApi(
//        @Header("Authorization") jwtToken: String,
//        @Body input: Input?
//    ): Call<SubCategoryOutput?>?



    // API for fetch Items based on category and subcategory
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("get_all_product")
    fun getAllProduct(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<StockProductOutput>


    // API for adding product(Items)
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("add_product")
    fun addProduct(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: AddProductInput?
    ): Call<StatusResponse?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("edit_product")
    fun editProduct(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: AddProductInput?
    ): Call<StatusResponse?>?


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("my_profile")
    fun getUserDetails(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<ProfileOutput?>?


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("single_product_detail")
    fun singleProductDetail(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: InputField?
    ): Call<AddProductOutput?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("delete_product")
    fun deleteProduct(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("product_status_update")
    fun productStatusUpdate(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>

}
