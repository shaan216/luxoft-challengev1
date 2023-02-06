package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



/**
 * 
 * This is supporting class for transferFunds endpiont
 * having transactional details.
 * 
 * @author Shantanu Das
 *
 */
@Data
@NoArgsConstructor
@ToString
public class TxInfo {
	String fromAccount;
	String toAccount;
	BigDecimal amount;
	
	@JsonCreator
	public TxInfo(@JsonProperty("fromAccount") String fromAccount, @JsonProperty("toAccount") String toAccount,
	    @JsonProperty("amount") BigDecimal amount) {
	    this.fromAccount = fromAccount;
	    this.toAccount = toAccount;
	    this.amount = amount;
	}
	
}
