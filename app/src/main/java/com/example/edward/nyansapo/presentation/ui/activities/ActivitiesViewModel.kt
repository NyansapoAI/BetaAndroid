package com.example.edward.nyansapo.presentation.ui.activities

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.data.repositories.Repository
import com.example.edward.nyansapo.presentation.ui.preassessment.MainRepository
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.activity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ActivitiesViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    private val TAG = "ActivitiesViewModel"


    fun setEvent(event: Event) {
        viewModelScope.launch {
            when (event) {
                is Event.StartQuery -> {
                    startQuery(event.query)
                }
            }
        }
    }


    private fun startQuery(query: String) {
        Log.d(TAG, "startQuery: query:$query")
        val activities = allActivitiesFlow.value.filter { it.name.contains(query, ignoreCase = true) }
        wholeClassFlow.value = activities.filter { it.level.contains("all", ignoreCase = true) }
        beginnerFlow.value = activities.filter { it.level.contains("beginner", ignoreCase = true) }
        letterFlow.value = activities.filter { it.level.contains("letter", ignoreCase = true) }
        wordFlow.value = activities.filter { it.level.contains("word", ignoreCase = true) }
        paragraphFlow.value = activities.filter { it.level.contains("paragraph", ignoreCase = true) }
        storyFlow.value = activities.filter { it.level.contains("story", ignoreCase = true) }

    }


    val getActivitiesStatus get() = _getActivitiesStatus
    private val _getActivitiesStatus: Flow<Resource<List<Activity>>> = flow {
        allActivitiesFlow.value.clear()
        emit(Resource.loading("loading"))
        val activities = repository.getActivities()
        allActivitiesFlow.value.addAll(activities.toMutableList())
        try {
            val result = repository.getRemoteActivities()
            Log.d(TAG, "remoteActivites:Size:${result.size} ")
            val remoteActivities = result.map {
                it.activity
            }
            allActivitiesFlow.value.addAll(remoteActivities.toMutableList())
        } catch (e: Exception) {
            emit(Resource.error<List<Activity>>(e))

        }
        if (allActivitiesFlow.value.isEmpty()) {
            emit(Resource.error<List<Activity>>(Exception("no activities available")))
        } else {
            emit(Resource.success(allActivitiesFlow.value))
            startSortingActivities(allActivitiesFlow.value)
        }
    }


    val allActivitiesFlow = MutableStateFlow(mutableListOf<Activity>())

    val wholeClassFlow = MutableStateFlow(listOf<Activity>())
    val beginnerFlow = MutableStateFlow(listOf<Activity>())
    val letterFlow = MutableStateFlow(listOf<Activity>())
    val wordFlow = MutableStateFlow(listOf<Activity>())
    val paragraphFlow = MutableStateFlow(listOf<Activity>())
    val storyFlow = MutableStateFlow(listOf<Activity>())
    private fun startSortingActivities(activities: List<Activity>) {
        Log.d(TAG, "startSortingActivities: size:${activities.size}")
        activities.forEachIndexed { index, activity ->
            Log.d(TAG, "startSortingActivities: activity $index::$activity")
        }
        wholeClassFlow.value = activities.filter { it.level.contains("all", ignoreCase = true) }
        beginnerFlow.value = activities.filter { it.level.contains("beginner", ignoreCase = true) }
        letterFlow.value = activities.filter { it.level.contains("letter", ignoreCase = true) }
        wordFlow.value = activities.filter { it.level.contains("word", ignoreCase = true) }
        paragraphFlow.value = activities.filter { it.level.contains("paragraph", ignoreCase = true) }
        storyFlow.value = activities.filter { it.level.contains("story", ignoreCase = true) }
    }


    private val _activitiesQueryStatus = MutableStateFlow(Resource.empty<List<Activity>>())
    val activitiesQueryStatus get() = _activitiesQueryStatus
    private fun startLearningLevelQuery(query: String) {
        if (query.isBlank()) {
            activitiesQueryStatus.value = Resource.error(Exception("query is empty"))
        } else {
            getActivitiesLearningLevelQuery(query)

        }
    }

    private fun startSearchQuery(query: String) {
        if (query.isBlank()) {
            activitiesQueryStatus.value = Resource.error(Exception("query is empty"))
        } else {
            getActivitiesSearchQuery(query)

        }
    }

    private fun getActivitiesSearchQuery(query: String) {
        activitiesQueryStatus.value = Resource.loading("loading")
        viewModelScope.launch {
            val activities = repository.getActivities()
            if (activities == null || activities?.isEmpty()) {
                activitiesQueryStatus.value = Resource.error(Exception("no activities available"))
            } else {
                val list = doTheFilterProcessSearchQuery(query, activities)
                activitiesQueryStatus.value = Resource.success(list)
                Log.d(TAG, "getActivitiesSearchQuery: query:$query list:$list")
            }

        }
    }

    private fun getActivitiesLearningLevelQuery(query: String) {
        activitiesQueryStatus.value = Resource.loading("loading")
        viewModelScope.launch {
            val activities = repository.getActivities()
            if (activities == null || activities?.isEmpty()) {
                activitiesQueryStatus.value = Resource.error(Exception("no activities available"))
            } else {
                val list = doTheFilterProcessLearningLevel(query, activities)
                activitiesQueryStatus.value = Resource.success(list)
                Log.d(TAG, "getActivitiesLearningLevelQuery: query:$query list:$list")
            }

        }
    }

    private fun doTheFilterProcessLearningLevel(query: String, originalList: List<Activity>): List<Activity> {
        Log.d(TAG, "doTheFilterProcessLearningLevel: ")
        val list = mutableListOf<Activity>()
        for (activity in originalList) {
            if (activity.level.contains(query, ignoreCase = true)) {
                list.add(activity)
            }

        }

        return list

    }

    private fun doTheFilterProcessSearchQuery(query: String, originalList: List<Activity>): List<Activity> {
        Log.d(TAG, "doTheFilterProcessSearchQuery: ")
        val list = mutableListOf<Activity>()
        for (activity in originalList) {
            if (activity.name.contains(query, ignoreCase = true)) {
                list.add(activity)
            }

        }
        Log.d(TAG, "doTheFilterProcessSearchQuery:query::$query list::$list")
        return list
    }


    private val activity = MutableStateFlow(Activity())

    private val _channelClickEvents = Channel<ActivitiesFragment2.Event>()
    val channelClickEvents get() = _channelClickEvents.receiveAsFlow()

    fun setEvent(event: ActivitiesFragment2.Event) {
        Log.d(TAG, "setEvent: ")
        when (event) {
            is ActivitiesFragment2.Event.SearchQuery -> {
                startSearchQuery(event.query!!)
            }
            is ActivitiesFragment2.Event.SearchLearningLevel -> {
                startLearningLevelQuery(event.query!!)
            }

            is ActivitiesFragment2.Event.ActivityClicked -> {
                Log.d(TAG, "setEvent: onclick event")
                activity.value = event.data
                activityClicked()

            }

        }
    }

    private fun activityClicked() {
        Log.d(TAG, "activityClicked: ")
        val event = ActivitiesFragment2.Event.ActivityClicked(activity.value)
        viewModelScope.launch {
            _channelClickEvents.send(event)

        }
    }

    sealed class Event {
        data class StartQuery(val query: String) : Event()
    }


}