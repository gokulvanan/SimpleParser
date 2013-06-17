package org.simple.parser;

import java.util.List;

import org.junit.Test;
import org.simple.parser.core.ErrorBean;
import org.simple.parser.core.ErrorBean.ColErrors;
import org.simple.parser.core.FileBean;

/**
 * Unit test for simple App.
 */
public class ParsingTest {
   
    /*@Test
    public void excelFileTest() throws Exception
    {
    	System.out.println("Starting test ");
    	FileBean<SampleModel> model = FileBean.getBean(SampleModel.class);
    	List<SampleModel> objs =model.read();
    	System.out.println(model.isSucessfull());
    	if(model.isSucessfull())
    	{
    		System.out.println(objs.size());
    		for(SampleModel obj : objs){
    			obj.age=35;
    		}
    		model.update();
    		model.write("src/test/excelFiles/TestFileOutput.xlsx");
    		System.out.println(model.isSucessfull());
    		
    	}else{
    		model.printErrors();
    	}
    	
    	System.out.println("End of test");
    }*/
    
    @Test
    public void csvFiletest() throws Exception
    {
    	System.out.println("Starting test ");
    	FileBean<CSVModel> model = FileBean.getBean(CSVModel.class);
    	List<CSVModel> objs =model.read();
    	System.out.println(model.isSucessfull());
    	if(model.isSucessfull())
    	{
    		System.out.println(objs.size());
    		for(CSVModel obj : objs){
    			obj.age=35;
    		}
//    		model.update();
    		model.write("src/test/csvFiles/output.csv");
    		System.out.println(model.isSucessfull());
    		
    	}else{
    		model.printErrors();
    	}
    	
    	System.out.println("End of test");
    }
}
