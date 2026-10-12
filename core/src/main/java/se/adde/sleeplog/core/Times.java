package se.adde.sleeplog.core;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Times {
    private static final Set<String> ZONES = new HashSet<>(Arrays.asList(TimeZone.getAvailableIDs()));
    private Times() {}
    public static TimeZone zone(String id) {
        if (id == null || (!ZONES.contains(id) && !id.matches("GMT[+-](0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]")))
            throw new IllegalArgumentException("zone");
        return TimeZone.getTimeZone(id);
    }
    public static long parse(String text, String zone) {
        if (!text.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}"))
            throw new IllegalArgumentException("date/time");
        int year = Integer.parseInt(text.substring(0,4));
        if (year < 1900 || year > 2100) throw new IllegalArgumentException("year");
        SimpleDateFormat f = formatter("yyyy-MM-dd HH:mm", zone);
        f.setLenient(false);
        ParsePosition p = new ParsePosition(0);
        Date value = f.parse(text, p);
        if (value == null || p.getIndex() != text.length()) throw new IllegalArgumentException("date/time");
        return value.getTime();
    }
    private static SimpleDateFormat formatter(String pattern, String zone) {
        SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.ROOT);
        f.setTimeZone(zone(zone));
        return f;
    }
    public static String format(long time, String pattern, String zone) {
        return formatter(pattern, zone).format(new Date(time));
    }
    public static SortedMap<String, Long> totals(List<SleepEntry> entries) {
        SortedMap<String, Long> result = new TreeMap<>(Collections.reverseOrder());
        for (SleepEntry e : entries) {
            if (e.lengthOnly) { add(result, e.date, e.minutes); continue; }
            long cursor = e.start;
            while (cursor < e.end) {
                String date = format(cursor, "yyyy-MM-dd", e.zone);
                Calendar next = Calendar.getInstance(zone(e.zone), Locale.ROOT);
                next.setTimeInMillis(cursor);
                next.set(Calendar.HOUR_OF_DAY, 0); next.set(Calendar.MINUTE, 0);
                next.set(Calendar.SECOND, 0); next.set(Calendar.MILLISECOND, 0);
                next.add(Calendar.DATE, 1);
                long stop = Math.min(e.end, next.getTimeInMillis());
                if (stop <= cursor) throw new IllegalArgumentException("midnight");
                add(result, date, (stop - cursor) / 60000);
                cursor = stop;
            }
        }
        return result;
    }
    private static void add(Map<String, Long> result, String date, long minutes) {
        Long old = result.get(date);
        result.put(date, (old == null ? 0 : old) + minutes);
    }
}
