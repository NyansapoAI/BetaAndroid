package com.example.edward.nyansapo.data.models.ui.grouping

import com.example.edward.nyansapo.data.models.ui.main.MainActivity2
import com.example.edward.nyansapo.data.models.ui.main.campID
import com.example.edward.nyansapo.data.models.ui.main.groupID
import com.example.edward.nyansapo.data.models.ui.main.programID
import com.example.edward.nyansapo.presentation.utils.FirebaseUtils
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class GroupingRepository : Repository_G {


    //1 .- We use callbackFlow , this is like channelFlow and will propagate our data to our viewmodel
    override suspend fun getAllStudents(): Flow<Resource<List<DocumentSnapshot>>> = callbackFlow {

        // 2.- We create a reference to our data inside Firestore
        val eventDocument = FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(MainActivity2.sharedPreferences.programID(), MainActivity2.sharedPreferences.groupID(), MainActivity2.sharedPreferences.campID())

        // 3.- We generate a subscription that is going to let us listen for changes with
        // .addSnapshotListener and then offer those values to the channel that will be collected in our viewmodel
        val subscription = eventDocument.addSnapshotListener { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot.documents))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }

        //Finally if collect is not in use or collecting any data we cancel this channel to prevent any leak and remove the subscription listener to the database
        awaitClose { subscription.remove() }

    }


}