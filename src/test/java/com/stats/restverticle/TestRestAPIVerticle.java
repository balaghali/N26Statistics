package com.stats.restverticle;

import static com.stats.restverticle.RestAPIVerticle.ADD_TRANSACTION_END_POINT;
import static com.stats.restverticle.RestAPIVerticle.APPLICATION_JSON_CHARSET_UTF_8;
import static com.stats.restverticle.RestAPIVerticle.GET_STATS_END_POINT;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.internal.services.StatisticsServiceImpl;
import com.stats.restservice.internal.services.TransactionServiceImpl;
import com.stats.restservice.transaction.ITransaction;
import com.stats.restservice.transaction.TransactionFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


@RunWith(VertxUnitRunner.class)
public class TestRestAPIVerticle {
    private Vertx vertx;
    private int port = 8081;
    IStatisticsService mStatisticsService = null;
    ITransactionService mTransactionService = null;
	
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        // Pick an available and random
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("HTTP_PORT", port));
        
        // prepare mocks and spied objects
        IStatisticsService statisticsService = new StatisticsServiceImpl();
        ITransactionService transactionService = new TransactionServiceImpl(statisticsService);
    	
        mStatisticsService = Mockito.spy(statisticsService);
        mTransactionService = Mockito.spy(transactionService);
        
		vertx.deployVerticle(new RestAPIVerticle(transactionService, statisticsService ), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Given 
     * When
     * Then
     * @param context
     */
    @Test
    public void testRestVerticleDefaultPath(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/",
            response ->
                response.handler(body -> {
                    context.assertTrue(body.toString().contains("Welcome"));
                    async.complete();
                }));
    }
    
    /**
     * Given a json input request with valid inputs (timestamp < 60 seconds)
     * When a rest request to add a new transaction is perfomred
     * Then Asser the content type , response status for success scenario - 201
     * @param context
     */
    @Test
    public void checkThatWeCanAddTransaction(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(TransactionFactory.getTransaction(234.2, new Date().getTime()));
        vertx.createHttpClient().post(port, "localhost", ADD_TRANSACTION_END_POINT)
            .putHeader("Content-Type", "application/json")
            .putHeader("Content-Length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 201);
                context.assertTrue(response.headers().get("content-type").equals(APPLICATION_JSON_CHARSET_UTF_8));
                response.bodyHandler(body -> {
                	context.assertEquals(body.toString(), "");
                    async.complete();
                });
            })
            .write(json)
            .end();
    }
    
    
    /**
     * Given a json input request with invalid inputs (timestamp older than 60 seconds)
     * When a rest request to add a new transaction is perfomred
     * Then Assert the content type , response status for invalid timestamp scenario - 204
     * @param context
     */
    @Test
    public void checkThatWeCannotAddTransaction(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(TransactionFactory.getTransaction(234.2, /*new Date("07/13/2018 11:06:37 AM").getTime()*/ System.currentTimeMillis() - 70000l));
        vertx.createHttpClient().post(port, "localhost", ADD_TRANSACTION_END_POINT)
            .putHeader("Content-Type", "application/json")
            .putHeader("Content-Length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 204);
                context.assertTrue(response.headers().get("content-type").equals(APPLICATION_JSON_CHARSET_UTF_8));
                response.bodyHandler(body -> {
                	 context.assertEquals(body.toString(), "");
                    async.complete();
                });
            })
            .write(json)
            .end();
    }
    
    
    /**
     * Given a json input request with invalid inputs (timestamp older than 60 seconds)
     * When a rest request to add a new transaction is perfomred
     * Then Assert the content type , response status for success scenario
     * @param context
     */
    @Test
    public void checkThatWeCanGetStatistics(TestContext context) {
        Async async = context.async();
        DoubleSummaryStatistics stats = createTestTransactions(context , vertx.createHttpClient() , 5);
        vertx.createHttpClient().get(port, "localhost", GET_STATS_END_POINT)
            .handler(response -> {
                context.assertEquals(response.statusCode(), 200);
                context.assertTrue(response.headers().get("content-type").equals(APPLICATION_JSON_CHARSET_UTF_8));
                response.bodyHandler(body -> {
                    JsonObject receivedStats = new JsonObject(body.toString());
                    context.assertEquals(receivedStats.getValue("sum") , stats.getSum());
                    context.assertEquals((Double)receivedStats.getValue("avg") , stats.getAverage() );
                    context.assertEquals(receivedStats.getValue("min") , stats.getMin());
                    context.assertEquals(receivedStats.getValue("max") , stats.getMax());
                    context.assertEquals(String.valueOf(receivedStats.getValue("count")) , String.valueOf(stats.getCount()));
                    
                    async.complete();
                });
            }).end();
    }

	private DoubleSummaryStatistics createTestTransactions(TestContext context , HttpClient createHttpClient, int numberOfTransactions) {
        List<ITransaction> transactionList = 
		IntStream.rangeClosed(1, numberOfTransactions).mapToObj(e -> {
			ITransaction transaction = TransactionFactory.getTransaction(234.2+e, new Date().getTime());
			final String json = Json.encodePrettily(transaction);
			createTransaction(context, json);
			return transaction;
		}).collect(Collectors.toList());
				
		return transactionList.stream().mapToDouble(e -> e.getAmount()).summaryStatistics();
	}

	private void createTransaction(TestContext context, final String json) {
		vertx.createHttpClient().post(port, "localhost", ADD_TRANSACTION_END_POINT)
        .putHeader("Content-Type", "application/json")
        .putHeader("Content-Length", Integer.toString(json.length()))
        .handler(response -> {
            context.assertEquals(response.statusCode(), 201);
            response.bodyHandler(body -> {
            	 context.assertEquals(body.toString(), "");
            	 context.async().complete();
            });
        })
        .write(json)
        .end();
		
		// Sleep for 5 ms to create new transaction timestamp
		try {
			Thread.sleep(75);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}