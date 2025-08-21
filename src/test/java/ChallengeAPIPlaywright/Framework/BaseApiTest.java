package ChallengeAPIPlaywright.Framework;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class BaseApiTest {
    protected WireMockServer wireMockServer;

    @BeforeClass
    public void setUp() {
        if (TestConfig.getMode() == TestConfig.Mode.DEVELOPMENT) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));
            wireMockServer.start();
            setupStubs();
        }
    }

    protected void setupStubs() {
        // To be overridden by subclasses for specific endpoint stubs
    }

    @AfterClass
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
