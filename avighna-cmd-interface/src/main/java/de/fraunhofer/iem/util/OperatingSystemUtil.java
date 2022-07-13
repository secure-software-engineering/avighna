package de.fraunhofer.iem.util;

public class OperatingSystemUtil {
    private static String os;

    public static String getOs() {
        if (os == null) {
            os = System.getProperty("os.name");
            System.out.println(os);
        }

        return os;
    }

    public static boolean isWindows() {
        return getOs().startsWith("Win");
    }

    public static boolean isUnix() {
        return getOs().startsWith("Linux");
    }

    public static boolean isMac() {
        return getOs().startsWith("Mac") || getOs().startsWith("Darwin");
    }
}
