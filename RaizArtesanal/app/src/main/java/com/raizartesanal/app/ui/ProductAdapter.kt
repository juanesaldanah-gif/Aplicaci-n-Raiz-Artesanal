package com.raizartesanal.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raizartesanal.app.R
import com.raizartesanal.app.data.Product

class ProductAdapter(private val onClick: (Product) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.Holder>() {

    private var items: List<Product> = emptyList()

    fun submit(list: List<Product>) {
        items = list
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = items[position]
        holder.name.text = product.name
        holder.price.text = product.price.toCop()
        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount(): Int = items.size
}
