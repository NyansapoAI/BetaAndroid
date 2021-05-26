package com.example.edward.nyansapo.presentation.ui.attendance

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class AttendanceViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    val getCurrentDate = repository.getCurrentDate()

    private val _attendanceEvents = Channel<AttendanceFragment2.Event>()
    val attendanceEvents = _attendanceEvents.receiveAsFlow()
    fun setEvent(event: AttendanceFragment2.Event) {
        viewModelScope.launch {
            when (event) {
                is AttendanceFragment2.Event.InitDataFetching -> {
                    initDataFetching(event.date)
                }

            }
        }
    }

    private fun initDataFetching(date: Date) {

    }
}