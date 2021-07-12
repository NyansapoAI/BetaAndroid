package com.example.edward.nyansapo.presentation.ui.change_program

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class ChangeProgramViewModel @ViewModelInject constructor(val repository: HomeRepository) : ViewModel() {


 
         private  val TAG="HomePageViewModel"
   
    //group

    val name="justice"
    val programsStatus = flow {
        Log.d(TAG, "started fetching programs: ")
        emit(Resource.loading("loading"))

        try {
            repository.getPrograms().collect {

                emit(it)
            }

        } catch (e: Exception) {
            emit(Resource.error(e))
            Log.e("ERROR:", e.message)
        }
    }

    //group
    val groupsStatus = MutableStateFlow<Resource<QuerySnapshot>>(Resource.empty())
    suspend fun startFetchingGroups(programId:String) {
        Log.d(TAG, "startFetchingGroups: ")
        groupsStatus.value = Resource.loading("loading")

        try {
            repository.getGroups(programId).collect {

                groupsStatus.value = it
            }

        } catch (e: Exception) {

            Log.e("ERROR:", e.message)
        }

    }

    //camp
    val campStatus = MutableStateFlow<Resource<QuerySnapshot>>(Resource.empty())
    suspend fun startFetchingCamps(programId: String, groupId: String) {
        Log.d(TAG, "startFetchingCamps: ")
        campStatus.value = Resource.loading("loading")

        try {
            repository.getCamps(programId, groupId).collect {

                campStatus.value = it
            }

        } catch (e: Exception) {

            Log.e("ERROR:", e.message)
        }

    }

}