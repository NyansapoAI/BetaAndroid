package com.example.edward.nyansapo.presentation.ui.assessment

import android.util.Log
import android.widget.Filter
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception


class AssessmentViewModel @ViewModelInject constructor(private val repo: AssessmentRepo) : ViewModel() {

    private val TAG = "AssessmentViewModel"

    private val _assessmentEvents = Channel<AssessmentFragment2.Event>()
    val assessmentEvents = _assessmentEvents.receiveAsFlow()
    fun setEvent(event: AssessmentFragment2.Event) {
        viewModelScope.launch {
            when (event) {
                is AssessmentFragment2.Event.FetchStudents -> {
                    fetchStudents(event.programId, event.groupId, event.campId)
                }
                is AssessmentFragment2.Event.FetchStudentsQuery -> {
                    fetchStudentQuery(event.query)
                }
                is AssessmentFragment2.Event.GetSuggestionsRecycler -> {
                    getSuggestionsRecycler()
                }
                is AssessmentFragment2.Event.GetSuggestionsSearch -> {
                    getSuggestionsSearch()
                }
                is AssessmentFragment2.Event.StudentClicked -> {
                    onStudentClicked(event.student)
                }
            }
        }
    }

    private suspend fun onStudentClicked(student: Student) {
        _assessmentEvents.send(AssessmentFragment2.Event.StudentClicked(student))
    }

    private val _getSuggestionsRecyclerStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val getSuggestionsRecyclerStatus = _getSuggestionsRecyclerStatus.receiveAsFlow()

    private suspend fun getSuggestionsRecycler() {
        Log.d(TAG, "getSuggestions: allStudent Size:${allStudents.size}")
        if (allStudents.size > 3) {
            val list = allStudents.subList(0, 2)
            _getSuggestionsRecyclerStatus.send(Resource.success(list))
        } else {
            Log.d(TAG, "getSuggestions: list is less than 3")
        }

    }

    private val _getSuggestionsSearchStatus = Channel<Resource<List<Student>>>()
    val getSuggestionsSearchStatus = _getSuggestionsSearchStatus.receiveAsFlow()

    private suspend fun getSuggestionsSearch() {
        Log.d(TAG, "getSuggestions: allStudent Size:${allStudents.size}")
        if (allStudents.size > 3) {
            val list = allStudents.subList(0, 2)
            val data = list.map { it.toObject(Student::class.java)!! }
            _getSuggestionsSearchStatus.send(Resource.success(data))
        } else {
            Log.d(TAG, "getSuggestions: list is less than 3")
        }

    }

    private val _fetchStudentsQuerysStatus = Channel<Resource<List<Student>>>()
    val fetchStudentsQuerysStatus = _fetchStudentsQuerysStatus.receiveAsFlow()
    private suspend fun fetchStudentQuery(query: String) {
        if (query.isBlank()) {
            _fetchStudentsQuerysStatus.send(Resource.error(Exception("Query is Blank!!")))
        } else {
            _fetchAllStudentsStatus.send(Resource.loading("querying students..."))
            getStudentsAccordingToQuery(query) { results ->
                viewModelScope.launch {
                    if (results.isEmpty()) {
                        _fetchStudentsQuerysStatus.send(Resource.empty())
                    } else {
                        val data = results.map {
                            it.toObject(Student::class.java)!!
                        }
                        _fetchStudentsQuerysStatus.send(Resource.success(data))


                    }
                }

            }

        }

    }

    private fun getStudentsAccordingToQuery(newQuery: String, onComplete: (MutableList<DocumentSnapshot>) -> Unit) {
        val filteredList = mutableListOf<DocumentSnapshot>()

        object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {

                val filterResults = FilterResults()
                for (snapshot in allStudents) {
                    val studentFullName = snapshot.toObject(Student::class.java)!!.firstname + " " + snapshot.toObject(Student::class.java)!!.lastname
                    if (studentFullName.contains(newQuery, ignoreCase = true)) {
                        filteredList.add(snapshot)
                    }

                }


                filterResults.values = filteredList


                return filterResults

            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                val result = p1?.values as MutableList<DocumentSnapshot>
                onComplete(result)
            }
        }.filter(newQuery)


    }

    private var allStudents = listOf<DocumentSnapshot>()
    private val _fetchAllStudentsStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val fetchAllStudentsStatus = _fetchAllStudentsStatus.receiveAsFlow()
    private suspend fun fetchStudents(programId: String, groupId: String, campId: String) {
        repo.fetchStudents(programId, groupId, campId).collect {
            if (it.status == Resource.Status.SUCCESS) {
                allStudents = it.data!!
            }
            _fetchAllStudentsStatus.send(it)
        }
    }


}