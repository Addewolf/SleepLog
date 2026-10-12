package se.adde.sleeplog.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.*
import se.adde.sleeplog.R

/** Shared spacing, buttons and surfaces; type and colors live in resource files. */
class Controls(private val context: Context) {
    fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
    fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    fun text(value: String, style: Int = R.style.TextBody) = TextView(context).apply {
        text = value
        setTextAppearance(style)
        setPadding(0, dp(5), 0, dp(5))
    }
    fun button(value: String, action: () -> Unit) = Button(context).apply {
        text = value
        setOnClickListener { action() }
    }
    fun add(parent: LinearLayout, view: View, gap: Int = 4) {
        val params = if (parent.orientation == LinearLayout.HORIZONTAL)
            LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(4) }
        else LinearLayout.LayoutParams(-1, -2)
        parent.addView(view, params.apply { bottomMargin = dp(gap) })
    }
    fun card(parent: LinearLayout): LinearLayout = column().apply {
        setPadding(dp(16), dp(12), dp(16), dp(12))
        background = GradientDrawable().apply {
            setColor(context.getColor(R.color.surface))
            cornerRadius = dp(16).toFloat()
            setStroke(dp(1), context.getColor(R.color.line))
        }
        add(parent, this, 16)
    }
}
