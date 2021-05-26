package com.example.edward.nyansapo.presentation.ui.home


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor() : ViewModel() {

    private val _homeEvents = Channel<HomeFragment.Event>()
    val homeEvents = _homeEvents.receiveAsFlow()
    fun setEvent(event: HomeFragment.Event){
        viewModelScope.launch {
                _homeEvents.send(event)
        }
    }
}