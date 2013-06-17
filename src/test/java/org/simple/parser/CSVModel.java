package org.simple.parser;

import org.simple.parser.core.annotations.ColumnDef;
import org.simple.parser.core.annotations.ParserDef;
import org.simple.parser.core.formatters.BasicFormatters;
import org.simple.parser.core.interfaces.IFileBean;
import org.simple.parser.csv.CSVParser;

@ParserDef(parser=CSVParser.class,noOfColumns=5, srcFilePath="src/test/csvFiles/input.csv")
public class CSVModel implements IFileBean{
	
	@ColumnDef(index=0, unique=true)
	public int id;
	@ColumnDef(index=1, formatter=BasicFormatters.ToUpperCase.class)
	public String name;
	@ColumnDef(index=2, writeFormatter=BasicFormatters.ToUpperCase.class)
	public String name2;
	@ColumnDef(index=3)
	public String desc;
	@ColumnDef(index=4)
	public int age;

}
