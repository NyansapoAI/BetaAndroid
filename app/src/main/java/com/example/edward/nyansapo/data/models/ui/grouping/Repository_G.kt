package com.example.edward.nyansapo.data.models.ui.grouping

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface Repository_G {
    //val resourceFlow:MutableStateFlow<Resource<List<DocumentSnapshot>>>
    suspend fun getAllStudents(): Flow<Resource<List<DocumentSnapshot>>>
}