package com.huawei.codelabs.iap.example.huawei

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.iap.IapClient
import kotlinx.android.synthetic.main.recycler_view_item.view.*

class RecyclerViewAdapter(private val products: ArrayList<ProductModel>,
                          private val payProcess: (String, Int) -> Unit) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener {
                with(products[adapterPosition]) {
                    payProcess(productId, type)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            priceTextView.text = products[position].price
            itemHeader.text = products[position].title
            itemText.text = products[position].description
            itemType.text = when (products[position].type) {
                IapClient.PriceType.IN_APP_SUBSCRIPTION -> "Subscription"
                IapClient.PriceType.IN_APP_CONSUMABLE -> "Consumable"
                IapClient.PriceType.IN_APP_NONCONSUMABLE -> "Non-consumable"
                else -> "undefined"
            }
        }
    }

}