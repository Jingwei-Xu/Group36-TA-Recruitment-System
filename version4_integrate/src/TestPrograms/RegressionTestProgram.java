package TestPrograms;

/**
 * Regression test program.
 *
 * Regression testing: after each build, re-run existing tests to ensure
 * earlier functionality is not broken by new updates. This class re-runs
 * all individual test suites in sequence and prints a consolidated summary.
 */
public final class RegressionTestProgram {

    private RegressionTestProgram() {
    }

    public static void run() {
        System.out.println("==================================================");
        System.out.println("  REGRESSION TEST RUN");
        System.out.println("  Re-running core test suites against current build");
        System.out.println("==================================================");

        AuthenticationTestProgram.run();
        ProfileModuleTestProgram.run();
        CvValidationTestProgram.run();
        TaApplicationDataTestProgram.run();
        MoReviewAndAdminTestProgram.run();
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();
        run();
        TestSupport.printSummary();
        System.exit(TestSupport.failed > 0 ? 1 : 0);
    }
}
