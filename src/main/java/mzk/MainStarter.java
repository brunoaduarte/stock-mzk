package mzk;

import io.vertx.core.Vertx;

// This class was created for easy execution in Eclipse IDE, but there are alternatives
// https://stackoverflow.com/questions/24277301/run-vertx-in-an-ide

public class MainStarter {

	public static void main(String[] args) throws Exception {
		Vertx.vertx().deployVerticle(new MainVerticle());
	}

}
