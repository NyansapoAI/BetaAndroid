package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemLevelSectionsBinding
import com.edward.nyansapo.databinding.ItemNumeracyLearningLeveBinding
import com.example.edward.nyansapo.Student
import com.google.firebase.firestore.DocumentSnapshot

class LevelSectionsAdapter(private val context: Context, val onStudentClicked: (DocumentSnapshot) -> Unit) : ListAdapter<LevelSections, LevelSectionsAdapter.ViewHolder>(DIFF_UTIL) {

    private val TAG = "LevelSectionsAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<LevelSections>() {
            override fun areItemsTheSame(oldItem: LevelSections, newItem: LevelSections): Boolean {
                return oldItem.header==newItem.header
            }

            override fun areContentsTheSame(oldItem: LevelSections, newItem: LevelSections): Boolean {
            return oldItem.equals(newItem)
            }

        }
    }

    inner class ViewHolder(val binding: ItemLevelSectionsBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var beginnerAdapter: NumeracyLearningLevelAdapter

        init {
            initRecyclerViewAdapters()
        }

        fun bind(model: LevelSections) {
            Log.d(TAG, "bind: model:$model")
            Log.d(TAG, "bind:size:${model.students.size} ")
            binding.tvBeginner.text = model.header
            beginnerAdapter.submitList(model.students)
            setOnClickListener()
        }

        private fun initRecyclerViewAdapters() {
            beginnerAdapter = NumeracyLearningLevelAdapter { onStudentClicked(it) }
            binding.rvBeginner.apply {
                setHasFixedSize(false)
                addItemDecoration(NumeracyItemDecoration())
                layoutManager = LinearLayoutManager(context)
                adapter = beginnerAdapter
            }


        }

        private fun setOnClickListener() {
            binding.ivBeginner.setOnClickListener {
                dropDownClicked()
            }
        }

        private fun dropDownClicked() {
            binding.apply {
                if (rvBeginner.isVisible) {
                    Log.d(TAG, "dropDownClicked: isVisible:${rvBeginner.isVisible}")
                    ivBeginner.setImageResource(R.drawable.ic_arrow_up)
                    rvBeginner.isVisible = false
                } else {
                    Log.d(TAG, "dropDownClicked: isVisible:${rvBeginner.isVisible}")
                    ivBeginner.setImageResource(R.drawable.ic_arrow_down)
                    rvBeginner.isVisible = true

                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level_sections, parent, false)
        val binding = ItemLevelSectionsBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = getItem(position)
        holder.bind(section)
    }
}