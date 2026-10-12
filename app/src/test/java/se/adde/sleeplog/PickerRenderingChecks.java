package se.adde.sleeplog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.Button;
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
                    Button button = dialog.getButton(which);
                    assertFalse(button.isPressed());
                    Bitmap bitmap = Bitmap.createBitmap(button.getWidth(), button.getHeight(), Bitmap.Config.ARGB_8888);
                    button.draw(new Canvas(bitmap));
                    int green = 0, white = 0;
                    for (int y = 12; y < bitmap.getHeight() - 12; y++)
                        for (int x = 12; x < bitmap.getWidth() - 12; x++) {
                            int pixel = bitmap.getPixel(x, y);
                            if (pixel == activity.getColor(R.color.accent)) green++;
                            if (pixel == activity.getColor(R.color.on_accent)) white++;
                        }
                    assertTrue("Missing solid idle background", green > 100);
                    assertTrue("Missing readable idle label", white > 4);
                    bitmap.recycle();
                }
                dialog.dismiss();
            }
        }
    }
}
