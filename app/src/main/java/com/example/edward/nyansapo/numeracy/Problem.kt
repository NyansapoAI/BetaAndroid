package com.example.edward.nyansapo.numeracy

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class Problem(val first:Int=0,val second:Int=0,val answer:String=""):Parcelable
