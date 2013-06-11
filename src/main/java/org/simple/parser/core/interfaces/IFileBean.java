package org.simple.parser.core.interfaces;

import java.io.File;
import java.util.List;
import org.simple.parser.core.ErrorBean;
import org.simple.parser.exceptions.SimpleParserException;


public interface IFileBean {
	
//	public void initialize() throws SimpleParserException;
	
	public  List<? extends IFileBean> read() throws SimpleParserException;
	
	public  List<? extends IFileBean> read(File newFile) throws SimpleParserException;
	
	public  void write(String path) throws SimpleParserException;
	
	public  void append(IFileBean obj) throws SimpleParserException;
	
	public  void append(List<? extends IFileBean> objs) throws SimpleParserException;
	
	public  void update() throws SimpleParserException;
	
	public List<ErrorBean> getErrors();
	
	public boolean isSucessfull();

}
