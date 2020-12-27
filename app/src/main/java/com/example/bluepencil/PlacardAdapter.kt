package com.example.bluepencil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlacardAdapter(private val onClickListener: OnClickListener): RecyclerView.Adapter<PlacardAdapter.ViewHolder>() {

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
        holder.bind(item, onClickListener)
    }


    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val price: TextView = itemView.findViewById(R.id.price)
        val orderButton: Button = itemView.findViewById(R.id.order_btn)

        fun bind(item: Placard, onClickListener: OnClickListener) {
            userName.text = item.userName
            price.text = getCurrencyString(item.cost)
            orderButton.setOnClickListener {
                onClickListener.onClick(item)
            }

        }

        companion object {
             fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_placard, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class OnClickListener(val clickListener: (placard: Placard) -> Unit) {
        fun onClick(placard: Placard) = clickListener(placard)
    }


}