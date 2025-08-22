package ChallengeAPIPlaywright.Framework;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;

import java.util.*;

public class SkuApiTest extends BaseApiTest {
    @Override
    protected void setupStubs() {
        // 1.1: Valid create
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.sku", WireMock.matching(".+")))
            .withRequestBody(WireMock.matchingJsonPath("$.description", WireMock.matching(".+")))
            .withRequestBody(WireMock.matchingJsonPath("$.price", WireMock.matching("^\\d+(\\.\\d{1,2})?$")))
            .atPriority(1)
            .willReturn(WireMock.aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\",\"id\":\"1\"}")));
        // 1.2: Missing sku
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.notMatching("$.sku"))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"'sku' is required\"}")));
        // 1.3: Missing description
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.notMatching("$.description"))
            .atPriority(3)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"'description' is required\"}")));
        // 1.4: Missing price
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.notMatching("$.price"))
            .atPriority(4)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"'price' is required\"}")));
        // 1.5: Duplicate sku
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.sku", WireMock.equalTo("berliner")))
            .atPriority(5)
            .willReturn(WireMock.aResponse().withStatus(409).withBody("{\"error\":\"Duplicate SKU\"}")));
        // 1.6: Invalid price (non-numeric)
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.price", WireMock.notMatching("^\\d+(\\.\\d{1,2})?$")))
            .atPriority(6)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"Invalid price format\"}")));
        // 1.7: Long description
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.description", WireMock.matching(".{500,}")))
            .atPriority(7)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"Description too long\"}")));
        // 1.8: Empty body
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.equalToJson("{}"))
            .atPriority(99)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"Missing fields\"}")));
        // 2.1: GET all
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/skus"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}]")));
        // 2.2: GET by valid id
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/skus/berliner"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}")));
        // 2.3: GET by non-existent id
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/skus/(?!berliner$)[^/]+"))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(404).withBody("{\"error\":\"SKU not found\"}")));
        // 3.1: Update existing SKU (POST with valid data)
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.sku", WireMock.equalTo("berliner")))
            .withRequestBody(WireMock.matchingJsonPath("$.description", WireMock.matching(".+")))
            .withRequestBody(WireMock.matchingJsonPath("$.price", WireMock.matching("^\\d+(\\.\\d{1,2})?$")))
            .atPriority(1)
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"sku\":\"berliner\",\"description\":\"Updated desc\",\"price\":\"3.99\"}")));
        // 3.2: Update with invalid price
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.price", WireMock.notMatching("^\\d+(\\.\\d{1,2})?$")))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"Invalid price format\"}")));
        // 3.3: Update without sku
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.notMatching("$.sku"))
            .atPriority(3)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"SKU/id required\"}")));
        // 4.1: DELETE by valid id
        wireMockServer.stubFor(WireMock.delete(WireMock.urlEqualTo("/api/skus/berliner"))
            .willReturn(WireMock.aResponse().withStatus(204)));
        // 4.2: DELETE by non-existent id
        wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/api/skus/(?!berliner$)[^/]+"))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(404).withBody("{\"error\":\"SKU not found\"}")));
        // 4.3: DELETE with invalid id format
        wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/api/skus/[^a-zA-Z0-9]+"))
            .atPriority(3)
            .willReturn(WireMock.aResponse().withStatus(400).withBody("{\"error\":\"Invalid SKU ID\"}")));
        // 5.1: Unsupported HTTP methods
        wireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/skus"))
            .willReturn(WireMock.aResponse().withStatus(405).withBody("{\"error\":\"Method Not Allowed\"}")));
        wireMockServer.stubFor(WireMock.patch(WireMock.urlEqualTo("/api/skus"))
            .willReturn(WireMock.aResponse().withStatus(405).withBody("{\"error\":\"Method Not Allowed\"}")));
        // 5.2: Extra/unexpected fields
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.extraField"))
            .atPriority(5)
            .willReturn(WireMock.aResponse().withStatus(200).withBody("{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}")));
        // 6.1: Unauthorized (if required)
        wireMockServer.stubFor(WireMock.any(WireMock.urlMatching("/api/skus.*"))
            .withHeader("Authorization", WireMock.absent())
            .atPriority(10)
            .willReturn(WireMock.aResponse().withStatus(401).withBody("{\"error\":\"Unauthorized\"}")));
        // 7.1: Delete then get/update
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/skus/deleted"))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(404).withBody("{\"error\":\"SKU not found\"}")));
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .withRequestBody(WireMock.matchingJsonPath("$.sku", WireMock.equalTo("deleted")))
            .atPriority(2)
            .willReturn(WireMock.aResponse().withStatus(404).withBody("{\"error\":\"SKU not found\"}")));
    }

    @Test
    public void testCreateSku() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),               
					RequestOptions.create()
					.setHeader("Content-Type", "application/json")
                    .setData(body)
            );
            Assert.assertEquals(response.status(), 201, "Expected 201 Created");
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSku: " + e.getMessage());
        }
    }

    @Test
    public void testGetAllSkus() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            APIResponse response = request.get(TestConfig.getBaseUri());
            Assert.assertEquals(response.status(), 200, "Expected 200 OK");
            Assert.assertTrue(response.text().contains("berliner"), "SKU 'berliner' should be present");
        } catch (Exception e) {
            Assert.fail("Exception during testGetAllSkus: " + e.getMessage());
        }
    }

    @Test
    public void testGetSkuById() {
        String skuId = "berliner";
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            APIResponse response = request.get(TestConfig.getBaseUri() + "/" + skuId);
            Assert.assertEquals(response.status(), 200, "Expected 200 OK");
            Assert.assertTrue(response.text().contains(skuId), "SKU by id should be present");
        } catch (Exception e) {
            Assert.fail("Exception during testGetSkuById: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteSku() {
        String skuId = "berliner";
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            APIResponse response = request.delete(TestConfig.getBaseUri() + "/" + skuId);
            Assert.assertEquals(response.status(), 204, "Expected 204 No Content");
        } catch (Exception e) {
            Assert.fail("Exception during testDeleteSku: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuWithInvalidPayload() {
    	try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
               // Intentionally sending an invalid payload
            String body = "{" +
                    "\"sku\":\"\"," +
                    "\"description\":\"\"," +
                    "\"price\":\"\"}";                    
            APIResponse response = request.post(TestConfig.getBaseUri(),               
            		RequestOptions.create()
                    .setHeader("Content-Type", "application/json")
                    .setData(body)
            );            
            Assert.assertTrue(response.status() >= 400, "Expected client error for invalid payload");                        
        } catch (Exception e)         
        {
            Assert.fail("Exception during testCreateSkuWithInvalidPayload: " + e.getMessage());
        }
    }
            
    @Test
    public void testCreateSkuMissingSku() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("sku"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuMissingSku: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuMissingDescription() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("description"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuMissingDescription: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuMissingPrice() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("price"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuMissingPrice: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuDuplicate() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            // First create
            request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            // Duplicate create
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 409);
            Assert.assertTrue(response.text().contains("Duplicate"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuDuplicate: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuInvalidPrice() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"abc\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("price"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuInvalidPrice: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuLongDescription() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String longDesc = "a".repeat(500);
            String body = String.format("{\"sku\":\"berliner\",\"description\":\"%s\",\"price\":\"2.99\"}", longDesc);
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("Description too long"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuLongDescription: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuEmptyBody() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json"));
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("Missing fields"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuEmptyBody: " + e.getMessage());
        }
    }

    @Test
    public void testGetSkuByNonExistentId() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String nonExistentId = "random123";
            APIResponse response = request.get(TestConfig.getBaseUri() + "/" + nonExistentId);
            Assert.assertEquals(response.status(), 404);
            Assert.assertTrue(response.text().contains("not found"));
        } catch (Exception e) {
            Assert.fail("Exception during testGetSkuByNonExistentId: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateSkuValid() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Updated desc\"," +
                    "\"price\":\"3.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 200);
            Assert.assertTrue(response.text().contains("Updated desc"));
        } catch (Exception e) {
            Assert.fail("Exception during testUpdateSkuValid: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateSkuInvalidPrice() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Updated desc\"," +
                    "\"price\":\"bad\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("price"));
        } catch (Exception e) {
            Assert.fail("Exception during testUpdateSkuInvalidPrice: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateSkuMissingSku() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"description\":\"Updated desc\"," +
                    "\"price\":\"3.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("SKU/id required"));
        } catch (Exception e) {
            Assert.fail("Exception during testUpdateSkuMissingSku: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteSkuNonExistent() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String nonExistentId = "random123";
            APIResponse response = request.delete(TestConfig.getBaseUri() + "/" + nonExistentId);
            Assert.assertEquals(response.status(), 404);
            Assert.assertTrue(response.text().contains("not found"));
        } catch (Exception e) {
            Assert.fail("Exception during testDeleteSkuNonExistent: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteSkuInvalidIdFormat() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String invalidId = "!!!";
            APIResponse response = request.delete(TestConfig.getBaseUri() + "/" + invalidId);
            Assert.assertEquals(response.status(), 400);
            Assert.assertTrue(response.text().contains("Invalid SKU ID"));
        } catch (Exception e) {
            Assert.fail("Exception during testDeleteSkuInvalidIdFormat: " + e.getMessage());
        }
    }

    @Test
    public void testUnsupportedHttpMethodPut() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.put(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 405);
            Assert.assertTrue(response.text().contains("Method Not Allowed"));
        } catch (Exception e) {
            Assert.fail("Exception during testUnsupportedHttpMethodPut: " + e.getMessage());
        }
    }

    @Test
    public void testUnsupportedHttpMethodPatch() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.patch(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 405);
            Assert.assertTrue(response.text().contains("Method Not Allowed"));
        } catch (Exception e) {
            Assert.fail("Exception during testUnsupportedHttpMethodPatch: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSkuWithExtraFields() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"," +
                    "\"extraField\":\"extra\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(response.status(), 200);
            Assert.assertTrue(response.text().contains("berliner"));
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuWithExtraFields: " + e.getMessage());
        }
    }

    @Test
    public void testUnauthorizedAccess() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            // No Authorization header
            APIResponse response = request.get(TestConfig.getBaseUri());
            Assert.assertEquals(response.status(), 401);
            Assert.assertTrue(response.text().contains("Unauthorized"));
        } catch (Exception e) {
            Assert.fail("Exception during testUnauthorizedAccess: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteThenGetOrUpdate() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            // Delete SKU 'deleted'
            request.delete(TestConfig.getBaseUri() + "/deleted");
            // Try GET
            APIResponse getResponse = request.get(TestConfig.getBaseUri() + "/deleted");
            Assert.assertEquals(getResponse.status(), 404);
            // Try POST update
            String body = "{" +
                    "\"sku\":\"deleted\"," +
                    "\"description\":\"desc\"," +
                    "\"price\":\"2.99\"}";
            APIResponse postResponse = request.post(TestConfig.getBaseUri(),
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(body)
            );
            Assert.assertEquals(postResponse.status(), 404);
        } catch (Exception e) {
            Assert.fail("Exception during testDeleteThenGetOrUpdate: " + e.getMessage());
        }
    }
}
