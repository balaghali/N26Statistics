package com.stats.restservice.external.services;

import java.util.Map;

import com.stats.restservice.transaction.ITransaction;

/**
 *  Interface exposed to outside world to fetch the Statistics and to cleanse the older transactions
 */
public interface IStatisticsService {

	Map<String, Number> getStatistics();

	void computeTransaction(ITransaction transaction);

	void removeStaleTransactions();
}
