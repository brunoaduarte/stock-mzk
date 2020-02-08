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
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import mzk.model.Product;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class TestMainVerticle {
	
	final static String API_PRODUCTS = "/api/products";
	final static String ADDRESS = "localhost";
	final static int PORT = MainVerticle.HTTP_PORT;
	private static WebClient webClient;

	@BeforeAll
	static void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
			webClient = WebClient.create(vertx);
			testContext.completeNow();
		}));
	}

	@Test
	@Order(1)
	@DisplayName("Test index.html")
	void checkIndexPage(Vertx vertx, VertxTestContext testContext) {
		webClient.get(PORT, ADDRESS, "/").as(BodyCodec.string())
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
	void checkApiGetList(Vertx vertx, VertxTestContext testContext) {
		webClient.get(PORT, ADDRESS, API_PRODUCTS).as(BodyCodec.string())
				.send(testContext.succeeding(resp -> {
					testContext.verify(() -> {
						assertEquals(200, resp.statusCode());
						testContext.completeNow();
					});
				}));
	}
	
	@Test
	@Order(3)
	@DisplayName("Test /api/products GET (Get one product)")
	void checkApiGetOne(Vertx vertx, VertxTestContext testContext) {
		int productId = 1;
		webClient.get(PORT, ADDRESS, API_PRODUCTS + "/" + productId).as(BodyCodec.string())
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
	public void checkApiAddProduct(Vertx vertx, VertxTestContext testContext) {

		String productName = "Test";
		Long productBarCode = 1L;
		Integer productSerialNumber = 1;

		JsonObject json = new JsonObject()
				.put("name", productName)
				.put("barCode", productBarCode)
				.put("serialNumber", productSerialNumber);

		webClient.post(PORT, ADDRESS, API_PRODUCTS)

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
	@DisplayName("Test /api/products PUT (Update existing product)")
	public void checkApiUpdateProduct(Vertx vertx, VertxTestContext testContext) {

		int productId = 0;
		String productName = "New name";
		Long productBarCode = 2L;
		Integer productSerialNumber = 2;

		JsonObject json = new JsonObject()
				.put("name", productName)
				.put("barCode", productBarCode)
				.put("serialNumber", productSerialNumber);

		webClient.put(PORT, ADDRESS, API_PRODUCTS + "/" + productId)

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
	@DisplayName("Test /api/products DELETE (Remove existing product)")
	public void checkApiDeleteProduct(Vertx vertx, VertxTestContext testContext) {
		
		int productId = 0;
		webClient.delete(PORT, ADDRESS, API_PRODUCTS + "/" + productId)

		.send(testContext.succeeding(resp -> {

			testContext.verify(() -> {
				assertEquals(204, resp.statusCode());
				testContext.completeNow();
			});
		}));

	}

}