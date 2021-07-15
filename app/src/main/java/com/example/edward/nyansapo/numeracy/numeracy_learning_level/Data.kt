package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels

object Data {
    val list = mutableListOf(LevelSections(Numeracy_Learning_Levels.BEGINNER.name),
            LevelSections(Numeracy_Learning_Levels.ADDITION.name),
            LevelSections(Numeracy_Learning_Levels.SUBTRACTION.name),
            LevelSections(Numeracy_Learning_Levels.SUBTRACTION.name),
            LevelSections(Numeracy_Learning_Levels.MULTIPLICATION.name),
            LevelSections(Numeracy_Learning_Levels.DIVISION.name),
            LevelSections(Numeracy_Learning_Levels.ABOVE.name))

    @JvmName("getList1")
    fun getList(): MutableList<LevelSections> {

        return list
    }

}