package com.example.edward.nyansapo.paragraph_chooser

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.ASSESSMENT_ARG
import com.example.edward.nyansapo.Assessment
import com.example.edward.nyansapo.Assessment_Content
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ParagraphChooserViewModel @ViewModelInject constructor(private val repo: MainRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val _paragraphChooserEvents= Channel<ParagraphChooserFragment.Event>()
    val paragraphChooserEvents=_paragraphChooserEvents.receiveAsFlow()
    fun setEvent(event: ParagraphChooserFragment.Event){
        viewModelScope.launch {
            when(event){
                is ParagraphChooserFragment.Event.ParagraphClicked->{
                    _paragraphChooserEvents.send(ParagraphChooserFragment.Event.ParagraphClicked(event.paragraph))
                }
            }
        }
    }

    private val _getParagraphsStatus = Channel<Resource<Array<String>>>()
    val getParagraphsStatus = _getParagraphsStatus.receiveAsFlow()

    init {
        viewModelScope.launch {
            val assessmentKey = savedStateHandle.get<Assessment>(ASSESSMENT_ARG)!!.assessmentKey
            _getParagraphsStatus.send(Resource.loading("getting paragraphs..."))
            val paragraphs = getParagraphs(assessmentKey.toString())
            _getParagraphsStatus.send(Resource.success(paragraphs))
        }
    }

   private fun getParagraphs(key: String?): Array<String> {
        return when (key) {
            "3" -> {

                Assessment_Content.getP3()
            }
            "4" -> {
                Assessment_Content.getP4()
            }
            "5" -> {
                Assessment_Content.getP5()
            }
            "6" -> {
                Assessment_Content.getP6()
            }
            "7" -> {
                Assessment_Content.getP7()
            }
            "8" -> {
                Assessment_Content.getP8()
            }
            "9" -> {
                Assessment_Content.getP9()
            }
            "10" -> {
                Assessment_Content.getP10()
            }
            else -> Assessment_Content.getP3()
        }
    }
}