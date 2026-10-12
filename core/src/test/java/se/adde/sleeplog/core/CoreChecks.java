package se.adde.sleeplog.core;

import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class CoreChecks {
    private static int checks;
    private static void eq(Object expected, Object actual) {
        checks++;
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
    }
    private static void invalid(Runnable action) {
        checks++;
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection");
    }
    private static SleepEntry timed(String id, String start, String end) {
        return SleepEntry.timed(id, Times.parse(start, "Europe/Stockholm"),
                Times.parse(end, "Europe/Stockholm"), "Europe/Stockholm");
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            List<SleepEntry> reopened = new SleepStore(new File(args[0])).load();
            eq(1, reopened.size()); eq(90L, reopened.get(0).minutes);
            eq(true, reopened.get(0).lengthOnly);
            System.out.println("Separate process storage reload passed"); return;
        }
        SleepEntry night = timed("night", "2026-10-11 23:00", "2026-10-12 07:30");
        eq(510L, night.minutes);
        SleepEntry nap = timed("nap", "2026-10-12 14:00", "2026-10-12 14:30");
        SleepEntry length = SleepEntry.length("length", "2026-10-12", 45);
        List<SleepEntry> entries = new ArrayList<>(Arrays.asList(night, nap, length));
        eq(60L, Times.totals(entries).get("2026-10-11"));
        eq(525L, Times.totals(entries).get("2026-10-12"));
        eq(0L, length.start); eq(0L, length.end); eq(true, length.lengthOnly);
        SleepEntry midnight = timed("mid", "2026-12-31 23:30", "2027-01-01 00:00");
        eq(1, Times.totals(Arrays.asList(midnight)).size());
        eq(30L, Times.totals(Arrays.asList(midnight)).get("2026-12-31"));
        SleepEntry multi = timed("multi", "2026-10-01 23:00", "2026-10-03 01:00");
        eq(1440L, Times.totals(Arrays.asList(multi)).get("2026-10-02"));
        eq(1560L, multi.minutes);
        eq(120L, timed("spring", "2026-03-29 01:00", "2026-03-29 04:00").minutes);
        eq(240L, timed("autumn", "2026-10-25 01:00", "2026-10-25 04:00").minutes);
        eq("+0100", Times.format(Times.parse("2026-10-25 02:30", "Europe/Stockholm"), "Z", "Europe/Stockholm"));
        eq("2026-10-12 07:00", Times.format(Times.parse("2026-10-12 07:00", "GMT+02:00"), "yyyy-MM-dd HH:mm", "GMT+02:00"));
        invalid(() -> Times.parse("2026-03-29 02:30", "Europe/Stockholm"));
        invalid(() -> Times.parse("2026-02-30 12:00", "UTC"));
        invalid(() -> Times.parse("2026-10-01 24:00", "UTC"));
        invalid(() -> SleepEntry.length("x", "2026-10-12", 0));
        invalid(() -> SleepEntry.length("x", "2026-10-12", -1));
        invalid(() -> timed("x", "2026-10-12 07:00", "2026-10-11 23:00"));
        invalid(() -> timed("x", "2026-10-12 07:00", "2026-10-12 07:00"));
        String report = StandardReport.render(entries, true);
        eq(true, report.contains("2026-10-12: 8 tim 45 min"));
        eq(true, report.contains("2026-10-11: 1 tim 0 min"));
        eq(true, report.contains("Enbart längd – exakta klockslag saknas"));
        eq(true, StandardReport.render(entries, false).contains("2026-10-12: 8 h 45 min"));
        Path directory = Files.createTempDirectory("sleeplog-check-");
        try {
            SleepStore store = new SleepStore(directory.toFile());
            eq(0, store.load().size()); store.save(entries);
            List<SleepEntry> loaded = new SleepStore(directory.toFile()).load();
            eq(report, StandardReport.render(loaded, true));
            entries.set(2, SleepEntry.length("length", "2026-10-12", 90));
            store.save(entries);
            eq(570L, Times.totals(store.load()).get("2026-10-12"));
            entries.remove(0); entries.remove(0); store.save(entries);
            eq(1, store.load().size());
            String java = new File(System.getProperty("java.home"), "bin/java").getPath();
            Process child = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                    CoreChecks.class.getName(), directory.toString()).inheritIO().start();
            eq(0, child.waitFor());
            // Simulate interruption after old data was backed up, before commit completed.
            Files.move(directory.resolve("sleep-v1.bin"), directory.resolve("sleep-v1.bak"));
            Files.write(directory.resolve("sleep-v1.bin"), new byte[]{1,2});
            eq(90L, new SleepStore(directory.toFile()).load().get(0).minutes);
            Files.write(directory.resolve("sleep-v1.bin"), new byte[]{1,2});
            boolean rejected = false;
            try { store.load(); } catch (IOException expected) { rejected = true; }
            eq(true, rejected);
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.delete(p); } catch (IOException ex) { throw new UncheckedIOException(ex); }
                });
            }
        }
        System.out.println("PASS: " + checks + " checks (time, days, DST, validation, edit/delete, storage/recovery, report)");
    }
}
