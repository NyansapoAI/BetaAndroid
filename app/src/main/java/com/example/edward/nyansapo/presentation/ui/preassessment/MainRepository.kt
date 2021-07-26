package com.example.edward.nyansapo.presentation.ui.preassessment

import android.content.SharedPreferences
import android.util.Log
import com.example.edward.nyansapo.Student
import com.example.edward.nyansapo.data.StaticData
import com.example.edward.nyansapo.presentation.ui.activities.Activity
import com.example.edward.nyansapo.presentation.ui.attendance.StudentAttendance
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.example.edward.nyansapo.util.cleanString
import com.example.edward.nyansapo.util.formatDate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainRepository @Inject constructor(private val sharedPref: SharedPreferences) {

    private val TAG = "MainRepository"
    private lateinit var listener: ListenerRegistration

    fun getCurrentDate() = callbackFlow<Resource<Date>> {
        offer(Resource.loading("Fetching current date..."))
        FirebaseUtils.getCurrentDate { date ->
            Log.d(TAG, "setCurrentDate: date retrieved:${date}")
            if (date == null) {
                val currentDateServer = Calendar.getInstance().time
                offer(Resource.success(currentDateServer))

            } else {
                offer(Resource.success(date))

            }


        }

        awaitClose { }
    }

    suspend fun getActivities() = StaticData.getActivities()

    suspend fun addStudentToGroup(student: Student) {
        FirebaseUtils.addStudentToGroup(sharedPref.programId!!, sharedPref.groupId!!, student)

    }

    suspend fun addStudentToCamp(student: Student) {
        FirebaseUtils.addStudentsToCamp(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, student)
    }

    suspend fun attendanceIsEmpty(date: String) =
            FirebaseUtils.getCollectionStudentFromCamp_attendance_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, date).get().await().isEmpty


    suspend fun getAttendanceContinuously(date: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        resetListener()
        listener = FirebaseUtils.getCollectionStudentFromCamp_attendance_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, date).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                offer(Resource.error(firebaseFirestoreException))
            } else {
                offer(Resource.success(querySnapshot!!.documents))
            }
        }

        awaitClose {
            resetListener()

        }
    }

    suspend fun getAttendanceOnce(date: String) = flow<Resource<List<DocumentSnapshot>>> {
        try {
            val it = FirebaseUtils.getCollectionStudentFromCamp_attendance_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, date).get().await()
            emit(Resource.success(it.documents))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }

    }

    private fun resetListener() {
        try {
            if (this::listener.isInitialized) {
                listener.remove()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    suspend fun getStudentsFromCamp() =
            FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!).get().await().documents

    suspend fun addStudentToAttenance(studentId: String, date: String, studentAttendance: StudentAttendance) =
            FirebaseUtils.addStudentsToAttendance(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, studentId, date, studentAttendance)

    suspend fun fetchAssessments(id: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        resetListener()
        offer(Resource.loading("fetching assessments..."))
        listener = FirebaseUtils.getAssessmentsNumeracyFromStudent_Collection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!, id).orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                offer(Resource.error(firebaseFirestoreException))
            } else if (querySnapshot!!.isEmpty) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(querySnapshot!!.documents))
            }

        }

        awaitClose {
            resetListener()
        }
    }

    fun saveActivity(activity: Activity) = flow<Resource<Activity>> {
        FirebaseUtils.addActivity(sharedPref.programId!!, sharedPref.groupId!!, activity).await()
    }

    fun saveActivity2(activity: Activity) = callbackFlow<Resource<Activity>> {
        offer(Resource.loading(""))
        FirebaseUtils.getActivityCollectionRef(sharedPref.programId!!, sharedPref.groupId!!).add(activity).addOnSuccessListener {
            Log.d(TAG, "saveActivity2: success")
            offer(Resource.success(activity))
        }.addOnFailureListener {
            offer(Resource.error(it))
            Log.d(TAG, "saveActivity2: error:${it.message}")
        }

        awaitClose { }
    }

    suspend fun getRemoteActivities(): List<DocumentSnapshot> {
        return FirebaseUtils.getActivities(sharedPref.programId!!, sharedPref.groupId!!)

    }


}