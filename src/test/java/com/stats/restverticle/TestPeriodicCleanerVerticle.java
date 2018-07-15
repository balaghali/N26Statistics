package com.stats.restverticle;

import static com.stats.restverticle.RestAPIVerticle.APPLICATION_JSON_CHARSET_UTF_8;
import static com.stats.restverticle.RestAPIVerticle.GET_STATS_END_POINT;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.internal.services.StatisticsServiceImpl;
import com.stats.restservice.transaction.ITransaction;
import com.stats.restservice.transaction.TransactionFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test class to test PeriodicCleanerVertice - test to create transactions that are older and not older than 60 seconds ,
 * Assert that cleaner Daemon is pruning the entries as they become older than 60 seconds 
 */
@RunWith(VertxUnitRunner.class)
public class TestPeriodicCleanerVerticle {
	
	/**
	 * Sleep delay for the junit thread in conjunction with delay of cleaner verticle  
	 */
    private static final int SLEEP_DELAY = 3000;
    /**
     * Delay to be specified for cleaner daemon
     */
    private static final int delay = 500;
	private Vertx vertx;
    private int port = 8081;
    
    IStatisticsService mStatisticsService = null;
	private long mTimerId;
	
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
    	
        mStatisticsService = Mockito.spy(statisticsService);
        
		PeriodicCleanerVerticle verticle = new PeriodicCleanerVerticle(mStatisticsService , delay );
		mTimerId = verticle.getPeriodicVerticleId();
		vertx.deployVerticle(new RestAPIVerticle(Mockito.mock(ITransactionService.class), statisticsService ), options, context.asyncAssertSuccess());
		vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Given a json input request with invalid inputs (timestamp older than 60 seconds)
     * When a rest request to add a new transaction is perfomred
     * Then Assert the content type , response status for success scenario
     * @param context
     * @throws InterruptedException 
     */
    @Test
    public void checkThatPeriodicVerticleCleansStaleTransactions(TestContext context) throws InterruptedException {
        Async async = context.async();
        //Create stale transaction older than 60 seconds , good transactions < 60 sec (transactions with 59 seconds older)
        createTestTransactions(100);
        //Sleep for some time to get past the 60 second interval for transactions
        Thread.sleep(SLEEP_DELAY);
        
        Mockito.verify(mStatisticsService , Mockito.atLeastOnce()).removeStaleTransactions();
        vertx.cancelTimer(mTimerId);
        
        vertx.createHttpClient().get(port, "localhost", GET_STATS_END_POINT)
        .handler(response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertTrue(response.headers().get("content-type").equals(APPLICATION_JSON_CHARSET_UTF_8));
            response.bodyHandler(body -> {
            	//Assert that received stats after cleanup of entire transactions should be 0
                    JsonObject receivedStats = new JsonObject(body.toString());
                    context.assertEquals(receivedStats.getValue("sum") , 0.0);
                    context.assertEquals((Double)receivedStats.getValue("avg") , 0.0 );
                    context.assertNotNull(receivedStats.getValue("min") );
                    context.assertNotNull(receivedStats.getValue("max") );
                    context.assertEquals(String.valueOf(receivedStats.getValue("count")) , "0");
                
                async.complete();
            });
        }).end();
        
    }

	private void createTestTransactions(int numberOfTransactions) {
        //Create valid transactions , transactions that are not older than 60 seconds
		IntStream.rangeClosed(1, numberOfTransactions).forEach(e -> {
			ITransaction transaction = TransactionFactory.getTransaction(234.2+e, new Date().getTime()-59000l);
			mStatisticsService.computeTransaction(transaction);
			sleep();
		});
        
		//Create stale transactions , transactions that are  older than 60 seconds
        IntStream.rangeClosed(1, numberOfTransactions).forEach(e -> {
			ITransaction transaction = TransactionFactory.getTransaction(234.2+e, new Date().getTime() - 60001l);
			mStatisticsService.computeTransaction(transaction);
			sleep();
		});
				
	}

	private void sleep() {
		// Sleep for 5 ms to create new transaction timestamp
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}