package TestPrograms;

/**
 * Partition + boundary tests for the CV upload requirement.
 *
 * The CV upload UI screens ({@code OnboardingScreen}, {@code ManageCvScreen})
 * encode the acceptance rule inline as private UI logic. This test program
 * does NOT modify business code; instead it re-implements the requirement
 * as a small helper here, exactly mirroring the user-facing rule:
 *
 *   "Accepted formats: PDF, DOC, DOCX (Max 5MB)"
 *
 * This is a test-only restatement of the requirement to support
 * Validation / Partition (equivalence classes) / Boundary-value tests.
 */
public final class CvValidationTestProgram {

    private static final long MAX_BYTES = 5L * 1024L * 1024L; // 5 MB

    private CvValidationTestProgram() {
    }

    public static void run() {
        System.out.println("---- CvValidationTestProgram ----");

        // Partition tests: file type
        TestSupport.runTest("testValidPdfFileType", CvValidationTestProgram::testValidPdfFileType);
        TestSupport.runTest("testValidDocFileType", CvValidationTestProgram::testValidDocFileType);
        TestSupport.runTest("testValidDocxFileType", CvValidationTestProgram::testValidDocxFileType);
        TestSupport.runTest("testInvalidJpgFileType", CvValidationTestProgram::testInvalidJpgFileType);
        TestSupport.runTest("testInvalidPngFileType", CvValidationTestProgram::testInvalidPngFileType);
        TestSupport.runTest("testInvalidExeFileType", CvValidationTestProgram::testInvalidExeFileType);
        TestSupport.runTest("testInvalidTxtFileType", CvValidationTestProgram::testInvalidTxtFileType);

        // Boundary tests: file size
        TestSupport.runTest("testEmptyFileRejected", CvValidationTestProgram::testEmptyFileRejected);
        TestSupport.runTest("testFileBelowFiveMbAccepted", CvValidationTestProgram::testFileBelowFiveMbAccepted);
        TestSupport.runTest("testFileExactlyFiveMbAccepted", CvValidationTestProgram::testFileExactlyFiveMbAccepted);
        TestSupport.runTest("testFileAboveFiveMbRejected", CvValidationTestProgram::testFileAboveFiveMbRejected);
        TestSupport.runTest("testUpperCaseExtensionAccepted", CvValidationTestProgram::testUpperCaseExtensionAccepted);
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();
        run();
        TestSupport.printSummary();
        System.exit(TestSupport.failed > 0 ? 1 : 0);
    }

    // ===================================================================
    // Partition tests: file extension (equivalence classes)
    //   valid:   pdf, doc, docx (case-insensitive)
    //   invalid: jpg, png, exe, txt, ...
    // ===================================================================

    private static void testValidPdfFileType() {
        TestSupport.assertTrue(isValidCv("resume.pdf", 1024L), "PDF must be accepted");
    }

    private static void testValidDocFileType() {
        TestSupport.assertTrue(isValidCv("resume.doc", 1024L), "DOC must be accepted");
    }

    private static void testValidDocxFileType() {
        TestSupport.assertTrue(isValidCv("resume.docx", 1024L), "DOCX must be accepted");
    }

    private static void testInvalidJpgFileType() {
        TestSupport.assertFalse(isValidCv("photo.jpg", 1024L), "JPG must be rejected");
    }

    private static void testInvalidPngFileType() {
        TestSupport.assertFalse(isValidCv("photo.png", 1024L), "PNG must be rejected");
    }

    private static void testInvalidExeFileType() {
        TestSupport.assertFalse(isValidCv("setup.exe", 1024L), "EXE must be rejected");
    }

    private static void testInvalidTxtFileType() {
        TestSupport.assertFalse(isValidCv("note.txt", 1024L), "TXT must be rejected");
    }

    // ===================================================================
    // Boundary tests: file size
    //   0 byte                 -> reject (no content)
    //   below 5 MB             -> accept
    //   exactly 5 MB           -> accept (inclusive upper bound)
    //   above 5 MB             -> reject
    // ===================================================================

    // Boundary test: 0-byte file must be rejected as "empty".
    private static void testEmptyFileRejected() {
        TestSupport.assertFalse(isValidCv("resume.pdf", 0L),
                "Empty (0-byte) file must be rejected");
    }

    // Boundary test: 1 MB (well below limit) is accepted.
    private static void testFileBelowFiveMbAccepted() {
        long oneMb = 1L * 1024L * 1024L;
        TestSupport.assertTrue(isValidCv("resume.pdf", oneMb), "1 MB CV must be accepted");
    }

    // Boundary test: exactly 5 MB is accepted (inclusive upper bound).
    private static void testFileExactlyFiveMbAccepted() {
        TestSupport.assertTrue(isValidCv("resume.pdf", MAX_BYTES),
                "Exactly 5 MB CV must be accepted (inclusive)");
    }

    // Boundary test: 5 MB + 1 byte must be rejected.
    private static void testFileAboveFiveMbRejected() {
        TestSupport.assertFalse(isValidCv("resume.pdf", MAX_BYTES + 1L),
                "Greater than 5 MB must be rejected");
    }

    // Partition test: case-insensitive extension matching.
    private static void testUpperCaseExtensionAccepted() {
        TestSupport.assertTrue(isValidCv("RESUME.PDF", 1024L),
                "Upper-case extension must be accepted");
        TestSupport.assertTrue(isValidCv("Resume.DocX", 1024L),
                "Mixed-case extension must be accepted");
    }

    // ===================================================================
    // Helper restating the CV upload requirement for test purposes only.
    // Mirrors the user-facing rule: PDF / DOC / DOCX, Max 5MB, non-empty.
    // ===================================================================
    private static boolean isValidCv(String fileName, long sizeBytes) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        if (sizeBytes <= 0L) {
            return false;
        }
        if (sizeBytes > MAX_BYTES) {
            return false;
        }
        String lower = fileName.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return false;
        }
        String ext = lower.substring(dot + 1);
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx");
    }
}
