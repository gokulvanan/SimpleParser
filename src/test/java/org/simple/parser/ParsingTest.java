package org.simple.parser;

import java.util.List;

import org.junit.Test;
import org.simple.parser.core.FileBean;

/**
 * Unit test for simple App.
 */
public class ParsingTest {
   
   @Test
    public void excelFileTest() throws Exception
    {
    	System.out.println("Starting test ");
    	long start = System.currentTimeMillis();
    	FileBean<SampleModel> model = FileBean.getBean(SampleModel.class);
    	System.out.println("INIT "+(System.currentTimeMillis()-start));
    	start = System.currentTimeMillis();
    	List<SampleModel> objs =model.read();
    	System.out.println("READ - "+(System.currentTimeMillis()-start));
    	System.out.println(model.isSucessfull());
    	if(model.isSucessfull())
    	{
    		for(SampleModel obj : objs){
    			obj.age=35;
    		}
    		start = System.currentTimeMillis(); 
    		model.update();
    		System.out.println("UPDATE "+(System.currentTimeMillis()-start));
    		start = System.currentTimeMillis();
    		model.write("src/test/excelFiles/TestFileOutput.xlsx");
    		System.out.println("WRITE "+(System.currentTimeMillis()-start));
    		System.out.println(model.isSucessfull());
    		
    	}else{
    		model.printErrors();
    	}
    	
    	System.out.println("End of test");
    }
    
    @Test
    public void csvFiletest() throws Exception
    {
    	System.out.println("Starting test ");
    	long start = System.currentTimeMillis();
    	FileBean<CSVModel> model = FileBean.getBean(CSVModel.class);
    	System.out.println("INIT "+(System.currentTimeMillis()-start));
    	start = System.currentTimeMillis();
    	List<CSVModel> objs =model.read();
    	System.out.println("READ "+(System.currentTimeMillis()-start));
    	System.out.println(model.isSucessfull());
    	if(model.isSucessfull())
    	{
    		System.out.println(objs.size());
    		for(CSVModel obj : objs){
    			obj.age=35;
    		}
    		start = System.currentTimeMillis();
    		model.update();
    		System.out.println("UPDATE "+(System.currentTimeMillis()-start));
    		start = System.currentTimeMillis();
    		model.write("src/test/csvFiles/output.csv");
    		System.out.println("WRITE "+(System.currentTimeMillis()-start));
    		System.out.println(model.isSucessfull());
    	}else{
    		model.printErrors();
    	}
    	
    	System.out.println("End of test");
    }
}
