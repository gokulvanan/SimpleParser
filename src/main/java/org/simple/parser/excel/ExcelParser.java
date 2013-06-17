package org.simple.parser.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.ErrorBean.ColErrors;
import org.simple.parser.core.interfaces.FileParser;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.exceptions.ErrorsException;
import org.simple.parser.exceptions.SimpleParserException;


public class ExcelParser<T extends IFileBean> extends FileParser<T>{

	public List<T> parse(File fileObj) throws SimpleParserException, ErrorsException {
		Workbook w = getWorkbook(fileObj,true);// file has to exist for read case

		List<T> fileObjList= new LinkedList<T>();
		List<ErrorBean> errorList=new LinkedList<ErrorBean>();
		Map<Integer,Map<Object,Integer>> uniqueMap = new HashMap<Integer,Map<Object,Integer>>();

		Sheet sheet = w.getSheetAt(sheetNo);
		noOfRows =(noOfRows == -1) ? sheet.getLastRowNum()+1 : noOfRows;

		int colWidth=this.noOfColumns-this.startCol;
		if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");
		int actualRowCount=0;
		L2: for (int i = startRow; i < this.noOfRows; i++)
		{
			ErrorBean err = new ErrorBean(i);
			T obj;
			try	{
				obj = ouptutDTOClass.newInstance();
			}catch(Exception er)	{
				throw new SimpleParserException("Error in creating class instace from input class Object using reflection.. Check JVM security settings");
			}

			Row row = sheet.getRow(i);
			if(row == null) 	continue L2; // ignore blank rows
			int j=0;
			int emptyCount=1;
			try{
				actualRowCount++;
				L1:for (j = this.startCol; j < this.noOfColumns; j++) 
				{
					Cell cell = row.getCell(j);
					Field fld = flds.get(j);
					if(fld == null) continue L1;// ignore columns not mapped to DTO objects
					Object data = (cell == null) ? null : getCellVal(cell);// added to prevent null pointer exception for unused columns
					if(data == null)emptyCount++;
					try{
						if(unique.get(j))	checkUnique(uniqueMap,data, j); // unique constraint check
						data=validateAndFormat(data, validators.get(j), readFormatters.get(j));
					}catch(SimpleParserException p){
						err.addColError(new ColErrors(j, p.getMessage()));// col error
						continue L1;
					}
					fld.setAccessible(true);
					fld.set(obj, typeConversion(fld.getType(),data));
				}
			}catch (Exception e) { // Added to coninute processing other rows
				err.addColError(new ColErrors(j,e.getMessage()));// make sure this obj is not added to fileObjList
				j=0;
			}
			if(!err.hasErrors() )			fileObjList.add(obj);// completed full loop without error caseobject
			else if(!ignoreEmptyRows)		errorList.add(err); 
			else if(emptyCount < colWidth)  errorList.add(err); 
			else							actualRowCount--;// empty row case
		}

		if(errorList.size() != 0) throw new ErrorsException("Error", errorList);
		if(maxNoOfRows != -1 && maxNoOfRows < actualRowCount)	throw new SimpleParserException("Exceed maximun number("+maxNoOfRows+") of permitted rows ");
		return fileObjList;
	}

	private Object getCellVal(Cell cell) throws SimpleParserException {
		switch(cell.getCellType()){
		case Cell.CELL_TYPE_NUMERIC:
			return (DateUtil.isCellDateFormatted(cell)) ? new SimpleDateFormat(dateFormat).format(cell.getDateCellValue())/* date case */: cell.getNumericCellValue();
		case Cell.CELL_TYPE_BLANK: 		return null;
		case Cell.CELL_TYPE_BOOLEAN: 	return cell.getBooleanCellValue();
		case Cell.CELL_TYPE_ERROR: 		throw new SimpleParserException("Invalid Cell type (Error)");
		case Cell.CELL_TYPE_FORMULA: 	throw new SimpleParserException("Invalid Cell type (Formula)");
		default:						return cell.getStringCellValue(); // String case
		}
	}

