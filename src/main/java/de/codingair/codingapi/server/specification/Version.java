package de.codingair.codingapi.server.specification;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum Version {
    UNKNOWN(0),
    v1_7(7),
    v1_8(8),
    v1_9(9),
    v1_10(10),
    v1_11(11),
    v1_12(12),
    v1_13(13),
    v1_14(14),
    v1_15(15),
    v1_16(16),
    v1_17(17),
    v1_17_1(17.1),
    ;

    private static Version VERSION = null;
    private static String NAME = null;
    private static String SPECIFICATION = null;
    private static Type TYPE = null;
    private final double id;

    Version(double id) {
        this.id = id;
    }

    public static Version get() {
        return VERSION;
    }

    public static Type type() {
        return TYPE;
    }

    public static void load() {
        if (VERSION == null) {
            //server type
            try {
                String s = Bukkit.getVersion();
                int from = s.indexOf('-');

                if (from >= 0) {
                    from += 1;
                    int to = s.indexOf("-", from);

                    if (to >= 0) {
                        TYPE = Type.getByName(Bukkit.getVersion().substring(from, to));
                    } else TYPE = Type.UNKNOWN;
                } else TYPE = Type.UNKNOWN;
            } catch (StringIndexOutOfBoundsException ex) {
                TYPE = Type.UNKNOWN;
            }

            //version
            NAME = Bukkit.getBukkitVersion().split("-", -1)[0];
            double version = Double.parseDouble(NAME.substring(2));
            SPECIFICATION = Bukkit.getBukkitVersion().replace(NAME + "-", "");

            VERSION = byId(version);
        }
    }

    public static boolean atLeast(double version) {
        return get().id >= version;
    }

    public static boolean less(double version) {
        return get().id < version;
    }

    private static @NotNull Version byId(double version) {
        for (Version value : Version.values()) {
            if (value.id == version) return value;
        }

        //1.16.5 -> 1.16
        int casted = (int) version;
        for (Version value : Version.values()) {
            if (value.id == casted) return value;
        }

        throw new IllegalArgumentException("Version not found: " + version);
    }

    @SafeVarargs
    public static <T> T since(double version, T old, T... updated) {
        Version v = byId(version);

        int diff = Version.get().ordinal() - v.ordinal();
        if (diff >= 0) {
            if (diff >= updated.length || updated[diff] == null) {
                diff = Math.min(diff, updated.length - 1);

                //go back to fewer value
                for (int i = diff; i >= 0; i--) {
                    if (updated[diff] != null) return updated[diff];
                }
            } else return updated[diff];
        }

        return old;
    }

    public double getId() {
        return id;
    }

    public String fullVersion() {
        return NAME + " (" + SPECIFICATION + ", " + TYPE + ")";
    }

    public String getShortVersionName() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    public boolean isBiggerThan(Version version) {
        return id > version.id;
    }

    public boolean isBiggerThan(double version) {
        return id > version;
    }
}
