package com.example.bluepencil.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluepencil.model.Order
import com.example.bluepencil.R
import com.example.bluepencil.formatDate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class OrderJobAdapter(private val onClickListener: OnClickListener): RecyclerView.Adapter<OrderJobAdapter.ViewHolder>() {

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
        holder.bind(item, onClickListener)
    }


    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val orderRemark: TextView = itemView.findViewById(R.id.order_remark)
        val date: TextView = itemView.findViewById(R.id.date)
        val orderType: TextView = itemView.findViewById(R.id.order_type)
        val completeOrderBtn: Button = itemView.findViewById(R.id.complete_order_btn)
        val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(item: Order, onClickListener: OnClickListener) {
            date.text = formatDate(item.date)
            orderRemark.text = item.remark
            orderType.text = if (item.type == "photo") "Photo" else "Graphic"

            val size = item.photoUrls?.size ?: 0

            for (i in 0..2) {
                val v: View = chipGroup.getChildAt(i)
                if (v is Chip) {
                    if (i < size) {
                        v.visibility = View.VISIBLE
                        v.setOnClickListener {
                            Intent(Intent.ACTION_VIEW, Uri.parse(item.photoUrls?.get(i))).apply {
                                v.context.startActivity(this)
                            }
                        }
                    } else {
                        v.visibility = View.GONE
                    }
                }
            }

            completeOrderBtn.setOnClickListener {
                onClickListener.onClick(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_order_job, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class OnClickListener(val clickListener: (order: Order) -> Unit) {
        fun onClick(order: Order) = clickListener(order)
    }


}