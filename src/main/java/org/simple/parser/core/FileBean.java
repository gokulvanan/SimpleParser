package org.simple.parser.core;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.simple.parser.core.annotations.ParserDef;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.core.interfaces.IFileParser;
import org.simple.parser.exceptions.SimpleParserException;


/**
 * Parent class for any Bean mapped to a file 
 * This class provides methods to readFile and retrive List<Beans>   
 * @author gokulvanan
 *
 */
public class FileBean<T extends IFileBean> implements IFileBean{

	@SuppressWarnings("rawtypes")
	private IFileParser parser;
	private File srcFile=null;
	private List<T> fileObjs;


	public static <T extends FileBean<T>> T getBean(Class<T> clazz) throws  SimpleParserException{
		T obj = null;
		try{
			obj = clazz.newInstance();
			obj.initialize(null);
		}catch(InstantiationException i){
			throw new SimpleParserException(i);
		}catch (IllegalAccessException e) {
			throw new SimpleParserException(e);
		}
		return obj;
	}
	
	public static <T extends FileBean<T>> T getBean(Class<T> clazz, File newFile) throws  SimpleParserException{
		T obj = null;
		try{
			obj = clazz.newInstance();
			obj.initialize(newFile);
		}catch(InstantiationException i){
			throw new SimpleParserException(i);
		}catch (IllegalAccessException e) {
			throw new SimpleParserException(e);
		}
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	private void initialize(File newFile) throws SimpleParserException{
		Class<T> childClass=(Class<T>) this.getClass();
		ParserDef parserAnno = childClass.getAnnotation(ParserDef.class);
		if(parserAnno == null)		throw new SimpleParserException("ParserDef Annotation not maped to model");
		if(newFile != null){
			srcFile = newFile;
		}else{
			String srcPath=parserAnno.srcFilePath();
			if(!srcPath.equals( "NULL")){
				try{
					srcFile = new File(srcPath);
				}catch(Exception e){
					throw new SimpleParserException("Error in reading input file "+e.getMessage());
				}	
			}
		}
		try{
			parser = parserAnno.parser().newInstance();
		} catch (Exception e){
			throw new SimpleParserException("Invalid parser class specified in parserDef");
		}
		System.out.println("Initializing parser");
		parser.initialize(parserAnno,this.getClass());
	}

	@SuppressWarnings("unchecked")
	public  List<T> read() throws SimpleParserException{
		if(fileObjs != null)	return fileObjs;
		if(parser == null || srcFile == null)	throw new SimpleParserException("Parser not initialized use FileBean.getBean() to get instance of parser");
		parser.parse(srcFile);
		fileObjs = parser.getParsedObjects();
		return fileObjs;
	}

	@SuppressWarnings("unchecked")
	public  List<T> read(File file) throws SimpleParserException{
		srcFile = file;
		if(parser == null || srcFile == null)	throw new SimpleParserException("Parser not initialized use FileBean.getBean() to get instance of parser");
		parser.parse(srcFile);
		fileObjs = parser.getParsedObjects();
		return fileObjs;
	}
	
	@SuppressWarnings("unchecked")
	public void update() throws SimpleParserException{
		if(fileObjs == null || srcFile == null)	throw new SimpleParserException("Can not updated before reading a file");
		parser.writeObjects(fileObjs,srcFile);
	}

	@SuppressWarnings("unchecked")
	public void write(String newFilePath) throws SimpleParserException{
		if(fileObjs == null)	throw new SimpleParserException("Can not write without any objects");
		File destFile = null;
		try{
			destFile = new File(newFilePath);
		}catch(Exception e){
			throw new SimpleParserException("Error in reading input file "+e.getMessage());
		}
		System.out.println(destFile);
		parser.writeObjects(fileObjs,destFile);
	}

	@SuppressWarnings("unchecked")
	public List<ErrorBean> getErrors(){
		return parser.getErrorObjects();
	}

	public boolean isSucessfull(){
		return parser.isSucessfull();
	}

	@SuppressWarnings("unchecked")
	public void append(IFileBean obj) throws SimpleParserException {
		fileObjs = (fileObjs == null) ? new LinkedList<T>() : fileObjs;
		fileObjs.add((T) obj);
	}

	@SuppressWarnings("unchecked")
	public void append(List<? extends IFileBean> objs) throws SimpleParserException {
		fileObjs = (fileObjs == null) ? new LinkedList<T>() : fileObjs;
		fileObjs.addAll((Collection<? extends T>) objs);

	}




}
