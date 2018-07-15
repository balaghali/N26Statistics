package com.stats.restservice.external.services;

import java.util.Optional;

import com.stats.restservice.transaction.ITransaction;

/**
 * 
 *
 */
public interface ITransactionService {

	Optional<Boolean> processTransaction(ITransaction transaction);

}
