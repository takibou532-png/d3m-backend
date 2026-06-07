package com.menu.demo.Exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
	
	public EmailAlreadyExistsException(String message) {
		super(message);
	}

}
