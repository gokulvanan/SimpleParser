package org.simple.parser.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.ErrorBean.ColErrors;
import org.simple.parser.core.interfaces.FileParser;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.exceptions.SimpleParserException;

public class CSVParser<T extends IFileBean> extends FileParser<T>{

	@Override
	public void parse(File file) throws SimpleParserException {

		fileObjList= new ArrayList<T>();
		errorList=new ArrayList<ErrorBean>();
		uniqueMap = new HashMap<Integer,Map<Object,Integer>>();

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
							if(unique.get(j))	checkUnique(data, j); // unique constraint check
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

				if(!err.hasErrors() )			this.fileObjList.add(obj);// completed full loop without error caseobject
				else if(!ignoreEmptyRows)		this.errorList.add(err); 
				else if(emptyCount < colWidth)  this.errorList.add(err); 
				else							actualRowCount--;// empty row case
			}

			if(maxNoOfRows != -1 && maxNoOfRows < actualRowCount)	throw new SimpleParserException("Exceed maximun number("+maxNoOfRows+") of permitted rows ");

		} catch (FileNotFoundException e) {
			throw new SimpleParserException("File Not Found");
		}	
	}

	//TODO 
	/*
	 * Add code to backup in case of update and create and delete incase of update
	 * Add code to selectively update, delte objects.. Use timeStamps in IFileBean to do this
	 */
	@Override
	public boolean writeObjects(List<T> objs, File fileObj)
			throws SimpleParserException {
		try{
			boolean fileExist = fileObj.exists();
			if(!fileExist){
				boolean created = fileObj.createNewFile();
				if(!created) throw new SimpleParserException("Error in File Creation for write operation. Check write permissions ");
			}
			BufferedWriter buff = new BufferedWriter(new FileWriter(fileObj));

			int colWidth=this.noOfColumns-this.startCol;
			if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");

			int start=startRow, i =0;
			for (T obj : objs)
			{
				i++;
				ErrorBean err = new ErrorBean(start);
				int j=0;
				uniqueMap = new HashMap<Integer, Map<Object,Integer>>();//added fo checking unique constrain violation
				try{
					L1:for (j = this.startCol; j < this.noOfColumns; j++) 
					{
						Field fld = flds.get(j);
						if(fld == null) continue L1;// ignore columns not mapped to DTO objects
						Object data = fld.get(obj);
						try{
							if(unique.get(j) && data != null)	checkUnique(data,j); // unique constraint check
							data=validateAndFormat(data, validators.get(j), writeFormatters.get(j));
							buff.write(data.toString());
						}catch(SimpleParserException p){
							err.addColError(new ColErrors(j, p.getMessage()));// col error
							break L1;
						}
					}

				}catch (Exception e) { // Added to coninute processing other rows
					err.addColError(new ColErrors(j,e.getMessage()));
					j=0;// make sure this obj is not added to fileObjList
				}
				if(err.hasErrors() )	this.errorList.add(err); //TODO Remove this check
				else					buff.write("\n");
				if( i % 50 == 0) 		buff.flush();
			}
			buff.flush();
			buff.close();
			if(this.errorList.size() != 0){
				fileObj.delete(); // remove file created in case of errors
				return false;
			}
			else	return true;
		}catch(Exception e){
			throw new SimpleParserException(e);
		}
	} 




}