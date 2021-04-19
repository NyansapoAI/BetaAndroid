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

class ActivitiesAdapter(val fragment: ActivitiesFragment, val onSearchViewEmpty: () -> Unit, val onActivityClicked: (Activity) -> Unit) : ListAdapter<Activity, ActivitiesAdapter.ViewHolder>(DIFF_UTIL), Filterable {

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
         var currentItem:Activity
            try {
                currentItem=getItem(position)
            }catch (e:IndexOutOfBoundsException){
                currentItem=getItem(position-1)
                e.printStackTrace()
            }

             onActivityClicked(currentItem)
        }
    }



    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(string: CharSequence?): FilterResults {
                val list = mutableListOf<Activity>()
                for (actitity in fragment.originalList!!) {
                    if (actitity.level.contains(string!!, ignoreCase = true)) {
                        list.add(actitity)
                    } else if (actitity.name.contains(string!!, ignoreCase = true)) {
                        list.add(actitity)
                    }

                }

                val results = FilterResults()
                results.values = list

                return results

            }

            override fun publishResults(string: CharSequence?, results: FilterResults?) {

                if (string?.isBlank()!!) {
                    onSearchViewEmpty()
                } else {
                    submitList(results?.values as List<Activity>?)

                }

            }
        }
    }


}