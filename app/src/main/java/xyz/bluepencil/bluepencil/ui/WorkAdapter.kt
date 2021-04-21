package xyz.bluepencil.bluepencil.ui



import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xyz.bluepencil.bluepencil.R

class WorkAdapter: RecyclerView.Adapter<WorkAdapter.ViewHolder>() {

    var data = listOf<String>()
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
        val work: TextView = itemView.findViewById(R.id.work)

        fun bind(item: String) {
            work.text = item
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_work, parent, false)
                return ViewHolder(view)
            }
        }
    }

}