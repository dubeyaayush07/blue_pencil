package xyz.bluepencil.bluepencil.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.formatDate
import xyz.bluepencil.bluepencil.model.Order
import xyz.bluepencil.bluepencil.model.User
import java.io.File


class OrderInboxAdapter: RecyclerView.Adapter<OrderInboxAdapter.ViewHolder>() {

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
        holder.bind(item, userCollection)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val orderStatus: TextView = itemView.findViewById(R.id.order_status)
        val date: TextView = itemView.findViewById(R.id.date)
        val editorNameTxt: TextView = itemView.findViewById(R.id.artist_name)
        val orderTypeTxt: TextView = itemView.findViewById(R.id.order_type)
        val contact: Chip = itemView.findViewById(R.id.contact)
        val complete: Chip = itemView.findViewById(R.id.jobUrls)
        val count: Chip = itemView.findViewById(R.id.count)

        fun bind(item: Order, ref: CollectionReference) {
            date.text = formatDate(item.date)
            editorNameTxt.text = item.editorName
            orderTypeTxt.text = "Graphic"
            count.text = item.count.toString()

            contact.setOnClickListener { view->
                ref.whereEqualTo("uid", item.editorId).get()
                    .addOnSuccessListener {
                        val email = it.documents[0].toObject(User::class.java)?.email
                        sendEmail(email.toString(), item.id.toString(), view.context)
                    }
                    .addOnFailureListener {
                        Toast.makeText(view.context, "Unable to fetch patient", Toast.LENGTH_SHORT).show()
                    }
            }

            if (item.complete == false) {
                orderStatus.text = "Pending"
                complete.visibility = View.GONE
            } else {
                orderStatus.text ="Completed"
                complete.visibility = View.VISIBLE
                complete.setOnClickListener {
                    Intent(Intent.ACTION_VIEW, Uri.parse(item.jobUrls?.get(0))).apply {
                        it.context.startActivity(this)
                    }
                }
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
                    .inflate(R.layout.list_item_order_inbox, parent, false)
                return ViewHolder(view)
            }
        }
    }

}