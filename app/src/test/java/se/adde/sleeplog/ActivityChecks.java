package se.adde.sleeplog;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import se.adde.sleeplog.core.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;

/** JVM Android simulation; these checks do not replace physical-phone testing. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {23, 36})
public class ActivityChecks {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private TimeZone previousZone;
    @Before public void setup() {
        previousZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"));
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
    }
    @After public void cleanup() {
        controller.pause().stop().destroy();
        TimeZone.setDefault(previousZone);
    }
    private Button button(String text) {
        Button found = find(activity.getWindow().getDecorView(), text);
        assertNotNull("Missing button: " + text, found); return found;
    }
    private Button find(View view, String text) {
        if (view instanceof Button && ((Button)view).getText().toString().equals(text)) return (Button)view;
        if (view instanceof ViewGroup) for (int i=0; i<((ViewGroup)view).getChildCount(); i++) {
            Button found = find(((ViewGroup)view).getChildAt(i), text);
            if (found != null) return found;
        }
        return null;
    }
    private List<SleepEntry> saved() throws Exception { return new SleepStore(activity.getFilesDir()).load(); }
    private void length(String value) {
        activity.findViewById(R.id.mode_length).performClick();
        ((EditText) activity.findViewById(R.id.duration_minutes)).setText(value);
    }
    private void date(int id, int year, int month, int day) {
        activity.findViewById(id).performClick();
        DatePickerDialog picker = (DatePickerDialog)ShadowDialog.getLatestDialog();
        picker.getDatePicker().updateDate(year, month - 1, day);
        picker.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    private void time(int id, int hour, int minute) {
        activity.findViewById(id).performClick();
        TimePickerDialog picker = (TimePickerDialog)ShadowDialog.getLatestDialog();
        TimePicker view = picker.findViewById(android.content.res.Resources.getSystem().getIdentifier("timePicker", "id", "android"));
        assertTrue(view.is24HourView());
        view.setHour(hour); view.setMinute(minute);
        picker.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    @Test public void timedRegistrationSplitsAtMidnightAndAllowsMultipleSessions() throws Exception {
        date(R.id.start_date, 2026, 10, 11); time(R.id.start_time, 23, 0);
        date(R.id.end_date, 2026, 10, 12); time(R.id.end_time, 7, 30);
        button("Spara registrering").performClick();
        assertEquals(510L, saved().get(0).minutes);
        length("45"); date(R.id.length_date, 2026, 10, 12);
        button("Spara registrering").performClick();
        assertEquals(2, saved().size());
        assertEquals(Long.valueOf(495), Times.totals(saved()).get("2026-10-12"));
        assertEquals(Long.valueOf(60), Times.totals(saved()).get("2026-10-11"));
    }
    @Test public void durationCanBeEditedAndDeletionNeedsConfirmation() throws Exception {
        length("45"); button("Spara registrering").performClick();
        assertTrue(saved().get(0).lengthOnly); assertEquals(0, saved().get(0).start);
        String id = saved().get(0).id;
        button("Ändra").performClick();
        ((EditText)activity.findViewById(R.id.duration_minutes)).setText("90");
        button("Spara ändringar").performClick();
        assertEquals(1, saved().size()); assertEquals(id, saved().get(0).id);
        assertEquals(90L, saved().get(0).minutes);
        button("Radera").performClick();
        assertEquals(1, saved().size());
        ShadowAlertDialog.getLatestAlertDialog().getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertEquals(1, saved().size());
        button("Radera").performClick();
        ShadowAlertDialog.getLatestAlertDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertEquals(0, saved().size());
    }
    @Test public void draftAndSavedEntriesSurviveActivityRecreation() throws Exception {
        length("45"); button("Spara registrering").performClick();
        length("128"); date(R.id.length_date, 2026, 10, 9);
        controller.recreate(); activity = controller.get();
        assertEquals(1, saved().size());
        assertEquals("128", ((EditText)activity.findViewById(R.id.duration_minutes)).getText().toString());
        assertTrue(((RadioButton)activity.findViewById(R.id.mode_length)).isChecked());
        assertEquals("2026-10-09", ((Button)activity.findViewById(R.id.length_date)).getText().toString());
    }
    @Test public void languageSelectionPersistsWithoutChangingHistory() throws Exception {
        length("45"); button("Spara registrering").performClick();
        length("70");
        button("Språk / Language").performClick();
        ListView choices = ShadowAlertDialog.getLatestAlertDialog().getListView();
        choices.performItemClick(choices.getChildAt(1), 1, 1);
        // ActivityController applies the recreation requested by the language picker.
        controller.recreate(); activity = controller.get();
        assertNotNull(button("Save entry"));
        assertEquals("70", ((EditText)activity.findViewById(R.id.duration_minutes)).getText().toString());
        assertEquals(1, saved().size());
        assertEquals("en", activity.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("language", ""));
    }
    @Test public void documentSnapshotMatchesHistoryAndPickerCancelKeepsData() throws Exception {
        length("45"); button("Spara registrering").performClick();
        button("Skapa dokument").performClick();
        ListView templates = ShadowAlertDialog.getLatestAlertDialog().getListView();
        templates.performItemClick(templates.getChildAt(0), 0, 0);
        Intent intent = Shadows.shadowOf(activity).getNextStartedActivityForResult().intent;
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.getAction());
        assertEquals("text/plain", intent.getType());
        assertEquals(StandardReport.render(saved(), true),
            Files.readString(new File(activity.getCacheDir(), "pending-report.txt").toPath()));
        controller.recreate(); activity = controller.get();
        assertFalse(button("Skapa dokument").isEnabled());
        activity.onActivityResult(1, Activity.RESULT_CANCELED, null);
        assertTrue(button("Skapa dokument").isEnabled());
        assertEquals(1, saved().size());
    }
    @Test public void invalidDurationDoesNotOverwriteHistory() throws Exception {
        length("45"); button("Spara registrering").performClick();
        button("Ändra").performClick();
        ((EditText)activity.findViewById(R.id.duration_minutes)).setText("0");
        button("Spara ändringar").performClick();
        assertTrue(ShadowAlertDialog.getLatestAlertDialog().isShowing());
        assertEquals(45L, saved().get(0).minutes);
    }

    private void action(AlertDialog dialog, int which) {
        assertEquals(which == DialogInterface.BUTTON_POSITIVE ? "Välj" : "Avbryt",
            dialog.getButton(which).getText().toString());
        dialog.getButton(which).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertFalse(dialog.isShowing());
    }
    private TimePicker timeView(TimePickerDialog dialog) {
        return dialog.findViewById(android.content.res.Resources.getSystem().getIdentifier("timePicker", "id", "android"));
    }
    @Test public void everyDateCommitsCancelsAndReopensCurrentValue() {
        for (int id : new int[]{R.id.start_date, R.id.end_date, R.id.length_date}) {
            activity.findViewById(id == R.id.length_date ? R.id.mode_length : R.id.mode_times).performClick();
            Button field = activity.findViewById(id);
            String before = field.getText().toString();
            field.performClick();
            DatePickerDialog dialog = (DatePickerDialog)ShadowDialog.getLatestDialog();
            dialog.getDatePicker().updateDate(2026, 10, 5);
            action(dialog, DialogInterface.BUTTON_NEGATIVE);
            assertEquals(before, field.getText().toString());
            field.performClick();
            dialog = (DatePickerDialog)ShadowDialog.getLatestDialog();
            DatePicker picker = dialog.getDatePicker();
            assertEquals(before, String.format(Locale.ROOT, "%04d-%02d-%02d", picker.getYear(), picker.getMonth()+1, picker.getDayOfMonth()));
            picker.updateDate(2026, 10, 5);
            action(dialog, DialogInterface.BUTTON_POSITIVE);
            assertEquals("2026-11-05", field.getText().toString());
            field.performClick();
            dialog = (DatePickerDialog)ShadowDialog.getLatestDialog();
            assertEquals(2026, dialog.getDatePicker().getYear());
            assertEquals(10, dialog.getDatePicker().getMonth());
            assertEquals(5, dialog.getDatePicker().getDayOfMonth());
            dialog.getDatePicker().updateDate(2027, 0, 1);
            dialog.cancel();
            assertEquals("2026-11-05", field.getText().toString());
        }
    }
    @Test public void bothTimesCommitCancelAndReopenIn24HourMode() {
        for (int id : new int[]{R.id.start_time, R.id.end_time}) {
            Button field = activity.findViewById(id);
            String before = field.getText().toString();
            field.performClick();
            TimePickerDialog dialog = (TimePickerDialog)ShadowDialog.getLatestDialog();
            TimePicker picker = timeView(dialog);
            assertTrue(picker.is24HourView());
            picker.setHour(22); picker.setMinute(47);
            action(dialog, DialogInterface.BUTTON_NEGATIVE);
            assertEquals(before, field.getText().toString());
            field.performClick();
            dialog = (TimePickerDialog)ShadowDialog.getLatestDialog();
            picker = timeView(dialog);
            assertEquals(before, String.format(Locale.ROOT, "%02d:%02d", picker.getHour(), picker.getMinute()));
            picker.setHour(22); picker.setMinute(47);
            action(dialog, DialogInterface.BUTTON_POSITIVE);
            assertEquals("22:47", field.getText().toString());
            field.performClick();
            dialog = (TimePickerDialog)ShadowDialog.getLatestDialog();
            picker = timeView(dialog);
            assertEquals(22, picker.getHour()); assertEquals(47, picker.getMinute());
            picker.setHour(1); dialog.cancel();
            assertEquals("22:47", field.getText().toString());
        }
    }
    @Test public void editedTimesDrivePreviewPersistenceAndReport() throws Exception {
        date(R.id.start_date, 2026, 10, 10); time(R.id.start_time, 22, 0);
        date(R.id.end_date, 2026, 10, 11); time(R.id.end_time, 6, 0);
        button("Spara registrering").performClick();
        String id = saved().get(0).id;
        button("Ändra").performClick();
        assertEquals("22:00", ((Button)activity.findViewById(R.id.start_time)).getText().toString());
        date(R.id.start_date, 2026, 11, 1); time(R.id.start_time, 23, 15);
        date(R.id.end_date, 2026, 11, 2); time(R.id.end_time, 7, 45);
        assertNotNull(findText(activity.getWindow().getDecorView(), "Sovtid: 8 tim 30 min"));
        button("Spara ändringar").performClick();
        controller.recreate(); activity = controller.get();
        assertEquals(1, saved().size()); assertEquals(id, saved().get(0).id);
        SleepEntry entry = saved().get(0);
        assertEquals(Times.parse("2026-11-01 23:15", "Europe/Stockholm"), entry.start);
        assertEquals(Times.parse("2026-11-02 07:45", "Europe/Stockholm"), entry.end);
        assertEquals(510L, entry.minutes);
        assertEquals(Long.valueOf(45), Times.totals(saved()).get("2026-11-01"));
        assertEquals(Long.valueOf(465), Times.totals(saved()).get("2026-11-02"));
        assertTrue(StandardReport.render(saved(), true).contains("23:15"));
        assertTrue(StandardReport.render(saved(), true).contains("07:45"));
    }
    private TextView findText(View view, String text) {
        if (view instanceof TextView && ((TextView)view).getText().toString().equals(text)) return (TextView)view;
        if (view instanceof ViewGroup) for (int i=0; i<((ViewGroup)view).getChildCount(); i++) {
            TextView found = findText(((ViewGroup)view).getChildAt(i), text);
            if (found != null) return found;
        }
        return null;
    }
    @Test public void editingLengthDatePreservesUnknownTimesAndIdentity() throws Exception {
        length("45"); date(R.id.length_date, 2026, 10, 10);
        button("Spara registrering").performClick();
        String id = saved().get(0).id;
        button("Ändra").performClick();
        date(R.id.length_date, 2026, 11, 2);
        button("Spara ändringar").performClick();
        assertEquals(1, saved().size()); assertEquals(id, saved().get(0).id);
        assertEquals("2026-11-02", saved().get(0).date);
        assertTrue(saved().get(0).lengthOnly); assertEquals(45, saved().get(0).minutes);
    }
}
