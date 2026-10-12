package se.adde.sleeplog.core;

import java.io.*;
import java.util.*;

/** Versioned private storage. A synced temporary file and backup protect the last save. */
public final class SleepStore {
    private final File file, backup, temp;
    public SleepStore(File directory) {
        file = new File(directory, "sleep-v1.bin");
        backup = new File(directory, "sleep-v1.bak");
        temp = new File(directory, "sleep-v1.tmp");
    }
    public synchronized List<SleepEntry> load() throws IOException {
        // An interrupted transaction rolls back to the last completed save.
        if (backup.exists()) {
            if (file.exists() && !file.delete()) throw new IOException("Cannot restore storage");
            if (!backup.renameTo(file)) throw new IOException("Cannot restore backup");
        }
        List<SleepEntry> entries = new ArrayList<>();
        if (!file.exists()) return entries;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            if (in.readInt() != 0x534C0001) throw new IOException("Unknown data version");
            int count = in.readInt();
            if (count < 0 || count > 100000) throw new IOException("Invalid count");
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < count; i++) {
                String id = in.readUTF();
                if (!ids.add(id)) throw new IOException("Duplicate id");
                entries.add(in.readBoolean()
                    ? SleepEntry.length(id, in.readUTF(), in.readLong())
                    : SleepEntry.timed(id, in.readLong(), in.readLong(), in.readUTF()));
            }
            if (in.read() != -1) throw new IOException("Unexpected data");
        } catch (IllegalArgumentException ex) { throw new IOException("Invalid entry", ex); }
        return entries;
    }
    public synchronized void save(List<SleepEntry> entries) throws IOException {
        if (backup.exists()) throw new IOException("Recovery required");
        if (entries.size() > 100000) throw new IOException("Too many entries");
        Set<String> ids = new HashSet<>();
        for (SleepEntry e : entries) if (!ids.add(e.id)) throw new IOException("Duplicate id");
        try (FileOutputStream stream = new FileOutputStream(temp);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(stream))) {
            out.writeInt(0x534C0001); out.writeInt(entries.size());
            for (SleepEntry e : entries) {
                out.writeUTF(e.id); out.writeBoolean(e.lengthOnly);
                if (e.lengthOnly) { out.writeUTF(e.date); out.writeLong(e.minutes); }
                else { out.writeLong(e.start); out.writeLong(e.end); out.writeUTF(e.zone); }
            }
            out.flush(); stream.getFD().sync();
        }
        if (file.exists() && !file.renameTo(backup)) throw new IOException("Cannot back up storage");
        if (!temp.renameTo(file)) {
            if (backup.exists()) backup.renameTo(file);
            throw new IOException("Cannot commit storage");
        }
        if (backup.exists() && !backup.delete()) {
            load(); // Roll back rather than report a save that will be undone on next launch.
            throw new IOException("Cannot finish storage transaction");
        }
    }
}
