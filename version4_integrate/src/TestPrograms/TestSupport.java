package TestPrograms;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight test support utility used by every test program in this package.
 * No JUnit / Mockito dependency. Pure Java assertions + run/skip helpers + summary.
 */
public final class TestSupport {

    public static int total = 0;
    public static int passed = 0;
    public static int failed = 0;
    public static int skipped = 0;

    private static final List<String> failures = new ArrayList<>();

    private TestSupport() {
    }

    /** Run a test case. The body throws AssertionError on failure (via assert* helpers). */
    public static void runTest(String name, ThrowingRunnable body) {
        total++;
        try {
            body.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (AssertionError ae) {
            failed++;
            String msg = ae.getMessage() == null ? "assertion failed" : ae.getMessage();
            failures.add(name + " - " + msg);
            System.out.println("[FAIL] " + name + " - " + msg);
        } catch (Throwable t) {
            failed++;
            String msg = t.getClass().getSimpleName() + ": " + t.getMessage();
            failures.add(name + " - " + msg);
            System.out.println("[FAIL] " + name + " - " + msg);
        }
    }

    /** Mark a test as skipped (e.g. requires network / API key / unavailable API). */
    public static void skipTest(String name, String reason) {
        total++;
        skipped++;
        System.out.println("[SKIP] " + name + " - " + reason);
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        boolean equal = (expected == null) ? (actual == null) : expected.equals(actual);
        if (!equal) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    public static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    public static void assertNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + " (value=" + value + ")");
        }
    }

    /** Asserts that {@code body} throws an instance of {@code expected}. */
    public static void assertThrows(Class<? extends Throwable> expected, ThrowingRunnable body, String message) {
        try {
            body.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionError(message + " (got " + t.getClass().getSimpleName() + ")");
        }
        throw new AssertionError(message + " (no exception thrown)");
    }

    /** Resets counters. Useful when running multiple suites in isolation. */
    public static void reset() {
        total = 0;
        passed = 0;
        failed = 0;
        skipped = 0;
        failures.clear();
    }

    public static void printSummary() {
        int rate = total == 0 ? 0 : (int) Math.round(passed * 100.0 / total);
        System.out.println();
        System.out.println("========================================");
        System.out.println("Total: " + total
                + ", Passed: " + passed
                + ", Failed: " + failed
                + ", Skipped: " + skipped
                + ", Pass rate: " + rate + "%");
        if (!failures.isEmpty()) {
            System.out.println("---- Failures ----");
            for (String f : failures) {
                System.out.println("  - " + f);
            }
        }
        System.out.println("========================================");
    }

    /** Functional interface allowing test bodies to throw any exception. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
