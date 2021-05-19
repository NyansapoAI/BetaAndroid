package com.example.edward.nyansapo.presentation.ui.assessment

import com.example.edward.nyansapo.presentation.ui.main.MainActivity2
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BeginAssessmentRepo {
    fun getStudent(id: String) = flow<Resource<DocumentSnapshot>> {
        emit(Resource.loading("getting student..."))
        val programID = MainActivity2.sharedPref.programId!!
        val groupId = MainActivity2.sharedPref.groupId!!
        val campId = MainActivity2.sharedPref.campId!!
        FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(programID, groupId, campId).document(id).get().addOnSuccessListener {
            CoroutineScope(Dispatchers.IO).launch { emit(Resource.success(it)) }
        }.addOnFailureListener {
            CoroutineScope(Dispatchers.IO).launch { emit(Resource.error(it)) }
        }
    }

    fun getAssessments(snapshot: DocumentSnapshot) = flow<Resource<List<DocumentSnapshot>>> {
        emit(Resource.loading("getting assessments..."))
        FirebaseUtils.getAssessmentsFromStudent2(snapshot).get().addOnSuccessListener {
            if (it.isEmpty) {
                CoroutineScope(Dispatchers.IO).launch { emit(Resource.empty()) }

            } else {
                CoroutineScope(Dispatchers.IO).launch { emit(Resource.success(it.documents)) }
            }
        }.addOnFailureListener {
            CoroutineScope(Dispatchers.IO).launch { emit(Resource.error(it)) }
        }

    }
}