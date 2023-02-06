package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	Account getAccount(String accountId);

	public void updateAccount(Account account);

	void clearAccounts();
}