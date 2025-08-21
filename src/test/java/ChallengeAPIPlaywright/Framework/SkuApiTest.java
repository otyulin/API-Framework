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
        // Mock POST (Create/Update)
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/skus"))
            .willReturn(WireMock.aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}")));
        // Mock GET all
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/skus"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}]") ));
        // Mock GET by id
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/skus/.*"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"sku\":\"berliner\",\"description\":\"Jelly donut\",\"price\":\"2.99\"}")));
        // Mock DELETE
        wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/api/skus/.*"))
            .willReturn(WireMock.aResponse().withStatus(204)));
    }

    @Test
    public void testCreateSku() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String body = "{" +
                    "\"sku\":\"berliner\"," +
                    "\"description\":\"Jelly donut\"," +
                    "\"price\":\"2.99\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
                //new APIRequestContext.RequestOptions()
            		                RequestOptions.create()
                    //.setHeaders(headers)
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
/*
    @Test
    public void testCreateSkuWithInvalidPayload() {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext request = playwright.request().newContext();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String body = "{" +
                    "\"sku\":\"\"," +
                    "\"description\":\"\"," +
                    "\"price\":\"\"}";
            APIResponse response = request.post(TestConfig.getBaseUri(),
               // new APIRequestContext.RequestOptions()
            		RequestOptions.create()
                    //.setHeaders(headers)
                    .setData(body)
            );
            Assert.assertTrue(response.status() >= 400, "Expected client error for invalid payload");
        } catch (Exception e) {
            Assert.fail("Exception during testCreateSkuWithInvalidPayload: " + e.getMessage());
        }
            }
            */
}