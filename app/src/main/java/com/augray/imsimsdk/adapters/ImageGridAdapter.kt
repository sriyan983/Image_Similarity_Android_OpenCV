package com.augray.imsimsdk.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.augray.imsimsdk.R
import com.augray.imsimsdk.viewmodel.ImageViewModel
import java.util.*

class ImageGridAdapter(
    private val courseList: ArrayList<ImageViewModel>,
    private val context: Context
) : RecyclerView.Adapter<ImageGridAdapter.CourseViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageGridAdapter.CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.image_item,
            parent, false
        )
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageGridAdapter.CourseViewHolder, position: Int) {
        holder.courseNameTV.text = courseList.get(position).fileName
        holder.courseIV.setImageBitmap(courseList.get(position).imgBitmap)
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameTV: TextView = itemView.findViewById(R.id.idTVCourse)
        val courseIV: ImageView = itemView.findViewById(R.id.idIVCourse)
    }
}
