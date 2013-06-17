package org.simple.parser.core;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.simple.parser.core.annotations.ParserDef;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.core.interfaces.FileParser;
import org.simple.parser.exceptions.ErrorsException;
import org.simple.parser.exceptions.SimpleParserException;


/**
 * Main Wrapper class to couple any Bean mapped to a file 
 * This class provides methods to readFile and retrieve List<Beans>   
 * @author gokulvanan
 *
 */
public class FileBean<T extends IFileBean>{

	@SuppressWarnings("rawtypes")
	private FileParser parser = null;
	private File srcFile=null;
	private List<T> fileObjs = null;  
	private List<ErrorBean> errorObjs = null;  

	private FileBean(){	}

	public static <T extends IFileBean> FileBean<T> getBean(Class<T> clazz) throws  SimpleParserException{
		FileBean<T> obj = null;
		obj = new FileBean<T>();
		obj.initialize(clazz,null);
		return obj;
	}

	public static <T extends IFileBean> FileBean<T> getBean(Class<T> clazz, File newFile) throws  SimpleParserException{
		FileBean<T> obj = null;
		obj = new FileBean<T>();
		obj.initialize(clazz,newFile);
		return obj;
	}

	@SuppressWarnings("unchecked")
	private void initialize(Class<T> clazz,File newFile) throws SimpleParserException{
		ParserDef parserAnno = clazz.getAnnotation(ParserDef.class);
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
		parser.initialize(parserAnno,clazz);
	}

	@SuppressWarnings("unchecked")
	public  List<T> read() throws SimpleParserException{
		fileObjs = null;
		errorObjs = null;
		if(parser == null || srcFile == null)	throw new SimpleParserException("Parser not initialized use FileBean.getBean() to get instance of parser");
		try {
			fileObjs = parser.parse(srcFile);
		} catch (ErrorsException e) {
			errorObjs = e.getErrors();
		}
		return fileObjs;
	}

	@SuppressWarnings("unchecked")
	public  List<T> read(File file) throws SimpleParserException{
		srcFile = file;
		return read();
	}

	@SuppressWarnings("unchecked")
	public void update() throws SimpleParserException{
		if(fileObjs == null || srcFile == null)	throw new SimpleParserException("Can not updated before reading a file");
		errorObjs = null;
		try {
			parser.writeObjects(fileObjs,srcFile,true);
		} catch (ErrorsException e) {
			errorObjs=e.getErrors();
		}
	}

	@SuppressWarnings("unchecked")
	public void write(String newFilePath) throws SimpleParserException{
		if(fileObjs == null)	throw new SimpleParserException("Can not write without any objects");
		errorObjs = null;
		File destFile = null;
		try{
			destFile = new File(newFilePath);
			parser.writeObjects(fileObjs,destFile,false);
		} catch (ErrorsException e) {
			errorObjs=e.getErrors();
		}catch(Exception e){
			throw new SimpleParserException("Error in reading input file "+e.getMessage());
		}
	}

	public List<ErrorBean> getErrors(){
		return errorObjs;
	}

	public boolean isSucessfull(){
		return (errorObjs == null || errorObjs.size() == 0);
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

	public void printErrors() {
		if(parser == null) return;
		List<ErrorBean> errors = getErrors();
		if(errors == null) return;
		for(ErrorBean err : errors)		System.out.println(err);
	}

}
