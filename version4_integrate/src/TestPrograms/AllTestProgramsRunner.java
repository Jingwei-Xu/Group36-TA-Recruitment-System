package TestPrograms;

/**
 * Entry point that runs every test program in this package and exits with
 * non-zero status on failure (suitable for CI / submission pipelines).
 *
 * Headless mode is forced so no Swing GUI is created even when business
 * classes pull AWT/Swing transitively.
 */
public final class AllTestProgramsRunner {

    private AllTestProgramsRunner() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();

        AuthenticationTestProgram.run();
        ProfileModuleTestProgram.run();
        CvValidationTestProgram.run();
        TaApplicationDataTestProgram.run();
        MoReviewAndAdminTestProgram.run();

        TestSupport.printSummary();

        if (TestSupport.failed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
