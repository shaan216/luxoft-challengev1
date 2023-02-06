package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

	
	@Test
	void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
			      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}
	 

	@Test
	void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}
  
	@Test
	void test_createWithdrawAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-444\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-444");
		assertThat(account.getAccountId()).isEqualTo("Id-444");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}
  
	@Test
	void test_createDepositeAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-555\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-555");
		assertThat(account.getAccountId()).isEqualTo("Id-555");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}
	
	@Test
	void test_fundTransWithNullAccount() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
						"{\"fromAccount\":null,\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"500\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	  
	@Test
	void test_fundTransWithEmptyAccount() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
						"{\"fromAccount\":\"\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"500\"\n" + "}"))
				.andExpect(status().isBadRequest());

	}
	  
	@Test
	void test_fundTransWithUnRegisteredSourceAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-111\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"500\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void test_fundTransWithUnRegisteredDestiAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-111\",\n" + " \"amount\": \"500\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	  
	@Test
	void test_fundTransWithZeroTransferAmount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"0\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	  
	@Test
	void test_fundTransWithNegativeTransferAmount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"-100\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void test_fundTransWithInsufficientBalance() throws Exception {
		test_createWithdrawAccount();
		test_createDepositeAccount();
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"5000\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	  
	@Test
	void test_fundTransferNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
	  
	  
	  
	@Test
	void test_nullTransferAmount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": null\n" + "}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void test_emptyTransferAmount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	void test_sameWithdrawDepositAcc() throws Exception {
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"444\",\n"
				+ "  \"toAccount\":\"444\",\n"
				+ " \"amount\": \"100\"\n"
				+ "}"))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	void test_noTransferAmount() throws Exception {
		this.mockMvc
				.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccount\":\"444\",\n" + "  \"toAccount\":\"555\"\n" + "}"))
				.andExpect(status().isBadRequest());
	}
	  
	@Test
	void test_fundTransferSuccess() throws Exception {
		test_createWithdrawAccount();
		test_createDepositeAccount();
		this.mockMvc.perform(post("/v1/accounts/transferFunds").contentType(MediaType.APPLICATION_JSON).content(
				"{\"fromAccount\":\"Id-444\",\n" + "  \"toAccount\":\"Id-555\",\n" + " \"amount\": \"500\"\n" + "}"))
				.andExpect(status().isOk());
		Account account = accountsService.getAccount("Id-444");
	    assertThat(account.getAccountId()).isEqualTo("Id-444");
	    assertThat(account.getBalance()).isEqualByComparingTo("500");
	    
	    account = accountsService.getAccount("Id-555");
	    assertThat(account.getAccountId()).isEqualTo("Id-555");
	    assertThat(account.getBalance()).isEqualByComparingTo("1500");
	    
	    
	}
}