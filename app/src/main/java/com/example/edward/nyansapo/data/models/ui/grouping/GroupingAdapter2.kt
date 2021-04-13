package com.example.edward.nyansapo.data.models.ui.grouping

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemStudentBinding
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.data.models.ui.main.MainActivity2
import com.example.edward.nyansapo.presentation.utils.studentDocumentSnapshot
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import es.dmoral.toasty.Toasty

class GroupingAdapter2(val fragment2: GroupingFragment2?=null, val searchViewEmpty: () -> Unit={}, val onStudentClick: (DocumentSnapshot) -> Unit, val onStudentLongClicked: (DocumentSnapshot) -> Unit={}) : ListAdapter<DocumentSnapshot, GroupingAdapter2.ViewHolder>(DIFF_UTIL), Filterable {

    private val TAG = "LearningLevelAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {


                return oldItem.toObject(Student::class.java)?.id == newItem.toObject(Student::class.java)?.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.toObject(Student::class.java) == newItem.toObject(Student::class.java)
            }
        }
    }

    private val context: Context? = MainActivity2.activityContext!!


    private fun setOnClickListeners(holder: ViewHolder, positionDummy: Int) {

        val position = holder.bindingAdapterPosition
        holder.itemView.setOnClickListener {
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            studentDocumentSnapshot = getItem(position)
            onStudentClick(getItem(position))
        }
        holder.itemView.setOnLongClickListener {
            if (position == RecyclerView.NO_POSITION) {
                return@setOnLongClickListener false
            }
            studentDocumentSnapshot = getItem(position)
            onStudentLongClicked(getItem(position))

            true
        }


    }

    fun deleteFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(context!!).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteData(position) }.show()
    }

    private fun deleteData(position: Int) {
        val currentSnapshot = getItem(position)

        currentSnapshot.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(context!!, "Deletion Success", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(context!!, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        val binding: ItemStudentBinding = ItemStudentBinding.bind(view)

        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val model = getItem(position).toObject(Student::class.java)!!
            Log.d(TAG, "onBindViewHolder: " + model.firstname + " " + model.lastname)

            setOnClickListeners(holder, position)

            nameTxtView.setText(model.firstname + " " + model.lastname)


        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var list: MutableList<DocumentSnapshot> = mutableListOf()
                for (snapshot in fragment2!!.originalList) {
                    val student = snapshot.toObject(Student::class.java)
                    if (student?.firstname!!.toLowerCase().contains(constraint.toString()) || student?.lastname!!.toLowerCase().contains(constraint.toString())) {
                        list.add(snapshot)
                    }
                }
                val results = FilterResults()
                results.values = list
                return results

            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (constraint?.trim()?.isEmpty()!!) {
                    searchViewEmpty()
                } else {
                    val list = results?.values as MutableList<DocumentSnapshot>
                    submitList(list)
                }
            }
        }
    }


}

