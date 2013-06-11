package org.simple.parser.core.interfaces;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.annotations.ColumnDef;
import org.simple.parser.core.annotations.ParserDef;
import org.simple.parser.core.formatters.CellFormatter;
import org.simple.parser.core.validators.CellValidator;
import org.simple.parser.exceptions.SimpleParserException;


/**
 * Main interface used by classes to access parser functionalities
 * @author gokulvanan
 *
 */
public abstract class FileParser<T extends IFileBean> {

	//mandatory 
	protected int noOfColumns=-1;

	//optional
	protected int noOfRows=-1;
	protected int sheetNo=-1;
	protected int startRow=-1;
	protected int startCol=-1;
	protected int maxNoOfRows=-1;
	protected String dateFormat=null;
	protected boolean ignoreEmptyRows=false;

	//config fields
	protected final Map<Integer,Field> flds= new HashMap<Integer,Field>();
	protected final Map<Integer,Class<? extends CellValidator>[]> validators = new HashMap<Integer,Class<? extends CellValidator>[]>();
	protected final Map<Integer,CellFormatter> writeFormatters = new HashMap<Integer, CellFormatter>();
	protected final Map<Integer,CellFormatter> readFormatters = new HashMap<Integer, CellFormatter>();
	protected final Map<Integer,Boolean> unique = new HashMap<Integer, Boolean>();
	protected Class<T> ouptutDTOClass;

	protected List<T> fileObjList=null;
	protected List<ErrorBean> errorList=null;
	protected Map<Integer,Map<Object,Integer>> uniqueMap=null;

	public abstract void parse(File file ) throws SimpleParserException;

	public abstract boolean writeObjects(List<T> objs, File fileObj)	throws SimpleParserException;



	/**
	 * Initialise parser configurations from {@link ParserDef} annotation file
	 */
	public void initialize(ParserDef props,Class<T> clazz) throws SimpleParserException  {
		try{
			this.noOfColumns= props.noOfColumns();
			this.noOfRows=props.noOfRows();
			this.sheetNo=props.sheetNo();
			this.startRow=props.startRow();
			this.startCol=props.startCol();
			this.maxNoOfRows=props.maxNoOfRows();
			this.dateFormat=props.dateformat();
			this.ignoreEmptyRows=props.ignoreEmptyRows();
			this.ouptutDTOClass=clazz;
			initMaps();
		}catch (Exception e) {
			throw new SimpleParserException("Error in configuration msg"+e.getMessage());
		}
	}

	protected void initMaps() throws SimpleParserException{
		int maxIndex=0;
		try
		{
			Field[] allFlds= ouptutDTOClass.getDeclaredFields();
			for(Field fld : allFlds){
				fld.setAccessible(true);
				ColumnDef colDef =fld.getAnnotation(ColumnDef.class);
				if(colDef == null) continue;
				int index = colDef.index();
				flds.put(index, fld);
				validators.put(index,colDef.validators());
				writeFormatters.put(index, colDef.writeFormatter().newInstance());
				readFormatters.put(index, colDef.formatter().newInstance());
				unique.put(index, colDef.unique());
				maxIndex = (maxIndex < index) ? index : maxIndex;
			}

		}catch (Exception e) {
			throw new SimpleParserException("Error in parsing annotations.. Error msg : "+e.getMessage());
		}

		if(maxIndex > noOfColumns) throw new SimpleParserException("Error in annoation configuration. Col index exceed noOf Columns declared");
	}
	

	public List<T> getParsedObjects() {
		return this.fileObjList;
	}


	public List<ErrorBean> getErrorObjects() {
		return this.errorList;
	}

	public boolean isSucessfull() {
		return (this.errorList.size() == 0);
	}
	

	protected Object typeConversion(Class<?> clazz,Object val) throws ParseException
	{
		if(val == null) 						return null;
		String name = clazz.getSimpleName();
		if(name.equalsIgnoreCase("Short") || name.equalsIgnoreCase("short"))	return (short)((Number)val).doubleValue();
		if(name.equalsIgnoreCase("Integer") || name.equalsIgnoreCase("int"))	return (int)  ((Number)val).doubleValue();
		if(name.equalsIgnoreCase("Long"))										return (long) ((Number)val).doubleValue();
		if(name.equalsIgnoreCase("Float"))										return (float)((Number)val).doubleValue();
		if(name.equalsIgnoreCase("Double"))										return 		  ((Number)val).doubleValue();
		if(name.equalsIgnoreCase("Date")){
			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); // MOVE THIS TO A PROPERTY FILE
			return dateFormat.parse(val.toString());
		}
		return val.toString();
	}
	
	protected Object validateAndFormat(Object data, Class<? extends CellValidator>[] validatorclses,CellFormatter formatter) throws SimpleParserException{
		try{
			for(Class<? extends CellValidator> validatorCls : validatorclses){
				CellValidator validator = validatorCls.newInstance();
				String errorMsg=validator.valid(data) ;
				if(errorMsg  != null){// invalid case
					throw new SimpleParserException(errorMsg);
				}
			}
			return formatter.format(data);
		}catch (Exception e) {
			throw new SimpleParserException(e.getLocalizedMessage());
		}
	}
	
	protected void checkUnique(Object data,int colIndx) throws SimpleParserException{
		Map<Object,Integer> m = uniqueMap.get(colIndx);
		if(m== null)
		{	
			m=new HashMap<Object, Integer>();
			m.put(data, 1);
		}
		else
		{
			if(m.containsKey(data))	throw new SimpleParserException("Unique contraint violated");
			else					m.put(data,1);
		}
		uniqueMap.put(colIndx, m);
	}
}
