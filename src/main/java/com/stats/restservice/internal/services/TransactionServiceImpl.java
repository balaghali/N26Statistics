package com.stats.restservice.internal.services;

import java.util.Optional;
import java.util.function.Supplier;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.transaction.ITransaction;
import com.stats.restservice.transaction.TransactionFactory;
import com.stats.restservice.utils.CustomDateTimeUtils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Transaction service to process the received transactions 
 */
public class TransactionServiceImpl implements ITransactionService{
	
	private IStatisticsService mStatisticsService;

	public TransactionServiceImpl(IStatisticsService statService) {
		mStatisticsService = statService;
	}
	
	 private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

		@Override
		public Optional<Boolean> unMarshallTransactionData(Supplier<JsonObject> jsonData) {
			  ITransaction transaction = getTransactionFromRequestBody(jsonData);
			if (transaction  == null ||
	    	            transaction.getAmount() == null ||
	    	            transaction.getTimestamp() == null ||
	    	            !CustomDateTimeUtils.isTransactionOlderThanMinute(transaction.getTimestamp())) {
	    		  
	    		  logger.debug("Discard received transaction , older / invalid values received in json => "+ transaction);
	    	            return Optional.empty();
	    	      }
	    	
	        logger.info("Received new transaction => "+ transaction);
	        mStatisticsService.computeTransaction(transaction);
	        return Optional.of(true);
		}
		
		private ITransaction getTransactionFromRequestBody(Supplier<JsonObject> jsonBody) {
			ITransaction transaction = null;
			try {
				JsonObject jsonData = jsonBody.get();
				logger.debug("Received jsonData to be unmarshalled" +jsonData);
				Double amount = Double.parseDouble(String.valueOf(jsonData.getValue("amount")));
				Long timestamp = Long.parseLong(String.valueOf(jsonData.getValue("timestamp")));
				transaction = TransactionFactory.getTransaction(amount , timestamp);
			} catch (Exception e) {
				logger.error("Encountered exception while parsing transaction data" +e.getMessage());
				logger.debug(e);
			}
			return transaction;
		}
}
