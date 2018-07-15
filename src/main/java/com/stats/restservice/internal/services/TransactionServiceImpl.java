package com.stats.restservice.internal.services;

import java.util.Optional;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.transaction.ITransaction;
import com.stats.restservice.utils.CustomDateTimeUtils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class TransactionServiceImpl implements ITransactionService{
	
	
	private IStatisticsService mStatisticsService;

	public TransactionServiceImpl(IStatisticsService statService) {
		mStatisticsService = statService;
	}
	
	 private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

	    @Override
	    public Optional<Boolean> processTransaction(ITransaction transaction) {
	    	  if (transaction == null ||
	    	            transaction.getAmount() == null ||
	    	            transaction.getTimestamp() == null ||
	    	            !CustomDateTimeUtils.isTransactionOlderThanMinute(transaction.getTimestamp())) {

	    	            return Optional.empty();
	    	        }
	    	
	        logger.info("Received new transaction => "+ transaction);
	        mStatisticsService.computeTransaction(transaction);
	        return Optional.of(true);
	    }
}
