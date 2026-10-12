package se.adde.sleeplog;

import android.app.*;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {23, 36}, qualifiers = "w320dp-h480dp-mdpi")
public class PickerChecks {
    @Test public void actionsFitSmallPortraitWithLargeText() { checkActions(2f); }

    @Test @Config(qualifiers = "w480dp-h320dp-land-mdpi")
    public void englishActionsFitShortLandscapeWithLargeText() {
        RuntimeEnvironment.getApplication().getSharedPreferences("settings", 0).edit().putString("language", "en").commit();
        checkActions(2f);
    }

    @Test @Config(qualifiers = "w480dp-h320dp-land-mdpi")
    public void actionsFitShortLandscapeWithLargeText() { checkActions(2f); }

    @Test public void actionsFitSmallPortraitAtDefaultSize() { checkActions(1f); }

    private void checkActions(float scale) {
        RuntimeEnvironment.setFontScale(scale);
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            MainActivity activity = controller.get();
            for (int id : new int[]{R.id.start_date, R.id.start_time, R.id.end_date, R.id.end_time, R.id.length_date}) {
                activity.findViewById(id == R.id.length_date ? R.id.mode_length : R.id.mode_times).performClick();
                activity.findViewById(id).performClick();
                AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
                Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                assertEquals(scale, dialog.getContext().getResources().getConfiguration().fontScale, 0.01f);
                for (int which : new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE}) {
                Button button = dialog.getButton(which);
                Rect rect = new Rect();
                boolean visible = button.getGlobalVisibleRect(rect);
                assertEquals(activity.getColor(R.color.accent), button.getCurrentTextColor());
                assertEquals(activity.getString(which == DialogInterface.BUTTON_POSITIVE ? R.string.select : R.string.cancel), button.getText().toString());
                assertTrue(button.isShown()); assertTrue(visible); assertTrue(button.isEnabled());
                assertEquals(button.getHeight(), rect.height());
                assertEquals(button.getWidth(), rect.width());
                assertTrue(button.getHeight() >= 48);
                assertTrue(rect.bottom <= activity.getResources().getDisplayMetrics().heightPixels);
                assertNotNull(button.getLayout());
                assertTrue(button.getLayout().getHeight() <= button.getHeight() - button.getCompoundPaddingTop() - button.getCompoundPaddingBottom());
                for (int line = 0; line < button.getLineCount(); line++) {
                    assertEquals(0, button.getLayout().getEllipsisCount(line));
                    assertTrue(button.getLayout().getLineWidth(line) <= button.getWidth() - button.getCompoundPaddingLeft() - button.getCompoundPaddingRight());
                }
                }
                dialog.dismiss();
            }
        }
    }
}
