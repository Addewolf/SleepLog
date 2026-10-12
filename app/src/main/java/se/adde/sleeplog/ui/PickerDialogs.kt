package se.adde.sleeplog.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.DatePicker
import android.widget.TimePicker
import se.adde.sleeplog.R

/** Let the picker scroll inside the dialog so its actions remain reachable on short screens. */
object PickerDialogs {
    fun show(dialog: AlertDialog) {
        dialog.show()
        // Apply to the actual platform buttons before their first draw. Some dialog themes
        // supply stateful tints; the idle appearance must not depend on focus or a press.
        for (which in listOf(DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE)) {
            dialog.getButton(which).apply {
                backgroundTintList = null
                setBackgroundResource(R.drawable.picker_action_background)
                setTextColor(context.getColor(R.color.on_accent))
            }
        }
        val picker = findPicker(dialog.window!!.decorView) ?: return
        val parent = picker.parent as ViewGroup
        val position = parent.indexOfChild(picker)
        val params = picker.layoutParams
        parent.removeView(picker)
        val content = object : ScrollView(dialog.context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val density = resources.displayMetrics.density
                val available = resources.configuration.screenHeightDp * density
                val action = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                // Reserve two action rows (landscape side panel), title and window margins.
                // ScrollView alone is insufficient: platform picker layouts may demand their full height.
                val reserve = 96 * density + 2 * maxOf(48 * density, action.textSize * 2)
                val limit = maxOf(1, (available - reserve).toInt())
                val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) limit
                    else minOf(limit, MeasureSpec.getSize(heightMeasureSpec))
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))
            }
        }.apply {
            addView(picker, ViewGroup.LayoutParams(-1, -2))
        }
        parent.addView(content, position, params)
    }

    private fun findPicker(view: View): View? {
        if (view is DatePicker || view is TimePicker) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) {
            findPicker(view.getChildAt(i))?.let { return it }
        }
        return null
    }
}
