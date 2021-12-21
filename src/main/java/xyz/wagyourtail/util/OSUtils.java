package xyz.wagyourtail.util;

public class OSUtils {

    public static String getOSId() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("darwin") || osName.contains("mac")) {
            return "osx";
        }
        if (osName.contains("win")) {
            return "windows";
        }
        if (osName.contains("nux")) {
            return "linux";
        }
        return "unknown";
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
