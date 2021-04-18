package xyz.bluepencil.bluepencil.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import xyz.bluepencil.bluepencil.model.Placard
import xyz.bluepencil.bluepencil.R
import xyz.bluepencil.bluepencil.getCurrencyString
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
        val editorImgView: ImageView = itemView.findViewById((R.id.editor_img))
        val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)
        val verify: ImageView = itemView.findViewById(R.id.verify)
        val tag: TextView = itemView.findViewById(R.id.editor_tag)
        val free: TextView = itemView.findViewById(R.id.free)
        val discount: ImageView = itemView.findViewById(R.id.discount)

        fun bind(item: Placard, onClickListener: OnClickListener) {
            userName.text = item.userName
            userName.setOnClickListener {
                onClickListener.onClick(item, true)
            }
            price.text = getCurrencyString(item.cost)
            tag.text = if (item.type == "photo") "Photo Editor" else "Graphic Designer"
            if (item!!.free!!) {
                free.visibility = View.VISIBLE
                discount.visibility = View.VISIBLE
            } else {
                free.visibility = View.GONE
                discount.visibility = View.GONE
            }

            orderButton.setOnClickListener {
                onClickListener.onClick(item, false)
            }

            item.url?.let {
                val imgUri = it.toUri().buildUpon().scheme("https").build()
                Glide.with(editorImgView.context)
                    .load(imgUri)
                    .apply(
                        RequestOptions()
                        .error(R.drawable.ic_broken_image))
                    .into(editorImgView)
            }

            val size = item.tags?.size ?: 0

            for (i in 0..2) {
                val v: View = chipGroup.getChildAt(i)
                if (v is Chip) {
                    if (i < size) {
                        v.visibility = View.VISIBLE
                        v.text = item.tags?.get(i)
                    } else {
                        v.visibility = View.GONE
                    }
                }
            }

            verify.setOnClickListener {
                Toast.makeText(it.context, "Verified", Toast.LENGTH_SHORT).show()
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

    class OnClickListener(val clickListener: (placard: Placard, isProfile: Boolean) -> Unit) {
        fun onClick(placard: Placard, isProfile: Boolean) = clickListener(placard, isProfile)
    }

}