	private Workbook getWorkbook(File fileObj, boolean fileExist) throws SimpleParserException{
		InputStream in = null;
		if(fileExist){ // fileOb exist for read case and does not exist for write case
			try	{
				in = new FileInputStream(fileObj);
			} catch (FileNotFoundException e1)
			{
				throw new SimpleParserException("Invalid File path");
			}
			if(noOfColumns == -1)	throw new SimpleParserException("No of Columns is manadatory for Excel parsing");
		}

		String fileName=fileObj.getName();
		String[] ext= fileName.split("\\.");
		String type = ext[ext.length-1];
		try
		{
			if(fileExist){
				if(type.equalsIgnoreCase("xls"))	return new HSSFWorkbook(in);
				else								return new XSSFWorkbook(in);
			}else{
				if(type.equalsIgnoreCase("xls"))	return new HSSFWorkbook();
				else								return new XSSFWorkbook();
			}

		}catch(IOException i)
		{
			throw new SimpleParserException("Error in parsing file using appache.poi lib.. File not a valid excel");
		}
	}

	private void setCellVal(Cell cell, Class<?> clazz, Object val) throws ParseException {
		if(val == null) {														cell.setCellValue(""); return;   }
		String name = clazz.getSimpleName();
		if(name.equalsIgnoreCase("String") )									{cell.setCellValue(val.toString()); return;}
		if(name.equalsIgnoreCase("Short") || name.equalsIgnoreCase("short"))	{cell.setCellValue(((Number)val).doubleValue()); return;}
		if(name.equalsIgnoreCase("Integer") || name.equalsIgnoreCase("int"))	{cell.setCellValue(((Number)val).doubleValue()); return;}
		if(name.equalsIgnoreCase("Long"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
		if(name.equalsIgnoreCase("Float"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
		if(name.equalsIgnoreCase("Double"))										{cell.setCellValue(((Number)val).doubleValue()); return;}
		if(name.equalsIgnoreCase("Date")){
			SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat); 
			cell.setCellValue(dateFormat.parse(val.toString()));
			return;
		}
	}

	@Override
	public boolean writeObjects(List<T> objs,File fileObj,boolean update) throws SimpleParserException, ErrorsException {
		OutputStream out;
		boolean fileExist = fileObj.exists();
		if(update && !fileExist) throw new SimpleParserException("File path ("+fileObj.getAbsolutePath()+")  is invalid. File doesnt exist to udpate");
		Workbook w = getWorkbook(fileObj,fileExist);

		Sheet sheet = (fileExist)? w.getSheetAt(sheetNo) : w.createSheet();
		int colWidth=this.noOfColumns-this.startCol;
		if(colWidth <= 0) throw new SimpleParserException("Error startCol value exceeds noOfColumns, Check ParserDef/Property file configuration ");

		int start=startRow;
		List<ErrorBean> errorList=new LinkedList<ErrorBean>();
		for (T obj : objs)
		{
			ErrorBean err = new ErrorBean(start);
			Row row = (fileExist)? sheet.getRow(start++) : sheet.createRow(start++);
			if(row == null) 	throw new SimpleParserException("Row returned null from Sheet for row id "+start);
			int j=0;
			Map<Integer,Map<Object,Integer>> uniqueMap = new HashMap<Integer, Map<Object,Integer>>();//added fo checking unique constrain violation
			try{
				L1:for (j = this.startCol; j < this.noOfColumns; j++) 
				{
					Cell cell = (fileExist)?row.getCell(j): row.createCell(j);
					Field fld = flds.get(j);
					if(fld == null) continue L1;// ignore columns not mapped to DTO objects
					Object data = (cell == null) ? null : fld.get(obj);// added to prevent null pointer exception for unused columns
					try{
						if(unique.get(j) && data != null)	checkUnique(uniqueMap,data,j); // unique constraint check
						data=validateAndFormat(data, validators.get(j), writeFormatters.get(j));
						setCellVal(cell,fld.getType(),data);
					}catch(SimpleParserException p){
						err.addColError(new ColErrors(j, p.getMessage()));// col error
						break L1;
					}
				}
			}catch (Exception e) { // Added to coninute processing other rows
				err.addColError(new ColErrors(j,e.getMessage()));
				j=0;// make sure this obj is not added to fileObjList
			}

			if(err.hasErrors() )	errorList.add(err); //TODO Remove this check
		}
		if(errorList.size() != 0){
			throw new ErrorsException("Errors",errorList);
		}
		else{
			try{
				out = new FileOutputStream(fileObj);
				w.write(out);
				out.flush();
				out.close();
				return true;
			}catch (Exception e) {
				throw new SimpleParserException(e);
			}
		}
	}

}
