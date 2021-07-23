package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.results_list

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.AssessmentRowNormalBinding
import com.edward.nyansapo.databinding.ItemNumeracyAssessmentBinding

import com.example.edward.nyansapo.util.assessmentDocumentSnapshot
import com.example.edward.nyansapo.util.assessmentNumeracy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import es.dmoral.toasty.Toasty

class StudentAssessmentAdapter2(val onAssessmentClicked: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, StudentAssessmentAdapter2.ViewHolder>(DIFF_UTIL) {

    private val TAG = "StudentAssessmentAdapte"


    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.reference.path == newItem.reference.path

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.assessmentNumeracy
                val new = newItem.assessmentNumeracy
                return old == new
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_numeracy_assessment, parent, false)
        val binding: ItemNumeracyAssessmentBinding = ItemNumeracyAssessmentBinding.bind(view)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        holder.bind(getItem(position)!!)
    }

    inner class ViewHolder(val binding: ItemNumeracyAssessmentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DocumentSnapshot) {
            Log.d(TAG, "bind: assessment:${item.assessmentNumeracy}")
            setOnClickListeners(binding, position)
            binding.tvAssessment.setText("Assessment " + Integer.toString(itemCount - position))


        }

        private fun setOnClickListeners(binding: ItemNumeracyAssessmentBinding, position: Int) {


            binding.tvAssessment.setOnClickListener {
                Log.d(TAG, "setOnClickListeners: ")
                   onAssessmentClicked(getItem(position))
            }
        }

    }



}




