package com.dws.challenge.exception;

public class InvalidTransferAmount extends RuntimeException {
	public InvalidTransferAmount(String message) {
		super(message);
	}
}
