package com.stats.restservice.transaction;

/**
 * Factory class to instantiate transactions , can be extended with enum based on  transaction type
 */
public class TransactionFactory {
	
	public static ITransaction getTransaction(Double amount , Long dateTimeStamp) {
		ITransaction transaction = new Transaction(amount , dateTimeStamp);
		return transaction;
	}
}
