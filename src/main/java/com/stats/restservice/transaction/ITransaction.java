package com.stats.restservice.transaction;

public interface ITransaction {
	
	/*enum TransactionType {
		BASIC,ADVANCED;
	}*/

	Double getAmount();

	Long getTimestamp();

}
