package com.example.edward.nyansapo.presentation.ui.change_program

data class Group(override val number:String):Organisation{

    constructor():this("")
    override var name:String?=null
}

