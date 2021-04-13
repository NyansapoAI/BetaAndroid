package com.example.edward.nyansapo.data.local.db


import androidx.room.*

@Dao
interface AssessmentDao {


/*
    @Query("SELECT * FROM assessment_recording_table WHERE id LIKE '%' || :searchQuery || '%' ")
    fun getTasksSortedByName(searchQuery: String): Flow<List<Assessment>>
*/


    @Query("SELECT * FROM assessment_recording_table WHERE id = :id LIMIT 1")
    fun getAssessment(id: String): AssessmentRecording

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: AssessmentRecording)

    @Update
    suspend fun update(assessment: AssessmentRecording)

    @Delete
    suspend fun delete(assessment: AssessmentRecording)

    @Query("DELETE FROM assessment_recording_table ")
    suspend fun deleteAssessments()
}