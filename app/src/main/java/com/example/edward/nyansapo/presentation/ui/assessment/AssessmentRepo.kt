package com.example.edward.nyansapo.presentation.ui.assessment

import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class AssessmentRepo {
    fun fetchStudents(programId: String, groupId: String, campId: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {

        offer(Resource.loading("fetching students..."))
        val listener = FirebaseUtils.getCollectionStudentFromCamp_ReturnSnapshot2(programId, groupId, campId) { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
            if (firebaseFirestoreException != null) {
                offer(Resource.error(firebaseFirestoreException))
            } else if (querySnapshot!!.isEmpty) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(querySnapshot!!.documents))
            }

        }
        awaitClose {
            listener.remove()
        }
    }
}