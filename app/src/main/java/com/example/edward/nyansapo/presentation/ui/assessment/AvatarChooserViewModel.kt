package com.example.edward.nyansapo.presentation.ui.assessment

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edward.nyansapo.R
import com.example.edward.nyansapo.STUDENT_ARG
import com.example.edward.nyansapo.Student
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AvatarChooserViewModel @ViewModelInject constructor(@Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _avatarChooserEvents = Channel<AvatarChooserFragment.Event>()
    val avatarChooserEvents = _avatarChooserEvents.receiveAsFlow()

    fun setEvent(event: AvatarChooserFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AvatarChooserFragment.Event.AvatarSelected -> {
                    avatarSelected(event.viewId)
                }
            }
        }
    }

    private suspend fun avatarSelected(viewId: Int?) {
        val student = savedStateHandle.get<Student>(STUDENT_ARG)!!
        when (viewId) {
            R.id.cheetaImageView -> {
                student.avatar = R.drawable.nyansapo_avatar_cheeta
            }
            R.id.lionImageView -> {
                student.avatar = R.drawable.nyansapo_avatar_lion
            }
            R.id.rhinoImageView -> {
                student.avatar = R.drawable.nyansapo_avatar_rhino
            }
            R.id.buffaloImageView -> {
                student.avatar = R.drawable.nyansapo_avatar_waterbuffalo
            }
            R.id.elephantImageView -> {
                student.avatar = R.drawable.nyansapo_avatar_elelphant
            }

        }

        _avatarChooserEvents.send(AvatarChooserFragment.Event.GoSelectAssessment(student))
    }
}