package com.example.edward.nyansapo.presentation.ui.change_program

import android.content.SharedPreferences
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HomeRepository @Inject constructor(private val sharedPref: SharedPreferences) {

    suspend fun getPrograms(): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val subscription = FirebaseUtils.getProgramNamesContinuously2 { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }

        awaitClose { subscription.remove() }

    }

    suspend fun getGroups(programId: String): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val subscription = FirebaseUtils.getGroupNamesContinously2(programId) { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }

        awaitClose { subscription.remove() }

    }

    suspend fun getCamps(programId: String, groupId: String): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val subscription = FirebaseUtils.getCampNamesContinously2(programId, groupId) { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }

        awaitClose { subscription.remove() }

    }

    fun getStudents(campId: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading("loading students..."))
        FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, campId).get().addOnSuccessListener {
            if (it.documents.isEmpty()) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(it.documents))
            }

        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }

    suspend fun fetchAssessmentsOnce(campId: String, id: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {

        offer(Resource.loading("fetching assessments..."))
        FirebaseUtils.getAssessmentsNumeracyFromStudent_Collection(sharedPref.programId!!, sharedPref.groupId!!, campId, id).orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                    if (it.isEmpty) {
                        offer(Resource.empty())
                    } else {
                        offer(Resource.success(it.documents))
                    }
                }.addOnFailureListener {
                    offer(Resource.error(it))
                }

        awaitClose {

        }
    }
    suspend fun fetchAssessmentsOnceSynchonous(campId: String, id: String) = flow<Resource<List<DocumentSnapshot>>> {
       try {
        val assessments=   FirebaseUtils.getAssessmentsNumeracyFromStudent_Collection(sharedPref.programId!!, sharedPref.groupId!!, campId, id).orderBy("timestamp", Query.Direction.DESCENDING)
                   .get().await()

           emit(Resource.success(assessments.documents))
       }catch (e:Exception){
           e.printStackTrace()
       }
    }
}