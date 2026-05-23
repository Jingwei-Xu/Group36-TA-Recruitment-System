package TestPrograms;

import Authentication_Module.model.User;
import Authentication_Module.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Validation, defect, white-box, partition and boundary tests for the
 * Authentication module (User + AuthService).
 *
 * Test accounts come from the real {@code data/users/...} JSON files shipped
 * with the project:
 *   johnsmith / test1234       (TA)
 *   davidpark / test1234       (MO)
 *   systemadmin / test1234     (Admin)
 *
 * No new users are written to the real users data: all register-related tests
 * either fail in the password regex (before any save) or reuse an existing
 * username so {@code AuthService.register} short-circuits on duplicate.
 */
public final class AuthenticationTestProgram {

    private static final String TA_USERNAME = "johnsmith";
    private static final String MO_USERNAME = "davidpark";
    private static final String ADMIN_USERNAME = "systemadmin";
    private static final String VALID_PASSWORD = "test1234";

    private AuthenticationTestProgram() {
    }

    public static void run() {
        System.out.println("---- AuthenticationTestProgram ----");

        // ---- Validation / black-box tests ----
        TestSupport.runTest("testValidTaLogin", AuthenticationTestProgram::testValidTaLogin);
        TestSupport.runTest("testValidMoLogin", AuthenticationTestProgram::testValidMoLogin);
        TestSupport.runTest("testValidAdminLogin", AuthenticationTestProgram::testValidAdminLogin);
        TestSupport.runTest("testInvalidPasswordLogin", AuthenticationTestProgram::testInvalidPasswordLogin);
        TestSupport.runTest("testUnknownUserLogin", AuthenticationTestProgram::testUnknownUserLogin);
        TestSupport.runTest("testGetRoleByUsername", AuthenticationTestProgram::testGetRoleByUsername);

        // ---- White-box tests on User.passwordMatches ----
        TestSupport.runTest("testPasswordMatchesPlainText", AuthenticationTestProgram::testPasswordMatchesPlainText);
        TestSupport.runTest("testPasswordMatchesWrongPassword", AuthenticationTestProgram::testPasswordMatchesWrongPassword);
        TestSupport.runTest("testPasswordMatchesNullPassword", AuthenticationTestProgram::testPasswordMatchesNullPassword);
        TestSupport.runTest("testPasswordMatchesHashedBranch", AuthenticationTestProgram::testPasswordMatchesHashedBranch);

        // ---- Partition / boundary tests on register password rule ----
        TestSupport.runTest("testRegisterPasswordTooShort", AuthenticationTestProgram::testRegisterPasswordTooShort);
        TestSupport.runTest("testRegisterPasswordOnlyLetters", AuthenticationTestProgram::testRegisterPasswordOnlyLetters);
        TestSupport.runTest("testRegisterPasswordOnlyNumbers", AuthenticationTestProgram::testRegisterPasswordOnlyNumbers);
        TestSupport.runTest("testRegisterPasswordValidBoundary", AuthenticationTestProgram::testRegisterPasswordValidBoundary);
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();
        run();
        TestSupport.printSummary();
        System.exit(TestSupport.failed > 0 ? 1 : 0);
    }

    // ===================================================================
    // Validation / black-box tests
    // ===================================================================

    // Black-box test: valid TA login with correct credentials.
    private static void testValidTaLogin() {
        AuthService svc = new AuthService();
        User u = svc.login(TA_USERNAME, VALID_PASSWORD);
        TestSupport.assertNotNull(u, "TA login should return a user");
        TestSupport.assertTrue(TA_USERNAME.equalsIgnoreCase(u.getUsername()),
                "Returned user must match requested username");
        TestSupport.assertEquals("TA", u.getRole(), "Role must be TA");
    }

    // Black-box test: valid MO login with correct credentials.
    private static void testValidMoLogin() {
        AuthService svc = new AuthService();
        User u = svc.login(MO_USERNAME, VALID_PASSWORD);
        TestSupport.assertNotNull(u, "MO login should return a user");
        TestSupport.assertEquals("MO", u.getRole(), "Role must be MO");
    }

    // Black-box test: valid Admin login with correct credentials.
    private static void testValidAdminLogin() {
        AuthService svc = new AuthService();
        User u = svc.login(ADMIN_USERNAME, VALID_PASSWORD);
        TestSupport.assertNotNull(u, "Admin login should return a user");
        TestSupport.assertEquals("Admin", u.getRole(), "Role must be Admin");
    }

