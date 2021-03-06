package xyz.bluepencil.bluepencil.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.formatDate
import xyz.bluepencil.bluepencil.model.Order
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.storage.FirebaseStorage
import java.io.File


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
        val progress: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)
        val editorNameTxt: TextView = itemView.findViewById(R.id.artist_name)
        val orderTypeTxt: TextView = itemView.findViewById(R.id.order_type)

        fun bind(item: Order) {
            date.text = formatDate(item.date)
            editorNameTxt.text = item.editorName
            orderTypeTxt.text = if (item.type == "photo") "Photo" else "Graphic"

            if (item.complete == false) {
                orderStatus.text = "Pending"
                chipGroup.visibility = View.GONE
            } else {
                orderStatus.text ="Completed"
                chipGroup.visibility = View.VISIBLE
                configureChips(item)

            }

        }

        private fun configureChips(order: Order) {
            val size = order.jobUrls?.size ?: 0
            for (i in 0..2) {
                val v: View = chipGroup.getChildAt(i)
                if (v is Chip) {
                    if (i < size) {
                        v.visibility = View.VISIBLE
                        v.setOnClickListener {
                            downloadAndOpenImage(it.context, order.jobUrls?.get(i) ?: "")
                        }
                    } else {
                        v.visibility = View.GONE
                    }
                }
            }
        }

        private fun downloadAndOpenImage(context: Context, url: String) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            val localFile = File(context.getExternalFilesDir(null)?.absolutePath.toString() + ref.name)

            progress.visibility = View.VISIBLE

            if (localFile.exists()) {
                openImage(context, localFile)
                progress.visibility = View.GONE
            }
            else {
                ref.getFile(localFile).addOnSuccessListener {
                    openImage(context, localFile)
                    progress.visibility = View.GONE
                }.addOnProgressListener {
                    val prog = (100.0 * it.bytesTransferred) / it.totalByteCount
                    progress.progress = prog.toInt()
                }.addOnFailureListener {
                    progress.visibility = View.GONE
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_LONG).show()
                }
            }



        }

        private fun openImage(context: Context, file: File) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
            intent.setDataAndType(uri, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent)
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