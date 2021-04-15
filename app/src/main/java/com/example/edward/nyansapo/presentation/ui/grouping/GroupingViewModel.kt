package com.example.edward.nyansapo.presentation.ui.grouping

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class GroupingViewModel @ViewModelInject constructor(private val repository: Repository_G) : ViewModel() {

    private val TAG = "GroupingViewModel"


    fun setEvent(event: GroupingFragment3.Event) {
        Log.d(TAG, "setEvent: event set")
        when (event) {
            is GroupingFragment3.Event.StudentLearningLevelQuery -> {
                Log.d(TAG, "setEvent: studentLearningLevelQuery")
                viewModelScope.launch {
                    _studentQueryStatus.value = Resource.loading("loading")
                    val list = queryLearningLevel(event.query)
                    _studentQueryStatus.value = Resource.success(list)
                }

            }
            is GroupingFragment3.Event.StudentQuery -> {
                viewModelScope.launch {
                    _studentQueryStatus.value = Resource.loading("loading")
                    val list = querysearch(event.query)
                    _studentQueryStatus.value = Resource.success(list)
                }
            }
            is GroupingFragment3.Event.SearchActive -> {
                viewModelScope.launch {
                    _searchWidgetStatus.send(GroupingFragment3.Event.SearchActive)

                }
            }
            is GroupingFragment3.Event.SearchNotActive -> {
                viewModelScope.launch {
                    _searchWidgetStatus.send(GroupingFragment3.Event.SearchNotActive)

                }
            }
            is GroupingFragment3.Event.SwipeLeft -> {
                viewModelScope.launch {
                    onSwipLeft(event.selectedTabPosition, event.tabCount)

                }
            }
            is GroupingFragment3.Event.SwipeRight -> {
                viewModelScope.launch {
                    onSwipeRight(event.selectedTabPosition, event.tabCount)

                }
            }
            is GroupingFragment3.Event.StudentClicked -> {
                viewModelScope.launch {
                    _clickedStudentSnapshot.send(event.studentSnapshot)
                }
            }
            is GroupingFragment3.Event.StudentLongClicked -> {
                viewModelScope.launch {
                    _longClickedStudentSnapshot.send(event.studentSnapshot)
                }
            }
            is GroupingFragment3.Event.DeleteStudent -> {
                viewModelScope.launch {
                    _deleteStudentAction.send(event.studentSnapshot)
                }
            }
        }

    }

    private suspend fun onSwipLeft(selectedTabPosition: Int, tabCount: Int) {
        val position = (selectedTabPosition + 1) % tabCount
        _swipeLeftAction.send(SwipePosition(position))

    }

    private suspend fun onSwipeRight(selectedTabPosition: Int, tabCount: Int) {
        var position = (selectedTabPosition - 1) % tabCount
        if (position < 0) {
            position = tabCount - 1
        }
        _swipeLeftAction.send(SwipePosition(position))

    }

    private val _swipeLeftAction = Channel<SwipePosition>()
    val swipeLeftAction get() = _swipeLeftAction.receiveAsFlow()
    private fun queryLearningLevel(learningLevel: String): List<DocumentSnapshot> {
        Log.d(TAG, "queryLearningLevel: query:$learningLevel")
        val originalList = studentsListFlow.value
        val list = mutableListOf<DocumentSnapshot>()

        for (snapshot in originalList!!) {
            val student = snapshot.toObject(Student::class.java)
            Log.d(TAG, "queryLearningLevel: student:$student")
            if (student!!.learningLevel!!.toLowerCase() == learningLevel.toLowerCase()) {
                Log.d(TAG, "queryLearningLevel: added: student:$student")
                list.add(snapshot)
            }
        }

        return list

    }

    private fun querysearch(_query: String): List<DocumentSnapshot> {
        val query = _query.toLowerCase()
        Log.d(TAG, "querysearch: query:$query")
        val originalList = studentsListFlow.value
        val list = mutableListOf<DocumentSnapshot>()

        for (snapshot in originalList!!) {
            val student = snapshot.toObject(Student::class.java)
            if (student?.firstname!!.toLowerCase().contains(query) || student?.lastname!!.toLowerCase().contains(query)) {
                list.add(snapshot)
            }


        }
        return list
    }

    private val _studentsListFlow = MutableStateFlow<List<DocumentSnapshot>>(listOf())
    private val studentsListFlow get() = _studentsListFlow
    private val _studentQueryStatus = MutableStateFlow<Resource<List<DocumentSnapshot>>>(Resource.nothing())
    val studentQueryStatus get() = _studentQueryStatus


    val fetchStudents = flow<Resource<List<DocumentSnapshot>>> {
        emit(Resource.loading("loading"))

        try {
            repository.getAllStudents().collect {
                //set the list
                _studentsListFlow.value = it.data!!

                emit(it)
            }

        } catch (e: Exception) {
            emit(Resource.error(e))
            Log.e("ERROR:", e.message)
        }
    }

    private val _searchWidgetStatus = Channel<GroupingFragment3.Event>()
    val searchWidgetStatus get() = _searchWidgetStatus.receiveAsFlow()

    data class SwipePosition(val position: Int)

    private val _selectedStudentSnapshot = MutableStateFlow(Resource.nothing<DocumentSnapshot>())
    val selectedStudentSnapshot get() = _selectedStudentSnapshot
    private val _clickedStudentSnapshot = Channel<DocumentSnapshot>()
    val clickedStudentSnapshot get() = _clickedStudentSnapshot.receiveAsFlow()
    private val _longClickedStudentSnapshot = Channel<DocumentSnapshot>()
    val longClickedStudentSnapshot get() = _longClickedStudentSnapshot.receiveAsFlow()
    private val _deleteStudentAction = Channel<DocumentSnapshot>()
    val deleteStudentAction get() = _deleteStudentAction.receiveAsFlow()
}



