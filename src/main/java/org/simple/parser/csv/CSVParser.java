package org.simple.parser.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.ErrorBean.ColErrors;
import org.simple.parser.core.interfaces.FileParser;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.exceptions.ErrorsException;
import org.simple.parser.exceptions.SimpleParserException;

public class CSVParser<T extends IFileBean> extends FileParser<T>{

	@Override
	public List<T> parse(File file) throws SimpleParserException, ErrorsException {

		List<T> fileObjList= new ArrayList<T>();
		List<ErrorBean> errorList=new ArrayList<ErrorBean>();
		Map<Integer,Map<Object,Integer>> uniqueMap = new HashMap<Integer,Map<Object,Integer>>();

		int colWidth=this.noOfColumns-this.startCol;
		if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");

		try {
			Scanner sc  = new Scanner(file);
			int actualRowCount=0;
			int i = 0;
			while(sc.hasNextLine()){
				if(i < startRow) continue;
				ErrorBean err = new ErrorBean(i++);
				T obj;
				try	{
					obj = ouptutDTOClass.newInstance();
				}catch(Exception er)	{
					throw new SimpleParserException("Error in creating class instace from input class Object using reflection.. Check JVM security settings");
				}
				String[] rowData = StringUtils.split(sc.nextLine(),',');
				int j = 0, emptyCount =1;
				try{
					L1:for(j=startCol; j<noOfColumns; j++){
						Field fld = flds.get(j);
						if(fld == null) continue L1;// ignore columns not mapped to DTO objects
						String data = rowData[j];
						if(StringUtils.trim(data).length() == 0) emptyCount++;
						try{
							if(unique.get(j))	checkUnique(uniqueMap,data, j); // unique constraint check
							Object val=validateAndFormat(data, validators.get(j), readFormatters.get(j));
							fld.setAccessible(true);
							fld.set(obj, typeConversion(fld.getType(),val));
						}catch(SimpleParserException p){
							err.addColError(new ColErrors(j, p.getMessage()));// col error
							continue L1;
						}
					}
				}catch(Exception e){
					err.addColError(new ColErrors(j,e.getMessage()));// make sure this obj is not added to fileObjList
					j=0;
				}

				if(!err.hasErrors() )			fileObjList.add(obj);// completed full loop without error caseobject
				else if(!ignoreEmptyRows)		errorList.add(err); 
				else if(emptyCount < colWidth)  errorList.add(err); 
				else							actualRowCount--;// empty row case
			}

			if(maxNoOfRows != -1 && maxNoOfRows < actualRowCount)	throw new SimpleParserException("Exceed maximun number("+maxNoOfRows+") of permitted rows ");

		} catch (FileNotFoundException e) {
			throw new SimpleParserException("File Not Found");
		}
		if(errorList.size() != 0) throw new ErrorsException("Errors",errorList);
		return fileObjList;
	}

	@Override
	public boolean writeObjects(List<T> objs, File fileObj, boolean update)
	throws SimpleParserException {
		File bk = null;
		try{
			
			boolean fileExist = fileObj.exists(); 
			if(update && !fileExist) throw new SimpleParserException("File path ("+fileObj.getAbsolutePath()+")  is invalid. File doesnt exist to udpate");
			if(update){
				String filePath = StringUtils.chomp(fileObj.getAbsolutePath(),".csv");
				System.out.println(filePath);
				bk = new File(filePath+"_bk"+System.currentTimeMillis()+".csv");
				bk.setWritable(true);
				copyFiles(fileObj,bk);
			}else{
				if(fileExist){ // createCase
					fileObj.delete();
				}
				boolean created = fileObj.createNewFile();
				if(!created) throw new SimpleParserException("Error in File Creation for write operation. Check write permissions ");
				// code to init header and hidden columns if any here
			}
			fileObj.setWritable(true);
			BufferedWriter buff = new BufferedWriter(new FileWriter(fileObj));
			int colWidth=this.noOfColumns-this.startCol;
			if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");

			int start=startRow, i =0;
			List<ErrorBean> errorList = new LinkedList<ErrorBean>();
			for (T obj : objs)
			{
				i++;
				ErrorBean err = new ErrorBean(start);
				int j=0;
				Map<Integer,Map<Object,Integer>> uniqueMap = new HashMap<Integer, Map<Object,Integer>>();//added fo checking unique constrain violation
				try{
					StringBuilder build = new StringBuilder(10*noOfColumns);
					L1:for (j = this.startCol; j < this.noOfColumns; j++) 
					{
						Field fld = flds.get(j);
						if(fld == null) continue L1;// ignore columns not mapped to DTO objects
						Object data = fld.get(obj);
						try{
							if(unique.get(j) && data != null)	checkUnique(uniqueMap,data,j); // unique constraint check
							data=validateAndFormat(data, validators.get(j), writeFormatters.get(j));
							if(!err.hasErrors()) build.append(data).append(",");
						}catch(SimpleParserException p){
							err.addColError(new ColErrors(j, p.getMessage()));// col error
							break L1;
						}
					}
					if(!err.hasErrors()) buff.write(StringUtils.chop(build.toString()));
				}catch (Exception e) { // Added to coninute processing other rows
					err.addColError(new ColErrors(j,e.getMessage()));
					j=0;// make sure this obj is not added to fileObjList
				}
				if(err.hasErrors() )	errorList .add(err); //TODO Remove this check
				else					buff.write("\n");
				if( i % 50 == 0) 		buff.flush();
			}
			buff.flush();
			buff.close();
			if(errorList.size() != 0){
				fileObj.delete(); // remove file created/updated in case of errors
				if(update) copyFiles(bk, fileObj);// rollback
				throw new ErrorsException("Errors",errorList);
			}
			else{
				if(update){
					if(bk.delete()) return true;
					else return false;
				}
				return true;
			}
		}catch(Exception e){
			fileObj.delete(); // remove file created/updated in case of errors
			if(update)
				try {
					IOUtils.copy(new FileInputStream(bk), new FileOutputStream(fileObj));
				} catch (Exception e1) {
					throw new SimpleParserException("Critical Error... Unable to rollback File changes..");
				}
			throw new SimpleParserException(e);
		}
	} 


	private void copyFiles(File input, File output) throws Exception {
		FileInputStream in = new FileInputStream(input);
		FileOutputStream o = new FileOutputStream(output);
		try{
			IOUtils.copy(in,o);// create backup tempFile
		}finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(o);
		}
	}

	@Override
	protected Object typeConversion(Class<?> clazz,Object val) throws ParseException
	{
		if(val == null) 						return null;
		String name = clazz.getSimpleName();
		if(name.equalsIgnoreCase("Short") || name.equalsIgnoreCase("short"))	return (short) 	Double.parseDouble(val.toString());
		if(name.equalsIgnoreCase("Integer") || name.equalsIgnoreCase("int"))	return (int)  	Double.parseDouble(val.toString());
		if(name.equalsIgnoreCase("Long"))										return (long) 	Double.parseDouble(val.toString());
		if(name.equalsIgnoreCase("Float"))										return (float)	Double.parseDouble(val.toString());
		if(name.equalsIgnoreCase("Double"))										return 		  	Double.parseDouble(val.toString());
		if(name.equalsIgnoreCase("Date")){
			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); 
			return dateFormat.parse(val.toString());
		}
		return val.toString();
	}
}