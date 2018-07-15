package com.stats.restservice.internal.services;

import static com.stats.restservice.internal.services.StatisticsServiceImpl.AVERAGE;
import static com.stats.restservice.internal.services.StatisticsServiceImpl.COUNT;
import static com.stats.restservice.internal.services.StatisticsServiceImpl.MAX;
import static com.stats.restservice.internal.services.StatisticsServiceImpl.MIN;
import static com.stats.restservice.internal.services.StatisticsServiceImpl.SUM;
import static org.junit.Assert.assertEquals;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.transaction.ITransaction;
import com.stats.restservice.transaction.TransactionFactory;

/**
 * Test class to ascertain the functionality of StatisticsServiceImpl 
 */
public class TestStatisticsService {
	
	/**
	 * Given - StatisticsService object and some valid transaction , older transactions 
	 * When  - predicate to remove older transactions is executed 
	 * Then  - Assert the predicate test result is appropriate 
	 */
    @Test
    public void checkPredicateOlderThanMinuteFunctionality(){
    	CustomStatisticsServiceImpl statisticsService = new CustomStatisticsServiceImpl();		
		// Create a test data of 100 transactions that are not older than 60 seconds
		List<ITransaction> validTransactions = createAndGetValidTestTransactions(statisticsService , 100);

		// Create a test data of 100 transactions that are older than 60 seconds
		List<ITransaction> olderTransactions = createAndGetInValidTestTransactions(statisticsService , 100);
		
		Predicate<ITransaction> predicate = statisticsService.isOlderThanOneMinute();
		validTransactions.forEach(e -> {
			Assert.assertFalse(predicate.test(e));
		});
		
		olderTransactions.forEach(e -> {
			Assert.assertTrue(predicate.test(e));
		});
    }

	/**
		 * Given - Statistics Service spied object, set of 2 transactions 
		 * When  - getStatistics is executed
		 * Then -  Assert that getAmount method is called twice
		 */
		  @Test
		  public void whengetStatisticsexecuted_assertGetAmountCall(){
			  CustomStatisticsServiceImpl statisticsServiceOriginal = new CustomStatisticsServiceImpl();
			  CustomStatisticsServiceImpl spiedStatisticsService = Mockito.spy(statisticsServiceOriginal);
		        spiedStatisticsService.computeTransaction((getTransaction(10.5, System.currentTimeMillis() - 50000)));
		        spiedStatisticsService.computeTransaction((getTransaction(12.5, System.currentTimeMillis() - 50000)));
		        spiedStatisticsService.getStatistics();
				
		        Mockito.verify(spiedStatisticsService, Mockito.times(2)).getAmount(Mockito.any(ITransaction.class));
		        
		  }
	  
		/**
		 * Given - StatisticsService object and some valid transaction 
		 * When  - getStatistics is executed
		 * Then  - Assert the validity of the statistics computed by the service
		 */
	    @Test
	    public void whenValidTimestamp_computeTransaction(){
			IStatisticsService statisticsService = new StatisticsServiceImpl();
	        statisticsService.computeTransaction(getTransaction(5.5, System.currentTimeMillis() - 10000));
	        statisticsService.computeTransaction(getTransaction(15.5, System.currentTimeMillis() - 9000));
	        statisticsService.computeTransaction(getTransaction(25.2, System.currentTimeMillis() - 8000));
	        statisticsService.computeTransaction(getTransaction(65.5, System.currentTimeMillis() - 7000));
	        statisticsService.computeTransaction(getTransaction(5.7, System.currentTimeMillis() - 6000));
	        statisticsService.computeTransaction(getTransaction(5.8, System.currentTimeMillis() - 5000));
	        statisticsService.computeTransaction(getTransaction(3.5, System.currentTimeMillis() - 4000));
	        statisticsService.computeTransaction(getTransaction(2.8, System.currentTimeMillis() - 3000));
	        statisticsService.computeTransaction(getTransaction(9.5, System.currentTimeMillis() - 2000));
	        statisticsService.computeTransaction(getTransaction(12.3, System.currentTimeMillis() - 1000));

	        Map<String, Number> summary = statisticsService.getStatistics();
	        assertEquals((Double)summary.get(SUM), 151.3 , 0.1);
	        assertEquals((Double)summary.get(AVERAGE), 15.13 , 0.1 );
	        assertEquals((Double)summary.get(MIN), 2.8 , 0.1);
	        assertEquals(summary.get(COUNT), 10l);
	        assertEquals((Double)summary.get(MAX), 65.5 , 0.1);
	    }
	    
	    
	    /**
		 * Given - StatisticsService object and some valid transaction , older transactions 
		 * When  - removeStaleEntries is executed
		 * Then  - Assert that only older entries which are stale (> 60 seconds) were pruned
		 */
	    @Test
	    public void checkRemoveStaleTransactionsFunctionality(){
			IStatisticsService statisticsService = new StatisticsServiceImpl();
			
			// Create a test data of 100 transactions that are not older than 60 seconds
			DoubleSummaryStatistics expectedStats = createTestTransactions(statisticsService , 100);
			
			// Call to remove stale Transactions should delete older entries - 60 sec
			statisticsService.removeStaleTransactions();

	        Map<String, Number> summary = statisticsService.getStatistics();
	        assertEquals((Double)summary.get(SUM), expectedStats.getSum() , 0.0);
	        assertEquals((Double)summary.get(AVERAGE), expectedStats.getAverage()  , 0.0);
	        assertEquals(summary.get(MIN), expectedStats.getMin());
	        assertEquals(summary.get(MAX), expectedStats.getMax());
	        assertEquals(summary.get(COUNT), expectedStats.getCount());
	        
	    }
	    
	    private ITransaction getTransaction(Double doubleValue, Long time) {
			return TransactionFactory.getTransaction(doubleValue, time);
		}
	    
	    private DoubleSummaryStatistics createTestTransactions(IStatisticsService statisticsService ,int numberOfTransactions) {
	    	
	        List<ITransaction> validTransactionList = createAndGetValidTestTransactions(statisticsService , numberOfTransactions);
			
	        createAndGetInValidTestTransactions(statisticsService , numberOfTransactions);
	        
			return validTransactionList.stream().mapToDouble(e -> e.getAmount()).summaryStatistics();
		}
	    
	    /**
	     * Transactions that are not older than 60 seconds
	     * @param statisticsService
	     * @param numberOfTransactions
	     * @return
	     */
	    private List<ITransaction> createAndGetValidTestTransactions(IStatisticsService statisticsService, int numberOfTransactions) {
			return IntStream.rangeClosed(1, numberOfTransactions).mapToObj(e -> {
				ITransaction transaction = getTransaction(234.2+e, System.currentTimeMillis() - (20000+e));
				statisticsService.computeTransaction(transaction);
				return transaction;
			}).collect(Collectors.toList());
		}
	    
	    /**
	     * Transactions that are older than 60 seconds
	     * @param statisticsService
	     * @param numberOfTransactions
	     * @return
	     */
	    private List<ITransaction> createAndGetInValidTestTransactions(IStatisticsService statisticsService, int numberOfTransactions) {
			return IntStream.rangeClosed(1, numberOfTransactions).mapToObj(e -> {
				ITransaction transaction = getTransaction(234.2+e, System.currentTimeMillis() - (70000+e));
				statisticsService.computeTransaction(transaction);
				return transaction;
			}).collect(Collectors.toList());
		}

	    /**
	     * Custom class to spy , assert the mocked stub methods (like verify validation etc) 
	     */
	    private static class CustomStatisticsServiceImpl extends StatisticsServiceImpl{
	    	
	    }
}
