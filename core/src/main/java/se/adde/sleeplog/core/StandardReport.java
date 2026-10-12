package se.adde.sleeplog.core;

import java.util.*;

/** Plain-text standard template, independent of Android and registration UI. */
public final class StandardReport {
    private StandardReport() {}
    public static String duration(long minutes, boolean swedish) {
        return (minutes / 60) + (swedish ? " tim " : " h ") + (minutes % 60) + " min";
    }
    public static String entry(SleepEntry e, boolean sv) {
        if (e.lengthOnly) return e.date + " · " + duration(e.minutes, sv) + "\n" +
                (sv ? "Enbart längd – exakta klockslag saknas" : "Duration only – exact times unknown");
        return Times.format(e.start, "yyyy-MM-dd HH:mm", e.zone) + " →\n" +
                Times.format(e.end, "yyyy-MM-dd HH:mm", e.zone) + "\n" +
                duration(e.minutes, sv) + " · " + e.zone;
    }
    public static List<SleepEntry> sorted(List<SleepEntry> entries) {
        List<SleepEntry> sorted = new ArrayList<>(entries);
        Collections.sort(sorted, (a,b) -> {
            int date = b.date.compareTo(a.date);
            return date != 0 ? date : Long.compare(b.start, a.start);
        });
        return sorted;
    }
    public static String render(List<SleepEntry> entries, boolean sv) {
        StringBuilder b = new StringBuilder(sv ? "SleepLog – sömnregistrering\nStandardmall\n\n" : "SleepLog – sleep log\nStandard template\n\n");
        b.append(sv ? "Registrerad sovtid per kalenderdygn\n" : "Recorded sleep per calendar day\n");
        for (Map.Entry<String,Long> day : Times.totals(entries).entrySet())
            b.append(day.getKey()).append(": ").append(duration(day.getValue(), sv)).append('\n');
        b.append(sv ? "\nRegistreringar\n" : "\nEntries\n");
        for (SleepEntry e : sorted(entries)) b.append(entry(e, sv)).append("\n\n");
        if (entries.isEmpty()) b.append(sv ? "Inga registreringar.\n" : "No entries.\n");
        b.append(sv ? "Pass över midnatt delas mellan dygnen. Längdposter räknas på valt datum.\nÖverlappande pass summeras var för sig.\n" : "Sessions crossing midnight are split by day. Duration-only entries count on the chosen date.\nOverlapping sessions are added separately.\n");
        return b.toString();
    }
}
