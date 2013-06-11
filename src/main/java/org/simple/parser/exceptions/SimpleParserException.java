package org.simple.parser.exceptions;

public class SimpleParserException extends Exception{

	public SimpleParserException(Exception e)
	{
		super(e);
	}
	
	public SimpleParserException(String msg)
	{
		super(msg);
	}
}
