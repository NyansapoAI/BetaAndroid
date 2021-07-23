package com.example.edward.nyansapo.numeracy.numeracy_assessment_result.addition

import com.example.edward.nyansapo.numeracy.Problem

data class Operation(val correct:Int,val correctList:MutableList<Problem>,val wrongList:MutableList<Problem>,val sign:String)
