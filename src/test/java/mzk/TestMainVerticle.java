package mzk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.web.VertxWebClientExtension;
import io.vertx.junit5.web.WebClientOptionsInject;
import mzk.model.Product;

@ExtendWith({VertxExtension.class, VertxWebClientExtension.class})
@TestMethodOrder(OrderAnnotation.class)
public class TestMainVerticle {
	
	final static String API_PRODUCTS = "/api/products";
	final static String ADDRESS = "localhost";
	final static int PORT = MainVerticle.HTTP_PORT;
	
	@WebClientOptionsInject
	public WebClientOptions options = new WebClientOptions().setDefaultHost(ADDRESS).setDefaultPort(PORT);

	@BeforeAll
	static void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
			testContext.completeNow();
		}));
	}

	@Test
	@Order(1)
	@DisplayName("Test index.html")
	void checkIndexPage(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		webClient.get("/").as(BodyCodec.string())
				.send(testContext.succeeding(resp -> {
					testContext.verify(() -> {
						assertEquals(200, resp.statusCode());
						assertTrue(resp.body().contains("<title>MZK Product Management</title>"));
						testContext.completeNow();
					});
				}));
	}

	@Test
	@Order(2)
	@DisplayName("Test /api/products GET (List products)")
	void checkApiGetList(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		webClient.get(API_PRODUCTS).as(BodyCodec.string())
				.send(testContext.succeeding(resp -> {
					testContext.verify(() -> {
						assertEquals(200, resp.statusCode());
						testContext.completeNow();
					});
				}));
	}
	
	@Test
	@Order(3)
	@DisplayName("Test /api/products/1 GET (Get one product)")
	void checkApiGetOne(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		int productId = 1;
		webClient.get(API_PRODUCTS + "/" + productId).as(BodyCodec.string())
				.send(testContext.succeeding(resp -> {
					testContext.verify(() -> {
						assertEquals(200, resp.statusCode());
						final Product p = Json.decodeValue(resp.body().toString(), Product.class);
						assertEquals(productId, p.getId());
						testContext.completeNow();
					});
				}));
	}

	@Test
	@Order(4)
	@DisplayName("Test /api/products POST (Add new product)")
	public void checkApiAddProduct(WebClient webClient, Vertx vertx, VertxTestContext testContext) {

		String productName = "Test";
		Long productBarCode = 1L;
		Integer productSerialNumber = 1;

		JsonObject json = new JsonObject()
				.put("name", productName)
				.put("barCode", productBarCode)
				.put("serialNumber", productSerialNumber);

		webClient.post(API_PRODUCTS)

				.putHeader("content-type", "application/json")
				.putHeader("content-length", Integer.toString(json.toString().length()))
				.sendJson(json, testContext.succeeding(resp -> {

					testContext.verify(() -> {

						assertEquals(201, resp.statusCode());
						assertTrue(resp.headers().get("content-type").contains("application/json"));

						final Product p = Json.decodeValue(resp.body().toString(), Product.class);
						assertNotNull(p.getId());
						assertEquals(productName, p.getName());
						assertEquals(productBarCode, p.getBarCode());
						assertEquals(productSerialNumber, p.getSerialNumber());

						testContext.completeNow();
					});
				}));

	}
	
	@Test
	@Order(5)
	@DisplayName("Test /api/products/0 PUT (Update existing product)")
	public void checkApiUpdateProduct(WebClient webClient, Vertx vertx, VertxTestContext testContext) {

		int productId = 0;
		String productName = "New name";
		Long productBarCode = 2L;
		Integer productSerialNumber = 2;

		JsonObject json = new JsonObject()
				.put("name", productName)
				.put("barCode", productBarCode)
				.put("serialNumber", productSerialNumber);

		webClient.put(API_PRODUCTS + "/" + productId)

				.putHeader("content-type", "application/json")
				.putHeader("content-length", Integer.toString(json.toString().length()))
				.sendJson(json, testContext.succeeding(resp -> {

					testContext.verify(() -> {

						assertEquals(201, resp.statusCode());
						assertTrue(resp.headers().get("content-type").contains("application/json"));

						final Product p = Json.decodeValue(resp.body().toString(), Product.class);
						assertEquals(productId, p.getId());
						assertEquals(productName, p.getName());
						assertEquals(productBarCode, p.getBarCode());
						assertEquals(productSerialNumber, p.getSerialNumber());

						testContext.completeNow();
					});
				}));

	}
	
	@Test
	@Order(6)
	@DisplayName("Test /api/products/0 DELETE (Remove existing product)")
	public void checkApiDeleteProduct(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		
		int productId = 0;
		webClient.delete(API_PRODUCTS + "/" + productId)

		.send(testContext.succeeding(resp -> {

			testContext.verify(() -> {
				assertEquals(204, resp.statusCode());
				testContext.completeNow();
			});
		}));

	}

}