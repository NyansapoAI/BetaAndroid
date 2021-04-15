package com.example.edward.nyansapo.presentation.ui.tests

import android.util.Log
import com.example.edward.nyansapo.Assessment_Content

const val TAG = "Tests"

class Tests

private fun doTest() {
    var sentenceList = Assessment_Content.s3!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")

    sentenceList = Assessment_Content.s4!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s5!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s6!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s7!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s8!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s9!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")
    sentenceList = Assessment_Content.s10!!.split(".")
            .filter { line ->
                line.isNotBlank()
            }.map {
                it.trim()
            }.toTypedArray()
    Log.d(TAG, "doTest: size:${sentenceList.size}")


}
