package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import com.example.edward.nyansapo.Learning_Level
import com.example.edward.nyansapo.numeracy.Numeracy_Learning_Levels
import com.example.edward.nyansapo.presentation.ui.activities.ActivitySections

object Data {
    val list = mutableListOf(LevelSections(Numeracy_Learning_Levels.UNKNOWN.name),
            LevelSections(Numeracy_Learning_Levels.BEGINNER.name),
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

    val list2 = mutableListOf(
            ActivitySections("WHOLE CLASS"),
            ActivitySections(Numeracy_Learning_Levels.BEGINNER.name),
            ActivitySections(Learning_Level.LETTER.name),
            ActivitySections(Learning_Level.WORD.name),
            ActivitySections(Learning_Level.PARAGRAPH.name),
            ActivitySections(Learning_Level.STORY.name),
    )


    @JvmName("getList21")
    fun getList2(): MutableList<ActivitySections> {

        return list2
    }

}