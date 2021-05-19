package com.example.edward.nyansapo.presentation.ui.home

import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HomeRepository {

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

}