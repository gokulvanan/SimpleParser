package org.simple.parser.core.interfaces;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.annotations.ParserDef;
import org.simple.parser.exceptions.SimpleParserException;


/**
 * Main interface used by classes to access parser functionalities
 * @author gokulvanan
 *
 */
public interface IFileParser<T extends IFileBean> {

	
	public void initialize(ParserDef props, Class<T> ouptutDTOClass) throws SimpleParserException;
	
	public void parse(File file ) throws SimpleParserException;
	
	public List<T> getParsedObjects();
	
	boolean writeObjects(List<T> objs, File fileObj)	throws SimpleParserException;
	
	public List<ErrorBean> getErrorObjects();
	
	public boolean isSucessfull();

	
	
	
}
