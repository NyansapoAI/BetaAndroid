package com.example.edward.nyansapo.presentation.ui.attendance

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemAttendanceBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.item_attendance.view.*
import kotlinx.android.synthetic.main.item_spinner.view.*
import kotlinx.android.synthetic.main.item_spinner.view.nameTxtView

class AttendanceAdapter2(val onStudentChecked: (documentSnapshot: DocumentSnapshot, isChecked: Boolean) -> Unit) : ListAdapter<DocumentSnapshot, AttendanceAdapter2.ViewHolder>(DIFF_UTIL) {

    private val TAG = "AttendanceAdapter2"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.reference.path == newItem.reference.path
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(StudentAttendance::class.java)!!
                val new = newItem.toObject(StudentAttendance::class.java)!!
                return old == new
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        val binding: ItemAttendanceBinding = ItemAttendanceBinding.bind(view)

        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudentAttendance) = with(itemView) {

            nameTxtView.setText(item.name)
            attendanceCheckbox.isChecked = item.present
            binding.attendanceCheckbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                    val student = getItem(bindingAdapterPosition).toObject(StudentAttendance::class.java)!!
                    if (student.present == isChecked) {
                        Log.d(TAG, "onCheckedChanged: no need to change")
                    } else {
                        onStudentChecked(getItem(adapterPosition), isChecked)
                    }


                }
            })

        }


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position].toObject(StudentAttendance::class.java)!!
        holder.bind(item)

    }


}

