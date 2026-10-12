package se.adde.sleeplog.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import se.adde.sleeplog.R

/** One theme and first-draw treatment for every app-owned dialog action. */
object Dialogs {
    fun builder(context: Context) = AlertDialog.Builder(context, R.style.DialogTheme)

    fun show(dialog: AlertDialog) {
        dialog.show()
        // Set the actual buttons before first draw; inherited stateful tints must not hide labels.
        for (which in listOf(DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE,
            DialogInterface.BUTTON_NEUTRAL)) {
            dialog.getButton(which)?.apply {
                backgroundTintList = null
                setBackgroundResource(R.drawable.dialog_action_background)
                setTextColor(context.getColor(R.color.on_accent))
            }
        }
    }
}
