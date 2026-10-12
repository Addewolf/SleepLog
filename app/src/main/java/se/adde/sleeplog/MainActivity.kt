package se.adde.sleeplog

import android.app.*
import android.content.*
import android.content.res.Configuration
import android.os.*
import android.text.InputType
import android.view.*
import android.widget.*
import se.adde.sleeplog.core.*
import se.adde.sleeplog.ui.Controls
import java.io.File
import java.util.*

class MainActivity : Activity() {
    private lateinit var ui: Controls
    private lateinit var root: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var store: SleepStore
    private var entries = mutableListOf<SleepEntry>()
    private lateinit var startDate: Button
    private lateinit var startTime: Button
    private lateinit var endDate: Button
    private lateinit var endTime: Button
    private lateinit var lengthDate: Button
    private lateinit var minutes: EditText
    private lateinit var modes: RadioGroup
    private lateinit var timedFields: LinearLayout
    private lateinit var lengthFields: LinearLayout
    private lateinit var title: TextView
    private lateinit var preview: TextView
    private lateinit var zoneNote: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var exportButton: Button
    private lateinit var summary: LinearLayout
    private lateinit var history: LinearLayout
    private var editingId: String? = null
    private var zone = TimeZone.getDefault().id
    private var pendingExport = false
    private var visibleEntries = 30
    private var visibleDays = 30
    @Suppress("DEPRECATION") // Configuration.locale also supports Android 6 (API 23).
    private val swedish get() = resources.configuration.locale.language == "sv"

