package com.example.edward.nyansapo.numeracy.count_and_match

import com.example.edward.nyansapo.numeracy.DataSet
import com.example.edward.nyansapo.wrappers.Resource
import kotlinx.coroutines.flow.MutableStateFlow


class NumeracyRepository {
    val countAndMatch = MutableStateFlow<Resource<Array<Int>>>(Resource.success(DataSet.countAndMatchBalls))
    val numberRecognition_2 = MutableStateFlow<Resource<Array<Int>>>(Resource.success(DataSet.numberRecognition_2))
    val getAddition = MutableStateFlow<Resource<Array<Pair<Int,Int>>>>(Resource.success(DataSet.addition))
    val wordProblem = MutableStateFlow<Resource<Pair<String,String>>>(Resource.success(DataSet.wordProblem))

}
