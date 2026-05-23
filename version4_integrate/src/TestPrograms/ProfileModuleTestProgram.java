package TestPrograms;

import profile_module.data.CvInfo;
import profile_module.data.JsonStore;
import profile_module.data.ProfileData;
import profile_module.data.SkillItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Unit + partition tests for {@code profile_module.data.ProfileData} and
 * {@code profile_module.data.JsonStore}.
 *
 * Profile completion rule (see {@link ProfileData#recomputeCompletion()}):
 *   basic info ✓  -> +33
 *   has skills ✓  -> +33
 *   has CV     ✓  -> +33
 *   rounded to nearest integer (so all three give 100).
 */
public final class ProfileModuleTestProgram {

    /** Unique sentinel username so the test never overwrites a real profile. */
    private static final String TEST_USERNAME = "__test_profile_user__";

    private ProfileModuleTestProgram() {
    }

    public static void run() {
        System.out.println("---- ProfileModuleTestProgram ----");

        // Unit tests
        TestSupport.runTest("testDemoProfileNotNull", ProfileModuleTestProgram::testDemoProfileNotNull);
        TestSupport.runTest("testAddSkill", ProfileModuleTestProgram::testAddSkill);
        TestSupport.runTest("testCompletionEmptyProfile", ProfileModuleTestProgram::testCompletionEmptyProfile);
        TestSupport.runTest("testCompletionBasicInfoOnly", ProfileModuleTestProgram::testCompletionBasicInfoOnly);
        TestSupport.runTest("testCompletionBasicInfoAndSkills", ProfileModuleTestProgram::testCompletionBasicInfoAndSkills);
        TestSupport.runTest("testCompletionFullProfileWithCv", ProfileModuleTestProgram::testCompletionFullProfileWithCv);

        // Partition tests
        TestSupport.runTest("testYearSelectYearNotComplete", ProfileModuleTestProgram::testYearSelectYearNotComplete);
        TestSupport.runTest("testEmptyCvFileNameNotComplete", ProfileModuleTestProgram::testEmptyCvFileNameNotComplete);

        // JsonStore round-trip test (uses sentinel username + cleanup)
        TestSupport.runTest("testSaveAndLoadProfile", ProfileModuleTestProgram::testSaveAndLoadProfile);
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();
        run();
        TestSupport.printSummary();
        System.exit(TestSupport.failed > 0 ? 1 : 0);
    }

    // ===================================================================
    // Unit tests
    // ===================================================================

    // Unit test: ProfileData.demo() returns a fully-populated object.
    private static void testDemoProfileNotNull() {
        ProfileData d = ProfileData.demo();
        TestSupport.assertNotNull(d, "demo() must not return null");
        TestSupport.assertNotNull(d.fullName, "demo fullName must not be null");
        TestSupport.assertNotNull(d.skills, "demo skills must not be null");
        TestSupport.assertTrue(d.skills.size() > 0, "demo profile must have at least one skill");
    }

    // Unit test: addSkill appends a skill to the list and returns it.
    private static void testAddSkill() {
        ProfileData d = new ProfileData();
        int before = d.skills.size();
        SkillItem item = d.addSkill("Java", "Programming", "Intermediate");
        TestSupport.assertNotNull(item, "addSkill must return the added item");
        TestSupport.assertEquals("Java", item.name, "skill name");
        TestSupport.assertEquals(before + 1, d.skills.size(), "skills list size must grow by 1");
    }

    // Partition test (equivalence class: empty profile -> 0%).
    private static void testCompletionEmptyProfile() {
        ProfileData d = new ProfileData();
        d.recomputeCompletion();
        TestSupport.assertEquals(0, d.profileCompletionPercent, "empty profile completion");
    }

    // Partition test (basic info only -> ~33%).
    private static void testCompletionBasicInfoOnly() {
        ProfileData d = makeBasicInfoProfile();
        d.recomputeCompletion();
        TestSupport.assertEquals(33, d.profileCompletionPercent, "basic-info-only completion");
    }

    // Partition test (basic info + skills, no CV -> ~67%).
    private static void testCompletionBasicInfoAndSkills() {
        ProfileData d = makeBasicInfoProfile();
        d.addSkill("Java", "Programming", "Intermediate");
        d.recomputeCompletion();
        TestSupport.assertEquals(67, d.profileCompletionPercent, "basic+skills completion");
    }

    // Partition test (basic info + skills + CV -> 100%).
    private static void testCompletionFullProfileWithCv() {
        ProfileData d = makeBasicInfoProfile();
        d.addSkill("Java", "Programming", "Intermediate");
        d.cv = new CvInfo();
        d.cv.fileName = "John_CV.pdf";
        d.recomputeCompletion();
        TestSupport.assertEquals(100, d.profileCompletionPercent, "full profile completion");
    }

    // Partition test: "Select year" placeholder must NOT count as complete.
    // Boundary between "no year selected" and "real year".
    private static void testYearSelectYearNotComplete() {
        ProfileData d = makeBasicInfoProfile();
        d.year = "Select year";
        d.recomputeCompletion();
        TestSupport.assertEquals(0, d.profileCompletionPercent,
                "'Select year' placeholder must keep basic-info incomplete");
    }

    // Partition test: CV must have a non-blank file name to count.
    private static void testEmptyCvFileNameNotComplete() {
        ProfileData d = new ProfileData();
        d.cv = new CvInfo();
        d.cv.fileName = "";
        d.recomputeCompletion();
        TestSupport.assertEquals(0, d.profileCompletionPercent,
                "Blank CV file name must not contribute to completion");
    }

    // ===================================================================
    // JsonStore integration test (save -> load -> verify -> cleanup)
    // ===================================================================

    private static void testSaveAndLoadProfile() {
        JsonStore store = new JsonStore(TEST_USERNAME);
        Path profileFile = store.getProfileFile();
        try {
            ProfileData input = makeBasicInfoProfile();
            input.addSkill("Java", "Programming", "Intermediate");
            input.cv = new CvInfo();
            input.cv.fileName = "Test_CV.pdf";

            store.save(input);
            TestSupport.assertTrue(Files.exists(profileFile),
                    "Profile JSON should be written to " + profileFile);

            ProfileData loaded = store.load();
            TestSupport.assertNotNull(loaded, "Loaded profile must not be null");
            TestSupport.assertEquals(input.fullName, loaded.fullName, "fullName round-trips");
            TestSupport.assertEquals(input.studentId, loaded.studentId, "studentId round-trips");
            TestSupport.assertEquals(input.skills.size(), loaded.skills.size(), "skills count round-trips");
            TestSupport.assertEquals(input.cv.fileName, loaded.cv.fileName, "cv fileName round-trips");
            TestSupport.assertEquals(100, loaded.profileCompletionPercent,
                    "Full saved profile must be recomputed to 100");
        } finally {
            cleanupTestProfile(profileFile);
        }
    }

    // ===================================================================
    // Helpers
    // ===================================================================

    private static ProfileData makeBasicInfoProfile() {
        ProfileData d = new ProfileData();
        d.fullName = "Test User";
        d.studentId = "99999999";
        d.year = "3rd Year";
        d.programMajor = "Computer Science";
        d.email = "test@example.com";
        d.phoneNumber = "13800000000";
        return d;
    }

    private static void cleanupTestProfile(Path profileFile) {
        if (profileFile == null) {
            return;
        }
        try {
            // Delete file
            Files.deleteIfExists(profileFile);
            // Then walk up and delete empty directories (but stop at user.home boundary).
            Path home = Path.of(System.getProperty("user.home"));
            Path parent = profileFile.getParent();
            while (parent != null
                    && !parent.equals(home)
                    && parent.startsWith(home)
                    && parent.getFileName() != null) {
                if (!Files.isDirectory(parent)) {
                    break;
                }
                try (var stream = Files.list(parent)) {
                    if (stream.findAny().isPresent()) {
                        break;
                    }
                }
                Files.delete(parent);
                parent = parent.getParent();
            }
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    /** Helper kept for forward-compatibility with potential nested cleanup callers. */
    @SuppressWarnings("unused")
    private static Comparator<Path> bySegmentCount() {
        return Comparator.comparingInt(Path::getNameCount);
    }
}
