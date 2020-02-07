package mzk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import mzk.service.ProductService;

public class MainVerticle extends AbstractVerticle {

	public static int HTTP_PORT = 8080;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		Router router = Router.router(vertx);
		
		StaticHandler assetsHandler = StaticHandler.create("assets");
		router.route("/").handler(assetsHandler);
		router.route("/assets/*").handler(assetsHandler);

		new ProductService(router);

		vertx.createHttpServer().requestHandler(router).listen(HTTP_PORT, http -> {
			if (http.succeeded()) {
				startPromise.complete();
				System.out.println("HTTP server started on port " + HTTP_PORT);
			} else {
				startPromise.fail(http.cause());
			}
		});

	}

}
