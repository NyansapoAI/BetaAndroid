package com.example.edward.nyansapo.numeracy.numeracy_learning_level

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NumeracyItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = 10
    }
}