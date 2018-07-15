package com.stats.restservice.transaction;

import org.junit.Assert;
import org.junit.Test;

public class TestTransactionFactory {

	@Test
	public void whenValidInputs_returnTransaction() {
		Long dateTimeStamp = 1234567l;
		Double amount = 2.5;
		ITransaction transaction = TransactionFactory.getTransaction(amount , dateTimeStamp);
		
		Assert.assertNotNull(transaction);
		
		Assert.assertEquals(amount , transaction.getAmount());
		Assert.assertEquals(dateTimeStamp , transaction.getTimestamp());
	}
	
}
