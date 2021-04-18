package xyz.bluepencil.bluepencil.ui



import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xyz.bluepencil.bluepencil.R

class ProfileAdapter: RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

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
        val photoView: ImageView = itemView.findViewById(R.id.photo)

        fun bind(item: String) {
            val imgUri = item.toUri().buildUpon().scheme("https").build()
            Glide.with(photoView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .error(R.drawable.ic_broken_image))
                .into(photoView)

            photoView.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse(item)).apply {
                    it.context.startActivity(this)
                }
            }
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_photo, parent, false)
                return ViewHolder(view)
            }
        }
    }

}