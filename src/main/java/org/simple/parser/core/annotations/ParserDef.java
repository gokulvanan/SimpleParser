package org.simple.parser.core.annotations;

import org.simple.parser.core.interfaces.IFileParser;


@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ParserDef {

	public Class<? extends IFileParser> parser();
	//optional
	public String srcFilePath() default "NULL";
	public int noOfColumns() 	default -1;
	public int noOfRows() 		default -1;
	public int sheetNo() 		default  0; // specific to excel sheet
	public int startRow() 		default  0;
	public int startCol() 		default  0;
	public int maxNoOfRows()	default  -1;
	public String dateformat()  default "dd-MM-yyyy";
}
