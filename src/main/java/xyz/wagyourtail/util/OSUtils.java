package xyz.wagyourtail.util;

public class OSUtils {

    public static String getOSId() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    public static void main(String[] args) {
        System.out.println("OS: " + getOSId());
        System.out.println("Version: " + getOsVersion());
        System.out.println("Arch: " + getOsArch());
    }
}
