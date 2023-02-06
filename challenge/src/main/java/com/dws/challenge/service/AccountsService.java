package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidTransferAmount;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

/**
 * @author shaan
 *
 */
@Service
public class AccountsService {

	public static final String INSUFFICIENT_BAL = "Insufficient balance";
	public static final String SAME_WITHDRAW_DEPOSIT_ACC = "Withdraw/Deposit account cannot be same";
	public static final String UNREGISTERED_WITHDRAW_DEPOSIT_ACC = "from/to Account not registered";
	public static final String INVALID_TRANSFER_AMOUNT = "Transfer amount should be greater than 0";
	public static final String NULL_EMPTY_ACCOUNT = "from/to account can not be null or empty";

	@Getter
	private final AccountsRepository accountsRepository;
	private NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * 
	 * This method is responsible to transfer the funds from source to destination account 
	 * in a thread safe manner. 
	 * In real time scenario this method should be under Transactional 
	 * boundaries to ensure data consistency with proper Transactional Propagation and Isolation level
	 * 
	 */
	public ResponseEntity<Object> fundTransfer(String fromAccount, String toAccount, BigDecimal amount) {

		validateAccount(fromAccount, toAccount);
		validateTransferAmount(amount);

		Account sourceAccount = accountsRepository.getAccount(fromAccount);
		Account destinationAccount = accountsRepository.getAccount(toAccount);
		if (sourceAccount != null && destinationAccount != null) {
			if (sourceAccount.getAccountId() != destinationAccount.getAccountId()) {
				if (sourceAccount.getBalance().doubleValue() >= amount.doubleValue()) {
					
					synchronized (this) {

						transferFunds(sourceAccount, destinationAccount, amount);
					}
				} else {
					throw new InsufficientFundsException(INSUFFICIENT_BAL);
				}
			} else {
				throw new InvalidAccountException(SAME_WITHDRAW_DEPOSIT_ACC);
			}
		} else {
			throw new InvalidAccountException(UNREGISTERED_WITHDRAW_DEPOSIT_ACC);
		}

		notificationService.notifyAboutTransfer(sourceAccount,
				"Account: " + sourceAccount.getAccountId() + " has been debited by amount: " + amount);
		notificationService.notifyAboutTransfer(destinationAccount,
				"Account: " + destinationAccount.getAccountId() + " has been credited with amount: " + amount);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	private void transferFunds(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
		BigDecimal updatedBal = sourceAccount.getBalance().subtract(amount);
		sourceAccount.setBalance(updatedBal);
		accountsRepository.updateAccount(sourceAccount);

		updatedBal = destinationAccount.getBalance().add(amount);
		destinationAccount.setBalance(updatedBal);
		accountsRepository.updateAccount(destinationAccount);

	}

	private void validateTransferAmount(BigDecimal amount) {
		if (amount == null || amount.doubleValue() <= 0) {
			throw new InvalidTransferAmount(INVALID_TRANSFER_AMOUNT);
		}

	}

	private void validateAccount(String fromAccount, String toAccount) {
		if (fromAccount == null || toAccount == null || fromAccount.trim().isEmpty() || toAccount.trim().isEmpty()) {
			throw new InvalidAccountException(NULL_EMPTY_ACCOUNT);
		}
	}
}
