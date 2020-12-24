package com.example.bluepencil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlacardAdapter: RecyclerView.Adapter<PlacardAdapter.ViewHolder>() {

    var data = listOf<Placard>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }


    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val price: TextView = itemView.findViewById(R.id.price)

        fun bind( item: Placard) {
            userName.text = item.userName
            price.text = getCurrencyString(item.cost)
        }

        companion object {
             fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_placard, parent, false)
                return ViewHolder(view)
            }
        }
    }


}