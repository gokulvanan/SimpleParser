package org.simple.parser.core.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.UrlValidator;
/**
 * Created with IntelliJ IDEA.
 * User: valarmathyvelan
 * Date: 1/13/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BasicValidators {

    public static class SpecialCharValidator implements CellValidator{

        private static Pattern patt = Pattern.compile(".*[~!@#$%^&*()<>;']+.*");
        public String valid(Object val) {
        	if (val == null) return null;
        	String data = val.toString();
            Matcher match = patt.matcher(data);
            if(match.matches())
                return new StringBuilder().append(" Cell contains invalid characters ").toString();
            else
                return null;
        }
    }

    public static class URLValidator implements CellValidator{
        public static UrlValidator validator = new UrlValidator();
        public String valid(Object val){
        	if (val == null) return null;
        	String data = val.toString();
            if(validator.isValid(data))
                return null;
            else
                return new StringBuilder("Cell is not a valid url").toString();
        }
    }

    public static class MandatoryValidator implements CellValidator {
        public String valid(Object data) {
            if(data == null || data.toString().isEmpty())   return "Mandatory Column can not be Empty";	
            else  											return null;
        }
    }

  
    public static class NumberValidator implements CellValidator{

        public String valid(Object data){
            try{
            	Double val =(Double)data;
                return null;
            }catch (Exception e){
                return "Cell content is not a valid number";
            }
        }
    }
   
    public static class PositiveNumberValidator implements CellValidator{

        public String valid(Object data){
            try{
            	Double val =(Double)data;
                if(val != null && val < 0) return "Cell content has a neagative numebr.. only Positive numbers are allowed";
                return null;
            }catch (Exception e){
                return "Cell content is not a valid number";
            }
        }
    }
   
    
    /**
     * Sample Date validator that check for dates with DD-MM-YYYY format
     * @author gokulvanan
     *
     */
    public static class DateValidator implements CellValidator{
    	public static String dateFormat = "DD-MM-YYYY";
        public String valid(Object data) {
            try{
            	if (data == null) return null;
            	String val = data.toString();
            	new SimpleDateFormat().parse(val);
            	return null;
            }catch (ParseException e) {
            	return "Invalid Date format.. Date format should be : "+dateFormat;
			}
        }
    }
}
