package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemLevelSectionsBinding
import com.google.firebase.firestore.DocumentSnapshot

class LevelSectionsAdapter2(private val contextMain:Context, val onStudentClicked: (DocumentSnapshot) -> Unit) : RecyclerView.Adapter<LevelSectionsAdapter2.ViewHolder>() {

    private val TAG = "LevelSectionsAdapter2"
    private var list:MutableList<LevelSections> = mutableListOf()
    fun submitList(list: MutableList<LevelSections>){
        this.list=list
        notifyDataSetChanged()
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
            beginnerAdapter = NumeracyLearningLevelAdapter {
             onStudentClicked(it)
            }
            binding.rvBeginner.apply {
                setHasFixedSize(false)
                addItemDecoration(NumeracyItemDecoration())
                layoutManager = LinearLayoutManager(contextMain)
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
        val section = list.get(position)
        holder.bind(section)
    }

    override fun getItemCount(): Int {
       return list.size
    }

}