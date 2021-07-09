package com.example.edward.nyansapo.presentation.ui.select_task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemSelectTaskBinding

class SelectTaskAdapter(private val list: List<String>, val itemClicked: (Int) -> Unit) : RecyclerView.Adapter<SelectTaskAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_task, parent, false)
        val binding = ItemSelectTaskBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list.get(position)
        holder.bind(model)
    }

    override fun getItemCount() =
            list.size


    inner class ViewHolder(val binding: ItemSelectTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: String) {
            binding.tvPos.text = "${bindingAdapterPosition+1}"
            binding.tvName.text = "$model"

            binding.root.setOnClickListener { itemClicked(bindingAdapterPosition+1) }
        }

    }
}