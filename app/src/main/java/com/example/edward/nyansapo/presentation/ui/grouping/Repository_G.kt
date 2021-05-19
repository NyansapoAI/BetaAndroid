package com.example.edward.nyansapo.presentation.ui.grouping

import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface Repository_G {
     suspend fun getAllStudents(): Flow<Resource<List<DocumentSnapshot>>>
}