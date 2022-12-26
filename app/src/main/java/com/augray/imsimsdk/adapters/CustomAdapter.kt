package com.augray.imsimsdk.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.augray.imsimsdk.R
import com.augray.imsimsdk.viewmodel.ItemsViewModel
import java.util.*
import java.util.Collections.emptyList

class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    var onItemClick: ((Uri) -> Unit)? = null
    var uris: ArrayList<Uri>? = null
    var mList: List<ItemsViewModel> ? = null;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList?.get(position)
        holder.imageView.setImageResource(itemsViewModel!!.image)
        holder.textView.text = itemsViewModel!!.text
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(uris!![adapterPosition])
            }
        }
    }
}