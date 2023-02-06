package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidTransferAmount;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

	@Test
	void test_fundTransferFailsOnNullFromOrToAcc() {
		String fromAccount = null;
		String toAccount = "Id-555";
		BigDecimal amount = new BigDecimal(1000);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when from/to account invalid");
		} catch (InvalidAccountException ex) {
			assertThat(ex.getMessage()).isEqualTo("from/to account can not be null or empty");
		}
	}

	@Test
	void test_fundTransferFailsOnEmptyFromOrToAcc() {
		String fromAccount = "";
		String toAccount = " Id-555";
		BigDecimal amount = new BigDecimal(1000);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed while from/to account invalid");
		} catch (InvalidAccountException ex) {
			assertThat(ex.getMessage()).isEqualTo("from/to account can not be null or empty");
		}
	}

	@Test
	void test_fundTransferFailsOnZeroAmount() {
		String fromAccount = "Id-444";
		String toAccount = " Id-555";
		BigDecimal amount = new BigDecimal(0);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when transfer amount zero");
		} catch (InvalidTransferAmount ex) {
			assertThat(ex.getMessage()).isEqualTo("Transfer amount should be greater than 0");
		}
	}

	@Test
	void test_fundTransferFailsOnNegativeAmount() {
		String fromAccount = "Id-444";
		String toAccount = " Id-555";
		BigDecimal amount = new BigDecimal(-10);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when transfer amount is negative");
		} catch (InvalidTransferAmount ex) {
			assertThat(ex.getMessage()).isEqualTo("Transfer amount should be greater than 0");
		}
	}

	@Test
	void test_fundTransferFailsOnNullTransferAmount() {
		String fromAccount = "Id-444";
		String toAccount = " Id-555";
		BigDecimal amount = null;
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when transfer amount is null");
		} catch (InvalidTransferAmount ex) {
			assertThat(ex.getMessage()).isEqualTo("Transfer amount should be greater than 0");
		}
	}

	@Test
	void test_fundTransferFailsOnInsuffiBalance() {
		Account withAcc = new Account("Id-444");
		withAcc.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(withAcc);

		Account depoAcc = new Account("Id-555");
		depoAcc.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(depoAcc);

		String fromAccount = withAcc.getAccountId();
		String toAccount = depoAcc.getAccountId();
		BigDecimal amount = new BigDecimal(1000);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when transfer amount is insufficient");
		} catch (InsufficientFundsException ex) {
			assertThat(ex.getMessage()).isEqualTo("Insufficient balance");
		}
	}

	@Test
	void test_fundTrasferFailsOnUnregisteredAcc() {
		Account withAcc = new Account("Id-111");
		withAcc.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(withAcc);

		String fromAccount = withAcc.getAccountId();
		String toAccount = "Id-222";
		BigDecimal amount = new BigDecimal(100);
		try {
			this.accountsService.fundTransfer(fromAccount, toAccount, amount);
			fail("Should have failed when from/to account not registered");
		} catch (InvalidAccountException ex) {
			assertThat(ex.getMessage()).isEqualTo("from/to Account not registered");
		}
	}
}