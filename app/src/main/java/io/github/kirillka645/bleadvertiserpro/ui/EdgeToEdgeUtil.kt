package io.github.kirillka645.bleadvertiserpro.ui

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


fun setupEdgeToEdge(view: View, top: Boolean = true, bottom: Boolean = true) {
    val leftPadding = view.paddingLeft
    val topPadding = view.paddingTop
    val rightPadding = view.paddingRight
    val bottomPadding = view.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val i = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            left = leftPadding + i.left,
            top = topPadding + if (top) i.top else 0,
            right = rightPadding + i.right,
            bottom = bottomPadding + if (bottom) i.bottom else 0,
        )
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(view)
}
