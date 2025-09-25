package com.example.apitest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.StockProductData
import com.example.apitest.network.ApiClient
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsAdapter(
    private val items: MutableList<StockProductData>
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
    private var onEditClickListener: ((StockProductData) -> Unit)? = null
    private var onDeleteClickListener: ((StockProductData) -> Unit)? = null
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

    fun setOnEditClickListener(listener: (StockProductData) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (StockProductData) -> Unit) {
        onDeleteClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.PName)
        val editBtn: ShapeableImageView = itemView.findViewById(R.id.PEditOption)
        val deleteBtn: ShapeableImageView = itemView.findViewById(R.id.PDeleteProduct)
        val statusToggle: LabeledSwitch = itemView.findViewById(R.id.Status_on_off)





//        val price: TextView = itemView.findViewById(R.id.PPrice)
//        val image: ImageView = itemView.findViewById(R.id.PImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false) // your product layout
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.productName ?: "N/A"


        // Toggle listener
        // Reset toggle listener to prevent multiple triggers
        holder.statusToggle.setOnToggledListener(null)
        holder.statusToggle.isOn = item.productStatus == 1

        // Toggle listener
        holder.statusToggle.setOnToggledListener { _, isOn ->
            val newStatusInt = if (isOn) 1 else 0

            // Prepare API input
            val input = StatusUpdateInput(
                product_id = item.productId?.toString() ?: "0",
                product_variation_id = item.productVariationId?.toString() ?: "0",
                product_status = newStatusInt.toString(),
                status = 1
            )

            Log.d("ItemsAdapter", "Updating status for productId=${item.productId}, payload=$input")

            // API call
            ApiClient.instance.productStatusUpdate(jwtToken, input)
                .enqueue(object : Callback<StatusResponse> {
                    override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                        Log.d("ItemsAdapter", "API Response code: ${response.code()}")
                        Log.d("ItemsAdapter", "API Response body: ${response.body()}")

                        if (response.isSuccessful && response.body()?.status == true) {
                            item.productStatus = newStatusInt
                            Toast.makeText(holder.itemView.context, "Status updated", Toast.LENGTH_SHORT).show()
                        } else {
                            holder.statusToggle.isOn = !isOn // revert visually
                            Toast.makeText(holder.itemView.context, "Failed to update status", Toast.LENGTH_SHORT).show()
                            Log.e("ItemsAdapter", "Failed to update status: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        holder.statusToggle.isOn = !isOn // revert visually
                        Toast.makeText(holder.itemView.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ItemsAdapter", "API call failed: ${t.message}", t)
                    }
                })
        }




        // Handle edit click
        holder.editBtn.setOnClickListener {

            onEditClickListener?.invoke(item)
        }

        // Handle delete click
        holder.deleteBtn.setOnClickListener {
            onDeleteClickListener?.invoke(item)
        }



//        // Show first price if available
//        val firstPrice = item.productPrice?.firstOrNull()?.productPrice ?: "0.00"
//        holder.price.text = "₹ $firstPrice"

        // Load product image if available
//        Glide.with(holder.itemView.context)
//            .load(item.productImage)
//            .placeholder(R.drawable.placeholder) // add a placeholder in drawable
//            .into(holder.image)
    }
}
