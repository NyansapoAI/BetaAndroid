package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import com.example.edward.nyansapo.Student
import com.google.firebase.firestore.DocumentSnapshot

data class LevelSections(val header: String, var students: MutableList<DocumentSnapshot> = mutableListOf())