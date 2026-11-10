package com.raizartesanal.app.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/** FrameLayout cuadrado: la altura siempre es igual al ancho. */
class SquareFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
