package com.example.edward.nyansapo.paragraph_chooser

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.ActivityParagraphBinding
import com.example.edward.nyansapo.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ParagraphChooserFragment : Fragment(R.layout.activity_paragraph) {

             private  val TAG="ParagraphChooserFragment"

    private lateinit var binding: ActivityParagraphBinding
    private val viewModel: ParagraphChooserViewModel by viewModels()
    private val navArgs:ParagraphChooserFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityParagraphBinding.bind(view)
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getParagraphsStatus.collect {
                    Log.d(TAG, "subScribeToObservers: getParagraphsStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {
                            setParagraphs(it.data!!)
                            setOnClickListeners()

                        }
                        Resource.Status.ERROR -> {

                        }
                    }
                }
            }
            launch {
                viewModel.paragraphChooserEvents.collect {
                    when(it){
                        is Event.ParagraphClicked->{
                            startParagraph(it.paragraph)
                        }
                    }
                }
            }
        }
    }

    private fun startParagraph(paragraph: Int) {
        val assessment=navArgs.assessment
        assessment.paragraphChoosen=paragraph
        findNavController().navigate(ParagraphChooserFragmentDirections.actionParagraphChooserFragmentToParagraphAssessmentFragment(navArgs.assessment,navArgs.student))
    }

    private fun setOnClickListeners() {
        binding.paragraph1.setOnClickListener {
            viewModel.setEvent(Event.ParagraphClicked(0))
        }
        binding.paragraph2.setOnClickListener {
            viewModel.setEvent(Event.ParagraphClicked(1))

        }
    }

    private fun setParagraphs(data: Array<String>) {
        binding.paragraph1.text=data[0]
        binding.paragraph2.text=data[1]
    }

    sealed class Event {
        data class ParagraphClicked(val paragraph: Int) : Event()
    }
}