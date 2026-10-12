package se.adde.sleeplog.core;

/** A length-only entry deliberately has no invented start or end time. */
public final class SleepEntry {
    public final String id, date, zone;
    public final boolean lengthOnly;
    public final long start, end, minutes;

    private SleepEntry(String id, String date, String zone, boolean lengthOnly,
                       long start, long end, long minutes) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id");
        this.id = id; this.date = date; this.zone = zone; this.lengthOnly = lengthOnly;
        this.start = start; this.end = end; this.minutes = minutes;
    }
    public static SleepEntry timed(String id, long start, long end, String zone) {
        Times.zone(zone);
        long minutes = (end - start) / 60000;
        if (start % 60000 != 0 || end % 60000 != 0 || minutes <= 0 || minutes > 527040)
            throw new IllegalArgumentException("interval");
        Times.parse(Times.format(start, "yyyy-MM-dd HH:mm", zone), zone);
        Times.parse(Times.format(end, "yyyy-MM-dd HH:mm", zone), zone);
        return new SleepEntry(id, Times.format(start, "yyyy-MM-dd", zone), zone, false, start, end, minutes);
    }
    public static SleepEntry length(String id, String date, long minutes) {
        Times.parse(date + " 12:00", "UTC");
        if (minutes <= 0 || minutes > 527040) throw new IllegalArgumentException("minutes");
        return new SleepEntry(id, date, "", true, 0, 0, minutes);
    }
}
