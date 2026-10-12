package se.adde.sleeplog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import se.adde.sleeplog.core.SleepStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;

/** Check actual idle pixels, not only the text-color property or click callback. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36, qualifiers = "w320dp-h480dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PickerRenderingChecks {
    @Test public void buttonsHaveVisibleFillAndTextBeforeAnyPress() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            for (int id : new int[]{R.id.start_date, R.id.start_time, R.id.end_date, R.id.end_time, R.id.length_date}) {
                MainActivity activity = controller.get();
                activity.findViewById(id == R.id.length_date ? R.id.mode_length : R.id.mode_times).performClick();
                activity.findViewById(id).performClick();
                AlertDialog dialog = (AlertDialog)ShadowDialog.getLatestDialog();
                Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                for (int which : new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE}) {
                    assertIdlePixels(dialog.getButton(which));
                }
                dialog.dismiss();
            }
        }
    }

    @Test public void ordinaryDialogActionsAreVisibleAndDeletionStillNeedsConfirmation() throws Exception {
        checkOrdinaryDialogs(false);
    }
    @Test public void englishDialogActionsAreVisibleWithLargeText() throws Exception {
        checkOrdinaryDialogs(true);
    }
    private void checkOrdinaryDialogs(boolean english) throws Exception {
        RuntimeEnvironment.setFontScale(2f);
        RuntimeEnvironment.getApplication().getSharedPreferences("settings", 0).edit()
            .putString("language", english ? "en" : "sv").commit();
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            MainActivity activity = controller.get();
            activity.findViewById(R.id.mode_length).performClick();
            ((EditText)activity.findViewById(R.id.duration_minutes)).setText("45");
            click(activity, R.string.save);
            click(activity, R.string.delete);
            AlertDialog dialog = latest();
            assertEquals(activity.getString(R.string.delete), dialog.getButton(-1).getText().toString());
            assertEquals(activity.getString(R.string.cancel), dialog.getButton(-2).getText().toString());
            assertIdlePixels(dialog.getButton(-1)); assertIdlePixels(dialog.getButton(-2));
            dialog.getButton(-2).performClick(); idle();
            assertEquals(1, new SleepStore(activity.getFilesDir()).load().size());
            click(activity, R.string.delete);
            dialog = latest(); assertIdlePixels(dialog.getButton(-1));
            dialog.getButton(-1).performClick(); idle();
            assertEquals(0, new SleepStore(activity.getFilesDir()).load().size());

            click(activity, R.string.language);
            dialog = latest(); assertIdlePixels(dialog.getButton(-2));
            dialog.getButton(-2).performClick(); idle();
            click(activity, R.string.document);
            dialog = latest(); assertIdlePixels(dialog.getButton(-2));
            dialog.getButton(-2).performClick(); idle();
            activity.findViewById(R.id.mode_length).performClick();
            ((EditText)activity.findViewById(R.id.duration_minutes)).setText("0");
            click(activity, R.string.save);
            dialog = latest(); assertIdlePixels(dialog.getButton(-1));
            dialog.getButton(-1).performClick(); idle();
            assertEquals(0, new SleepStore(activity.getFilesDir()).load().size());
        }
    }
    private static void idle() { Shadows.shadowOf(android.os.Looper.getMainLooper()).idle(); }
    private static AlertDialog latest() { idle(); return (AlertDialog)ShadowDialog.getLatestDialog(); }
    private static void click(MainActivity activity, int textId) {
        Button button = find(activity.getWindow().getDecorView(), activity.getString(textId));
        assertNotNull(button); button.performClick(); idle();
    }
    private static Button find(View view, String text) {
        if (view instanceof Button && ((Button)view).getText().toString().equals(text)) return (Button)view;
        if (view instanceof ViewGroup) for (int i=0; i<((ViewGroup)view).getChildCount(); i++) {
            Button result = find(((ViewGroup)view).getChildAt(i), text);
            if (result != null) return result;
        }
        return null;
    }
    private static void assertIdlePixels(Button button) {
        assertFalse(button.isPressed()); assertTrue(button.isShown());
        Rect visible = new Rect(); assertTrue(button.getGlobalVisibleRect(visible));
        assertEquals(button.getWidth(), visible.width());
        assertEquals(button.getHeight(), visible.height());
        Bitmap bitmap = Bitmap.createBitmap(button.getWidth(), button.getHeight(), Bitmap.Config.ARGB_8888);
        button.draw(new Canvas(bitmap));
        int green = 0, white = 0;
        for (int y = 12; y < bitmap.getHeight() - 12; y++)
            for (int x = 12; x < bitmap.getWidth() - 12; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel == button.getContext().getColor(R.color.accent)) green++;
                if (pixel == button.getContext().getColor(R.color.on_accent)) white++;
            }
        assertTrue("Missing solid idle background", green > 100);
        assertTrue("Missing readable idle label", white > 4);
        bitmap.recycle();
    }
}
