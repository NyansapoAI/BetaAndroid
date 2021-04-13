package com.example.edward.nyansapo.wrappers

import java.lang.Exception

class Resource<out T>(val data:T?,val status:Status,val exception: Exception?,val message:String?){

    companion object{
        fun<T> success(data:T)=Resource<T>(data,Status.SUCCESS,null,null)
        fun<T> loading(message:String)=Resource<T>(null,Status.LOADING,null,message)
        fun<T> error(exception: Exception?)=Resource<T>(null,Status.ERROR,exception,null)
        fun<T> empty()=Resource<T>(null,Status.EMPTY,null,null)

    }


    enum class Status{
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }
}