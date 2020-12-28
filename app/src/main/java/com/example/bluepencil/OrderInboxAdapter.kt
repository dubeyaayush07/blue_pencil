package com.example.bluepencil

import android.content.Intent
import android.net.Uri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderInboxAdapter: RecyclerView.Adapter<OrderInboxAdapter.ViewHolder>() {

    var data = listOf<Order>()
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
        val orderStatus: TextView = itemView.findViewById(R.id.order_status)
        val date: TextView = itemView.findViewById(R.id.date)
        val photoBtn: Button = itemView.findViewById(R.id.view_photo_btn)

        fun bind(item: Order) {
            date.text = formatDate(item.date)
            if (item.jobUrl.equals("")) {
                orderStatus.text = "Pending"
            } else orderStatus.text ="Completed"

            photoBtn.setOnClickListener { view->
                Intent(Intent.ACTION_VIEW, Uri.parse(item.photoUrl)).apply {
                    view.context.startActivity(this)
                }
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_order_inbox, parent, false)
                return ViewHolder(view)
            }
        }
    }

}