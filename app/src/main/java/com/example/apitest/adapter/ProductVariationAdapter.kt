
package com.example.apitest.adapter

import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.StockProductData
import androidx.appcompat.widget.AppCompatTextView

class ProductVariationAdapter(
    private val variations: MutableList<StockProductData>,
    private val onEditClick: (StockProductData, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductVariationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productVariation: AppCompatTextView = view.findViewById(R.id.productVariation)
        val productPrice: AppCompatTextView = view.findViewById(R.id.productPrice)
        val productStock: AppCompatTextView = view.findViewById(R.id.productStock)
        val productLowStock: AppCompatTextView = view.findViewById(R.id.productLowStock)
        val productSKU: AppCompatTextView = view.findViewById(R.id.productSKU)
        val productMRPPrice: AppCompatTextView = view.findViewById(R.id.productMRPPrice)
        val productWholeSalePrice: AppCompatTextView = view.findViewById(R.id.productWholeSalePrice)
        val productTax: AppCompatTextView = view.findViewById(R.id.productTax)
        val productUnitName: AppCompatTextView = view.findViewById(R.id.productUnitName)

        val editBtn: LinearLayout = view.findViewById(R.id.editOption) // add edit button in item layout
        val deleteBtn: LinearLayout = view.findViewById(R.id.deleteOption) // add delete button in item layout


        // Dividers
        val view1: View = view.findViewById(R.id.view1)
        val view2: View = view.findViewById(R.id.view2)
        val view3: View = view.findViewById(R.id.view3)
        val view4: View = view.findViewById(R.id.view4)
        val view5: View = view.findViewById(R.id.view5)
        val view6: View = view.findViewById(R.id.view6)
        val view7: View = view.findViewById(R.id.view7)
        val view8: View = view.findViewById(R.id.view8)
        val view9: View = view.findViewById(R.id.view9)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_variation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = variations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = variations[position]
        holder.productVariation.text = item.productVariationName

        holder.editBtn.setOnClickListener {
            onEditClick(item, position)
        }
        holder.deleteBtn.setOnClickListener {
            onDeleteClick.invoke(position)
        }

        val prefs = holder.itemView.context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val showMRP = prefs.getBoolean("show_mrp", true)
        val showWholesale = prefs.getBoolean("show_wholesale", true)
        val showTax = prefs.getBoolean("show_tax", false)

        holder.productMRPPrice.visibility = if (showMRP && !item.mrp_price.isNullOrEmpty()) View.VISIBLE else View.GONE
        holder.view6.visibility = holder.productMRPPrice.visibility

        holder.productWholeSalePrice.visibility = if (showWholesale && !item.whole_sale_price.isNullOrEmpty()) View.VISIBLE else View.GONE
        holder.view7.visibility = holder.productWholeSalePrice.visibility



        fun setOrHide(view: AppCompatTextView, value: Any?, dividerView: View? = null, profileFlag: Boolean = true) {
            if (!profileFlag || value == null || value.toString().isEmpty()) {
                view.visibility = View.GONE
                (view.parent as? View)?.visibility = View.GONE
                dividerView?.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                (view.parent as? View)?.visibility = View.VISIBLE
                view.text = value.toString()
                dividerView?.visibility = View.VISIBLE
            }
        }

// Bind fields with their corresponding divider
        setOrHide(holder.productVariation, item.productVariationName, holder.view1)
        setOrHide(holder.productPrice, item.product_price, holder.view2)
        setOrHide(holder.productStock, item.stockCount, holder.view3)
        setOrHide(holder.productLowStock, item.low_stock_alert, holder.view4)
        setOrHide(holder.productSKU, item.productVariationId, holder.view5)
        setOrHide(holder.productMRPPrice, item.mrp_price, holder.view6, showMRP)
        setOrHide(holder.productWholeSalePrice, item.whole_sale_price, holder.view7, showWholesale)
        setOrHide(holder.productTax, item.recommended_status, holder.view8) // replace with real tax field
        setOrHide(holder.productUnitName, null, holder.view9) // replace with real unit field if available
    }

}
