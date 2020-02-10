package mzk.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import mzk.model.Product;

public class ProductService {
	
	// TODO Para um melhor controle sobre o estoque e posterior geração de relatórios o ideal é que os produtos
	// vendidos não sejam excluídos do banco de dados através do método DELETE, mas sim marcados como tal através do
	// campo 'sold' na entidade 'Product' e/ou então que seu ID seja incluído na lista de items de um pedido (Order)
	// para que não seja mais considerado como disponível.

	private Map<Integer, Product> products = new LinkedHashMap<>();

	public ProductService(Router router) {
		super();
		createMockData();
		
		router.get("/api/products").handler(this::getAll);
	    router.route("/api/products*").handler(BodyHandler.create());
	    router.post("/api/products").handler(this::add);
	    router.get("/api/products/:id").handler(this::get);
	    router.put("/api/products/:id").handler(this::update);
	    router.delete("/api/products/:id").handler(this::delete);
	}

	public void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(products.values()));
	}
	
	public void get(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Product p = products.get(idAsInteger);
			if (p == null) {
				routingContext.response().setStatusCode(404).end("Product not found");
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(p));
			}
		}
	}

	public void add(RoutingContext routingContext) {
		final Product newProduct = Json.decodeValue(routingContext.getBodyAsString(), Product.class);

		if(newProduct.getName() == null || newProduct.getName() == "") {
			routingContext.response().setStatusCode(400).end("Field 'name' is required");
		} else if(newProduct.getBarCode() == null) {
			routingContext.response().setStatusCode(400).end("Field 'barCode' is required");
		} else if(newProduct.getSerialNumber() == null) {
			routingContext.response().setStatusCode(400).end("Field 'serialNumber' is required");
		} else {

			// Check if new product already exists on products list (same barcode and serial number)
			List<Product> existingProducts = products.values().stream().filter(p ->
						p.getBarCode().equals(newProduct.getBarCode()) &&
						p.getSerialNumber().equals(newProduct.getSerialNumber())
					).collect(Collectors.toList());
			
			if(existingProducts.size() > 0) {
				routingContext.response().setStatusCode(400).end("Duplicated products not allowed. There's already a product with the same BarCode and SerialNumber");
			} else {
				products.put(newProduct.getId(), newProduct);
				routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(newProduct));	
			}
						
		}

	}
	
	public void update(RoutingContext routingContext) {
		
		// We don't map to 'Product' class here to avoid that the id counter gets incremented. So instead we use a regular JsonObject	
		JsonObject updateData = routingContext.getBodyAsJson();
		
		String updateName = updateData.getString("name");
		Long updateBarCode = updateData.getLong("barCode");
		Integer updateSerialNumber = updateData.getInteger("serialNumber");
		boolean updateSold = updateData.getBoolean("sold");
		
		final String id = routingContext.request().getParam("id");
		if (id == null || updateData == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Product p = products.get(idAsInteger);
			if (p == null) {
				routingContext.response().setStatusCode(404).end();
			} else {

				if(updateName == null || updateName == "") {
					routingContext.response().setStatusCode(400).end("Field 'name' is required");
				} else if(updateBarCode == null) {
					routingContext.response().setStatusCode(400).end("Field 'barCode' is required");
				} else if(updateSerialNumber == null) {
					routingContext.response().setStatusCode(400).end("Field 'serialNumber' is required");
				} else {

					// Check if another product (different id) with same barcode and serial number already exists on products list
					List<Product> existingProducts = products.values().stream().filter(item ->
								item.getId() != idAsInteger &&
								item.getBarCode().equals(updateBarCode) &&
								item.getSerialNumber().equals(updateSerialNumber)
							).collect(Collectors.toList());
								
					if(existingProducts.size() > 0) {
						routingContext.response().setStatusCode(400).end("Duplicated products not allowed. There's already a product with the same BarCode and SerialNumber");
					} else {
						p.setName(updateName);
						p.setBarCode(updateBarCode);
						p.setSerialNumber(updateSerialNumber);
						p.setSold(updateSold);
						routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(p));	
					}
								
				}
				
			}
		}
		
	}

	public void delete(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}

	private void createMockData() {
		addProduct("Camisa Polo Azul Marinho", 7898392930332L, 1, false);
		addProduct("Camisa Polo Azul Marinho", 7898392930332L, 2, false);
		addProduct("Camisa Polo Azul Marinho", 7898392930332L, 3, false);
		addProduct("Regata Masculina B-01 MXD", 5280001427920L, 1, false);
		addProduct("Bermuda Sarja Slim", 1466571884344L, 1, false);
	}

	private void addProduct(String name, Long barCode, int serialNumber, boolean sold) {
		Product p = new Product(name, barCode, serialNumber, sold);
		products.put(p.getId(), p);
	}
	
}
