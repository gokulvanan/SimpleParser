SimpleParser
============

A simple API to parse Excel, CSV,TSV and other sources of File Inputs to List of JavaObjects

The API provides Useful base code for extracting and loading data from Files to database.

The idea behind SimpleParser is inspired from JPA model of mapping Java Objects to Database tables. 
Simple Parser does not handle all DML's as in JPA or support any Object oriented mapping (Parent Child relationship)
It merely automates reading a File, and enables injecting validation formating rules for each column through 
validators and formatters in ColDef Annotation.


Implementation of Excel parser works. CSV and TSV are yet to be implemented

Example of Model implemenation 

	@ParserDef(parser=ExcelParser.class,
	srcFilePath="/home/xyz/Documents/TestFile.xlsx",
	noOfColumns=3, // 3 columns
	startRow=2 // ignore row 1 header
	)	
	public class SampleModel extends FileBean<SampleModel>{
	
		@ColumnDef(index=1, 
				validators={BasicValidators.MandatoryValidator.class},
				unique=true)
		public int id;
		@ColumnDef(index=2,
				formatter=BasicFormatters.ToUpperCase.class)
		public String name;
		@ColumnDef(index=3)
		public int age;
	
	}

Example of using above model to parser excel file

	public class ParsingTest {
	   
	    @Test
	    public void parseExcelFileTest() throws Exception
	    {
	    	System.out.println("Starting test ");
	    	SampleModel model = FileBean.getBean(SampleModel.class);
	    	List<SampleModel> objs =model.read();
	    	System.out.println(model.isSucessfull());
	    	if(model.isSucessfull())
	    	{
	    		System.out.println(objs.size());
	    		for(SampleModel obj : objs){
	    			obj.age=35;
	    		}
	    		model.update(); // updates existing File
	    		model.write("src/test/excelFiles/TestFileOutput.xlsx"); // writes to new file
	    		System.out.println(model.isSucessfull());
	    		
	    	}else{
	    		List<ErrorBean> errors=model.getErrors();
	    		for(ErrorBean err : errors){
	    			System.out.println(err.getRow());
	    			for(ColErrors colerr : err.getColErrors())
	    			{
	    				System.out.println(colerr.getCol());
	    				System.out.println(colerr.getMsg());
	    			}
	    		}
	    	}
	    	
	    	System.out.println("End of test");
	    }
	}
