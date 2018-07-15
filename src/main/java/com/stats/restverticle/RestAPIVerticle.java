package com.stats.restverticle;

import java.util.Optional;
import java.util.function.Supplier;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestAPIVerticle extends AbstractVerticle {  
	
	static final String ADD_TRANSACTION_END_POINT = "/transactions";
	static final String GET_STATS_END_POINT = "/statistics";
	static final String CONTENT_TYPE = "content-type";
	static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
	private ITransactionService mTransactionService ;
	private IStatisticsService mStatistcsService;
	 private static final Logger logger = LoggerFactory.getLogger(RestAPIVerticle.class);

    
	public RestAPIVerticle(ITransactionService transactionService , IStatisticsService statisticsService) {
		mTransactionService = transactionService;
		mStatistcsService = statisticsService;
	}

	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the verticle) 
	 */
	@Override
	public void start(Future<Void> fut) {
	
	    // Create a router object.
	    Router router = Router.router(vertx);
	
	    // Bind "/" to our hello message - so we are still compatible.
	    router.route("/").handler(routingContext -> {
	        HttpServerResponse response = routingContext.response();
	        response
	            .putHeader(CONTENT_TYPE, "text/html")
	            .end("<h1>Welcome to statistics application</h1>");
	    });
	    
	    //Bind body handler to fetch the json body of post requests
	    router.route().handler(BodyHandler.create());
	
	    // Bind respective end points 
	    router.get(GET_STATS_END_POINT).handler(this::getStats);
	    router.post(ADD_TRANSACTION_END_POINT).handler(this::addTransaction);
	
	    ConfigRetriever retriever = ConfigRetriever.create(vertx);
	    retriever.getConfig(
	        config -> {
	            if (config.failed()) {
	                fut.fail(config.cause());
	            } else {
	                // Create the HTTP server and pass the "accept" method to the request handler.
	                vertx
	                    .createHttpServer()
	                    .requestHandler(router::accept)
	                    .listen(
	                        // Retrieve the port from the configuration,
	                        // default to 8080.
	                        config.result().getInteger("HTTP_PORT", 8080),
	                        result -> {
	                            if (result.succeeded()) {
	                                fut.complete();
	                            } else {
	                                fut.fail(result.cause());
	                            }
	                        }
	                    );
	            }
	        }
	    );
	}
	
	/**
	 * Blocking call to fetch the statistics in case if there is humungous input traffic, avoid blocking the event loop thread
	 * @param routingContext
	 */
	private void getStats(RoutingContext routingContext) {
		vertx.executeBlocking(future -> {
		    future.complete(mStatistcsService.getStatistics());
		}, res -> {
		    if (res.succeeded()) {
		    	Object result = res.result();
				routingContext.response()
		        .putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
		        .end(Json.encodePrettily(result));
		    } else {
		    	logger.info("Future completion of Get request encountered error");
		    	routingContext.response()
		        .putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
		        .setStatusCode(500)
		        .end();
		    }
		});
	}
	
	private void addTransaction(RoutingContext routingContext) {
		Supplier<JsonObject> jsonData =  () -> routingContext.getBodyAsJson();
	    Optional<Boolean> status = mTransactionService.unMarshallTransactionData(jsonData);
			if (status.isPresent()) {
				sendResponeWithStatus(routingContext,201); 
			} else {
				sendResponeWithStatus(routingContext,204); 
			}
	}

	private void sendResponeWithStatus(RoutingContext routingContext , int statusCode) {
		routingContext.response().setStatusCode(statusCode).putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
				.end();
	}

}
