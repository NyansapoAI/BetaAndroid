package com.example.edward.nyansapo.data.models.ui.attendance

data class StudentAttendance (val name:String,val present:Boolean=true){
    constructor():this("",true)
}