package com.stats.restservice.transaction;

import org.junit.Assert;
import org.junit.Test;

public class TestTransaction {
	
	@Test
	public void whenSameInputs_assertEquality() {
		Long dateTimeStamp = 1234567l;
		Double amount = 2.5;
		ITransaction transaction1 = TransactionFactory.getTransaction(amount , dateTimeStamp);
		ITransaction transaction2 = TransactionFactory.getTransaction(amount , dateTimeStamp);

		Assert.assertEquals(transaction1.hashCode(), transaction2.hashCode());
		Assert.assertTrue(transaction1.equals(transaction2));
		Assert.assertEquals(transaction1 , transaction2);
	}
	
	
	@Test
	public void whenDifferentInputs_assertEquality() {
		Long dateTimeStamp = 1234567l;
		Double amount = 2.5;
		ITransaction transaction1 = TransactionFactory.getTransaction(amount , dateTimeStamp);
		ITransaction transaction2 = TransactionFactory.getTransaction(2.6 , 1456895l);
		
		Assert.assertFalse(transaction1.hashCode() ==  transaction2.hashCode());
		Assert.assertFalse(transaction1.equals(transaction2));
		Assert.assertNotEquals(transaction1 , transaction2);
		
	}

}