    // Defect test: correct username but wrong password.
    private static void testInvalidPasswordLogin() {
        AuthService svc = new AuthService();
        User u = svc.login(TA_USERNAME, "completely-wrong-password");
        TestSupport.assertNull(u, "Login with wrong password must return null");
        TestSupport.assertNotNull(svc.getLoginMessage(), "Login message must be set");
    }

    // Defect test: completely unknown username.
    private static void testUnknownUserLogin() {
        AuthService svc = new AuthService();
        User u = svc.login("__no_such_user_xyz__", VALID_PASSWORD);
        TestSupport.assertNull(u, "Login with unknown user must return null");
    }

    // Black-box test: role lookup for each role.
    private static void testGetRoleByUsername() {
        AuthService svc = new AuthService();
        TestSupport.assertEquals("TA", svc.getRoleByUsername(TA_USERNAME), "TA role lookup");
        TestSupport.assertEquals("MO", svc.getRoleByUsername(MO_USERNAME), "MO role lookup");
        TestSupport.assertEquals("Admin", svc.getRoleByUsername(ADMIN_USERNAME), "Admin role lookup");
        TestSupport.assertNull(svc.getRoleByUsername("__nobody__"), "Unknown user role lookup must be null");
    }

    // ===================================================================
    // White-box tests for User.passwordMatches branches
    // ===================================================================

    // White-box test: plain text branch (password equals plain).
    private static void testPasswordMatchesPlainText() {
        User u = new User("alice", "secret123", "TA");
        TestSupport.assertTrue(u.passwordMatches("secret123"), "Plain match must return true");
    }

    // White-box test: plain text branch with wrong input.
    private static void testPasswordMatchesWrongPassword() {
        User u = new User("alice", "secret123", "TA");
        TestSupport.assertFalse(u.passwordMatches("WRONG"), "Wrong plain must return false");
    }

    // White-box test: null guard at the top of passwordMatches.
    private static void testPasswordMatchesNullPassword() {
        User u = new User("alice", "secret123", "TA");
        TestSupport.assertFalse(u.passwordMatches(null), "Null plain must return false");
    }

    // White-box test: SHA-256 hash branch (plain password is blank, only hash is set).
    private static void testPasswordMatchesHashedBranch() {
        String plain = "hashedSecret1";
        String hash = sha256Hex(plain);
        // Construct: username, blank password, hash, role, status
        User u = new User("alice", "", hash, "TA", "ACTIVE");
        TestSupport.assertTrue(u.passwordMatches(plain), "Plain matching SHA-256 hash must return true");
        TestSupport.assertFalse(u.passwordMatches("other"), "Non-matching plain must return false");
    }

    // ===================================================================
    // Partition / boundary tests for register password rule
    // Password rule (AuthService.register): ^(?=.*[A-Za-z])(?=.*\d).{8,}$
    // Equivalence classes:
    //   - shorter than 8 chars                  -> reject
    //   - only letters (>=8)                    -> reject
    //   - only numbers (>=8)                    -> reject
    //   - mixed letters + numbers, length >= 8  -> accept
    // Boundary: length 8 with mixed content is the lower-bound valid value.
    // ===================================================================

    // Partition test: too short password (length 6) must be rejected.
    private static void testRegisterPasswordTooShort() {
        AuthService svc = new AuthService();
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> svc.register("__test_pw_short__", "ab12cd", "TA"),
                "Length<8 password must throw IllegalArgumentException");
    }

    // Partition test: letters-only password must be rejected.
    private static void testRegisterPasswordOnlyLetters() {
        AuthService svc = new AuthService();
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> svc.register("__test_pw_letters__", "abcdefgh", "TA"),
                "Letters-only password must throw IllegalArgumentException");
    }

    // Partition test: digits-only password must be rejected.
    private static void testRegisterPasswordOnlyNumbers() {
        AuthService svc = new AuthService();
        TestSupport.assertThrows(IllegalArgumentException.class,
                () -> svc.register("__test_pw_digits__", "12345678", "TA"),
                "Digits-only password must throw IllegalArgumentException");
    }

    // Boundary test: 8-char mixed password is the lower-bound valid value.
    // Uses an existing username so register passes the regex but short-circuits
    // on duplicate-username check WITHOUT writing to the real users data.
    private static void testRegisterPasswordValidBoundary() {
        AuthService svc = new AuthService();
        boolean result = svc.register(TA_USERNAME, "abc12345", "TA");
        TestSupport.assertFalse(result,
                "Existing username must cause register to return false (no write, regex accepted)");
    }

    // ===================================================================
    // Internal helpers
    // ===================================================================

    /** Local SHA-256 helper kept independent of {@link User} private impl. */
    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