    override fun attachBaseContext(base: Context) {
        val language = base.getSharedPreferences("settings", MODE_PRIVATE).getString("language", "sv")!!
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(language))
        super.attachBaseContext(base.createConfigurationContext(configuration))
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        ui = Controls(this)
        store = SleepStore(filesDir)
        try { entries = store.load().toMutableList() }
        catch (_: Exception) {
            setContentView(ui.text(getString(R.string.load_error)))
            return // Never replace unreadable history with an empty database.
        }
        pendingExport = state?.getBoolean("export") ?: false
        scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        root = ui.column().apply { setPadding(ui.dp(20), ui.dp(16), ui.dp(20), ui.dp(24)) }
        scroll.addView(root)
        setContentView(scroll)
        applyInsets()
        ui.add(root, ui.text("SleepLog", R.style.TextTitle), 0)
        ui.add(root, ui.text(getString(R.string.tagline), R.style.TextMuted), 8)
        ui.add(root, ui.button(getString(R.string.language)) { chooseLanguage() }, 16)
        buildEditor()
        ui.add(root, ui.text(getString(R.string.daily), R.style.TextSection))
        summary = ui.column(); ui.add(root, summary, 8)
        ui.add(root, ui.text(getString(R.string.day_note), R.style.TextMuted), 16)
        exportButton = ui.button(getString(R.string.document)) {
            AlertDialog.Builder(this).setTitle(R.string.template)
                .setItems(arrayOf(getString(R.string.standard_template))) { _, _ -> chooseDestination() }
                .setNegativeButton(R.string.cancel, null).show()
        }
        exportButton.isEnabled = !pendingExport
        ui.add(root, exportButton, 20)
        ui.add(root, ui.text(getString(R.string.history), R.style.TextSection))
        history = ui.column(); ui.add(root, history)
        resetEditor()
        state?.getBundle("draft")?.let { restoreDraft(it) }
        renderHistory()
    }

    private fun applyInsets() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            scroll.setOnApplyWindowInsetsListener { view, insets ->
                val safe = insets.getInsets(WindowInsets.Type.systemBars() or
                    WindowInsets.Type.ime() or WindowInsets.Type.displayCutout())
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
                insets
            }
            scroll.requestApplyInsets()
        }
    }

    private fun buildEditor() {
        val card = ui.card(root)
        title = ui.text(getString(R.string.new_entry), R.style.TextSection); ui.add(card, title)
        modes = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        modes.addView(RadioButton(this).apply { id = R.id.mode_times; text = getString(R.string.with_times); minHeight = ui.dp(48) })
        modes.addView(RadioButton(this).apply { id = R.id.mode_length; text = getString(R.string.length_only); minHeight = ui.dp(48) })
        ui.add(card, modes)
        timedFields = ui.column(); ui.add(card, timedFields)
        ui.add(timedFields, ui.text(getString(R.string.fell_asleep)))
        val startRow = ui.row(); ui.add(timedFields, startRow)
        startDate = dateButton(startRow, R.string.fell_asleep).apply { id = R.id.start_date }
        startTime = timeButton(startRow, R.string.fell_asleep).apply { id = R.id.start_time }
        ui.add(timedFields, ui.text(getString(R.string.woke_up)))
        val endRow = ui.row(); ui.add(timedFields, endRow)
        endDate = dateButton(endRow, R.string.woke_up).apply { id = R.id.end_date }
        endTime = timeButton(endRow, R.string.woke_up).apply { id = R.id.end_time }
        preview = ui.text(""); ui.add(timedFields, preview)
        zoneNote = ui.text("", R.style.TextMuted); ui.add(timedFields, zoneNote)
        lengthFields = ui.column(); ui.add(card, lengthFields)
        ui.add(lengthFields, ui.text(getString(R.string.chosen_date)))
        lengthDate = dateButton(lengthFields, R.string.chosen_date).apply { id = R.id.length_date }
        val label = ui.text(getString(R.string.minutes)); ui.add(lengthFields, label)
        minutes = EditText(this).apply {
            id = R.id.duration_minutes; inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true); minHeight = ui.dp(52)
            hint = getString(R.string.minutes_hint); setTextAppearance(R.style.TextBody)
            filters = arrayOf(android.text.InputFilter.LengthFilter(6))
        }
        label.labelFor = minutes.id
        ui.add(lengthFields, minutes)
        ui.add(lengthFields, ui.text(getString(R.string.unknown_times), R.style.TextMuted))
        modes.setOnCheckedChangeListener { _, id ->
            timedFields.visibility = if (id == R.id.mode_times) View.VISIBLE else View.GONE
            lengthFields.visibility = if (id == R.id.mode_length) View.VISIBLE else View.GONE
        }
        saveButton = ui.button(getString(R.string.save)) { saveEntry() }; ui.add(card, saveButton)
        cancelButton = ui.button(getString(R.string.cancel)) { resetEditor() }; ui.add(card, cancelButton)
    }

    private fun dateButton(parent: LinearLayout, label: Int): Button {
        lateinit var button: Button
        button = ui.button("") {
            val parts = button.text.toString().split("-").map { it.toInt() }
            DatePickerDialog(this, { _, year, month, day ->
                button.text = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)
                button.contentDescription = getString(label) + ", " + getString(R.string.date_label) + ": " + button.text
                updatePreview()
            }, parts[0], parts[1] - 1, parts[2]).apply {
                datePicker.minDate = Times.parse("1900-01-01 12:00", zone)
                datePicker.maxDate = Times.parse("2100-12-31 12:00", zone)
            }.show()
        }
        button.tag = label
        ui.add(parent, button)
        return button
    }

    private fun timeButton(parent: LinearLayout, label: Int): Button {
        lateinit var button: Button
        button = ui.button("") {
            val parts = button.text.toString().split(":").map { it.toInt() }
            TimePickerDialog(this, { _, hour, minute ->
                button.text = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
                button.contentDescription = getString(label) + ", " + getString(R.string.time_label) + ": " + button.text
                updatePreview()
            }, parts[0], parts[1], true).show()
        }
        button.tag = label
        ui.add(parent, button)
        return button
    }

    private fun updatePreview() {
        if (!::preview.isInitialized || startDate.text.isEmpty() || endDate.text.isEmpty()) return
        preview.text = try {
            val e = timedEntry("preview")
            getString(R.string.preview, StandardReport.duration(e.minutes, swedish))
        } catch (_: IllegalArgumentException) { getString(R.string.preview_invalid) }
    }

    private fun timedEntry(id: String) = SleepEntry.timed(id,
        Times.parse("${startDate.text} ${startTime.text}", zone),
        Times.parse("${endDate.text} ${endTime.text}", zone), zone)

    private fun resetEditor() {
        editingId = null; zone = TimeZone.getDefault().id
        val now = System.currentTimeMillis() / 60000 * 60000
        val start = now - 8 * 60 * 60000
        startDate.text = Times.format(start, "yyyy-MM-dd", zone)
        startTime.text = Times.format(start, "HH:mm", zone)
        endDate.text = Times.format(now, "yyyy-MM-dd", zone)
        endTime.text = Times.format(now, "HH:mm", zone)
        lengthDate.text = Times.format(now, "yyyy-MM-dd", zone)
        minutes.setText(""); modes.check(R.id.mode_times)
        updateEditorLabels()
    }

    private fun updateEditorLabels() {
        title.setText(if (editingId == null) R.string.new_entry else R.string.edit_entry)
        saveButton.setText(if (editingId == null) R.string.save else R.string.save_changes)
        cancelButton.visibility = if (editingId == null) View.GONE else View.VISIBLE
        zoneNote.text = getString(R.string.zone_note, zone)
        for (b in listOf(startDate, startTime, endDate, endTime, lengthDate)) {
            val kind = if (b === startTime || b === endTime) R.string.time_label else R.string.date_label
            b.contentDescription = getString(b.tag as Int) + ", " + getString(kind) + ": " + b.text
        }
        updatePreview()
    }

    private fun saveEntry() {
        val id = editingId ?: UUID.randomUUID().toString()
        val entry = try {
            if (modes.checkedRadioButtonId == R.id.mode_length)
                SleepEntry.length(id, lengthDate.text.toString(), minutes.text.toString().toLongOrNull() ?: 0)
            else timedEntry(id)
        } catch (_: IllegalArgumentException) {
            AlertDialog.Builder(this).setMessage(R.string.validation).setPositiveButton(android.R.string.ok, null).show()
            return
        }
        val updated = entries.toMutableList()
        val index = updated.indexOfFirst { it.id == id }
        if (index < 0) updated.add(entry) else updated[index] = entry
        if (persist(updated)) { resetEditor(); renderHistory(); toast(R.string.saved) }
    }

    private fun persist(updated: MutableList<SleepEntry>): Boolean = try {
        store.save(updated); entries = updated; true
    } catch (_: Exception) { toast(R.string.storage_error); false }

    private fun edit(entry: SleepEntry) {
        resetEditor(); editingId = entry.id
        if (entry.lengthOnly) {
            lengthDate.text = entry.date
            minutes.setText(String.format(Locale.ROOT, "%d", entry.minutes)); modes.check(R.id.mode_length)
        } else {
            zone = entry.zone
            startDate.text = Times.format(entry.start, "yyyy-MM-dd", zone)
            startTime.text = Times.format(entry.start, "HH:mm", zone)
            endDate.text = Times.format(entry.end, "yyyy-MM-dd", zone)
            endTime.text = Times.format(entry.end, "HH:mm", zone)
        }
        updateEditorLabels(); scroll.smoothScrollTo(0, 0)
    }

    private fun renderHistory() {
        summary.removeAllViews(); history.removeAllViews()
        if (entries.isEmpty()) {
            ui.add(summary, ui.text(getString(R.string.empty), R.style.TextMuted))
            return
        }
        val days = Times.totals(entries).entries.toList()
        days.take(visibleDays).forEach { (date, total) ->
            ui.add(summary, ui.text("$date   ·   ${StandardReport.duration(total, swedish)}"))
        }
        if (days.size > visibleDays) ui.add(summary, ui.button(getString(R.string.show_more)) {
            visibleDays += 30; renderHistory()
        })
        StandardReport.sorted(entries).take(visibleEntries).forEach { e ->
            val card = ui.card(history)
            ui.add(card, ui.text(StandardReport.entry(e, swedish)))
            ui.add(card, ui.button(getString(R.string.edit)) { edit(e) })
            ui.add(card, ui.button(getString(R.string.delete)) {
                AlertDialog.Builder(this).setTitle(R.string.delete_title).setMessage(R.string.delete_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        if (persist(entries.filter { it.id != e.id }.toMutableList())) {
                            if (editingId == e.id) resetEditor()
                            renderHistory(); toast(R.string.deleted)
                        }
                    }.show()
            })
        }
        if (entries.size > visibleEntries) ui.add(history, ui.button(getString(R.string.show_more)) {
            visibleEntries += 30; renderHistory()
        })
    }

    private fun chooseLanguage() {
        AlertDialog.Builder(this).setTitle(R.string.language)
            .setSingleChoiceItems(arrayOf("Svenska", "English"), if (swedish) 0 else 1) { dialog, choice ->
                val language = if (choice == 0) "sv" else "en"
                val stored = getSharedPreferences("settings", MODE_PRIVATE).edit().putString("language", language).commit()
                dialog.dismiss()
                if (stored) recreate() else toast(R.string.storage_error)
            }.setNegativeButton(R.string.cancel, null).show()
    }

    @Suppress("DEPRECATION")
    private fun chooseDestination() {
        try {
            File(cacheDir, "pending-report.txt").writeText(StandardReport.render(entries, swedish), Charsets.UTF_8)
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "SleepLog-${Times.format(System.currentTimeMillis(), "yyyy-MM-dd_HHmm", TimeZone.getDefault().id)}.txt")
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
            pendingExport = true; exportButton.isEnabled = false
            startActivityForResult(intent, 1)
        } catch (_: ActivityNotFoundException) {
            pendingExport = false; exportButton.isEnabled = true; toast(R.string.picker_missing)
        } catch (_: Exception) {
            pendingExport = false; exportButton.isEnabled = true; toast(R.string.document_error)
        }
    }

    @Deprecated("Platform callback avoids an extra UI library")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1) return
        if (!::exportButton.isInitialized) return
        pendingExport = false
        val snapshot = File(cacheDir, "pending-report.txt")
        if (resultCode != RESULT_OK) { snapshot.delete(); exportButton.isEnabled = true; return }
        val text = try { snapshot.readText(Charsets.UTF_8) } catch (_: Exception) { null }
        snapshot.delete()
        val uri = data?.data
        if (uri == null || text == null) { exportButton.isEnabled = true; toast(R.string.document_error); return }
        val context = applicationContext
        Thread {
            val message = try {
                val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("No stream")
                stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
                R.string.document_saved
            } catch (_: Exception) { R.string.document_error }
            runOnUiThread { if (!isDestroyed) { exportButton.isEnabled = true; toast(message) } }
        }.start()
    }

    private fun draft() = Bundle().apply {
        putString("id", editingId); putString("zone", zone)
        putString("sd", startDate.text.toString()); putString("st", startTime.text.toString())
        putString("ed", endDate.text.toString()); putString("et", endTime.text.toString())
        putString("ld", lengthDate.text.toString()); putString("minutes", minutes.text.toString())
        putInt("mode", modes.checkedRadioButtonId)
    }
    private fun restoreDraft(b: Bundle) {
        editingId = b.getString("id"); zone = b.getString("zone") ?: zone
        startDate.text = b.getString("sd"); startTime.text = b.getString("st")
        endDate.text = b.getString("ed"); endTime.text = b.getString("et")
        lengthDate.text = b.getString("ld"); minutes.setText(b.getString("minutes"))
        modes.check(b.getInt("mode", R.id.mode_times)); updateEditorLabels()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::minutes.isInitialized) outState.putBundle("draft", draft())
        outState.putBoolean("export", pendingExport)
    }
    private fun toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
