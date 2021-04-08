package xyz.bluepencil.bluepencil.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import xyz.bluepencil.bluepencil.model.Order
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.formatDate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import xyz.bluepencil.bluepencil.model.User

class OrderJobAdapter(private val onClickListener: OnClickListener): RecyclerView.Adapter<OrderJobAdapter.ViewHolder>() {

    var data = listOf<Order>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var userCollection = Firebase.firestore.collection("users")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, onClickListener, userCollection)
    }


    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val orderRemark: TextView = itemView.findViewById(R.id.order_remark)
        val date: TextView = itemView.findViewById(R.id.date)
        val orderType: TextView = itemView.findViewById(R.id.order_type)
        val completeOrderBtn: Button = itemView.findViewById(R.id.complete_order_btn)
        val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)
        val linkChip: Chip = itemView.findViewById(R.id.linkChip)
        val contact: Chip = itemView.findViewById(R.id.contact)


        fun bind(item: Order, onClickListener: OnClickListener, ref: CollectionReference) {
            date.text = formatDate(item.date)
            orderRemark.text = item.remark
            orderType.text = if (item.type == "photo") "Photo" else "Graphic"

            contact.setOnClickListener { view->
                ref.whereEqualTo("uid", item.userId).get()
                    .addOnSuccessListener {
                        val email = it.documents[0].toObject(User::class.java)?.email
                        sendEmail(email.toString(), item.id.toString(), view.context)
                    }
                    .addOnFailureListener {
                        Toast.makeText(view.context, "Operation Failed", Toast.LENGTH_SHORT).show()
                    }
            }



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

            if (item.link.isNullOrBlank()) {
                linkChip.visibility = View.GONE
            } else {
                linkChip.visibility = View.VISIBLE
                linkChip.setOnClickListener {
                    Intent(Intent.ACTION_VIEW, Uri.parse(item.link)).apply {
                        it.context.startActivity(this)
                    }
                }
            }

            completeOrderBtn.setOnClickListener {
                onClickListener.onClick(item)
            }

        }

        private fun sendEmail(recipient: String, subject: String, context: Context) {

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Order reference number $subject")
            context.startActivity(Intent.createChooser(intent, "Choose Email Client"))

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