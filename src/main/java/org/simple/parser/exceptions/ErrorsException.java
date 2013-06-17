package org.simple.parser.exceptions;

import java.util.List;

import org.simple.parser.core.ErrorBean;

public class ErrorsException extends Exception {

	private List<ErrorBean> errors;
	
	public ErrorsException(String msg, List<ErrorBean> errorList) {
		super(msg);
		this.errors=errorList;
	}
	
	public List<ErrorBean> getErrors(){
		return this.errors;
	}
}
