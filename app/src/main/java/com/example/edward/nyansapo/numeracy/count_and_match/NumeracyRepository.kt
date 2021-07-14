package com.example.edward.nyansapo.numeracy.count_and_match

import android.content.SharedPreferences
import com.example.edward.nyansapo.numeracy.AssessmentNumeracy
import com.example.edward.nyansapo.numeracy.DataSet
import com.example.edward.nyansapo.presentation.ui.main.campId
import com.example.edward.nyansapo.presentation.ui.main.groupId
import com.example.edward.nyansapo.presentation.ui.main.programId
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject


class NumeracyRepository @Inject constructor(private val sharedPref: SharedPreferences) {
    fun updateStudentLearningLevel(assessmentNumeracy: AssessmentNumeracy) = flow<Resource<AssessmentNumeracy>> {
        emit(Resource.loading("updating..."))
        val map = mapOf("learningLevelNumeracy" to assessmentNumeracy?.learningLevelNumeracy)
        try {
            FirebaseUtils.getCollectionStudentFromCamp_ReturnCollection(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!).document(assessmentNumeracy.student.id!!).set(map, SetOptions.merge()).await()
            emit(Resource.success(assessmentNumeracy))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    fun addAssessment(assessmentNumeracy: AssessmentNumeracy)= flow<Resource<AssessmentNumeracy>> {
        emit(Resource.loading("adding..."))
        try {
            FirebaseUtils.collectionAssessments(sharedPref.programId!!, sharedPref.groupId!!, sharedPref.campId!!,assessmentNumeracy.student.id!!).add(assessmentNumeracy).await()
            emit(Resource.success(assessmentNumeracy))
        } catch (e: Exception) {
            emit(Resource.error(e))
        }
    }

    fun getAllStudents()= callbackFlow<Resource<List<DocumentSnapshot>>> {

      val listener=  FirebaseUtils.getCollectionStudentFromCamp_ReturnSnapshot2(sharedPref.programId!!,sharedPref.groupId!!,sharedPref.campId!!){querySnapshot,error->
          if(error!=null){
              offer(Resource.error(error))
          }else{
              offer(Resource.success(querySnapshot!!.documents))
          }
      }



        awaitClose {  listener.remove()}
    }

    val countAndMatch = MutableStateFlow<Resource<Array<Int>>>(Resource.success(DataSet.countAndMatchBalls))
    val numberRecognition_2 = MutableStateFlow<Resource<Array<Int>>>(Resource.success(DataSet.numberRecognition_2))
    val getAddition = MutableStateFlow<Resource<Array<Pair<Int, Int>>>>(Resource.success(DataSet.addition))
    val getSubtraction = MutableStateFlow<Resource<Array<Pair<Int, Int>>>>(Resource.success(DataSet.subtraction))
    val getMultiplication = MutableStateFlow<Resource<Array<Pair<Int, Int>>>>(Resource.success(DataSet.multiplication))
    val getDivision = MutableStateFlow<Resource<Array<Pair<Int, Int>>>>(Resource.success(DataSet.division))
    val wordProblem = MutableStateFlow<Resource<Pair<String, String>>>(Resource.success(DataSet.wordProblem))

}
