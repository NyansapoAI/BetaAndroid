package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ItemOperationBinding
import com.example.edward.nyansapo.numeracy.Problem

class AdditionAdapter(private var originalList: MutableList<Operation> = mutableListOf()) : RecyclerView.Adapter<AdditionAdapter.ViewHolder>() {

         private  val TAG="AdditionAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_operation, parent, false)
        val binding = ItemOperationBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = originalList[position]
        Log.d(TAG, "onBindViewHolder: model:$model")
        holder.bind(model)
    }

    fun submitList(list: MutableList<Operation>) {
        Log.d(TAG, "submitList: size:${list.size}")
        this.originalList = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = originalList.size

    inner class ViewHolder(val binding: ItemOperationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Operation) {
            val correctWrongList = mutableListOf<Problem>()
            correctWrongList.addAll(model.correctList)
            correctWrongList.addAll(model.wrongList)
            for (i in 0..model.correct - 1) {
                val tvFirst = binding.root.findViewWithTag<TextView>("tvAdditionFirst_$i")
                val tvSecond = binding.root.findViewWithTag<TextView>("tvAdditionSecond_$i")
                val tvAnswer = binding.root.findViewWithTag<TextView>("tvAdditionAnswer_$i")
                val problem = correctWrongList.get(i)
                tvFirst.text = "${problem.first}"
                tvSecond.text = "${problem.second}"
                tvAnswer.text = "${problem.answer}"
                tvAnswer.setTextColor(Color.BLACK)
                tvAnswer.background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_number_recog)
                val tvSign = binding.root.findViewWithTag<TextView>("tvSign_$i")
                tvSign.text=model.sign


            }
            for (i in model.correct..2) {
                val tvFirst = binding.root.findViewWithTag<TextView>("tvAdditionFirst_$i")
                val tvSecond = binding.root.findViewWithTag<TextView>("tvAdditionSecond_$i")
                val tvAnswer = binding.root.findViewWithTag<TextView>("tvAdditionAnswer_$i")
                val problem = correctWrongList.get(i)
                tvFirst.text = "${problem.first}"
                tvSecond.text = "${problem.second}"
                tvAnswer.text = "${problem.answer}"
                tvAnswer.setTextColor(Color.RED)
                tvAnswer.background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_number_recog_red)

                val tvSign = binding.root.findViewWithTag<TextView>("tvSign_$i")
                tvSign.text=model.sign

            }

        }

    }
}