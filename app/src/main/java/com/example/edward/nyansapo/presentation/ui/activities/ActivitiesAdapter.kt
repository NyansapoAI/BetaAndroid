package com.example.edward.nyansapo.presentation.ui.activities

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemStudentBinding

class ActivitiesAdapter( val onActivityClicked: (Activity) -> Unit) : ListAdapter<Activity, ActivitiesAdapter.ViewHolder>(DIFF_UTIL) {

         private  val TAG="ActivitiesAdapter"


    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<Activity>() {
            override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
                return oldItem == newItem
            }

        }
    }


    inner class ViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        val binding = ItemStudentBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.nameTxtView.text = getItem(position).name
        holder.itemView.setOnClickListener {
            Log.d(TAG, "setOnClickListener: position:${holder.bindingAdapterPosition} item:${getItem(holder.bindingAdapterPosition).name}}")
            onActivityClicked(getItem(holder.bindingAdapterPosition))
        }
    }






}