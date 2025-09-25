package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.SubCategoryDetails
import com.google.android.material.card.MaterialCardView

class SubCategoryHorizontalAdapter(
    private val subCategories: MutableList<SubCategoryDetails>,
    private val onClick: (SubCategoryDetails) -> Unit
) : RecyclerView.Adapter<SubCategoryHorizontalAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSubCategory: TextView = itemView.findViewById(R.id.txtSubCategory)
        val cardSubCategory: MaterialCardView = itemView.findViewById(R.id.cardSubCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_category_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = subCategories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subCat = subCategories[position]
        holder.txtSubCategory.text = subCat.subcategoryName ?: "N/A"

        if (position == selectedPosition) {
            holder.cardSubCategory.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blue243757)
            )
            holder.txtSubCategory.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        } else {
            holder.cardSubCategory.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blueF1F4FF)
            )
            holder.txtSubCategory.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blue243757)
            )
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onClick(subCat)
        }
    }

    fun setData(newList: List<SubCategoryDetails>) {
        subCategories.clear()
        subCategories.addAll(newList)
        notifyDataSetChanged()
    }
}
