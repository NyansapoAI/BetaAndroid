package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemNumeracyLearningLeveBinding
import com.example.edward.nyansapo.Student
import com.google.firebase.firestore.DocumentSnapshot

class NumeracyLearningLevelAdapter(val onStudentClicked: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, NumeracyLearningLevelAdapter.ViewHolder>(DIFF_UTIL) {

         private  val TAG="NumeracyLearningLevelAd"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.reference.path == newItem.reference.path
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(Student::class.java)!!
                val new = newItem.toObject(Student::class.java)!!
                return old == new
            }
        }
    }

    inner class ViewHolder(val binding: ItemNumeracyLearningLeveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot) {
            val model = snapshot.toObject(Student::class.java)!!
            Log.d(TAG, "bind: model:$model")
            binding.tvName.text = "${model.firstname} ${model.lastname}"
            setOnClickListener(snapshot)
        }

        private fun setOnClickListener(snapshot: DocumentSnapshot) {
            binding.root.setOnClickListener {
                onStudentClicked(snapshot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_numeracy_learning_leve, parent, false)
        val binding = ItemNumeracyLearningLeveBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val snapshot = getItem(position)
        holder.bind(snapshot)
    }
}