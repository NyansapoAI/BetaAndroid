package com.example.edward.nyansapo.numeracy

object DataSet {

    val countAndMatchBalls = arrayOf(8, 5, 3, 9, 2, 4, 6, 1) //4 must be correct out of 8
    val numberRecognition_2 = arrayOf(52, 47, 66, 28, 93) //4 must be correct our of 5
    val addition = arrayOf(Pair(14, 21), Pair(34, 54), Pair(27, 62)) //2 must be correct out of 3
    val sentence = "Edward has 31 Roti's,Tanish takes 13 Roti's from Edward.How many Roti's does Edward have left?"
    val answer = 18.toString()
    val wordProblem = Pair<String, String>(sentence, answer)
}