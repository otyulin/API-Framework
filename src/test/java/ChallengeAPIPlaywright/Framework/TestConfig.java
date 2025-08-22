package ChallengeAPIPlaywright.Framework;

public class TestConfig {
    public enum Mode { PRODUCTION, DEVELOPMENT }
    private static final String MODE_PROPERTY = "test.mode";
    private static final String DEFAULT_MODE = "DEVELOPMENT";
    private static final String GROUP_PROPERTY = "test.group";
    private static final String DEFAULT_GROUP = "all";

    public static Mode getMode() {
        String mode = System.getProperty(MODE_PROPERTY, DEFAULT_MODE).toUpperCase();
        return Mode.valueOf(mode);
    }

    public static String getBaseUri() {
        if (getMode() == Mode.PRODUCTION) {
            return "https://example.com/api/skus";
        } else {
            return "http://localhost:8080/api/skus";
        }
    }

    /**
     * Returns the test group to run: "Smoke", "Regression", or "all" (default).
     * Can be set via -Dtest.group=Smoke or Regression
     */
    public static String getTestGroup() {
        return System.getProperty(GROUP_PROPERTY, DEFAULT_GROUP);
    }
}