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
    		model.update();
    		model.write("src/test/excelFiles/TestFileOutput.xlsx");
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
