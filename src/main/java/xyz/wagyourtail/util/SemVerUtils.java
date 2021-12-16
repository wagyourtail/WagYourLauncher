package xyz.wagyourtail.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemVerUtils {

    private static final Pattern semverPattern = Pattern.compile("^(\\d+)(\\.(\\d+)(\\.(\\d+))?)?(-(\\w+)(.(\\d+))?)?(\\+([0-9a-zA-Z-\\.]+))?$");

    public static boolean isSemVer(String s) {
        return semverPattern.matcher(s).matches();
    }

    public static boolean matches(String semVer, String pattern) {
        return SemVerMatcher.matcher(pattern).matches(SemVer.create(semVer));
    }

    public record SemVer(int major, int minor, int patch, String prerelease, String build) {
        public static SemVer create(String version) {
            var matcher = semverPattern.matcher(version);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid version string: " + version);
            }
            var major = Integer.parseInt(matcher.group(1));
            var minor = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
            var patch = matcher.group(5) == null ? 0 : Integer.parseInt(matcher.group(5));
            var prerelease = matcher.group(7);
            var build = matcher.group(9);
            return new SemVer(major, minor, patch, prerelease, build);
        }

        /**
         * @return 1 if this is greater than other, -1 if other is greater than this, 0 if they are equal
         */
        public int compareTo(SemVer other) {
            if (major > other.major) {
                return 1;
            }
            if (major < other.major) {
                return -1;
            }
            if (minor > other.minor) {
                return 1;
            }
            if (minor < other.minor) {
                return -1;
            }
            if (patch > other.patch) {
                return 1;
            }
            if (patch < other.patch) {
                return -1;
            }
            return preReleaseCompare(other);
        }

        public int preReleaseCompare(SemVer other) {
            if (prerelease == null && other.prerelease == null) {
                return 0;
            }
            if (prerelease == null) {
                return 1;
            }
            if (other.prerelease == null) {
                return -1;
            }
            if (prerelease.equals(other.prerelease)) {
                return 0;
            }
            if (prerelease.startsWith("alpha") && !other.prerelease.startsWith("alpha")) {
                return -1;
            }
            if (!prerelease.startsWith("alpha") && other.prerelease.startsWith("alpha")) {
                return 1;
            }
            if (prerelease.startsWith("beta") && !other.prerelease.startsWith("beta")) {
                return -1;
            }
            if (!prerelease.startsWith("beta") && other.prerelease.startsWith("beta")) {
                return 1;
            }
            if (prerelease.startsWith("pre") && !other.prerelease.startsWith("pre")) {
                return -1;
            }
            if (!prerelease.startsWith("pre") && other.prerelease.startsWith("pre")) {
                return 1;
            }
            if (prerelease.startsWith("rc") && !other.prerelease.startsWith("rc")) {
                return -1;
            }
            if (!prerelease.startsWith("rc") && other.prerelease.startsWith("rc")) {
                return 1;
            }
            // compare using actual int values in prerelease string
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(prerelease);
            Matcher otherMatcher = pattern.matcher(other.prerelease);
            while (matcher.find() && otherMatcher.find()) {
                int thisValue = Integer.parseInt(matcher.group(1));
                int otherValue = Integer.parseInt(otherMatcher.group(1));
                if (thisValue > otherValue) {
                    return 1;
                } else if (thisValue < otherValue) {
                    return -1;
                }
            }
            return 0;
        }

    }

    public interface SemVerMatcher {
        boolean matches(SemVer version);

        static SemVerMatcher matcher(String version) {
            if (version.matches(PrefixedRange.prefixedRangePattern.pattern())) {
                return PrefixedRange.parse(version);
            } else if (version.matches(HyphenRange.hyphenRangePattern.pattern())) {
                return HyphenRange.parse(version);
            } else if (version.matches(AndRange.andRangePattern.pattern())) {
                return AndRange.parse(version);
            } else if (version.matches(OrRange.orRangePattern.pattern())) {
                return OrRange.parse(version);
            }
            throw new IllegalArgumentException("Invalid version matcher: " + version);
        }

    }

    public record PrefixedRange(String prefix, String major, String minor, String patch) implements SemVerMatcher {

        @Override
        public boolean matches(SemVer version) {
            if (
                Objects.equals(prefix, "*") &&
                    major == null &&
                    minor == null &&
                    patch == null
            ) {
                return true;
            }
            if (Objects.equals(prefix, "^")) {
                if (major != null) {
                    if (version.major == 0 && !Integer.toString(version.minor).equals(minor)) {
                        return false;
                    }
                    if (version.major == 0 && version.minor == 0 && !Integer.toString(version.patch).equals(patch)) {
                        return false;
                    }
                    return Integer.toString(version.major).equals(major);
                }
            }
            if (Objects.equals(prefix, "~")) {
                if (minor != null) {
                    return Integer.toString(version.minor).equals(minor);
                } else {
                    return Integer.toString(version.major).equals(major);
                }
            }
            if (version.prerelease != null) {
                return false;
            }
            int major = this.major == null || this.major.equals("X") || this.major.equals("*") ? -1 : Integer.parseInt(
                this.major);
            int minor = this.minor == null || this.minor.equals("X") || this.minor.equals("*") ? -1 : Integer.parseInt(
                this.minor);
            int patch = this.patch == null || this.patch.equals("X") || this.patch.equals("*") ? -1 : Integer.parseInt(
                this.patch);
            if (Objects.equals(prefix, "=") || prefix == null) {
                if (major != -1 && version.major != major) {
                    return false;
                }
                if (minor != -1 && version.minor != minor) {
                    return false;
                }
                return patch == -1 || version.patch == patch;
            }
            if (Objects.equals(prefix, ">")) {
                if (major != -1 && version.major > major) {
                    return true;
                }
                if (minor != -1 && version.minor > minor) {
                    return true;
                }
                return patch == -1 || version.patch > patch;
            }
            if (Objects.equals(prefix, "<")) {
                if (major != -1 && version.major < major) {
                    return true;
                }
                if (minor != -1 && version.minor < minor) {
                    return true;
                }
                return patch == -1 || version.patch < patch;
            }
            if (Objects.equals(prefix, ">=")) {
                if (major != -1 && version.major < major) {
                    return false;
                }
                if (minor != -1 && version.minor < minor) {
                    return false;
                }
                return patch == -1 || version.patch >= patch;
            }
            if (Objects.equals(prefix, "<=")) {
                if (major != -1 && version.major > major) {
                    return false;
                }
                if (minor != -1 && version.minor > minor) {
                    return false;
                }
                return patch == -1 || version.patch <= patch;
            }
            return false;
        }

        public static Pattern prefixedRangePattern = Pattern.compile(
            "^(\\^|<=|>=|<|>|~|=)?(X|\\d+)?(\\.(\\*|X|\\d+|$))?(\\.(\\*|X|\\d+|$))?$");

        public static PrefixedRange parse(String range) {
            Matcher m = prefixedRangePattern.matcher(range);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid range " + range);
            }
            String prefix = m.group(1);
            String major = m.group(2);
            String minor = m.group(4);
            String patch = m.group(6);
            return new PrefixedRange(prefix, major, minor, patch);
        }

    }

    public record HyphenRange(SemVer from, SemVer to) implements SemVerMatcher {

        @Override
        public boolean matches(SemVer version) {
            return version.compareTo(from) >= 0 && version.compareTo(to) <= 0;
        }

        public static Pattern hyphenRangePattern = Pattern.compile(
            "^(\\d+|X)(\\.(\\d+|X))?(\\.(\\d+|X))?\\s*-\\s*(\\d+|X)(\\.(\\d+|X))?(\\.(\\d+|X))?$");

        public static HyphenRange parse(String range) {
            Matcher m = hyphenRangePattern.matcher(range);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid range " + range);
            }
            int fromMajor = m.group(1).equals("X") ? 0 : Integer.parseInt(m.group(1));
            int fromMinor = m.group(3).equals("X") ? 0 : Integer.parseInt(m.group(3));
            int fromPatch = m.group(5).equals("X") ? 0 : Integer.parseInt(m.group(5));
            int toMajor = m.group(6).equals("X") ? Integer.MAX_VALUE : Integer.parseInt(m.group(6));
            int toMinor = m.group(8).equals("X") ? Integer.MAX_VALUE : Integer.parseInt(m.group(8));
            int toPatch = m.group(10).equals("X") ? Integer.MAX_VALUE : Integer.parseInt(m.group(10));
            return new HyphenRange(
                new SemVer(fromMajor, fromMinor, fromPatch, null, null),
                new SemVer(toMajor, toMinor, toPatch, null, null)
            );
        }

    }

    public record AndRange(SemVerMatcher first, SemVerMatcher second) implements SemVerMatcher {
        @Override
        public boolean matches(SemVer version) {
            return first.matches(version) && second.matches(version);
        }

        public static Pattern andRangePattern = Pattern.compile("[^\\s]*[^-] [^-][^\\s]*");

        public static AndRange parse(String range) {
            if (!andRangePattern.matcher(range).matches()) {
                throw new IllegalArgumentException("Invalid range " + range);
            }
            String[] parts = range.split("\\s+");
            return new AndRange(SemVerMatcher.matcher(parts[0]), SemVerMatcher.matcher(parts[1]));
        }

    }

    public record OrRange(SemVerMatcher first, SemVerMatcher second) implements SemVerMatcher {
        @Override
        public boolean matches(SemVer version) {
            return first.matches(version) || second.matches(version);
        }

        public static Pattern orRangePattern = Pattern.compile("[^\\s]*[^-]\\|\\|[^-][^\\s]*");

        public static OrRange parse(String range) {
            if (!orRangePattern.matcher(range).matches()) {
                throw new IllegalArgumentException("Invalid range " + range);
            }
            String[] parts = range.split("\\s+");
            return new OrRange(SemVerMatcher.matcher(parts[0]), SemVerMatcher.matcher(parts[1]));
        }

    }

}