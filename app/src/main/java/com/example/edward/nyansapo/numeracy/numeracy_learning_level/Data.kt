package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels

object Data {

    val list = mutableListOf<LevelSections>()
    fun setList() {
        list.add(LevelSections(Numeracy_Learning_Levels.BEGINNER.name))
        list.add(LevelSections(Numeracy_Learning_Levels.ADDITION.name))
        list.add(LevelSections(Numeracy_Learning_Levels.SUBTRACTION.name))
        list.add(LevelSections(Numeracy_Learning_Levels.MULTIPLICATION.name))
        list.add(LevelSections(Numeracy_Learning_Levels.DIVISION.name))
        list.add(LevelSections(Numeracy_Learning_Levels.ABOVE.name))

    }

}