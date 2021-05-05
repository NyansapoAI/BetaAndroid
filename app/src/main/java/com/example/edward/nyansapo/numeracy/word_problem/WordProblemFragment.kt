package com.example.edward.nyansapo.numeracy.word_problem

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentWordProblemBinding
import com.example.edward.nyansapo.wrappers.Resource
import com.google.mlkit.vision.digitalink.Ink
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_addition.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordProblemFragment : Fragment(R.layout.fragment_word_problem) {

    private val TAG = "WordProblemFragment"

    private val viewModel: WordProblemViewModel by viewModels()
    private lateinit var binding: FragmentWordProblemBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWordProblemBinding.bind(view)
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getWordProblem.collect {
                    Log.d(TAG, "subScribeToObservers: getWordProblem:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            initUi(it.data!!)
                            setOnClickListeners()

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }
            launch {
                viewModel.analysesStatus.collect {
                    Log.d(TAG, "subScribeToObservers: analysesStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                        }
                        Resource.Status.ERROR -> {
                            showToastInfo("Error: ${it.exception?.message}")
                        }
                    }
                }
            }
        }


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListeners() {
        binding.imageViewAvatar.setOnClickListener {
            viewModel.setEvent(Event.StartAnalysis(binding.answerTxtView.inkBuilder))
        }

        binding.apply {
            btnResetAnswer.setOnClickListener {
                binding.answerTxtView.clearDrawing()
            }
            btnResetWorkSpace.setOnClickListener {
                binding.root.clearDrawing()
            }

        }
    }

    private fun initUi(data: Pair<String, String>) {
        binding.problemTxtView.text = data.first
    }


    sealed class Event {
        data class StartAnalysis(val inkBuilder: Ink.Builder) : Event()

    }
}