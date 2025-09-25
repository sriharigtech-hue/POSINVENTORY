package com.example.apitest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryStatusUpdateInput
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryAdapter(private val categories: MutableList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"


    private var onEditClickListener: ((Category) -> Unit)? = null
    private var onDeleteClickListener: ((Category) -> Unit)? = null
    private var filteredList: MutableList<Category> = categories.toMutableList()

    fun setOnEditClickListener(listener: (Category) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Category) -> Unit) {
        onDeleteClickListener = listener
    }

    fun updateCategory(updatedCategory: Category) {
        val index = categories.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (index != -1) categories[index] = updatedCategory

        val fIndex = filteredList.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (fIndex != -1) {
            filteredList[fIndex] = updatedCategory
            notifyItemChanged(fIndex)
        } else {
            filteredList = categories.toMutableList()
            notifyDataSetChanged()
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val editButton: ShapeableImageView = itemView.findViewById(R.id.EditOption)
        val deleteButton: ShapeableImageView = itemView.findViewById(R.id.deleteProduct)
        val statusSwitch: LabeledSwitch = itemView.findViewById(R.id.CpOn_off_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = filteredList[holder.bindingAdapterPosition]
        holder.name.text = category.category_name

        //  prevent double-trigger
        holder.statusSwitch.setOnToggledListener(null)

        // Set correct initial state
        holder.statusSwitch.isOn = category.category_status == 1

        // Re-attach listener
        holder.statusSwitch.setOnToggledListener { _, isOn ->
            val newStatus = if (isOn) 1 else 0
            val input = CategoryStatusUpdateInput(
                category_id = category.category_id,
                category_status = newStatus,
                status = 1
            )

            val jsonAdapter = Moshi.Builder().build().adapter(CategoryStatusUpdateInput::class.java)
            Log.d("CategoryAdapter", "JSON: ${jsonAdapter.toJson(input)}")
            Log.d("CategoryAdapter", "Input: $input")

            ApiClient.instance.categoryStatusUpdate(jwtToken, input)
                .enqueue(object : Callback<StatusResponse> {
                    override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                        Log.d("CategoryAdapter", "Response: ${response.body()}")

                        if (response.isSuccessful && response.body()?.status == true) {
                            category.category_status = newStatus
                            notifyItemChanged(holder.bindingAdapterPosition)
                            Toast.makeText(holder.itemView.context, "Status updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            holder.statusSwitch.isOn = !isOn
                            Toast.makeText(holder.itemView.context,
                                response.body()?.message ?: "Update failed",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        holder.statusSwitch.isOn = !isOn
                        Toast.makeText(holder.itemView.context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Edit & delete listeners
        holder.editButton.setOnClickListener { onEditClickListener?.invoke(category) }
        holder.deleteButton.setOnClickListener { onDeleteClickListener?.invoke(category) }
    }


    override fun getItemCount(): Int = filteredList.size

    fun removeCategory(category: Category) {
        val index = filteredList.indexOfFirst { it.category_id == category.category_id }
        if (index != -1) {
            filteredList.removeAt(index)
            categories.removeIf { it.category_id == category.category_id }
            notifyItemRemoved(index)
        }
    }

    fun updateData(newList: List<Category>) {
        categories.clear()
        categories.addAll(newList)
        filteredList = categories.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            categories.toMutableList()
        } else {
            categories.filter { it.category_name.contains(query, ignoreCase = true) }
                .toMutableList()
        }
        notifyDataSetChanged()
    }
}
